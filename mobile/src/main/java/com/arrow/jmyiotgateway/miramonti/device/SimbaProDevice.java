package com.arrow.jmyiotgateway.miramonti.device;

import android.support.annotation.NonNull;

import com.polidea.rxandroidble.RxBleScanResult;

public final class SimbaProDevice implements Comparable<SimbaProDevice> {

    private RxBleScanResult mSimbaBleDevice;
    private long mTimestamp;
    private String mPin;

    public long getTimestamp() {
        return mTimestamp;
    }

    public String getPin() {
        return mPin;
    }

    public SimbaProDevice setPin(String pin) {
        mPin = pin;
        return this;
    }

    public SimbaProDevice setTimestamp(long timestamp) {
        mTimestamp = timestamp;
        return this;

    }

    public RxBleScanResult getSimbaBleDevice() {
        return mSimbaBleDevice;
    }

    public SimbaProDevice setSimbaBleDevice(RxBleScanResult simbaBleDevice) {
        this.mSimbaBleDevice = simbaBleDevice;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SimbaProDevice that = (SimbaProDevice) o;

        boolean res = mSimbaBleDevice.getBleDevice().getMacAddress() != null ? mSimbaBleDevice.getBleDevice().getMacAddress().equals(that.mSimbaBleDevice.getBleDevice().getMacAddress()) : that.mSimbaBleDevice.getBleDevice().getMacAddress() == null;

        return res;
    }

    @Override
    public int hashCode() {
        return mSimbaBleDevice.getBleDevice().getMacAddress() != null ? mSimbaBleDevice.getBleDevice().getMacAddress().hashCode() : 0;
    }

    public SignalStrength getSignalStrength() {
        SignalStrength signalStrength;
        if (mSimbaBleDevice.getRssi() > -30) {
            signalStrength = SignalStrength.VERY_HIGH;
        } else if (mSimbaBleDevice.getRssi() > -40) {
            signalStrength = SignalStrength.HIGH;
        } else if (mSimbaBleDevice.getRssi() > -50) {
            signalStrength = SignalStrength.MEDIUM;
        } else if (mSimbaBleDevice.getRssi() > -60) {
            signalStrength = SignalStrength.LOW;
        } else {
            signalStrength = SignalStrength.VERY_LOW;
        }
        return signalStrength;
    }

    @Override
    public int compareTo(@NonNull SimbaProDevice o) {
        return mSimbaBleDevice.getBleDevice().getMacAddress().compareTo(o.getSimbaBleDevice().getBleDevice().getMacAddress());
    }

    public enum SignalStrength {
        VERY_LOW,
        LOW,
        MEDIUM,
        HIGH,
        VERY_HIGH
    }
}
