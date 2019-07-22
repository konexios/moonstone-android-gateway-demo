package com.arrow.jmyiotgateway.device.simbapro.data;

/**
 * Created by osminin on 16.01.2018.
 */

public final class EnvironmentData {
    private float mTemperature;
    private float mHumidity;
    private float mPressure;

    public float getTemperature() {
        return mTemperature;
    }

    public EnvironmentData setTemperature(float temperature) {
        mTemperature = temperature;
        return this;
    }

    public float getHumidity() {
        return mHumidity;
    }

    public EnvironmentData setHumidity(float humidity) {
        mHumidity = humidity;
        return this;
    }

    public float getPressure() {
        return mPressure;
    }

    public EnvironmentData setPressure(float pressure) {
        mPressure = pressure;
        return this;
    }
}
