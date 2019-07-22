package com.arrow.jmyiotgateway.device.thunderboard.data;

import com.arrow.jmyiotgateway.cloud.iot.IotParameter;
import com.arrow.jmyiotgateway.device.sensortile.data.SensorData;

/**
 * Created by osminin on 7/27/2016.
 */

public final class TBCommonData extends SensorData<Object> {
    public TBCommonData(Object data) {
        super(data);
    }

    @Override
    public IotParameter[] toIotParameters() {
        return null;
    }
}
