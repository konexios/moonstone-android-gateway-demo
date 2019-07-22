package com.arrow.jmyiotgateway.device.senseability;

/**
 * Created by osminin on 25.07.2016.
 */

public class SenseAbilityDataModel {
    private double mTemperature;
    private double mHumidity;
    private boolean mMagnet;
    private double mPressure;
    private double mAirflow;
    private String mStatus;

    public double getTemperature() {
        return mTemperature;
    }

    public void setTemperature(double temperature) {
        mTemperature = temperature;
    }

    public double getHumidity() {
        return mHumidity;
    }

    public void setHumidity(double humidity) {
        mHumidity = humidity;
    }

    public boolean isMagnet() {
        return mMagnet;
    }

    public void setMagnet(boolean magnet) {
        mMagnet = magnet;
    }

    public double getPressure() {
        return mPressure;
    }

    public void setPressure(double pressure) {
        mPressure = pressure;
    }

    public double getAirflow() {
        return mAirflow;
    }

    public void setAirflow(double airflow) {
        mAirflow = airflow;
    }

    public String getStatus() {
        return mStatus;
    }

    public void setStatus(String status) {
        mStatus = status;
    }
}
