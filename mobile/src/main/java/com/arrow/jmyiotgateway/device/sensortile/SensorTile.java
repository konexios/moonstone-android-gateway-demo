package com.arrow.jmyiotgateway.device.sensortile;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.util.Log;

import com.arrow.jmyiotgateway.device.DevicePropertiesAbstract;
import com.arrow.jmyiotgateway.device.DeviceState;
import com.arrow.jmyiotgateway.device.DeviceType;
import com.arrow.jmyiotgateway.device.ble.SimpleBleScannerFactory;
import com.arrow.jmyiotgateway.device.ble.abstracts.AbstractBleScannerFactory;
import com.arrow.jmyiotgateway.device.ble.abstracts.BleDeviceAbstract;
import com.arrow.jmyiotgateway.device.ble.abstracts.BleSensorAbstract;
import com.arrow.jmyiotgateway.device.sensortile.data.SensorData;
import com.arrow.jmyiotgateway.fragments.AbstractDetailsFragment;
import com.arrow.jmyiotgateway.fragments.SensorTileDetailsFragment;
import com.arrow.acn.api.models.DeviceRegistrationModel;
import com.google.firebase.crash.FirebaseCrash;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by osminin on 9/6/2016.
 */

public final class SensorTile extends BleDeviceAbstract {
    private final static String TAG = SensorTile.class.getSimpleName();
    private final static String DEVICE_UID_PREFIX = "stile-android-";
    private final static String DEVICE_TYPE_NAME = "st-sensortile";
    private final static String DEVICE_NAME = "BM2V2";
    private final static UUID ST_SERVICE_UUID = UUID.fromString("00000000-0001-11E1-9AB4-0002A5D5C51B");

    public SensorTile(Context context, long cardId, boolean isLocationNeeded, String deviceHid) {
        super(context, cardId, isLocationNeeded, deviceHid);
        mProperties = new SensorTileProperties(context);
        setName(DEVICE_NAME);
    }

    @Override
    protected BleSensorAbstract<?> createSensor(BluetoothGattService service) {
        UUID serviceUuid = service.getUuid();
        if (ST_SERVICE_UUID.equals(serviceUuid)) {
            for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                addSensor(characteristic.getUuid(), service);
            }
        }
        return null;
    }

    @Override
    protected void processCharacteristicChanged(BleSensorAbstract<?> sensor, BluetoothGattCharacteristic characteristic) {
        try {
            STSensorAbstract<?> stSensor = (STSensorAbstract<?>) sensor;
            SensorData<?> data = stSensor.read(characteristic);
            if (data != null) {
                FirebaseCrash.logcat(Log.INFO, TAG, "processCharacteristicChanged() putIotParams() ...");
                putIotParams(data.toIotParameters());
            } else {
                // ignore non-data characteristic
            }
        } catch (Exception e) {
            FirebaseCrash.logcat(Log.ERROR, TAG, "processCharacteristicChanged() error");
            FirebaseCrash.report(e);
        }
    }

    @Override
    protected AbstractBleScannerFactory getScannerFactory() {
        return new SimpleBleScannerFactory(DEVICE_NAME);
    }

    @Override
    public DeviceType getDeviceType() {
        return DeviceType.SensorTile;
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
        if (mDetailsFragment == null) {
            mDetailsFragment = new SensorTileDetailsFragment();
        }
        updateRegistrationModel();
        return mDetailsFragment;
    }

    @Override
    protected boolean enableSensor(BleSensorAbstract<?> sensor) {
        FirebaseCrash.logcat(Log.INFO, TAG, "enableSensor() enabling ...");
        return true;
    }

    @Override
    protected boolean disableSensor(BleSensorAbstract<?> sensor) {
        FirebaseCrash.logcat(Log.INFO, TAG, "disableSensor() disabling ...");
        return true;
    }

    @Override
    protected void processServiceCharacteristicChanged(BluetoothGattCharacteristic characteristic) {
        UUID uuid = characteristic.getUuid();
        BleSensorAbstract<?> sensor = mSensorMap.get(uuid);
        if (sensor != null) {
            processCharacteristicChanged(sensor, characteristic);
        }
    }

    private void addSensor(UUID uuid, BluetoothGattService service) {
        BleSensorAbstract<?> sensor = null;
        if (((SensorTileProperties) mProperties).isSensorEnabled(uuid)) {
            if (STileMovementSensor.SENSOR_UUID.equals(uuid)) {
                sensor = new STileMovementSensor(getDevice(), service);
            } else if (STileEnvironmentSensor.SENSOR_UUID.equals(uuid)) {
                sensor = new STileEnvironmentSensor(getDevice(), service);
            } else if (STileMicLevelSensor.SENSOR_UUID.equals(uuid)) {
                sensor = new STileMicLevelSensor(getDevice(), service);
            } else if (STileSwitchSensor.SENSOR_UUID.equals(uuid)) {
                sensor = new STileSwitchSensor(getDevice(), service);
            }
        }
        if (sensor != null) {
            mSensorMap.put(uuid, sensor);
        }
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

    @Override
    public void updateProperties() {
        mProperties.update();
        if (mGatt != null && (mState == DeviceState.Connected ||
                mState == DeviceState.Monitoring)) {
            BluetoothGattService service = mGatt.getService(ST_SERVICE_UUID);
            for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                UUID uuid = characteristic.getUuid();
                BleSensorAbstract<?> sensor = mSensorMap.get(uuid);
                if (sensor != null && !((SensorTileProperties) mProperties).isSensorEnabled(uuid)) {
                    try {
                        resetSensorNotification(sensor);
                    } catch (Exception e) {
                        e.printStackTrace();
                        FirebaseCrash.report(e);
                    }
                    mSensorMap.remove(uuid);
                } else if (sensor == null && ((SensorTileProperties) mProperties).isSensorEnabled(uuid)) {
                    addSensor(uuid, service);
                    try {
                        setSensorNotification(mSensorMap.get(uuid));
                    } catch (Exception e) {
                        e.printStackTrace();
                        FirebaseCrash.report(e);
                    }
                }
            }
        }
    }

    private void saveDeviceInfo() {
        Map<DevicePropertiesAbstract.PropertyKeys, Object> map = new HashMap<>();
        map.put(SensorTileProperties.SensorTileInfoKeys.UID, getDeviceUId());
        map.put(SensorTileProperties.SensorTileInfoKeys.NAME, getDeviceType().name());
        map.put(SensorTileProperties.SensorTileInfoKeys.TYPE, getDeviceTypeName());
        mProperties.saveToPreferences(map);
    }
}
