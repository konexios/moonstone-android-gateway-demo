package com.arrow.jmyiotgateway.device.sensortile.data;

import com.arrow.jmyiotgateway.cloud.iot.IotParameter;
import com.arrow.jmyiotgateway.device.TelemetriesNames;

/**
 * Created by osminin on 9/8/2016.
 */

public final class EnvironmentData extends SensorData<Double[]> {
    public EnvironmentData(Double[] data) {
        super(data);
    }

    @Override
    public IotParameter[] toIotParameters() {
        return new IotParameter[]{
                new IotParameter(TelemetriesNames.PRESSURE, Double.toString(getData()[0])),
                new IotParameter(TelemetriesNames.HUMIDITY, Double.toString(getData()[1])),
                new IotParameter(TelemetriesNames.TEMPERATURE, Double.toString(getData()[2] * 9.0 / 5.0 + 32)),
                new IotParameter(TelemetriesNames.IR_TEMPERATURE, Double.toString(getData()[3] * 9.0 / 5.0 + 32))
        };
    }
}
