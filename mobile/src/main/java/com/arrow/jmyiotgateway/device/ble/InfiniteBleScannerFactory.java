package com.arrow.jmyiotgateway.device.ble;

import com.arrow.jmyiotgateway.device.ble.abstracts.AbstractBleDeviceScanner;
import com.arrow.jmyiotgateway.device.ble.abstracts.AbstractBleScannerFactory;

/**
 * Created by osminin on 6/7/2016.
 */
public class InfiniteBleScannerFactory extends AbstractBleScannerFactory {
    @Override
    public AbstractBleDeviceScanner getBleScanner() {
        return new InfiniteBleDeviceScanner();
    }
}
