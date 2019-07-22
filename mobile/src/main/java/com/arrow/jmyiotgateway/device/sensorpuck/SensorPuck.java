package com.arrow.jmyiotgateway.device.sensorpuck;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.arrow.jmyiotgateway.cloud.iot.IotParameter;
import com.arrow.jmyiotgateway.device.DeviceState;
import com.arrow.jmyiotgateway.device.DeviceType;
import com.arrow.jmyiotgateway.device.TelemetriesNames;
import com.arrow.jmyiotgateway.device.ble.Advertisement;
import com.arrow.jmyiotgateway.device.ble.InfiniteBleScannerFactory;
import com.arrow.jmyiotgateway.device.ble.abstracts.AbstractBleScannerFactory;
import com.arrow.jmyiotgateway.device.ble.abstracts.BleDeviceAbstract;
import com.arrow.jmyiotgateway.device.ble.abstracts.BleSensorAbstract;
import com.arrow.jmyiotgateway.fragments.AbstractDetailsFragment;
import com.arrow.acn.api.models.DeviceRegistrationModel;
import com.google.firebase.crash.FirebaseCrash;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by osminin on 6/6/2016.
 */
public class SensorPuck extends BleDeviceAbstract {
    public static final int ENVIRONMENTAL_MODE = 0;
    public static final int BIOMETRIC_MODE = 1;
    public static final int NOT_FOUND_MODE = 9;
    public static final int PENDING_MODE = 8;
    public static final int SD_AMB_LIGHT = 4;
    public static final int SD_BATTERY = 6;
    public static final int SD_HRM_RATE = 17;
    public static final int SD_HRM_SAMPLE = 18;
    public static final int SD_HRM_STATE = 16;
    public static final int SD_HUMIDITY = 2;
    public static final int SD_MODE = 0;
    public static final int SD_SEQUENCE = 1;
    public static final int SD_TEMPERATURE = 3;
    public static final int SD_UV_LIGHT = 5;
    final static String DEVICE_TYPE_NAME = "sl-sensorpuck";
    private final static String DEVICE_UID_PREFIX = "sp-android-";
    private final static String TAG = SensorPuck.class.getSimpleName();
    private final static long TIMEOUT = 10000;

    private Handler mTimeoutHandler;
    private Map<Long, Puck> mCreatedPucksMap;
    private Map<Long, Puck> mRunningPucksMap;
    private boolean mIsLocationNeeded;

    private Runnable mDisconnectByTimeout = new Runnable() {
        @Override
        public void run() {
            for (Iterator<Map.Entry<Long, Puck>> iterator = mRunningPucksMap.entrySet().iterator();
                    iterator.hasNext(); ) {
                Map.Entry<Long, Puck> entry = iterator.next();
                if (entry.getValue() != null && entry.getValue().getAddress() != null) {
                    disable(entry.getKey());
                } else {
                    mCreatedPucksMap.put(entry.getKey(), entry.getValue());
                    iterator.remove();
                }
            }
            disable(0);
        }
    };

    public SensorPuck(Context context, long cardId, boolean isLocationNeeded, String deviceHid) {
        super(context, cardId, isLocationNeeded, deviceHid);
        FirebaseCrash.logcat(Log.DEBUG, TAG, "SensorPuck constructor");
        mTimeoutHandler = new Handler(context.getMainLooper());
        Puck puck = new Puck(context, cardId, isLocationNeeded, deviceHid);
        mCreatedPucksMap = new HashMap<>();
        mCreatedPucksMap.put(cardId, puck);
        mRunningPucksMap = new HashMap<>();
        mIsLocationNeeded = isLocationNeeded;
    }

    @Override
    public void scanComplete(List<Advertisement> advertisements) {
        FirebaseCrash.logcat(Log.DEBUG, TAG, "scanComplete");
        for (Advertisement advertisement : advertisements) {
            parseData(advertisement);
        }
    }

