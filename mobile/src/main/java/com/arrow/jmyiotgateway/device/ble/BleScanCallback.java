package com.arrow.jmyiotgateway.device.ble;

import java.util.List;

public interface BleScanCallback {
    void scanComplete(List<Advertisement> advertisements);
}
