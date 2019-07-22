package com.arrow.jmyiotgateway.device.ble;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.Log;

import com.arrow.jmyiotgateway.device.ble.abstracts.AbstractBleDeviceScanner;
import com.google.firebase.crash.FirebaseCrash;

import java.util.ArrayList;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.schedulers.Schedulers;


/**
 * Created by osminin on 6/7/2016.
 */
public class InfiniteBleDeviceScanner extends AbstractBleDeviceScanner {
    private final static String TAG = InfiniteBleDeviceScanner.class.getSimpleName();

    @Override
    public synchronized BluetoothDevice scanDevice(Context context, BleScanCallback callback) throws Exception {
        FirebaseCrash.logcat(Log.DEBUG, TAG, "scanDevice");
        startScanAsync(context, callback);
        return null;
    }

    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        FirebaseCrash.logcat(Log.DEBUG, TAG, "onLeScan");
        Advertisement advertisement = new Advertisement(device, scanRecord);
        ArrayList<Advertisement> list = new ArrayList<>();
        list.add(advertisement);
        mBleScanCallback.scanComplete(list);
    }

    private void startScanAsync(final Context context, final BleScanCallback callback) {
        FirebaseCrash.logcat(Log.DEBUG, TAG, "startScanAsync");
        Subscription subscription = Observable.empty()
                .observeOn(Schedulers.computation())
                .subscribe(new Observer<Object>() {
                    @Override
                    public void onCompleted() {
                        try {
                            InfiniteBleDeviceScanner.super.scanDevice(context, callback);
                        } catch (Throwable t) {
                            FirebaseCrash.logcat(Log.ERROR, TAG, "startScanAsync error");
                            FirebaseCrash.report(t);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        FirebaseCrash.logcat(Log.ERROR, TAG, "Ble scan error");
                        FirebaseCrash.report(e);
                    }

                    @Override
                    public void onNext(Object o) {

                    }
                });
    }
}