    @Override
    protected void enable(long cardId) {
        FirebaseCrash.logcat(Log.DEBUG, TAG, "enable");
        if (!mDeviceScanner.isScanning()) {
            try {
                mDeviceScanner.scanDevice(getContext(), SensorPuck.this);
                mState = DeviceState.Connecting;
                mCreatedPucksMap.get(cardId).notifyDeviceStateChanged(mState);
                FirebaseCrash.logcat(Log.DEBUG, TAG, "Connecting");
                mTimeoutHandler.postDelayed(mDisconnectByTimeout, TIMEOUT);
            } catch (Exception e) {
                FirebaseCrash.logcat(Log.ERROR, TAG, "enable failed");
                FirebaseCrash.report(e);
                disable(mCardId);
            }
        }
        if (!mRunningPucksMap.containsKey(cardId)) {
            Puck puck = mCreatedPucksMap.remove(cardId);
            if (puck == null) {
                throw new NullPointerException();
            }
            puck.preEnable();
            mRunningPucksMap.put(cardId, puck);
            puck.checkAndRegisterDevice();
        }
    }

    @Override
    public void disable(long cardId) {
        Puck puck;
        if (mRunningPucksMap.size() <= 1) {
            FirebaseCrash.logcat(Log.DEBUG, TAG, "disable");
            mState = DeviceState.Disconnecting;
            notifyDeviceStateChanged(mState);
            mDeviceScanner.stopScan();
            mState = DeviceState.Disconnected;
            notifyDeviceStateChanged(mState);
            mTimeoutHandler.removeCallbacks(mDisconnectByTimeout);
            puck = mRunningPucksMap.remove(cardId);
            FirebaseCrash.logcat(Log.DEBUG, TAG, "Disconnected");
        } else {
            puck = mRunningPucksMap.remove(cardId);
        }
        if (puck != null) {
            puck.notifyDeviceStateChanged(DeviceState.Disconnected);
            puck.postDisable();
            mCreatedPucksMap.put(cardId, puck);
        }
    }

    @Override
    public DeviceType getDeviceType() {
        return DeviceType.SensorPuck;
    }

    @Override
    public String getDeviceUId() {
        return getDevice() == null ? null : DEVICE_UID_PREFIX + getDevice().getAddress().toLowerCase().replace(":", "");
    }

    @Override
    public DeviceState getDeviceState() {
        return mState;
    }

    @Override
    public String getDeviceTypeName() {
        return DEVICE_TYPE_NAME;
    }

    @Override
    public AbstractDetailsFragment getDetailsFragment(long cardId) {
        AbstractDetailsFragment fragment;
        if (mRunningPucksMap.containsKey(cardId)) {
            fragment = mRunningPucksMap.get(cardId).getDetailsFragment(cardId);
        } else {
            Puck puck = new Puck(mContext, cardId, mIsLocationNeeded, "");
            mCreatedPucksMap.put(cardId, puck);
            fragment = puck.getDetailsFragment(cardId);
        }
        updateRegistrationModel();
        return fragment;
    }

    @Override
    protected BleSensorAbstract<?> createSensor(BluetoothGattService service) {
        //nothing to do here
        return null;
    }

    @Override
    protected void processCharacteristicChanged
            (BleSensorAbstract<?> sensor, BluetoothGattCharacteristic characteristic) {
        //nothing to do here
    }

    @Override
    protected AbstractBleScannerFactory getScannerFactory() {
        return new InfiniteBleScannerFactory();
    }

    private void parseData(Advertisement advertisement) {
        FirebaseCrash.logcat(Log.DEBUG, TAG, "parseData");
        int x = SD_MODE;
        try {
            while (x < advertisement.getData().length && advertisement.getData()[x] != 0) {
                onAdvertisingData(advertisement.getDevice(),
                        advertisement.getData()[x + SD_SEQUENCE],
                        Arrays.copyOfRange(advertisement.getData(), x + SD_HUMIDITY, (advertisement.getData()[x] + x) + SD_SEQUENCE));
                x += advertisement.getData()[x] + SD_SEQUENCE;
            }
        } catch (Exception e) {
            FirebaseCrash.logcat(Log.ERROR, TAG, "parseData");
            FirebaseCrash.report(e);
        }
    }

