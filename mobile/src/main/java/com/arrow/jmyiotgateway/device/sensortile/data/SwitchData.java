package com.arrow.jmyiotgateway.device.sensortile.data;

import com.arrow.jmyiotgateway.cloud.iot.IotParameter;
import com.arrow.jmyiotgateway.device.TelemetriesNames;

/**
 * Created by osminin on 9/8/2016.
 */

public final class SwitchData extends SensorData<Short> {
    public SwitchData(Short data) {
        super(data);
    }

    @Override
    public IotParameter[] toIotParameters() {
        return new IotParameter[]{new IotParameter(TelemetriesNames.SWITCH, getData().toString())};
    }
}
