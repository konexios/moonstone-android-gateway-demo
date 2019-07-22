package com.arrow.jmyiotgateway.device.ble;

import com.arrow.jmyiotgateway.device.ble.abstracts.AbstractBleDeviceScanner;
import com.arrow.jmyiotgateway.device.ble.abstracts.AbstractBleScannerFactory;

/**
 * Created by osminin on 6/7/2016.
 */
public class SimpleBleScannerFactory extends AbstractBleScannerFactory {
    private final String mDeviceName;
    private final String mMacAddress;

    public SimpleBleScannerFactory(String deviceName) {
        this(deviceName, "");
    }

    public SimpleBleScannerFactory(String deviceName, String macAddress) {
        mDeviceName = deviceName;
        mMacAddress = macAddress;
    }

    @Override
    public AbstractBleDeviceScanner getBleScanner() {
        return new SimpleBleDeviceScanner(mDeviceName, mMacAddress);
    }
}
