package com.arrow.jmyiotgateway.device.ble.abstracts;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.arrow.jmyiotgateway.Constant;
import com.arrow.jmyiotgateway.device.DeviceAbstract;
import com.arrow.jmyiotgateway.device.DeviceState;
import com.arrow.jmyiotgateway.device.ble.Advertisement;
import com.arrow.jmyiotgateway.device.ble.BleConstant;
import com.arrow.jmyiotgateway.device.ble.BleScanCallback;
import com.arrow.jmyiotgateway.device.ble.BleUtil;
import com.google.firebase.crash.FirebaseCrash;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Semaphore;

public abstract class BleDeviceAbstract extends DeviceAbstract implements BleScanCallback {
    private final static String TAG = BleDeviceAbstract.class.getSimpleName();

    protected BluetoothDevice mDevice;
    protected BluetoothGatt mGatt;

    private final Semaphore writeCharacteristicSemaphore = new Semaphore(0, true);
    private final Semaphore readCharacteristicSemaphore = new Semaphore(0, true);
    private final Semaphore writeDescriptorSemaphore = new Semaphore(0, true);
    private final Semaphore readDescriptorSemaphore = new Semaphore(0, true);
    private final Semaphore serviceDiscoveredSemaphore = new Semaphore(0, true);

    protected DeviceState mState;

    protected AbstractBleDeviceScanner mDeviceScanner;
    protected final Map<UUID, BleSensorAbstract<?>> mSensorMap = new HashMap<>();
    private String mName;

    public BleDeviceAbstract(Context context, long cardId, boolean isLocationNeeded, String deviceHid) {
        super(context, cardId, isLocationNeeded, deviceHid);
        mDeviceScanner = getScannerFactory().getBleScanner();
        mState = DeviceState.Disconnected;
    }

    @Override
    protected void enable(long cardId) {
        FirebaseCrash.logcat(Log.INFO, TAG, "enable() ...");
        try {
            if (mDevice == null) {
                for (BluetoothDevice bonded : BleUtil.getBluetoothAdapter(getContext()).getBondedDevices()) {
                    String bondedName = bonded.getName();
                    if (!TextUtils.isEmpty(mName) && (mName.equals(bondedName) ||
                            bondedName.contains(mName))) {
                        FirebaseCrash.logcat(Log.INFO, TAG, "enableTask() found bonded device: " + bonded.getAddress());
                        mDevice = bonded;
                        break;
                    }
                }
                if (mDevice == null) {
                    mDevice = mDeviceScanner.scanDevice(getContext(), this);
                }
            }
            if (mDevice != null) {
                checkAndRegisterDevice();
                mState = DeviceState.Connecting;
                notifyDeviceStateChanged(mState);
                FirebaseCrash.logcat(Log.INFO, TAG, "enableTask() found device: " + mDevice.getAddress());

                FirebaseCrash.logcat(Log.INFO, TAG, "enableTask() connecting to Gatt ...");
                mGatt = mDevice.connectGatt(getContext(), false, new GattCallback());
                serviceDiscoveredSemaphore.acquire();
                mState = DeviceState.Connected;
                notifyDeviceStateChanged(mState);

                for (BleSensorAbstract<?> sensor : mSensorMap.values()) {
                    try {
                        FirebaseCrash.logcat(Log.INFO, TAG, "enableTask() setting notification: " + sensor.getDisplayName());
                        setSensorNotification(sensor);

                        FirebaseCrash.logcat(Log.INFO, TAG, "enableTask() enabling service: " + sensor.getDisplayName());
                        if (enableSensor(sensor)) {
                            mState = DeviceState.Monitoring;
                            notifyDeviceStateChanged(mState);
                        } else {
                            mState = DeviceState.Error;
                            notifyDeviceStateChanged(mState);
                        }

                    } catch (Exception e) {
                        FirebaseCrash.logcat(Log.ERROR,  TAG, "enableTask() error");
                        e.printStackTrace();
                        FirebaseCrash.report(e);
                        mState = DeviceState.Error;
                        notifyDeviceStateChanged(mState);
                    }
                }
            } else {
                FirebaseCrash.logcat(Log.INFO, TAG, "enableTask() device not found");
                mState = DeviceState.Error;
                notifyDeviceStateChanged(mState);
            }
        } catch (Exception e) {
            FirebaseCrash.logcat(Log.INFO, TAG, "enableTask() error");
            e.printStackTrace();
            FirebaseCrash.report(e);
            mState = DeviceState.Error;
            notifyDeviceStateChanged(mState);

            // need to close gatt to return resources!
            if (mGatt != null) {
                mGatt.close();
            }
        }
    }

