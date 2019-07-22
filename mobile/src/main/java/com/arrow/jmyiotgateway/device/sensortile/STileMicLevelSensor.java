package com.arrow.jmyiotgateway.device.sensortile;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;

import com.arrow.jmyiotgateway.device.ble.BleUtil;
import com.arrow.jmyiotgateway.device.sensortile.data.MicLevelData;

import java.util.UUID;

/**
 * Created by osminin on 9/8/2016.
 */

public final class STileMicLevelSensor extends STSensorAbstract<MicLevelData> {
    static final UUID SENSOR_UUID = UUID.fromString("04000000-0001-11E1-AC36-0002A5D5C51B");

    public STileMicLevelSensor(BluetoothDevice device, BluetoothGattService service) {
        super(device, service);
    }

    @Override
    public UUID getDataUUID() {
        return UUID.fromString("04000000-0001-11E1-AC36-0002A5D5C51B");
    }

    @Override
    public UUID getConfigUUID() {
        return UUID.fromString("04000000-0001-11E1-AC36-0002A5D5C51B");
    }

    @Override
    public UUID getPeriodUUID() {
        return UUID.fromString("04000000-0001-11E1-AC36-0002A5D5C51B");
    }

    @Override
    public String getDisplayName() {
        return "MicLevelSensor";
    }

    @Override
    protected MicLevelData parse(byte[] value) {
        double level = (double) BleUtil.byteToUInt8(value, 2);
        return new MicLevelData(level);
    }
}
