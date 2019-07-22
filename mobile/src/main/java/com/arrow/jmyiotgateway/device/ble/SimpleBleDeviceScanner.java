package com.arrow.jmyiotgateway.device.ble;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.arrow.jmyiotgateway.Constant;
import com.arrow.jmyiotgateway.device.ble.abstracts.AbstractBleDeviceScanner;
import com.google.firebase.crash.FirebaseCrash;

import java.util.ArrayList;

public class SimpleBleDeviceScanner extends AbstractBleDeviceScanner {
    private final static String TAG = SimpleBleDeviceScanner.class.getSimpleName();
    private final String mDeviceName;
    private final Handler mHandler;
    private final String mMacAddress;

    public SimpleBleDeviceScanner(String deviceName, String macAddress) {
        mDeviceName = deviceName;
        mMacAddress = macAddress;
        mHandler = new Handler();
    }

    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        FirebaseCrash.logcat(Log.DEBUG, TAG, "onLeScan");
        if (!mDeviceMap.containsKey(device.getAddress())) {
            Advertisement advertisement = new Advertisement(device, scanRecord);
            FirebaseCrash.logcat(Log.INFO, TAG, "onLeScan() found device: " + device.getName() + " / " + device.getAddress());
            String deviceName = device.getName();
            if (TextUtils.isEmpty(mDeviceName)) {
                mDeviceMap.put(device.getAddress(), advertisement);
            } else if (!TextUtils.isEmpty(deviceName)) {
                if ((deviceName.equals(mDeviceName) || deviceName.contains(mDeviceName)) &&
                        (TextUtils.isEmpty(mMacAddress) || mMacAddress.equals(device.getAddress()))) {
                    mDeviceMap.put(device.getAddress(), advertisement);
                    stopScan();
                }
            }
        }
    }

    @Override
    protected synchronized void scan(Context context, BleScanCallback callback) throws Exception {
        super.scan(context, callback);
        FirebaseCrash.logcat(Log.DEBUG, TAG, "scan");
        // Stops mIsScanning after a pre-defined scan period.
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mIsScanning = false;
                stopScan();
            }
        }, Constant.DEFAULT_BLE_SCAN_TIMEOUT);
    }

    @Override
    public void stopScan() {
        FirebaseCrash.logcat(Log.DEBUG, TAG, "stopScan");
        super.stopScan();
        if (mBleScanCallback != null) {
            mBleScanCallback.scanComplete(new ArrayList<>(mDeviceMap.values()));
            mBleScanCallback = null;
        }
    }
}