    @Override
    protected void disable(long cardId) {
        FirebaseCrash.logcat(Log.INFO, TAG, "disable() ...");

        if (mDevice != null && mGatt != null) {
            if (mState != DeviceState.Reconnecting) {
                mState = DeviceState.Disconnecting;
                notifyDeviceStateChanged();
            }
            for (BleSensorAbstract<?> sensor : mSensorMap.values()) {
                try {
                    FirebaseCrash.logcat(Log.INFO, TAG, "disable() disabling sensor: " + sensor.getDisplayName());
                    disableSensor(sensor);
                } catch (Exception e) {
                    FirebaseCrash.logcat(Log.ERROR,  TAG, "disable() error");
                    FirebaseCrash.report(e);
                }
            }
            mSensorMap.clear();
            // need to close gatt to return resources!
            FirebaseCrash.logcat(Log.INFO, TAG, "closing gatt ...");
            mGatt.close();
            mState = DeviceState.Disconnected;
            notifyDeviceStateChanged();
            mDevice = null;
        }
        FirebaseCrash.logcat(Log.INFO, TAG, "disable() completed");
    }

    @Override
    public void scanComplete(List<Advertisement> advertisements) {
        for (Advertisement advertisement : advertisements) {
            String advName = advertisement.getDevice().getName();
            if (!TextUtils.isEmpty(advName) && (advName.equals(mName) ||
                                                advName.contains(mName))) {
                mDevice = advertisement.getDevice();
            }
        }
    }

    public BluetoothDevice getDevice() {
        return mDevice;
    }

    public Map<UUID, BleSensorAbstract<?>> getSensorMap() {
        return mSensorMap;
    }

    protected abstract BleSensorAbstract<?> createSensor(BluetoothGattService service);

    protected abstract void processCharacteristicChanged(BleSensorAbstract<?> sensor, BluetoothGattCharacteristic characteristic);

    protected void processServiceCharacteristicChanged(BluetoothGattCharacteristic characteristic){}

    protected abstract AbstractBleScannerFactory getScannerFactory();

