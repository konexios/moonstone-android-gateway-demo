package com.arrow.jmyiotgateway.device.senseability;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.util.Log;

import com.arrow.jmyiotgateway.Constant;
import com.arrow.jmyiotgateway.device.DeviceState;
import com.arrow.jmyiotgateway.device.DeviceType;
import com.arrow.jmyiotgateway.device.ble.SimpleBleScannerFactory;
import com.arrow.jmyiotgateway.device.ble.abstracts.AbstractBleScannerFactory;
import com.arrow.jmyiotgateway.device.ble.abstracts.BleDeviceAbstract;
import com.arrow.jmyiotgateway.device.ble.abstracts.BleSensorAbstract;
import com.arrow.jmyiotgateway.device.sensortile.data.SensorData;
import com.arrow.jmyiotgateway.fragments.AbstractDetailsFragment;
import com.arrow.jmyiotgateway.fragments.SenseAbilityDetailsFragment;
import com.arrow.acn.api.models.DeviceRegistrationModel;
import com.google.firebase.crash.FirebaseCrash;

import java.util.UUID;

/**
 * Created by osminin on 22.07.2016.
 */

public class SenseAbilityKit extends BleDeviceAbstract {
    private final static String TAG = SenseAbilityKit.class.getSimpleName();
    private final static String DEVICE_UID_PREFIX = "sa2-";
    private final static String DEVICE_TYPE_NAME = "senseability-2";

    public SenseAbilityKit(Context context, long cardId, boolean isLocationNeeded, String deviceHid) {
        super(context, cardId, isLocationNeeded, deviceHid);
        setName(Constant.SENSEABILITY_NAME);
    }

    @Override
    protected BleSensorAbstract<?> createSensor(BluetoothGattService service) {
        FirebaseCrash.logcat(Log.DEBUG, TAG, "processCharacteristicChanged");
        BleSensorAbstract<?> sensor = null;
        UUID uuid = service.getUuid();
        if (uuid.toString().equals(SenseAbilitySensor.SERVICE_UUID)) {
            sensor = new SenseAbilitySensor(mDevice, service);
        }
        return sensor;
    }

    @Override
    protected void processCharacteristicChanged(BleSensorAbstract<?> sensor, BluetoothGattCharacteristic characteristic) {
        FirebaseCrash.logcat(Log.DEBUG, TAG, "processCharacteristicChanged");
        try {
            SenseAbilitySensor sSensor = (SenseAbilitySensor) sensor;
            SensorData<?> data = sSensor.read(characteristic);
            if (data != null) {
                FirebaseCrash.logcat(Log.INFO, TAG, "processCharacteristicChanged() putIotParams() ...");
                putIotParams(data.toIotParameters());
            } else {
                // ignore non-data characteristic
            }
        } catch (Exception e) {
            FirebaseCrash.logcat(Log.ERROR,  TAG, "processCharacteristicChanged() error");
            FirebaseCrash.report(e);
        }
    }

    @Override
    protected AbstractBleScannerFactory getScannerFactory() {
        return new SimpleBleScannerFactory(Constant.SENSEABILITY_NAME);
    }

    @Override
    public DeviceType getDeviceType() {
        return DeviceType.SenseAbilityKit;
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
            mDetailsFragment = new SenseAbilityDetailsFragment();
        }
        updateRegistrationModel();
        return mDetailsFragment;
    }

    @Override
    protected DeviceRegistrationModel getRegisterPayload() {
        return super.getRegisterPayload();
        //TODO:
    }
}
