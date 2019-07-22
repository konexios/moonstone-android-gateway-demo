package com.arrow.jmyiotgateway.cloud;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.arrow.acn.api.AcnApiService;
import com.arrow.acn.api.BuildConfig;
import com.arrow.acn.api.listeners.CheckinGatewayListener;
import com.arrow.acn.api.listeners.CommonRequestListener;
import com.arrow.acn.api.listeners.ConnectionListener;
import com.arrow.acn.api.listeners.GatewayRegisterListener;
import com.arrow.acn.api.listeners.GetGatewayConfigListener;
import com.arrow.acn.api.listeners.ServerCommandsListener;
import com.arrow.acn.api.listeners.TelemetryRequestListener;
import com.arrow.acn.api.models.ApiError;
import com.arrow.acn.api.models.CommonResponse;
import com.arrow.acn.api.models.ConfigResponse;
import com.arrow.acn.api.models.GatewayEventModel;
import com.arrow.acn.api.models.GatewayModel;
import com.arrow.acn.api.models.GatewayResponse;
import com.arrow.acn.api.models.GatewayType;
import com.arrow.acn.api.models.TelemetryModel;
import com.arrow.jmyiotgateway.AcnServiceHolder;
import com.arrow.jmyiotgateway.Config;
import com.arrow.jmyiotgateway.Constant;
import com.arrow.jmyiotgateway.Util;
import com.arrow.jmyiotgateway.cloud.iot.IotConstant;
import com.arrow.jmyiotgateway.device.DeviceType;
import com.google.firebase.crash.FirebaseCrash;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.arrow.jmyiotgateway.Constant.CONFIG_EXTRA_INFO;

/**
 * Created by osminin on 3/14/2016.
 */
