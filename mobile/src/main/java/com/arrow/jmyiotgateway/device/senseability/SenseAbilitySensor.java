package com.arrow.jmyiotgateway.device.senseability;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattService;

import com.arrow.jmyiotgateway.Util;
import com.arrow.jmyiotgateway.device.ble.BleUtil;
import com.arrow.jmyiotgateway.device.ble.abstracts.BleSensorAbstract;
import com.arrow.jmyiotgateway.device.sensortile.data.SensorData;

import java.util.UUID;

/**
 * Created by osminin on 22.07.2016.
 */

public final class SenseAbilitySensor extends BleSensorAbstract {
    final static String SERVICE_UUID = "6789aaaa-0000-1000-8000-00805f9b0132";
    final static String DATA_UUID = "1234ffff-0000-1000-8000-00805f9b0132";
    final static String CONFIG_UUID = "00002a25-0000-1000-8000-00805f9b34fb";

    public SenseAbilitySensor(BluetoothDevice device, BluetoothGattService service) {
        super(device, service);
        initCharacteristics();
    }

    @Override
    public UUID getDataUUID() {
        return UUID.fromString(DATA_UUID);
    }

    @Override
    public UUID getConfigUUID() {
        return UUID.fromString(CONFIG_UUID);
    }

    @Override
    public UUID getPeriodUUID() {
        return UUID.fromString(CONFIG_UUID);
    }

    @Override
    public String getDisplayName() {
        return "SenseAbilitySensor";
    }

    @Override
    protected SensorData<?> parse(byte[] bytes) {
        SenseAbilityDataModel model = new SenseAbilityDataModel();

        model.setStatus(Util.toBinaryString(bytes[0]));

        double temperature = (Double.valueOf(bytes[1] & 0xff) * 9.0 / 5.0) + 32.0;
        model.setTemperature(temperature);

        double humidity = (double) (bytes[2] & 0xff);
        model.setHumidity(humidity);

        boolean magnet = bytes[3] == 0;
        model.setMagnet(magnet);

        double pressure = Double.valueOf(BleUtil.shortUnsignedAtOffset(bytes, 4)) / 10.0;
        model.setPressure(pressure);

        double airflow = Double.valueOf(BleUtil.shortSignedAtOffset(bytes, 6));
        model.setAirflow(airflow);

        return new SA2SensorData(model);
    }

    public boolean enable(BluetoothGatt gatt) {
        return true;
    }
}
