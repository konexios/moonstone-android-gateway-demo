package com.arrow.jmyiotgateway.device.sensortile.data;

public class Movement {
    private final Vector acc;
    private final Vector gyro;
    private final Vector mag;

    public Movement(Vector acc, Vector gyro, Vector mag) {
        this.acc = acc;
        this.gyro = gyro;
        this.mag = mag;
    }

    public Vector getAcc() {
        return acc;
    }

    public Vector getGyro() {
        return gyro;
    }

    public Vector getMag() {
        return mag;
    }
}
