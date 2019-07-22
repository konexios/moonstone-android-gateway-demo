package com.arrow.jmyiotgateway.device.thunderboard;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;

import com.arrow.jmyiotgateway.device.ble.abstracts.BleSensorAbstract;
import com.arrow.jmyiotgateway.device.thunderboard.data.TBCommonData;

import java.util.UUID;

/**
 * Created by osminin on 7/27/2016.
 */

public class ThunderBoardCommonSensor extends BleSensorAbstract<TBCommonData> {
    private UUID mUUID;

    public ThunderBoardCommonSensor(BluetoothDevice device, BluetoothGattService service, UUID uuid) {
        super(device, service);
        mUUID = uuid;
        initCharacteristics();
    }

    @Override
    public UUID getDataUUID() {
        return mUUID;
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
    protected TBCommonData parse(byte[] value) {
        return null;
    }
}
