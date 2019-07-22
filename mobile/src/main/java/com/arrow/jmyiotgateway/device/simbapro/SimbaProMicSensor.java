package com.arrow.jmyiotgateway.device.simbapro;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;

import com.arrow.jmyiotgateway.device.ble.abstracts.BleSensorAbstract;
import com.arrow.jmyiotgateway.device.simbapro.data.MicLevelData;

import java.util.UUID;

/**
 * Created by osminin on 15.01.2018.
 */

public final class SimbaProMicSensor extends BleSensorAbstract<MicLevelData> {
    static final UUID SENSOR_UUID = UUID.fromString("04000000-0001-11e1-ac36-0002a5d5c51b");
    private static int DATA_OFFSET = 2;

    public SimbaProMicSensor(BluetoothDevice device, BluetoothGattService service) {
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
    protected MicLevelData parse(byte[] value) {
        return new MicLevelData(value[DATA_OFFSET]);
    }
}
