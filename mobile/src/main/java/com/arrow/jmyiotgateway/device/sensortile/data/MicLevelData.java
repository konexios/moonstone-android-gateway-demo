package com.arrow.jmyiotgateway.device.sensortile.data;

import com.arrow.jmyiotgateway.cloud.iot.IotParameter;
import com.arrow.jmyiotgateway.device.TelemetriesNames;

/**
 * Created by osminin on 9/8/2016.
 */

public final class MicLevelData extends SensorData<Double> {
    public MicLevelData(Double data) {
        super(data);
    }

    @Override
    public IotParameter[] toIotParameters() {
        return new IotParameter[]{new IotParameter(TelemetriesNames.MIC_LEVEL, getData().toString())};
    }
}
