package com.arrow.jmyiotgateway.device.simbapro.data;

import com.arrow.jmyiotgateway.cloud.iot.IotParameter;
import com.arrow.jmyiotgateway.device.TelemetriesNames;
import com.arrow.jmyiotgateway.device.sensortile.data.SensorData;

/**
 * Created by osminin on 15.01.2018.
 */

public final class LightData extends SensorData<Short> {

    public LightData(Short data) {
        super(data);
    }

    @Override
    public IotParameter[] toIotParameters() {
        return new IotParameter[]{new IotParameter(TelemetriesNames.LIGHT, getData().toString())};
    }
}
