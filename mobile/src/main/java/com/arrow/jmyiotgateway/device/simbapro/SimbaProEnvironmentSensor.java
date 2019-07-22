package com.arrow.jmyiotgateway.device.simbapro;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;

import com.arrow.jmyiotgateway.device.ble.abstracts.BleSensorAbstract;
import com.arrow.jmyiotgateway.device.simbapro.data.EnvironmentData;
import com.arrow.jmyiotgateway.device.simbapro.data.SimbaProEnvironmentData;

import java.util.UUID;

/**
 * Created by osminin on 16.01.2018.
 */

public final class SimbaProEnvironmentSensor extends BleSensorAbstract<SimbaProEnvironmentData> {
    static final UUID SENSOR_UUID = UUID.fromString("001c0000-0001-11e1-ac36-0002a5d5c51b");
    private static int HUMIDITY_DATA_OFFSET = 6;
    private static int TEMPERATURE_DATA_OFFSET = 8;
    private static int PRESSURE_DATA_OFFSET = 2;

    public SimbaProEnvironmentSensor(BluetoothDevice device, BluetoothGattService service) {
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
    protected SimbaProEnvironmentData parse(byte[] value) {
        return new SimbaProEnvironmentData(new EnvironmentData()
                .setHumidity(SimbaProUtils.bytesToInt16(value, HUMIDITY_DATA_OFFSET) / 10.0f)
                .setTemperature(SimbaProUtils.bytesToInt16(value, TEMPERATURE_DATA_OFFSET) / 10.0f)
                .setPressure(SimbaProUtils.bytesToInt32(value, PRESSURE_DATA_OFFSET) / 100.0f));
    }
}
