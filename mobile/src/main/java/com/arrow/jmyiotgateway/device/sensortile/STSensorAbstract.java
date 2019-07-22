package com.arrow.jmyiotgateway.device.sensortile;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;

import com.arrow.jmyiotgateway.device.ble.abstracts.BleSensorAbstract;
import com.arrow.jmyiotgateway.device.sensortile.data.SensorData;

public abstract class STSensorAbstract<T extends SensorData<?>> extends BleSensorAbstract<T> {
    public STSensorAbstract(BluetoothDevice device, BluetoothGattService service) {
        super(device, service);
        initCharacteristics();
    }
}