    private void onAdvertisingData(BluetoothDevice device, byte adType, byte[] adData) {
        String deviceName = device.getName() + " " + device.getAddress();
        FirebaseCrash.logcat(Log.VERBOSE, TAG, "onAdvertisingData - " + deviceName);
        if (adType != -1) {
            return;
        }
        /* If the advertisement contains Silabs manufacturer specific data */
        if (((adData[0] == 0x34) || (adData[0] == 0x35)) && (adData[1] == 0x12)) {
            FirebaseCrash.logcat(Log.VERBOSE, TAG, "onAdvertisingData - sp found");
            //reset timeout
            mTimeoutHandler.removeCallbacks(mDisconnectByTimeout);
            Puck currentPuck = findPuck(device.getAddress());
            if (currentPuck == null) {
                Puck puck = findWaitingPuck();
                if (puck != null) {
                    puck.setAddress(device.getAddress());
                    puck.preEnable();
                    puck.checkAndRegisterDevice();
                    puck.notifyDeviceStateChanged(DeviceState.Connected);
                }
            } else {
                //* If its an old style advertisement
                if (adData[0] == 0x34) {
                    //* Process the sensor data
                    for (int x = 2; x < adData.length; x += adData[x] + 1) {
                        onSensorData(currentPuck, adData[x + 1], Arrays.copyOfRange(adData, x + 2, x + adData[x] + 1));
                    }
                }
                //* If its a new style advertisement
                if (adData[0] == 0x35) {
                    //* If its an environmental advertisement then process it
                    if (adData[2] == ENVIRONMENTAL_MODE) {
                        onEnvironmentalData(currentPuck, Arrays.copyOfRange(adData, 3, 14));
                    }
                    //* If its a biometric advertisement then process it
                    if (adData[2] == BIOMETRIC_MODE) {
                        onBiometricData(currentPuck, Arrays.copyOfRange(adData, 3, 18));
                    }
                }
                //set new timeout counter
                mTimeoutHandler.postDelayed(mDisconnectByTimeout, TIMEOUT);
            }
        }
    }

    private Puck findWaitingPuck() {
        Puck puck = null;
        long cardId = -1;
        for (Map.Entry<Long, Puck> tmp : mRunningPucksMap.entrySet()) {
            if (tmp.getValue().getAddress() == null) {
                puck = tmp.getValue();
                cardId = tmp.getKey();
                break;
            }
        }
        if (cardId >= 0) {
            mCreatedPucksMap.remove(cardId);
        }
        return puck;
    }

    private Puck findPuck(String address) {
        Puck result = null;
        for (Puck puck : mRunningPucksMap.values()) {
            if (puck.getAddress() != null && puck.getAddress().equals(address)) {
                result = puck;
            }
        }
        return result;
    }

    private void onSensorData(Puck puck, byte sdType, byte[] sdData) {
        FirebaseCrash.logcat(Log.DEBUG, TAG, "onSensorData");
        int Value;
        if (sdData.length == SD_HUMIDITY) {
            Value = Int16(sdData[SD_MODE], sdData[SD_SEQUENCE]);
        } else {
            Value = Int8(sdData[SD_MODE]);
        }
        IotParameter iotParameter = null;
        switch (sdType) {
            case SD_MODE /*0*/:
                puck.setMeasurementMode(Value);
                break;
            case SD_SEQUENCE /*1*/:
                puck.setSequence(Value);
                break;
            case SD_HUMIDITY /*2*/:
                puck.setHumidity(((float) Value) / 10.0f);
                iotParameter = new IotParameter(TelemetriesNames.HUMIDITY, Float.toString(puck.getHumidity()));
                break;
            case SD_TEMPERATURE /*3*/:
                puck.setTemperature(((float) Value) / 10.0f);
                iotParameter = new IotParameter(TelemetriesNames.HUMIDITY, Float.toString(puck.getTemperature()));
                break;
            case SD_AMB_LIGHT /*4*/:
                puck.setAmbientLight(Value * SD_HUMIDITY);
                iotParameter = new IotParameter(TelemetriesNames.LIGHT, Integer.toString(puck.getAmbientLight()));
                break;
            case SD_UV_LIGHT /*5*/:
                puck.setUV_Index(Value);
                iotParameter = new IotParameter(TelemetriesNames.UV, Integer.toString(puck.getUV_Index()));
                break;
            case SD_BATTERY /*6*/:
                puck.setBattery(((float) Value) / 10.0f);
                break;
            case SD_HRM_STATE /*16*/:
                puck.setHRM_State(Value);
                break;
            case SD_HRM_RATE /*17*/:
                puck.setHRM_Rate(Value);
                break;
            case SD_HRM_SAMPLE /*18*/:
                puck.setHRM_PrevSample(puck.getHRM_Sample()[SD_AMB_LIGHT]);
                for (int x = SD_MODE; x < SD_UV_LIGHT; x += SD_SEQUENCE) {
                    puck.getHRM_Sample()[x] = Int16(sdData[x * SD_HUMIDITY], sdData[(x * SD_HUMIDITY) + SD_SEQUENCE]);
                }
                break;
        }
        if (iotParameter != null) {
            puck.putIotParams(iotParameter);
        }
    }

