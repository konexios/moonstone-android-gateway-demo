package com.arrow.jmyiotgateway.device;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.arrow.acn.api.models.GatewayEventModel;
import com.arrow.jmyiotgateway.Constant;
import com.arrow.jmyiotgateway.cloud.iot.IotConstant;
import com.arrow.jmyiotgateway.device.android.DeviceAndroid;
import com.arrow.jmyiotgateway.device.msband.MsBand;
import com.arrow.jmyiotgateway.device.senseability.SenseAbilityKit;
import com.arrow.jmyiotgateway.device.sensorpuck.SensorPuck;
import com.arrow.jmyiotgateway.device.sensortile.SensorTile;
import com.arrow.jmyiotgateway.device.simbapro.SimbaPro;
import com.arrow.jmyiotgateway.device.thunderboard.ThunderBoard;
import com.google.firebase.crash.FirebaseCrash;

import org.parceler.Parcels;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import static com.arrow.jmyiotgateway.Constant.ACTION_IOT_DEVICE_STATE_REQUEST;
import static com.arrow.jmyiotgateway.cloud.iot.IotConstant.EXTRA_DATA_LABEL_CARD_ID;
import static com.arrow.jmyiotgateway.cloud.iot.IotConstant.EXTRA_DATA_LABEL_DEVICE_STATES;
import static com.arrow.jmyiotgateway.cloud.iot.IotConstant.EXTRA_DATA_LABEL_DEVICE_TYPE;

/**
 * Created by osminin on 3/21/2016.
 */
