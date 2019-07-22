package com.arrow.jmyiotgateway.device.ble;

import android.bluetooth.BluetoothDevice;

import java.util.Arrays;

/**
 * Created by osminin on 6/7/2016.
 */
public class Advertisement {
    private BluetoothDevice mDevice;
    private byte[] mData;

    public Advertisement(BluetoothDevice device, byte[] data) {
        mDevice = device;
        mData = data;
    }

    public BluetoothDevice getDevice() {
        return mDevice;
    }

    public byte[] getData() {
        return mData;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Advertisement that = (Advertisement) o;

        if (!mDevice.equals(that.mDevice)) return false;
        return Arrays.equals(mData, that.mData);

    }

    @Override
    public int hashCode() {
        int result = mDevice.hashCode();
        result = 31 * result + Arrays.hashCode(mData);
        return result;
    }

}
