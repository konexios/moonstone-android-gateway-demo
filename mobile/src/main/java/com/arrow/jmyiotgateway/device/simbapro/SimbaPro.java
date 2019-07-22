package com.arrow.jmyiotgateway.device.simbapro;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;

import com.arrow.acn.api.models.DeviceRegistrationModel;
import com.arrow.jmyiotgateway.device.DevicePropertiesAbstract;
import com.arrow.jmyiotgateway.device.DeviceState;
import com.arrow.jmyiotgateway.device.DeviceType;
import com.arrow.jmyiotgateway.device.ble.SimpleBleScannerFactory;
import com.arrow.jmyiotgateway.device.ble.abstracts.AbstractBleScannerFactory;
import com.arrow.jmyiotgateway.device.ble.abstracts.BleDeviceAbstract;
import com.arrow.jmyiotgateway.device.ble.abstracts.BleSensorAbstract;
import com.arrow.jmyiotgateway.device.sensortile.data.SensorData;
import com.arrow.jmyiotgateway.fragments.AbstractDetailsFragment;
import com.arrow.jmyiotgateway.fragments.SimbaProDetailsFragment;
import com.google.firebase.crash.FirebaseCrash;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import rx.Single;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.arrow.jmyiotgateway.cloud.iot.IotConstant.EXTRA_DATA_LABEL_CARD_ID;

/**
 * Created by osminin on 11.01.2018.
 */

public final class SimbaPro extends BleDeviceAbstract {
    private static final String TAG = SimbaPro.class.getName();
    private final static String DEVICE_TYPE_NAME = "SIMBA-PRO";
    private static final String SIMBA_PRO = "SensBLE";
    public static final String SIMBA_PRO_MAC_EXTRA = "simba_pro_mac_extra";
    private final static String DEVICE_UID_PREFIX = "simba-pro-android-";

    private final static String COMMON_UUID_SERVICES = "-11e1-9ab4-0002a5d5c51b";
    private final static String SERVICE_UUID_FORMAT = "00000000-[0-9a-fA-F]{4}-11e1-9ab4-0002a5d5c51b";
    public final static UUID DEBUG_TERM_UUID = UUID.fromString("00000001-000e-11e1-ac36-0002a5d5c51b");

    private BluetoothGattCharacteristic mDebugCharacteristic;
    private Subscription mUpdateFirmwareTask;
    private SimbaOtaListener mSimbaOtaListener;

    public SimbaPro(Context context, long cardId, boolean isLocationNeeded, String deviceHid) {
        super(context, cardId, isLocationNeeded, deviceHid);
        mProperties = new SimbaProProperties(context);
        setName(SIMBA_PRO);
    }

