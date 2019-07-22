package com.arrow.jmyiotgateway.miramonti.device;

import android.content.Context;
import android.util.Log;

import com.google.firebase.crash.FirebaseCrash;
import com.polidea.rxandroidble.RxBleClient;

import rx.Observable;
import rx.schedulers.Schedulers;

public final class SimbaProScanner {
    private static final String TAG = SimbaProScanner.class.getName();
    private RxBleClient rxBleClient;

    public SimbaProScanner(final Context context) {
        FirebaseCrash.logcat(Log.DEBUG, TAG, "BleSPScanner(): ");
        rxBleClient = RxBleClient.create(context);
    }

    public Observable<SimbaProDevice> startObserve() {
        return rxBleClient
                .scanBleDevices()
                .observeOn(Schedulers.computation())
                .filter(SimbaProParser::isSimbaProRecord)
                .map(SimbaProParser::parse);
    }
}

