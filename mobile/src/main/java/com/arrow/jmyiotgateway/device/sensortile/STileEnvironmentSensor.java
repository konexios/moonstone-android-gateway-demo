package com.arrow.jmyiotgateway.device.sensortile;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;

import com.arrow.jmyiotgateway.device.ble.BleUtil;
import com.arrow.jmyiotgateway.device.sensortile.data.EnvironmentData;

import java.util.UUID;

/**
 * Created by osminin on 9/8/2016.
 */

public final class STileEnvironmentSensor extends STSensorAbstract<EnvironmentData> {
    static final UUID SENSOR_UUID = UUID.fromString("001D0000-0001-11E1-AC36-0002A5D5C51B");

    private static final double SCALE = 10.0;
    private static final double PRESSURE_SCALE = 100.0;

    public STileEnvironmentSensor(BluetoothDevice device, BluetoothGattService service) {
        super(device, service);
    }

    @Override
    public UUID getDataUUID() {
        return UUID.fromString("001D0000-0001-11E1-AC36-0002A5D5C51B");
    }

    @Override
    public UUID getConfigUUID() {
        return UUID.fromString("001D0000-0001-11E1-AC36-0002A5D5C51B");
    }

    @Override
    public UUID getPeriodUUID() {
        return UUID.fromString("001D0000-0001-11E1-AC36-0002A5D5C51B");
    }

    @Override
    public String getDisplayName() {
        return "EnvironmentSensor";
    }

    @Override
    protected EnvironmentData parse(byte[] value) {
        double pressure = (double) BleUtil.bytesToInt32(value, 2) / PRESSURE_SCALE;
        double humidity = (double) BleUtil.bytesToInt16(value, 6) / SCALE;
        double surfaceTemp = (double) BleUtil.bytesToInt16(value, 8) / SCALE;
        double ambientTemp = (double) BleUtil.bytesToInt16(value, 10) / SCALE;
        return new EnvironmentData(new Double[]{pressure, humidity, surfaceTemp, ambientTemp});
    }
}
