package com.arrow.jmyiotgateway.device.simbapro.data;

import com.arrow.jmyiotgateway.cloud.iot.IotParameter;
import com.arrow.jmyiotgateway.device.TelemetriesNames;
import com.arrow.jmyiotgateway.device.sensortile.data.SensorData;

/**
 * Created by osminin on 16.01.2018.
 */

public final class SimbaProEnvironmentData extends SensorData<EnvironmentData> {

    public SimbaProEnvironmentData(EnvironmentData data) {
        super(data);
    }

    @Override
    public IotParameter[] toIotParameters() {
        return new IotParameter[]{
                new IotParameter(TelemetriesNames.PRESSURE, Double.toString(getData().getPressure())),
                new IotParameter(TelemetriesNames.TEMPERATURE, Double.toString(getData().getTemperature() * 9.0 / 5.0 + 32)),
                new IotParameter(TelemetriesNames.HUMIDITY, Double.toString(getData().getHumidity()))
        };
    }
}
