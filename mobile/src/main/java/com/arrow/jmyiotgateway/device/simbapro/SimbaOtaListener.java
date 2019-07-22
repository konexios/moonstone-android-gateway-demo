package com.arrow.jmyiotgateway.device.simbapro;

public interface SimbaOtaListener {

    void onOTAProcessStarted();

    void onFirmwareRead();

    void onOTAWriteStarted();

    void onOTACompleted();

    void onOTAError();
}
