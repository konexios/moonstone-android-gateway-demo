package com.arrow.jmyiotgateway.device.ble.abstracts;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.util.Log;

import com.arrow.jmyiotgateway.device.ble.BleConstant;
import com.arrow.jmyiotgateway.device.sensortile.data.SensorData;
import com.google.firebase.crash.FirebaseCrash;

import java.util.UUID;

public abstract class BleSensorAbstract<T extends SensorData<?>> {
    private final static String TAG = BleSensorAbstract.class.getSimpleName();


    private final BluetoothDevice device;
    private final BluetoothGattService service;

    private BluetoothGattCharacteristic dataCharacteristic;
    private BluetoothGattCharacteristic configCharacteristic;
    private BluetoothGattCharacteristic periodCharacteristic;

    private long period = BleConstant.DEFAULT_PERIOD_MILLIS;

    public BleSensorAbstract(BluetoothDevice device, BluetoothGattService service) {
        this.device = device;
        this.service = service;
    }

    protected void initCharacteristics() {
        for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
            if (characteristic.getUuid().toString().equals(getDataUUID().toString())) {
                dataCharacteristic = characteristic;
            }
            if (characteristic.getUuid().toString().equals(getConfigUUID().toString())) {
                configCharacteristic = characteristic;
            }
            if (characteristic.getUuid().toString().equals(getPeriodUUID().toString())) {
                periodCharacteristic = characteristic;
            }
        }
    }

    public T read(BluetoothGattCharacteristic characteristic) {
        if (characteristic.getUuid().compareTo(dataCharacteristic.getUuid()) == 0) {
            return parse(characteristic.getValue());
        } else {
            FirebaseCrash.logcat(Log.INFO, TAG, "read() ignored wrong characteristic");
            return null;
        }
    }

    public boolean enable(BluetoothGatt gatt) {
        configCharacteristic.setValue(new byte[]{0x01});
        return gatt.writeCharacteristic(configCharacteristic);
    }

    public boolean disable(BluetoothGatt gatt) {
        configCharacteristic.setValue(new byte[]{0x00});
        return gatt.writeCharacteristic(configCharacteristic);
    }

    public boolean updatePeriod(BluetoothGatt gatt) {
        byte p = (byte) ((period / 10) + 10);
        periodCharacteristic.setValue(new byte[]{p});
        return gatt.writeCharacteristic(periodCharacteristic);
    }

    public abstract UUID getDataUUID();

    public abstract UUID getConfigUUID();

    public abstract UUID getPeriodUUID();

    public abstract String getDisplayName();

    public BluetoothGattCharacteristic getDataCharacteristic() {
        return dataCharacteristic;
    }

    public BluetoothGattCharacteristic getConfigCharacteristic() {
        return configCharacteristic;
    }

    public BluetoothGattCharacteristic getPeriodCharacteristic() {
        return periodCharacteristic;
    }

    public long getPeriod() {
        return period;
    }

    public void setPeriod(long period) {
        this.period = period;
    }

    protected abstract T parse(byte[] value);

    public void readSensorDataRequest(BluetoothGatt gatt) {
        boolean res = gatt.readCharacteristic(dataCharacteristic);
        FirebaseCrash.logcat(Log.INFO, TAG, "readSensorDataRequest " + res);
    }
}
