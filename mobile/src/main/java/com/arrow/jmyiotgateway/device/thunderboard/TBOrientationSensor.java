package com.arrow.jmyiotgateway.device.thunderboard;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;

import com.arrow.jmyiotgateway.device.ble.abstracts.BleSensorAbstract;
import com.arrow.jmyiotgateway.device.thunderboard.data.TBVectorData;

import java.util.UUID;

/**
 * Created by osminin on 8/1/2016.
 */

public final class TBOrientationSensor extends BleSensorAbstract<TBVectorData> {
    public TBOrientationSensor(BluetoothDevice device, BluetoothGattService service) {
        super(device, service);
        initCharacteristics();
    }

    @Override
    public UUID getDataUUID() {
        return ThunderBoardUuids.UUID_CHARACTERISTIC_ORIENTATION;
    }

    @Override
    public UUID getConfigUUID() {
        return UUID.randomUUID();
    }

    @Override
    public UUID getPeriodUUID() {
        return UUID.randomUUID();
    }

    @Override
    public String getDisplayName() {
        return null;
    }

    @Override
    public boolean enable(BluetoothGatt gatt) {
        gatt.setCharacteristicNotification(getDataCharacteristic(), true);
        BluetoothGattDescriptor descriptor = getDataCharacteristic().
                getDescriptor(ThunderBoardUuids.UUID_DESCRIPTOR_CLIENT_CHARACTERISTIC_CONFIGURATION);
        return descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
    }

    @Override
    public boolean disable(BluetoothGatt gatt) {
        BluetoothGattDescriptor descriptor = getDataCharacteristic().
                getDescriptor(ThunderBoardUuids.UUID_DESCRIPTOR_CLIENT_CHARACTERISTIC_CONFIGURATION);
        return descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
    }

    @Override
    protected TBVectorData parse(byte[] value) {
        return null;
    }
}
