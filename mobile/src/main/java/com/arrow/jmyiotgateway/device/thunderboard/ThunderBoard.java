package com.arrow.jmyiotgateway.device.thunderboard;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.Log;

import com.arrow.acn.api.models.DeviceRegistrationModel;
import com.arrow.acn.api.models.StateModel;
import com.arrow.jmyiotgateway.R;
import com.arrow.jmyiotgateway.cloud.iot.IotParameter;
import com.arrow.jmyiotgateway.device.DevicePropertiesAbstract;
import com.arrow.jmyiotgateway.device.DeviceState;
import com.arrow.jmyiotgateway.device.DeviceType;
import com.arrow.jmyiotgateway.device.TelemetriesNames;
import com.arrow.jmyiotgateway.device.ble.SimpleBleScannerFactory;
import com.arrow.jmyiotgateway.device.ble.abstracts.AbstractBleScannerFactory;
import com.arrow.jmyiotgateway.device.ble.abstracts.BleDeviceAbstract;
import com.arrow.jmyiotgateway.device.ble.abstracts.BleSensorAbstract;
import com.arrow.jmyiotgateway.fragments.AbstractDetailsFragment;
import com.arrow.jmyiotgateway.fragments.ThunderBoardDetailsFragment;
import com.google.firebase.crash.FirebaseCrash;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.arrow.jmyiotgateway.device.TelemetriesNames.LED_0;
import static com.arrow.jmyiotgateway.device.TelemetriesNames.LED_1;
import static com.arrow.jmyiotgateway.device.thunderboard.ThunderBoardUuids.CHARACTERISTIC_DIGITAL_OUTPUT;
import static com.arrow.jmyiotgateway.device.thunderboard.ThunderBoardUuids.UUID_CHARACTERISTIC_ACCELERATION;
import static com.arrow.jmyiotgateway.device.thunderboard.ThunderBoardUuids.UUID_CHARACTERISTIC_AMBIENT_LIGHT;
import static com.arrow.jmyiotgateway.device.thunderboard.ThunderBoardUuids.UUID_CHARACTERISTIC_HUMIDITY;
import static com.arrow.jmyiotgateway.device.thunderboard.ThunderBoardUuids.UUID_CHARACTERISTIC_ORIENTATION;
import static com.arrow.jmyiotgateway.device.thunderboard.ThunderBoardUuids.UUID_CHARACTERISTIC_SYSTEM_ID;
import static com.arrow.jmyiotgateway.device.thunderboard.ThunderBoardUuids.UUID_CHARACTERISTIC_TEMPERATURE;
import static com.arrow.jmyiotgateway.device.thunderboard.ThunderBoardUuids.UUID_CHARACTERISTIC_UV_INDEX;
import static com.arrow.jmyiotgateway.device.thunderboard.ThunderBoardUuids.UUID_SERVICE_ACCELERATION_ORIENTATION;
import static com.arrow.jmyiotgateway.device.thunderboard.ThunderBoardUuids.UUID_SERVICE_AMBIENT_LIGHT;
import static com.arrow.jmyiotgateway.device.thunderboard.ThunderBoardUuids.UUID_SERVICE_AUTOMATION_IO;
import static com.arrow.jmyiotgateway.device.thunderboard.ThunderBoardUuids.UUID_SERVICE_DEVICE_INFORMATION;
import static com.arrow.jmyiotgateway.device.thunderboard.ThunderBoardUuids.UUID_SERVICE_ENVIRONMENT_SENSING;

/**
 * Created by osminin on 7/26/2016.
 */

