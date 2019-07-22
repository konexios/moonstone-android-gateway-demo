package com.arrow.jmyiotgateway.device.ble;

import java.util.UUID;

public class BleConstant {
    public final static long DEFAULT_PERIOD_MILLIS = 1000;

    public static class BleUUID {
        public static final UUID CLIENT_CHARACTERISTIC_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    }
}
