package com.arrow.jmyiotgateway.device.simbapro;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;

import com.arrow.jmyiotgateway.device.ble.BleUtil;
import com.arrow.jmyiotgateway.device.ble.abstracts.BleSensorAbstract;
import com.arrow.jmyiotgateway.device.simbapro.data.Movement;
import com.arrow.jmyiotgateway.device.simbapro.data.MovementData;
import com.arrow.jmyiotgateway.device.sensortile.data.Vector;

import java.util.UUID;

/**
 * Created by osminin on 16.01.2018.
 */

public final class SimbaProMovementSensor extends BleSensorAbstract<MovementData> {
    static final UUID SENSOR_UUID = UUID.fromString("00e00000-0001-11e1-ac36-0002a5d5c51b");

    public SimbaProMovementSensor(BluetoothDevice device, BluetoothGattService service) {
        super(device, service);
        initCharacteristics();
    }

    @Override
    public UUID getDataUUID() {
        return SENSOR_UUID;
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
    protected MovementData parse(byte[] value) {
        return new MovementData(new Movement(readAcc(value), readGyro(value), readMag(value)));
    }

    private Vector readAcc(byte[] value) {
        int x = BleUtil.bytesToInt16(value, 2);
        int y = BleUtil.bytesToInt16(value, 4);
        int z = BleUtil.bytesToInt16(value, 6);
        return new Vector(x, y, z);
    }

    private Vector readGyro(byte[] value) {
        int x = BleUtil.bytesToInt16(value, 8);
        int y = BleUtil.bytesToInt16(value, 10);
        int z = BleUtil.bytesToInt16(value, 12);
        return new Vector(x, y, z);
    }

    private Vector readMag(byte[] value) {
        int x = BleUtil.bytesToInt16(value, 14);
        int y = BleUtil.bytesToInt16(value, 16);
        int z = BleUtil.bytesToInt16(value, 18);
        return new Vector(x, y, z);
    }
}