public final class ThunderBoard extends BleDeviceAbstract {
    private final static String TAG = ThunderBoard.class.getSimpleName();
    private static final String THUNDER_BOARD_REACT_NAME = "Thunder React";
    private final static String DEVICE_TYPE_NAME = "silabs-thunderboard-react";
    private final static String DEVICE_UID_PREFIX = "thunderboard-";
    public final int MAX_AMBIENT_LIGHT = 99999;
    private boolean isLed0On;
    private boolean isLed1On;
    private int readStatus;
    private String mUid;
    private final CountDownTimer readTimer = new CountDownTimer(5000, 300) {

        @Override
        public void onTick(long millisUntilFinished) {

            // skip the first tick
            if (5000L - millisUntilFinished < 300) {
                return;
            }

            BleSensorAbstract<?> sensor = null;
            switch (readStatus & 0x055) {
                // temperature submitted already
                case 0x01:
                    sensor = mSensorMap.get(UUID_CHARACTERISTIC_HUMIDITY);
                    readStatus |= 0x04;
                    break;
                // temperature and humidity submitted already
                case 0x05:
                    sensor = mSensorMap.get(UUID_CHARACTERISTIC_UV_INDEX);
                    readStatus |= 0x10;
                    break;
                // temperature and humidity and uv index submitted already
                case 0x15:
                    sensor = mSensorMap.get(UUID_CHARACTERISTIC_AMBIENT_LIGHT);
                    readStatus |= 0x40;
                    break;
                default:
                    break;
            }
            if (sensor != null) {
                sensor.readSensorDataRequest(mGatt);
            }
            if (TextUtils.isEmpty(mUid)) {
                BluetoothGattService service = mGatt.getService(UUID_SERVICE_DEVICE_INFORMATION);
                if (service != null) {
                    for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                        if (characteristic.getUuid().equals(UUID_CHARACTERISTIC_SYSTEM_ID)) {
                            mGatt.readCharacteristic(characteristic);
                            break;
                        }
                    }
                }
            }
        }