public class DevicePollingService extends Service {
    private static final String TAG = DevicePollingService.class.getName();
    private static final int CORE_POOL_SIZE = DeviceType.values().length;
    private final IBinder mBinder = new LocalBinder();
    private Map<DeviceKey, PollingServiceEntry> mDeviceExecutorsMap;
    private boolean isReconnecting;


    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Constant.ACTION_IOT_DEVICE_POLLING_SERVICE_COMMAND)
                    || intent.getAction().equals(Constant.ACTION_IOT_DEVICE_STATE_CHANGED)
                    || intent.getAction().equals(ACTION_IOT_DEVICE_STATE_REQUEST)) {
                handleIncomingIntent(intent);
            }
        }
    };

    @Override
    public void onCreate() {
        FirebaseCrash.logcat(Log.DEBUG, TAG, "onCreate");
        mDeviceExecutorsMap = new HashMap<>(CORE_POOL_SIZE);
        IntentFilter filter = new IntentFilter(Constant.ACTION_IOT_DEVICE_POLLING_SERVICE_COMMAND);
        filter.addAction(Constant.ACTION_IOT_DEVICE_STATE_CHANGED);
        filter.addAction(ACTION_IOT_DEVICE_STATE_REQUEST);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, filter);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        FirebaseCrash.logcat(Log.DEBUG, TAG, "onStartCommand");
        if (intent != null) {
            handleIncomingIntent(intent);
        }
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        FirebaseCrash.logcat(Log.DEBUG, TAG, "onDestroy");
        super.onDestroy();
        stopAllDevicePolling();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
    }

    private void handleIncomingIntent(Intent intent) {
        FirebaseCrash.logcat(Log.DEBUG, TAG, "handleIncomingIntent + " + intent.toString());
        if (intent.hasExtra(DeviceCommand.EXTRA_DATA_LABEL_DEVICE_COMMAND)) {
            DeviceCommand command = Parcels.unwrap(intent.getParcelableExtra(DeviceCommand.EXTRA_DATA_LABEL_DEVICE_COMMAND));
            Long cardId = intent.getLongExtra(EXTRA_DATA_LABEL_CARD_ID, -1);
            handleCommand(command, cardId);
        }
        if (intent.hasExtra(IotConstant.EXTRA_DATA_LABEL_DEVICE_STATE) && intent.hasExtra(EXTRA_DATA_LABEL_DEVICE_TYPE)) {
            DeviceState deviceState = Parcels.unwrap(intent.getParcelableExtra(IotConstant.EXTRA_DATA_LABEL_DEVICE_STATE));
            DeviceType deviceType = Parcels.unwrap(intent.getParcelableExtra(EXTRA_DATA_LABEL_DEVICE_TYPE));
            Long cardId = intent.getLongExtra(EXTRA_DATA_LABEL_CARD_ID, -1);
            handleDeviceState(deviceType, deviceState, cardId);
        }
        if (intent.hasExtra(EXTRA_DATA_LABEL_DEVICE_STATES) && intent.hasExtra(EXTRA_DATA_LABEL_DEVICE_TYPE)) {
            DeviceType deviceType = Parcels.unwrap(intent.getParcelableExtra(EXTRA_DATA_LABEL_DEVICE_TYPE));
            GatewayEventModel eventModel = intent.getParcelableExtra(EXTRA_DATA_LABEL_DEVICE_STATES);
            Long cardId = intent.getLongExtra(EXTRA_DATA_LABEL_CARD_ID, -1);
            handleDeviceStateRequest(deviceType, eventModel, cardId);
        }
    }

    private void handleDeviceState(DeviceType deviceType, DeviceState deviceState, long cardId) {
        if (deviceType == null || deviceState == null) {
            return;
        }
        FirebaseCrash.logcat(Log.DEBUG, TAG, "handleDeviceState, device: " + deviceType.name() + " state: " + deviceState.name());
        switch (deviceState) {
            case Disconnected:
            case Disconnecting:
            case Bound:
                if (isReconnecting) {
                    isReconnecting = false;
                    startDevice(deviceType, cardId);
                    startPolling(deviceType, cardId);
                } else {
                    stopPolling(deviceType, cardId);
                }
                break;
            case Connecting:
                break;
            case Connected:
                break;
            case Monitoring:
                break;
            case Reconnecting:
                isReconnecting = true;
                stopPolling(deviceType, cardId);
                stopDevice(deviceType, cardId);
                break;
            case Error:
                stopPolling(deviceType, cardId);
                break;
        }
    }

    public void handleCommand(DeviceCommand command, long cardId) {
        FirebaseCrash.logcat(Log.DEBUG, TAG, "handleCommand + " + command.toString());
        DeviceType deviceType = command.getDeviceType();
        DeviceCommand.CommandType commandType = command.getCommandType();
        switch (commandType) {
            case Start:
                createDevice(deviceType, cardId, "");
                startDevice(deviceType, cardId);
                startPolling(deviceType, cardId);
                break;
            case Stop:
                stopPolling(deviceType, cardId);
                stopDevice(deviceType, cardId);
                break;
            case PropertyChanged:
                updateDeviceProperties(deviceType, cardId);
                break;
        }
    }

    private void handleDeviceStateRequest(DeviceType deviceType, GatewayEventModel model, long cardId) {
        FirebaseCrash.logcat(Log.INFO, TAG, "handleDeviceStateRequest + " + model);
        DeviceKey key = new DeviceKey(deviceType, cardId);
        PollingServiceEntry entry = mDeviceExecutorsMap.get(key);
        if (entry != null) {
            DeviceAbstract device = entry.getDevice();
            if (device.getDeviceState() == DeviceState.Connected) {
                ScheduledExecutorService service = entry.getService();
                service.schedule(device.handleDeviceStateRequest(model), 0, TimeUnit.MILLISECONDS);
            }
        }
    }

    public void startDevice(DeviceType deviceType, long cardId) {
        FirebaseCrash.logcat(Log.INFO, TAG, "startDevice");
        DeviceKey key = new DeviceKey(deviceType, cardId);
        PollingServiceEntry entry = mDeviceExecutorsMap.get(key);
        if (entry != null) {
            DeviceAbstract device = entry.getDevice();
            if (device.getDeviceState() == DeviceState.Disconnected ||
                    device.getDeviceState() == DeviceState.Error ||
                    device.isMultipleDevice()) {
                ScheduledExecutorService service = entry.getService();
                service.schedule(device.getEnableTask(cardId), 0, TimeUnit.MILLISECONDS);
            }
        }
    }

    public DeviceAbstract createDevice(DeviceType deviceType, long cardId, String deviceHid) {
        DeviceAbstract device = null;
        DeviceKey key = new DeviceKey(deviceType, cardId);
        if (!mDeviceExecutorsMap.containsKey(key)) {
            FirebaseCrash.logcat(Log.DEBUG, TAG, "startDevice: " + deviceType.name());
            boolean isLocationNeeded = isLocationNeeded();
            switch (deviceType) {
                case MicrosoftBand:
                    device = new MsBand(this, cardId, isLocationNeeded, deviceHid);
                    break;
                case SensorPuck:
                    device = new SensorPuck(this, cardId, isLocationNeeded, deviceHid);
                    break;
                case AndroidInternal:
                    device = new DeviceAndroid(this, cardId, isLocationNeeded, deviceHid);
                    break;
                case SenseAbilityKit:
                    device = new SenseAbilityKit(this, cardId, isLocationNeeded, deviceHid);
                    break;
                case ThunderBoard:
                    device = new ThunderBoard(this, cardId, isLocationNeeded, deviceHid);
                    break;
                case SensorTile:
                    device = new SensorTile(this, cardId, isLocationNeeded, deviceHid);
                    break;
                case SimbaPro:
                    device = new SimbaPro(this, cardId, isLocationNeeded, deviceHid);
                    break;
            }
            if (device != null) {
                ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor(
                        new PollingThreadFactory(deviceType.name()));
                PollingServiceEntry entry = new PollingServiceEntry();
                entry.setDevice(device);
                entry.setService(service);
                mDeviceExecutorsMap.put(key, entry);
            }
        } else {
            device = mDeviceExecutorsMap.get(key).getDevice();
            FirebaseCrash.logcat(Log.DEBUG, TAG, deviceType.name() + " is already exists");
        }
        return device;
    }

    public void startPolling(DeviceType deviceType, long cardId) {
        FirebaseCrash.logcat(Log.INFO, TAG, "startPolling");
        DeviceKey key = new DeviceKey(deviceType, cardId);
        PollingServiceEntry entry = mDeviceExecutorsMap.get(key);
        if (entry != null) {
            DeviceAbstract device = entry.getDevice();
            ScheduledExecutorService service = entry.getService();
            Runnable task = device.getPollingTask();
            final ScheduledFuture<?> future = service.scheduleAtFixedRate(task, 0, getPollingInterval(),
                    TimeUnit.MILLISECONDS);
            entry.setPollingTask(future);
        }
    }

    public void stopPolling(DeviceType deviceType, long cardId) {
        DeviceKey key = new DeviceKey(deviceType, cardId);
        PollingServiceEntry entry = mDeviceExecutorsMap.get(key);
        if (entry != null) {
            Future<?> pollingTask = entry.getPollingTask();
            if (pollingTask != null) {
                pollingTask.cancel(false);
            }
            FirebaseCrash.logcat(Log.DEBUG, TAG, "stopPolling " + deviceType.name() + " is stopped");
        } else {
            FirebaseCrash.logcat(Log.DEBUG, TAG, "stopPolling " + deviceType.name() + " is not running");
        }
    }

    public void stopDevice(DeviceType deviceType, long cardId) {
        DeviceKey key = new DeviceKey(deviceType, cardId);
        PollingServiceEntry entry = mDeviceExecutorsMap.get(key);
        if (entry != null) {
            DeviceAbstract device = entry.getDevice();
            ScheduledExecutorService service = entry.getService();
            service.schedule(device.getDisableTask(cardId), 0, TimeUnit.MILLISECONDS);
        }
    }

    public void removeDevice(DeviceType deviceType, long cardId) {
        DeviceKey key = new DeviceKey(deviceType, cardId);
        PollingServiceEntry entry = mDeviceExecutorsMap.get(key);
        if (entry != null) {
            ScheduledExecutorService service = entry.getService();
            mDeviceExecutorsMap.remove(key);
            service.shutdown();
        }
    }

    public void stopAllDevicePolling() {
        FirebaseCrash.logcat(Log.DEBUG, TAG, "stopAllDevicePolling");
        if (!mDeviceExecutorsMap.isEmpty()) {
            for (DeviceKey key : mDeviceExecutorsMap.keySet()) {
                stopPolling(key.getDeviceType(), key.getIndex());
            }
        }
    }

    public void updateDeviceProperties(DeviceType deviceType, long cardId) {
        PollingServiceEntry entry = mDeviceExecutorsMap.get(new DeviceKey(deviceType, cardId));
        if (entry != null) {
            entry.getDevice().updateProperties();
        }
    }

    public DeviceAbstract getDevice(DeviceType deviceType, long cardId) {
        PollingServiceEntry entry = mDeviceExecutorsMap.get(new DeviceKey(deviceType, cardId));
        DeviceAbstract device = null;
        if (entry != null) {
            device = entry.getDevice();
        }
        return device;
    }

    private boolean isLocationNeeded() {
        FirebaseCrash.logcat(Log.DEBUG, TAG, "isLocationNeeded");
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        return sp.getBoolean(Constant.SP_LOCATION_SERVICE, false);
    }

    private long getPollingInterval() {
        FirebaseCrash.logcat(Log.DEBUG, TAG, "getPollingInterval");
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        return sp.getInt(Constant.SP_SENDING_RATE, Constant.DEFAULT_DEVICE_POLLING_INTERVAL) * 1000L;
    }

    private class PollingThreadFactory implements ThreadFactory {
        private String mThreadName;

        public PollingThreadFactory(String threadName) {
            mThreadName = threadName;
        }

        @Override
        public Thread newThread(Runnable r) {
            FirebaseCrash.logcat(Log.DEBUG, TAG, "newThread is created for " + mThreadName);
            Thread thread = new Thread(r);
            thread.setName(mThreadName + " polling thread");
            thread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread thread, Throwable ex) {
                    FirebaseCrash.logcat(Log.ERROR, TAG, "thread: " + thread.getName());
                    FirebaseCrash.report(ex);
                }
            });
            return thread;
        }
    }

    private class PollingServiceEntry {
        private DeviceAbstract mDevice;
        private ScheduledExecutorService mService;
        private ScheduledFuture<?> mPollingTask;

        public DeviceAbstract getDevice() {
            return mDevice;
        }

        public void setDevice(DeviceAbstract device) {
            this.mDevice = device;
        }

        public ScheduledExecutorService getService() {
            return mService;
        }

        public void setService(ScheduledExecutorService service) {
            this.mService = service;
        }

        public ScheduledFuture<?> getPollingTask() {
            return mPollingTask;
        }

        public void setPollingTask(ScheduledFuture<?> future) {
            this.mPollingTask = future;
        }
    }

    public class LocalBinder extends Binder {
        public DevicePollingService getService() {
            // Return this instance of LocalService so clients can call public methods
            return DevicePollingService.this;
        }
    }
}