    @Override
    protected BleSensorAbstract<?> createSensor(BluetoothGattService service) {
        UUID serviceUuid = service.getUuid();

        if (isKnowService(serviceUuid) || serviceUuid.toString().endsWith(COMMON_UUID_SERVICES)) {
            for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                addSensor(characteristic.getUuid(), service);
            }
        }
        return null;
    }

    @Override
    protected void processCharacteristicChanged(BleSensorAbstract<?> sensor, BluetoothGattCharacteristic characteristic) {
        try {
            SensorData<?> data = sensor.read(characteristic);
            if (data != null) {
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
    protected void processServiceCharacteristicChanged(BluetoothGattCharacteristic characteristic) {
        UUID uuid = characteristic.getUuid();
        BleSensorAbstract<?> sensor = mSensorMap.get(uuid);
        if (sensor != null) {
            processCharacteristicChanged(sensor, characteristic);
        }
    }

    @Override
    protected AbstractBleScannerFactory getScannerFactory() {
        return new SimpleBleScannerFactory(SIMBA_PRO, SimbaProUtils.macAddressToString(mCardId));
    }

    @Override
    public DeviceType getDeviceType() {
        return DeviceType.SimbaPro;
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
            mDetailsFragment = new SimbaProDetailsFragment();
            Bundle bundle = new Bundle();
            bundle.putLong(EXTRA_DATA_LABEL_CARD_ID, mCardId);
            mDetailsFragment.setArguments(bundle);
        }
        updateRegistrationModel();
        return mDetailsFragment;
    }

    private void addSensor(UUID uuid, BluetoothGattService service) {
        BleSensorAbstract<?> sensor = null;
        if (((SimbaProProperties) mProperties).isSensorEnabled(uuid)) {
            if (uuid.compareTo(SimbaProLightSensor.SENSOR_UUID) == 0) {
                sensor = new SimbaProLightSensor(getDevice(), service);
            } else if (uuid.compareTo(SimbaProEnvironmentSensor.SENSOR_UUID) == 0) {
                sensor = new SimbaProEnvironmentSensor(getDevice(), service);
            } else if (uuid.compareTo(SimbaProMicSensor.SENSOR_UUID) == 0) {
                sensor = new SimbaProMicSensor(getDevice(), service);
            } else if (uuid.compareTo(SimbaProMovementSensor.SENSOR_UUID) == 0) {
                sensor = new SimbaProMovementSensor(getDevice(), service);
            }
            if (sensor != null) {
                mSensorMap.put(uuid, sensor);
            }
        } else if (DEBUG_TERM_UUID.compareTo(uuid) == 0) {
            mDebugCharacteristic = service.getCharacteristic(uuid);
        }
    }

    private static boolean isKnowService(UUID uuid) {
        String uuidString = uuid.toString();
        return uuidString.matches(SERVICE_UUID_FORMAT);
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
            for (BluetoothGattService service : mGatt.getServices()) {
                if (isKnowService(service.getUuid()) || service.getUuid().toString().endsWith(COMMON_UUID_SERVICES)) {
                    for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                        UUID uuid = characteristic.getUuid();
                        BleSensorAbstract<?> sensor = mSensorMap.get(uuid);
                        if (sensor != null && !((SimbaProProperties) mProperties).isSensorEnabled(uuid)) {
                            try {
                                resetSensorNotification(sensor);
                            } catch (Exception e) {
                                e.printStackTrace();
                                FirebaseCrash.report(e);
                            }
                            mSensorMap.remove(uuid);
                        } else if (sensor == null && ((SimbaProProperties) mProperties).isSensorEnabled(uuid)) {
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
        }
    }

    private void saveDeviceInfo() {
        Map<DevicePropertiesAbstract.PropertyKeys, Object> map = new HashMap<>();
        map.put(SimbaProProperties.SimbaProInfoKeys.UID, getDeviceUId());
        map.put(SimbaProProperties.SimbaProInfoKeys.NAME, getDeviceType().name());
        map.put(SimbaProProperties.SimbaProInfoKeys.TYPE, getDeviceTypeName());
        mProperties.saveToPreferences(map);
    }

    public void setFirmwareUpgradeListener(SimbaOtaListener listener) {
        mSimbaOtaListener = listener;
    }

    public void upgradeFirmware(String firmwareName) {
        if (mUpdateFirmwareTask != null && !mUpdateFirmwareTask.isUnsubscribed()) {
            return;
        }
        mSimbaOtaListener.onOTAProcessStarted();
        mUpdateFirmwareTask = Single.fromCallable(() -> {
            AssetManager assetManager = mContext.getAssets();
            //BufferedInputStream reader = null;
            InputStream inputStream = null;
            try (BufferedInputStream reader = new BufferedInputStream(assetManager.open(firmwareName))) {
                int len;
                int fullSize = 0;
                byte[] buff = new byte[1024];
                while ((len = reader.read(buff)) > 0) {
                    fullSize += len;
                }
                byte[] res = new byte[fullSize];
                reader.read(res, 0, fullSize);
                SimbaProUtils.write(mGatt, mDebugCharacteristic, res, 0, fullSize);
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    mSimbaOtaListener.onOTACompleted();
                }, throwable -> {
                    mSimbaOtaListener.onOTAError();
                });
    }
}
