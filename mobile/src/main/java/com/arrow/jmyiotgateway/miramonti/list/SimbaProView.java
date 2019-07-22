package com.arrow.jmyiotgateway.miramonti.list;

import com.arrow.acn.api.models.ApiError;
import com.arrow.jmyiotgateway.miramonti.device.SimbaProDevice;
import com.arrow.jmyiotgateway.miramonti.error.SPError;

import java.util.List;

public interface SimbaProView {

    void showDetailsFragment(SimbaProDevice model);

    void updateItems(List<SimbaProDevice> list);

    void showEnableBluetoothDialog();

    void showLocationPermissionDialog();

    void showEnableLocationDialog();

    void showError(SPError error);

    void showError(ApiError error);
}