    protected void setSensorNotification(BleSensorAbstract<?> sensor) throws Exception {
        setSensorNotification(sensor, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
    }

    protected void resetSensorNotification(BleSensorAbstract<?> sensor) throws Exception {
        setSensorNotification(sensor, BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
    }

    private void setSensorNotification(BleSensorAbstract<?> sensor, byte[] flag) throws Exception {
        BluetoothGattCharacteristic dataCharacteristic = sensor.getDataCharacteristic();
        mGatt.setCharacteristicNotification(dataCharacteristic, true);
        BluetoothGattDescriptor descriptor = dataCharacteristic.getDescriptor(
                BleConstant.BleUUID.CLIENT_CHARACTERISTIC_CONFIG);
        if (descriptor != null) {
            descriptor.setValue(flag);
            mGatt.writeDescriptor(descriptor);
            writeDescriptorSemaphore.acquire();
        }
    }

    protected boolean enableSensor(BleSensorAbstract<?> sensor) throws Exception {
        FirebaseCrash.logcat(Log.INFO, TAG, "enableSensor() enabling ...");
        boolean result = true;
        if (sensor.getConfigCharacteristic() != null) {
            result = sensor.enable(mGatt);
            if (result)
                writeCharacteristicSemaphore.acquire();
        }

        FirebaseCrash.logcat(Log.INFO, TAG, "enableSensor() updating period ...");
        if (sensor.getPeriodCharacteristic() != null) {
            result &= sensor.updatePeriod(mGatt);
            if (result)
                writeCharacteristicSemaphore.acquire();
        }
        return result;
    }

    protected boolean disableSensor(BleSensorAbstract<?> sensor) throws Exception {
        boolean result = sensor.disable(mGatt);
        writeCharacteristicSemaphore.acquire();
        return result;
    }

    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {}

    private class GattCallback extends BluetoothGattCallback {
        private final String TAG = GattCallback.class.getSimpleName();

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            FirebaseCrash.logcat(Log.INFO, TAG, "onConnectionStateChange() status: " + status + ", newState: " + newState);
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED: {
                    if (gatt != null) {
                        FirebaseCrash.logcat(Log.INFO, TAG, "onConnectionStateChange() discovering services ...");
                        gatt.discoverServices();
                    }
                    break;
                }
                case BluetoothProfile.STATE_DISCONNECTED: {
                    FirebaseCrash.logcat(Log.INFO, TAG, "onConnectionStateChange() BLE disconnected!");
                    if (mState == DeviceState.Monitoring) {
                        FirebaseCrash.logcat(Log.VERBOSE, TAG, "onConnectionStateChange() needs to reconnect");
                        mState = DeviceState.Reconnecting;
                    } else {
                        mState = DeviceState.Disconnected;
                    }
                    break;
                }
                case BluetoothProfile.STATE_CONNECTING:
                    mState = DeviceState.Connecting;
                    break;
                case BluetoothProfile.STATE_DISCONNECTING:
                    mState = DeviceState.Disconnecting;
                    break;
            }
            notifyDeviceStateChanged(mState);
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            FirebaseCrash.logcat(Log.INFO, TAG, "onServicesDiscovered() status: " + status);
            try {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    for (BluetoothGattService service : gatt.getServices()) {
                        UUID uuid = service.getUuid();
                        BleSensorAbstract<?> sensor = createSensor(service);
                        if (sensor != null) {
                            FirebaseCrash.logcat(Log.INFO, TAG, "onServicesDiscovered() adding sensor: " + sensor.getDisplayName());
                            mSensorMap.put(uuid, sensor);
                        } else {
                            FirebaseCrash.logcat(Log.INFO, TAG, "onServicesDiscovered() ignored service: " + uuid);
                        }
                    }
                }
            } catch (Exception e) {
                FirebaseCrash.logcat(Log.ERROR,  TAG, "onServicesDiscovered() error");
                FirebaseCrash.report(e);
            }
            serviceDiscoveredSemaphore.release();
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            try {
                UUID uuid = characteristic.getService().getUuid();
                FirebaseCrash.logcat(Log.VERBOSE, TAG, "onCharacteristicChanged() service: " + uuid);
                BleSensorAbstract<?> sensor = mSensorMap.get(uuid);
                if (sensor != null) {
                    processCharacteristicChanged(sensor, characteristic);
                } else {
                    processServiceCharacteristicChanged(characteristic);
                }
            } catch (Exception e) {
                FirebaseCrash.logcat(Log.ERROR,  TAG, "onCharacteristicChanged() error");
                FirebaseCrash.report(e);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            BleDeviceAbstract.this.onCharacteristicRead(gatt, characteristic, status);
            readCharacteristicSemaphore.release();
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            FirebaseCrash.logcat(Log.VERBOSE, TAG, "onCharacteristicWrite() " + characteristic.getUuid());
            writeCharacteristicSemaphore.release();
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            FirebaseCrash.logcat(Log.VERBOSE, TAG, "onDescriptorRead() " + descriptor.getUuid());
            readDescriptorSemaphore.release();
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            FirebaseCrash.logcat(Log.VERBOSE, TAG, "onDescriptorWrite() " + descriptor.getUuid());
            writeDescriptorSemaphore.release();
        }
    }

    public void setName(String name) {
        mName = name;
    }
}
