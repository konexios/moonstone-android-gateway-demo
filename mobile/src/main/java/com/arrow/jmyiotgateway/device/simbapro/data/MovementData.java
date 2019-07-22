package com.arrow.jmyiotgateway.device.simbapro.data;

import com.arrow.jmyiotgateway.cloud.iot.IotParameter;
import com.arrow.jmyiotgateway.device.TelemetriesNames;
import com.arrow.jmyiotgateway.device.sensortile.data.SensorData;

public class MovementData extends SensorData<Movement> {
    public MovementData(Movement data) {
        super(data);
    }

    @Override
    public IotParameter[] toIotParameters() {
        String accX = Double.toString(getData().getAcc().getX());
        String accY = Double.toString(getData().getAcc().getY());
        String accZ = Double.toString(getData().getAcc().getZ());
        String gyroX = Double.toString(getData().getGyro().getX());
        String gyroY = Double.toString(getData().getGyro().getY());
        String gyroZ = Double.toString(getData().getGyro().getZ());
        String magX = Double.toString(getData().getMag().getX());
        String magY = Double.toString(getData().getMag().getY());
        String magZ = Double.toString(getData().getMag().getZ());
        return new IotParameter[]{
                new IotParameter(TelemetriesNames.ACCELEROMETER_X, accX),
                new IotParameter(TelemetriesNames.ACCELEROMETER_Y, accY),
                new IotParameter(TelemetriesNames.ACCELEROMETER_Z, accZ),
                new IotParameter(TelemetriesNames.ACCELEROMETER_XYZ, String.format("%s|%s|%s", accX, accY, accZ)),
                new IotParameter(TelemetriesNames.GYROMETER_X, gyroX),
                new IotParameter(TelemetriesNames.GYROMETER_Y, gyroY),
                new IotParameter(TelemetriesNames.GYROMETER_Z, gyroZ),
                new IotParameter(TelemetriesNames.GYROMETER_XYZ, String.format("%s|%s|%s", gyroX, gyroY, gyroZ)),
                new IotParameter(TelemetriesNames.MAGNETOMETER_X, magX),
                new IotParameter(TelemetriesNames.MAGNETOMETER_Y, magY),
                new IotParameter(TelemetriesNames.MAGNETOMETER_Z, magZ),
                new IotParameter(TelemetriesNames.MAGNETOMETER_XYZ, String.format("%s|%s|%s", magX, magY, magZ))
        };
    }
}