    private void onEnvironmentalData(Puck puck, byte[] data) {
        FirebaseCrash.logcat(Log.DEBUG, TAG, "onEnvironmentalData");
        puck.setMeasurementMode(SD_MODE);
        puck.setSequence(Int8(data[SD_MODE]));
        puck.setHumidity(((float) Int16(data[SD_TEMPERATURE], data[SD_AMB_LIGHT])) / 10.0f);
        puck.setTemperature(((float) Int16(data[SD_UV_LIGHT], data[SD_BATTERY])) / 10.0f);
        puck.setAmbientLight(Int16(data[7], data[PENDING_MODE]) * SD_HUMIDITY);
        puck.setUV_Index(Int8(data[NOT_FOUND_MODE]));
        puck.setBattery(((float) Int8(data[10])) / 10.0f);
        puck.putIotParams(new IotParameter(TelemetriesNames.HUMIDITY, Float.toString(puck.getHumidity())),
                new IotParameter(TelemetriesNames.TEMPERATURE, Float.toString(puck.getTemperature())),
                new IotParameter(TelemetriesNames.LIGHT, Integer.toString(puck.getAmbientLight())),
                new IotParameter(TelemetriesNames.UV, Integer.toString(puck.getUV_Index())));
        //TODO: add Battery to params;
    }

    private void onBiometricData(Puck puck, byte[] data) {
        FirebaseCrash.logcat(Log.DEBUG, TAG, "onBiometricData");
        puck.setMeasurementMode(SD_SEQUENCE);
        puck.setSequence(Int8(data[SD_MODE]));
        puck.setHRM_State(Int8(data[SD_TEMPERATURE]));
        puck.setHRM_Rate(Int8(data[SD_AMB_LIGHT]));
        puck.setHRM_PrevSample(puck.getHRM_Sample()[SD_AMB_LIGHT]);
        for (int x = SD_MODE; x < SD_UV_LIGHT; x += SD_SEQUENCE) {
            puck.getHRM_Sample()[x] = Int16(data[(x * SD_HUMIDITY) + SD_UV_LIGHT], data[(x * SD_HUMIDITY) + SD_BATTERY]);
        }
        puck.putIotParams(new IotParameter(TelemetriesNames.HEART_RATE, Integer.toString(puck.getHRM_Rate())));
        //TODO: are other parameters needed?
    }

    private int Int8(byte data) {
        return ((char) data) & 0xff;
    }

    private int Int16(byte lsb, byte msb) {
        return Int8(lsb) + (Int8(msb) * 0x00000100);
    }

    @Override
    protected DeviceRegistrationModel getRegisterPayload() {
        DeviceRegistrationModel payload = super.getRegisterPayload();
        payload.setName(payload.getName().concat(getNameSuffix()));
        return payload;
    }

    private String getNameSuffix() {
        String result = "";
        if (!TextUtils.isEmpty(mDevice.getAddress())) {
            int length = mDevice.getAddress().length();
            result = "-" + mDevice.getAddress().substring(length - 5, length).replace(":", "");
        }
        return result;
    }

    @Override
    public void pollingTask() {
        for (Puck puck : mRunningPucksMap.values()) {
            puck.pollingTask();
        }
    }

    @Override
    public boolean isMultipleDevice() {
        return true;
    }
}
