package com.arrow.jmyiotgateway.device.simbapro;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;

import com.arrow.jmyiotgateway.device.ble.abstracts.BleSensorAbstract;
import com.arrow.jmyiotgateway.device.sensortile.data.SensorData;
import com.arrow.jmyiotgateway.device.simbapro.data.LightData;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.UUID;

/**
 * Created by osminin on 15.01.2018.
 */

public class SimbaProLightSensor extends BleSensorAbstract<LightData> {
    static final UUID SENSOR_UUID = UUID.fromString("01000000-0001-11e1-ac36-0002a5d5c51b");
    private static int DATA_OFFSET = 2;

    public SimbaProLightSensor(BluetoothDevice device, BluetoothGattService service) {
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
    protected LightData parse(byte[] value) {
        return new LightData(SimbaProUtils.bytesToInt16(value, DATA_OFFSET));
    }
}
