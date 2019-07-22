package com.arrow.jmyiotgateway.device.ble.abstracts;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.arrow.jmyiotgateway.device.ble.Advertisement;
import com.arrow.jmyiotgateway.device.ble.BleScanCallback;
import com.arrow.jmyiotgateway.device.ble.BleStatus;
import com.arrow.jmyiotgateway.device.ble.BleUtil;
import com.google.firebase.crash.FirebaseCrash;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by osminin on 6/7/2016.
 */
public abstract class AbstractBleDeviceScanner implements BluetoothAdapter.LeScanCallback{
    private final static String TAG = AbstractBleDeviceScanner.class.getSimpleName();

    protected boolean mIsScanning = false;
    private BluetoothAdapter mAdapter;
    private BluetoothLeScanner mScanner;
    protected Map<String, Advertisement> mDeviceMap = new HashMap<>();
    protected BleScanCallback mBleScanCallback;
    private Object mLock = null;
    private AbstractScanCallback mScanCallback;

    public synchronized BluetoothDevice scanDevice(Context context, BleScanCallback callback) throws Exception {
        if (mLock == null) {
            mLock = new Object();
        }
        synchronized (mLock) {
            FirebaseCrash.logcat(Log.INFO, TAG, "scanDevice() ...");
            scan(context, callback);
            FirebaseCrash.logcat(Log.INFO, TAG, "scanDevice() waiting ...");
            mLock.wait();
        }
        mLock = null;
        FirebaseCrash.logcat(Log.INFO, TAG, "scanDevice() complete");
        return mDeviceMap.isEmpty() ? null : mDeviceMap.values().iterator().next().getDevice();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    protected synchronized void scan(Context context, final BleScanCallback callback) throws Exception {
        if (mIsScanning) {
            throw new Exception("Scanning is currently running!");
        }

        BleStatus status = BleUtil.getBleStatus(context);
        if (status != BleStatus.BLE_AVAILABLE) {
            throw new Exception("Invalid BLE status: " + status);
        }

        mAdapter = BleUtil.getBluetoothAdapter(context);
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            mScanCallback = new AbstractScanCallback();
            mScanner = mAdapter.getBluetoothLeScanner();
        }
        if (mAdapter == null) {
            throw new Exception("Invalid BLE status: " + status);
        }

        // reset variables
        mIsScanning = true;
        this.mBleScanCallback = callback;
        mDeviceMap.clear();

        FirebaseCrash.logcat(Log.INFO, TAG, "scan() startLeScan ...");
        if (mScanner == null) {
            mAdapter.startLeScan(this);
        } else {
            mScanner.startScan(mScanCallback);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void stopScan() {
        FirebaseCrash.logcat(Log.INFO, TAG, "stopScan() stopping ...");
        if (mAdapter != null) {
            if (mScanner == null) {
                mAdapter.stopLeScan(this);
            } else if (mAdapter.getState() == BluetoothAdapter.STATE_ON){
                mScanner.stopScan(mScanCallback);
            }
            mIsScanning = false;
            if (mLock != null) {
                synchronized (mLock) {
                    mLock.notifyAll();
                }
            }
        }
    }

    public boolean isScanning() {
        return mIsScanning;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private class AbstractScanCallback extends ScanCallback {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            onLeScan(result.getDevice(), result.getRssi(), result.getScanRecord().getBytes());
        }
    }
}
