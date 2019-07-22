package com.arrow.jmyiotgateway.miramonti.list;

import com.arrow.jmyiotgateway.miramonti.device.SimbaProDevice;

public interface SimbaProListPresenter {

    void bind(SimbaProView view);

    void startScan();

    void stopScan();

    void onDeviceSelected(SimbaProDevice device);

    void onScannerFunctionalityEnabled(int requestCode, boolean isEnabled);

    void getSocialEventsList();
}