public final class CloudService extends Service implements ServerCommandsListener, GatewayRegisterListener, CheckinGatewayListener, GetGatewayConfigListener, ConnectionListener, TelemetryRequestListener {
    private static final String TAG = CloudService.class.getName();

    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;
    private final BroadcastReceiver mInternalMessagesReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Constant.ACTION_IOT_DATA_RECEIVED)) {
                handleIncomingIntent(intent);
            }
        }
    };

    private AcnApiService mTelemetrySendService;
    private HashMap<String, Runnable> mRunningDevicesMap = new HashMap<>();
    private List<TelemetryModel> mTelemetryQueue = new ArrayList<>();
    private long mPollingInterval;
    private ServerCommandsHandler mServerCommandsHandler;
    private String mGatewayId;
    private int mHeartBeatInterval;
    private Config mConfig;
    private Runnable mHeartBeatTask = new Runnable() {

        @Override
        public void run() {
            if (mTelemetrySendService != null) {
                mTelemetrySendService.gatewayHeartbeat(mGatewayId, new CommonRequestListener() {
                    @Override
                    public void onRequestSuccess(CommonResponse commonResponse) {
                        FirebaseCrash.logcat(Log.DEBUG, TAG, "gatewayHeartbeat");
                        if (mServiceHandler != null) {
                            mServiceHandler.postDelayed(mHeartBeatTask, mHeartBeatInterval * 1000L);
                        }
                    }

                    @Override
                    public void onRequestError(ApiError error) {
                        FirebaseCrash.logcat(Log.ERROR, TAG, "gatewayHeartbeat");
                        if (mServiceHandler != null) {
                            mServiceHandler.postDelayed(mHeartBeatTask, mHeartBeatInterval * 1000L);
                        }
                    }
                });
            }
        }
    };

    @Override
    public void onCreate() {
        FirebaseCrash.logcat(Log.DEBUG, TAG, "onCreate");
        HandlerThread thread = new HandlerThread(TAG, android.os.Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        mPollingInterval = sp.getInt(Constant.SP_SENDING_RATE, Constant.DEFAULT_DEVICE_POLLING_INTERVAL) * 1500L;
        mHeartBeatInterval = sp.getInt(Constant.SP_CLOUD_HERATBEAT_INTERVAL, Constant.HEART_BEAT_INTERVAL);
        IntentFilter filter = new IntentFilter(Constant.ACTION_IOT_DATA_RECEIVED);
        LocalBroadcastManager.getInstance(this).registerReceiver(mInternalMessagesReceiver, filter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        FirebaseCrash.logcat(Log.DEBUG, TAG, "onStartCommand");
        mConfig = Parcels.unwrap(intent.getParcelableExtra(CONFIG_EXTRA_INFO));
        initializeService();
        handleIncomingIntent(intent);
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        FirebaseCrash.logcat(Log.DEBUG, TAG, "onDestroy");
        super.onDestroy();
        if (mTelemetrySendService != null && mTelemetrySendService.isConnected()) {
            mTelemetrySendService.disconnect();
        }
        mServiceHandler.removeCallbacks(null);
        mServiceHandler = null;
        mConfig = null;
        mTelemetrySendService = null;
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mInternalMessagesReceiver);
    }

    private void handleIncomingIntent(Intent intent) {
        FirebaseCrash.logcat(Log.VERBOSE, TAG, "handleIncomingIntent");
        if (intent != null && intent.hasExtra(IotConstant.EXTRA_DATA_LABEL_TELEMETRY_BUNDLE)) {
            Bundle bundle = intent.getBundleExtra(IotConstant.EXTRA_DATA_LABEL_TELEMETRY_BUNDLE);
            Message msg = mServiceHandler.obtainMessage();
            msg.setData(bundle);
            mServiceHandler.sendMessage(msg);
        }
    }

    @Override
    public void onServerCommand(GatewayEventModel model) {
        mConfig = new Config().loadActive(this);
        mServerCommandsHandler.handleServerCommand(model, mConfig);
    }

    @Override
    public void onGatewayRegistered(GatewayResponse gatewayResponse) {
        FirebaseCrash.logcat(Log.VERBOSE, TAG, "onGatewayRegistered");
        if (mConfig != null) {
            mGatewayId = gatewayResponse.getHid();
            mConfig.setGatewayId(mGatewayId);
            mConfig.setExternalId(gatewayResponse.getExternalId());
            mConfig.setGatewayUid(generateGatewayUid());
            List<Config.ConfigDeviceModel> list = mConfig.getAddedDevices(this);
            mConfig.save(this);
            FirebaseCrash.logcat(Log.INFO, TAG, "save: " + mConfig.getEmail() + mConfig.getActive());
            mTelemetrySendService.getGatewayConfig(mGatewayId, this);
        }
    }

    @Override
    public void onGatewayRegisterFailed(ApiError error) {
        FirebaseCrash.logcat(Log.ERROR, TAG, "onGatewayRegisterFailed, code: " + error.getStatus() +
                " message: " + error.getMessage());
    }

    @Override
    public void onCheckinGatewaySuccess() {
        FirebaseCrash.logcat(Log.VERBOSE, TAG, "onCheckinGatewaySuccess");
        if (mServiceHandler != null) {
            mServiceHandler.post(mHeartBeatTask);
            mTelemetrySendService.connect(this);
            mServerCommandsHandler = new ServerCommandsHandler(this);
        }
    }

    @Override
    public void onCheckinGatewayError(ApiError error) {
        FirebaseCrash.logcat(Log.ERROR, TAG, "onCheckinGatewayError, code: " + error.getStatus() +
                " message: " + error.getMessage());
    }

    @Override
    public void onGatewayConfigReceived(ConfigResponse configResponse) {
        FirebaseCrash.logcat(Log.VERBOSE, TAG, "onGatewayConfigReceived");
        AcnServiceHolder.setApiKey(configResponse.getKey().getApiKey());
        AcnServiceHolder.setApiSecretKey(configResponse.getKey().getSecretKey());
        mTelemetrySendService.checkinGateway(mGatewayId, mConfig.getGatewayUid(), this);
    }

    @Override
    public void onGatewayConfigFailed(ApiError error) {
        FirebaseCrash.logcat(Log.ERROR, TAG, "onGatewayConfigFailed, code: " + error.getStatus() +
                " message: " + error.getMessage());
        if (error.getMessage().equals("gateway is not found") || error.getMessage().contains("400")){
            mConfig.setGatewayId("");
            mRunningDevicesMap.clear();
            mConfig.save(this);
            List<Config.ConfigDeviceModel> list = mConfig.getAddedDevices(this);
            registerGateway();
        }
    }

    private void addDeviceToSet(Bundle bundle) {
        String deviceId = bundle.getString(IotConstant.EXTRA_DATA_LABEL_DEVICE_ID);
        TelemetryModel model = new TelemetryModel();
        model.setTelemetry(bundle.getString(IotConstant.EXTRA_DATA_LABEL_TELEMETRY));
        DeviceType deviceType = Parcels.unwrap(bundle.getParcelable(IotConstant.EXTRA_DATA_LABEL_DEVICE_TYPE));
        model.setDeviceType(deviceType.name());
        mTelemetryQueue.add(model);
        if (!mRunningDevicesMap.containsKey(deviceId)) {
            Runnable removingTask = new DeviceRemovingTask(deviceId);
            mServiceHandler.postDelayed(removingTask, mPollingInterval);
            mRunningDevicesMap.put(deviceId, removingTask);
        } else if (mRunningDevicesMap.size() > 1 && mRunningDevicesMap.size() <= mTelemetryQueue.size()) {
            if (mTelemetrySendService != null) {
                mTelemetrySendService.sendBatchTelemetry(mTelemetryQueue, this);
            }
            mTelemetryQueue.clear();
        } else if (mRunningDevicesMap.size() <= 1) {
            mTelemetryQueue.clear();
        }
        Runnable removingTask = mRunningDevicesMap.get(deviceId);
        mServiceHandler.removeCallbacks(removingTask);
        mServiceHandler.postDelayed(removingTask, mPollingInterval);
    }

    private void initializeService() {
        FirebaseCrash.logcat(Log.DEBUG, TAG, "initializeService");
        mServiceHandler.removeCallbacks(mHeartBeatTask);
        mTelemetrySendService = AcnServiceHolder.createService(this, mConfig);
        mTelemetrySendService.setServerCommandsListener(CloudService.this);
        registerGateway();
    }

    private void registerGateway() {
        mGatewayId = mConfig.getGatewayId();
        final String applicationHid = mConfig.getApplicationHid();
        if (TextUtils.isEmpty(mGatewayId)) {
            String uid = generateGatewayUid();
            FirebaseCrash.logcat(Log.DEBUG, TAG, "registerGateway() UID: " + uid);

            String name = String.format("%s %s", Build.MANUFACTURER, Build.MODEL);
            String osName = String.format("Android %s", Build.VERSION.RELEASE);
            String swName = Constant.SOFTWARE_NAME;
            String userHid = mConfig.getUserId();

            GatewayModel gatewayModel = new GatewayModel();
            gatewayModel.setName(name);
            gatewayModel.setOsName(osName);
            gatewayModel.setSoftwareName(swName);
            gatewayModel.setUid(uid);
            gatewayModel.setType(GatewayType.Mobile);
            gatewayModel.setUserHid(userHid);
            gatewayModel.setApplicationHid(applicationHid);
            gatewayModel.setSoftwareVersion(Util.getVersionNumber());
            gatewayModel.setSdkVersion(BuildConfig.VERSION_NAME);

            mTelemetrySendService.registerGateway(gatewayModel, this);
        } else {
            mTelemetrySendService.getGatewayConfig(mGatewayId, this);
        }
    }

    private String generateGatewayUid() {
        return  Settings.Secure.getString(getContentResolver(),
                Settings.Secure.ANDROID_ID);
    }

    @Override
    public void onConnectionSuccess() {
        FirebaseCrash.logcat(Log.DEBUG, TAG, "onConnectionSuccess()");
    }

    @Override
    public void onConnectionError(ApiError apiError) {
        FirebaseCrash.logcat(Log.ERROR, TAG, "onConnectionError() " + apiError.getStatus());
    }

    @Override
    public void onTelemetrySendSuccess() {
        FirebaseCrash.logcat(Log.VERBOSE, TAG, "onTelemetrySendSuccess()");
    }

    @Override
    public void onTelemetrySendError(ApiError apiError) {
        FirebaseCrash.logcat(Log.ERROR, TAG, "onTelemetrySendError() " + apiError.getStatus());
    }

    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            TelemetryModel telemetryModel = new TelemetryModel();
            telemetryModel.setTelemetry(bundle.getString(IotConstant.EXTRA_DATA_LABEL_TELEMETRY));
            telemetryModel.setDeviceExternalId(bundle.getString(IotConstant.EXTRA_DATA_LABEL_EXTERNAL_DEVICE_ID));
            DeviceType deviceType = Parcels.unwrap(bundle.getParcelable(IotConstant.EXTRA_DATA_LABEL_DEVICE_TYPE));
            telemetryModel.setDeviceType(deviceType.name());
            if (mTelemetrySendService != null && mTelemetrySendService.isConnected()) {
                if (mTelemetrySendService.hasBatchMode()) {
                    addDeviceToSet(bundle);
                    if (mRunningDevicesMap.size() <= 1) {
                        mTelemetrySendService.sendSingleTelemetry(telemetryModel, CloudService.this);
                    }
                } else {
                    mTelemetrySendService.sendSingleTelemetry(telemetryModel, CloudService.this);
                }
            }
        }
    }

    private class DeviceRemovingTask implements Runnable {
        private String deviceId;

        public DeviceRemovingTask(String deviceId) {
            this.deviceId = deviceId;
        }

        @Override
        public void run() {
            mRunningDevicesMap.remove(deviceId);
        }
    }
}