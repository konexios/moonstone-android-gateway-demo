package com.arrow.jmyiotgateway.device.sensortile;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;

import com.arrow.jmyiotgateway.device.ble.BleUtil;
import com.arrow.jmyiotgateway.device.sensortile.data.Movement;
import com.arrow.jmyiotgateway.device.sensortile.data.MovementData;
import com.arrow.jmyiotgateway.device.sensortile.data.Vector;

import java.util.UUID;

public class STileMovementSensor extends STSensorAbstract<MovementData> {
    static final UUID SENSOR_UUID = UUID.fromString("00E00000-0001-11E1-AC36-0002A5D5C51B");

    public STileMovementSensor(BluetoothDevice device, BluetoothGattService service) {
        super(device, service);
    }

    @Override
    public UUID getDataUUID() {
        return UUID.fromString("00E00000-0001-11E1-AC36-0002A5D5C51B");
    }

    @Override
    public UUID getConfigUUID() {
        return UUID.fromString("00E00000-0001-11E1-AC36-0002A5D5C51B");
    }

    @Override
    public UUID getPeriodUUID() {
        return UUID.fromString("00E00000-0001-11E1-AC36-0002A5D5C51B");
    }

    @Override
    public String getDisplayName() {
        return "Movement Sensor";
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
