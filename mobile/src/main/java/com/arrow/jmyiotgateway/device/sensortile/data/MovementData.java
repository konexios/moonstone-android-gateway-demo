package com.arrow.jmyiotgateway.device.sensortile.data;

import com.arrow.jmyiotgateway.cloud.iot.IotParameter;
import com.arrow.jmyiotgateway.device.TelemetriesNames;

public class MovementData extends SensorData<Movement> {
    public MovementData(Movement data) {
        super(data);
    }

    @Override
    public IotParameter[] toIotParameters() {
        String accX = String.format("%1$,.2f",getData().getAcc().getX());
        String accY = String.format("%1$,.2f",getData().getAcc().getY());
        String accZ = String.format("%1$,.2f",getData().getAcc().getZ());
        String gyroX = String.format("%1$,.2f",getData().getGyro().getX());
        String gyroY = String.format("%1$,.2f",getData().getGyro().getY());
        String gyroZ = String.format("%1$,.2f",getData().getGyro().getZ());
        String magX = String.format("%1$,.2f",getData().getMag().getX());
        String magY = String.format("%1$,.2f",getData().getMag().getY());
        String magZ = String.format("%1$,.2f",getData().getMag().getZ());
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
