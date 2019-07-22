package com.arrow.jmyiotgateway.device.thunderboard.data;

import com.arrow.jmyiotgateway.cloud.iot.IotParameter;
import com.arrow.jmyiotgateway.device.TelemetriesNames;
import com.arrow.jmyiotgateway.device.sensortile.data.SensorData;
import com.arrow.jmyiotgateway.device.sensortile.data.Vector;

/**
 * Created by osminin on 8/1/2016.
 */

public final class TBVectorData extends SensorData<Vector> {
    public TBVectorData(Vector data) {
        super(data);
    }

    @Override
    public IotParameter[] toIotParameters() {
        return new IotParameter[]{
                new IotParameter(TelemetriesNames.ACCELEROMETER_X, Double.toString(getData().getX())),
                new IotParameter(TelemetriesNames.ACCELEROMETER_Y, Double.toString(getData().getY())),
                new IotParameter(TelemetriesNames.ACCELEROMETER_Z, Double.toString(getData().getZ()))
        };
    }
}