        @Override
        public void onFinish() {
            readStatus = 0;
            BleSensorAbstract<?> sensor = mSensorMap.get(UUID_CHARACTERISTIC_TEMPERATURE);
            if (sensor != null) {
                sensor.readSensorDataRequest(mGatt);
            }
            readStatus |= 0x01;
            start();
        }
    };

    public ThunderBoard(Context context, long cardId, boolean isLocationNeeded, String deviceHid) {
        super(context, cardId, isLocationNeeded, deviceHid);
        setName(THUNDER_BOARD_REACT_NAME);
        mProperties = new TBProperties(context);
    }

    @Override
    protected BleSensorAbstract<?> createSensor(BluetoothGattService service) {
        UUID serviceUUID = service.getUuid();
        if (UUID_SERVICE_ENVIRONMENT_SENSING.compareTo(serviceUUID) == 0 ||
                UUID_SERVICE_AMBIENT_LIGHT.compareTo(serviceUUID) == 0 ||
                UUID_SERVICE_ACCELERATION_ORIENTATION.compareTo(serviceUUID) == 0) {
            for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                addSensor(characteristic.getUuid(), service);
            }
        } else if (UUID_SERVICE_AUTOMATION_IO.compareTo(serviceUUID) == 0) {
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    service.getCharacteristics();
            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                int prop = gattCharacteristic.getProperties();
                if (BluetoothGattCharacteristic.PROPERTY_WRITE == (BluetoothGattCharacteristic.PROPERTY_WRITE & prop)) {
                    CHARACTERISTIC_DIGITAL_OUTPUT = gattCharacteristic;
                } else {
                    ThunderBoardUuids.CHARACTERISTIC_DIGITAL_INPUT = gattCharacteristic;
                }
            }

        }
        return null;
    }

    private void addSensor(UUID uuid, BluetoothGattService service) {
        BleSensorAbstract<?> sensor = null;
        if (uuid.compareTo(UUID_CHARACTERISTIC_TEMPERATURE) == 0 ||
                uuid.compareTo(UUID_CHARACTERISTIC_HUMIDITY) == 0 ||
                uuid.compareTo(UUID_CHARACTERISTIC_AMBIENT_LIGHT) == 0 ||
                uuid.compareTo(UUID_CHARACTERISTIC_UV_INDEX) == 0) {
            sensor = new ThunderBoardCommonSensor(getDevice(), service, uuid);
        } else if (uuid.compareTo(UUID_CHARACTERISTIC_ACCELERATION) == 0) {
            sensor = new TBAccelerationSensor(getDevice(), service);
        } else if (uuid.compareTo(UUID_CHARACTERISTIC_ORIENTATION) == 0) {
            sensor = new TBOrientationSensor(getDevice(), service);
        }

        if (sensor != null) {
            mSensorMap.put(uuid, sensor);
        }
    }

    @Override
    protected boolean enableSensor(BleSensorAbstract<?> sensor) {
        FirebaseCrash.logcat(Log.INFO, TAG, "enableSensor() enabling ...");
        UUID uuid = sensor.getDataUUID();
        if (UUID_CHARACTERISTIC_ACCELERATION.compareTo(uuid) == 0 ||
                UUID_CHARACTERISTIC_ORIENTATION.compareTo(uuid) == 0) {
            sensor.enable(mGatt);
        }
        return true;
    }

    @Override
    protected boolean disableSensor(BleSensorAbstract<?> sensor) {
        FirebaseCrash.logcat(Log.INFO, TAG, "disableSensor() disabling ...");
        UUID uuid = sensor.getDataUUID();
        boolean res = true;
        if (UUID_CHARACTERISTIC_ACCELERATION.compareTo(uuid) == 0 ||
                UUID_CHARACTERISTIC_ORIENTATION.compareTo(uuid) == 0) {
            res = sensor.disable(mGatt);
        }
        return res;
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        FirebaseCrash.logcat(Log.VERBOSE, TAG, "onCharacteristicRead() " + characteristic.getUuid());
        UUID uuid = characteristic.getUuid();
        byte[] ba = characteristic.getValue();

        if (ba == null || ba.length == 0) {
            FirebaseCrash.logcat(Log.DEBUG, TAG, "characteristic: %s is not initialized" + characteristic.getUuid());
        } else if (((TBProperties) mProperties).isSensorEnabled(uuid)) {

            if (ThunderBoardUuids.UUID_CHARACTERISTIC_DEVICE_NAME.equals(uuid)) {
                String deviceName = characteristic.getStringValue(0);
            } else if (ThunderBoardUuids.UUID_CHARACTERISTIC_BATTERY_LEVEL.equals(uuid)) {
                int batteryLevel = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
            } else if (ThunderBoardUuids.UUID_CHARACTERISTIC_FIRMWARE_REVISION.equals(uuid)) {
                String firmwareVersion = characteristic.getStringValue(0);
            } else if (UUID_CHARACTERISTIC_TEMPERATURE.equals(uuid)) {
                int temperature = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, 0);
                Float t = (temperature / 100f);
                t = t * 1.8f + 32;
                putIotParams(new IotParameter[]{new IotParameter(TelemetriesNames.TEMPERATURE, t.toString())});
                readStatus = 0x03;
            } else if (UUID_CHARACTERISTIC_HUMIDITY.equals(uuid)) {
                Float humidity = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 0) / 100f;
                putIotParams(new IotParameter[]{new IotParameter(TelemetriesNames.HUMIDITY, humidity.toString())});
                readStatus |= 0x0c;
            } else if (UUID_CHARACTERISTIC_UV_INDEX.equals(uuid)) {
                Integer uvIndex = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);
                putIotParams(new IotParameter[]{new IotParameter(TelemetriesNames.UV, uvIndex.toString())});
                readStatus |= 0x30;
            } else if (UUID_CHARACTERISTIC_AMBIENT_LIGHT.equals(uuid)) {
                int ambientLight = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT32, 0);
                long ambientLightLong = (ambientLight < 0) ? (long) Math.abs(ambientLight) + (long) Integer.MAX_VALUE : ambientLight;
                ambientLightLong /= 100;
                Long light = ambientLightLong > MAX_AMBIENT_LIGHT ? MAX_AMBIENT_LIGHT : ambientLightLong;
                putIotParams(new IotParameter[]{new IotParameter(TelemetriesNames.LIGHT, light.toString())});
                readStatus |= 0xc0;
            } else if (ThunderBoardUuids.UUID_CHARACTERISTIC_CSC_FEATURE.equals(uuid)) {
                byte cscFeature = ba[0];
            }
        } else if (ThunderBoardUuids.UUID_CHARACTERISTIC_SYSTEM_ID.equals(uuid)) {
            Integer val = fromByteArray(ba);
            if (TextUtils.isEmpty(mUid)) {
                mUid = val.toString();
                checkAndRegisterDevice();
            }
        }
    }

    int fromByteArray(byte[] bytes) {
        return bytes[0] << 24 | (bytes[1] & 0xFF) << 16 | (bytes[2] & 0xFF) << 8 | (bytes[3] & 0xFF);
    }

    @Override
    protected void processCharacteristicChanged(BleSensorAbstract<?> sensor,
                                                BluetoothGattCharacteristic characteristic) {

    }

    @Override
    protected void processServiceCharacteristicChanged(BluetoothGattCharacteristic characteristic) {
        UUID uuid = characteristic.getUuid();
        if (UUID_CHARACTERISTIC_ACCELERATION.equals(uuid) && ((TBProperties) mProperties).isSensorEnabled(uuid)) {
            String accelerationX = Double.toString(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, 0) / 1000f);
            String accelerationY = Double.toString(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, 2) / 1000f);
            String accelerationZ = Double.toString(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, 4) / 1000f);
            putIotParams(
                    new IotParameter(TelemetriesNames.ACCELEROMETER_X, accelerationX),
                    new IotParameter(TelemetriesNames.ACCELEROMETER_Y, accelerationY),
                    new IotParameter(TelemetriesNames.ACCELEROMETER_Z, accelerationZ),
                    new IotParameter(TelemetriesNames.ACCELEROMETER_XYZ, String.format("%s|%s|%s", accelerationX, accelerationY, accelerationZ)));
        } else if (UUID_CHARACTERISTIC_ORIENTATION.equals(uuid) && ((TBProperties) mProperties).isSensorEnabled(uuid)) {
            String orientationX = Double.toString(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, 0) / 100f);
            String orientationY = Double.toString(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, 2) / 100f);
            String orientationZ = Double.toString(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_SINT16, 4) / 100f);
            putIotParams(
                    new IotParameter(TelemetriesNames.ORIENTATION_X, orientationX),
                    new IotParameter(TelemetriesNames.ORIENTATION_Y, orientationY),
                    new IotParameter(TelemetriesNames.ORIENTATION_Z, orientationZ),
                    new IotParameter(TelemetriesNames.ORIENTATION_XYZ, String.format("%s|%s|%s", orientationX, orientationY, orientationZ)));
        } else if (characteristic.equals(CHARACTERISTIC_DIGITAL_OUTPUT)) {
            FirebaseCrash.logcat(Log.VERBOSE, TAG, "onCharacteristicRead() " + characteristic.getUuid());
        }
    }

    @Override
    protected AbstractBleScannerFactory getScannerFactory() {
        return new SimpleBleScannerFactory(THUNDER_BOARD_REACT_NAME);
    }

    @Override
    public DeviceType getDeviceType() {
        return DeviceType.ThunderBoard;
    }

    @Override
    public String getDeviceUId() {
        return getDevice() == null ? null : DEVICE_UID_PREFIX + mUid;
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
        if (mDetailsFragment == null) {
            mDetailsFragment = new ThunderBoardDetailsFragment();
        }
        updateRegistrationModel();
        return mDetailsFragment;
    }

    @Override
    public void pollingTask() {
        super.pollingTask();
    }

    @Override
    protected void enable(long cardId) {
        super.enable(cardId);
        if (mState == DeviceState.Connected ||
                mState == DeviceState.Monitoring) {
            BleSensorAbstract<?> sensor = mSensorMap.get(UUID_CHARACTERISTIC_TEMPERATURE);
            if (sensor != null) {
                sensor.readSensorDataRequest(mGatt);
            }
            readTimer.cancel();
            readTimer.start();
        }
    }

    @Override
    protected void disable(long cardId) {
        isLed0On = false;
        isLed1On = false;
        readTimer.cancel();
        mUid = null;
        super.disable(cardId);
    }

    @Override
    public void updateProperties() {
        mProperties.update();
    }

    @Override
    protected DeviceRegistrationModel getRegisterPayload() {
        saveDeviceInfo();
        mProperties.update();
        DeviceRegistrationModel payload = super.getRegisterPayload();
        payload.setProperties(mProperties.getPropertiesAsJson());
        payload.setInfo(mProperties.getInfoAsJson());
        return payload;
    }

    private void saveDeviceInfo() {
        Map<DevicePropertiesAbstract.PropertyKeys, Object> map = new HashMap<>();
        map.put(TBProperties.TBInfoKeys.UID, getDeviceUId());
        map.put(TBProperties.TBInfoKeys.NAME, getDeviceType().name());
        map.put(TBProperties.TBInfoKeys.TYPE, getDeviceTypeName());
        mProperties.saveToPreferences(map);
    }

    public boolean led0Action(boolean on) {
        isLed0On = on;
        boolean result;
        int action = on ? mContext.getResources().getInteger(R.integer.led0_on) : 0;
        action |= isLed1On ? mContext.getResources().getInteger(R.integer.led1_on) : 0;
        result = CHARACTERISTIC_DIGITAL_OUTPUT != null ?
                writeCharacteristic(mGatt, CHARACTERISTIC_DIGITAL_OUTPUT, action, BluetoothGattCharacteristic.FORMAT_UINT8, 0)
                : false;
        isLed0On &= result;
        putIotParams(new IotParameter(LED_0, Boolean.toString(result)));
        return result;
    }

    public boolean led1Action(boolean on) {
        isLed1On = on;
        boolean result;
        int action = on ? mContext.getResources().getInteger(R.integer.led1_on) : 0;
        action |= isLed0On ? mContext.getResources().getInteger(R.integer.led0_on) : 0;
        result = CHARACTERISTIC_DIGITAL_OUTPUT != null ?
                writeCharacteristic(mGatt, CHARACTERISTIC_DIGITAL_OUTPUT, action, BluetoothGattCharacteristic.FORMAT_UINT8, 0)
                : false;
        isLed1On &= result;
        putIotParams(new IotParameter(LED_1, Boolean.toString(result)));
        return result;
    }

    private boolean writeCharacteristic(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic,
                                        final int value, final int format, final int offset) {
        if (gatt == null) {
            return false;
        }
        characteristic.setValue(value, format, offset);
        return gatt.writeCharacteristic(characteristic);
    }

    @Override
    public void notifyDeviceStateChanged(DeviceState state) {
        if (mState == DeviceState.Connected && TextUtils.isEmpty(mUid)) {


        } else if (!mSensorMap.isEmpty() && mState == DeviceState.Disconnected) {
            disable(mCardId);
        }
        super.notifyDeviceStateChanged(state);
    }

    @Override
    protected void handleDeviceStateRequestInternal(String payload) {
        JsonParser p = new JsonParser();
        Gson gson = new Gson();
        JsonObject result = p.parse(payload).getAsJsonObject();
        Set<Map.Entry<String, JsonElement>> entrySet = result.entrySet();
        for (Map.Entry<String, JsonElement> entry : entrySet) {
            String key = entry.getKey();
            if (key.equals(LED_0)) {
                StateModel stateModel = gson.fromJson(entry.getValue(), StateModel.class);
                boolean led0 = Boolean.parseBoolean(stateModel.getValue());
                if (isLed0On != led0) {
                    led0Action(led0);
                }
            }
            if (key.equals(LED_1)) {
                StateModel stateModel = gson.fromJson(entry.getValue(), StateModel.class);
                boolean led1 = Boolean.parseBoolean(stateModel.getValue());
                if (isLed1On != led1) {
                    led1Action(led1);
                }
            }
        }
    }
}

