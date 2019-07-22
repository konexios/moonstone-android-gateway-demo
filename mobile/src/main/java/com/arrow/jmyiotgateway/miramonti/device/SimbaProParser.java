package com.arrow.jmyiotgateway.miramonti.device;

import android.util.Log;

import com.google.firebase.crash.FirebaseCrash;
import com.polidea.rxandroidble.RxBleScanResult;

public final class SimbaProParser {
    private static final String TAG = SimbaProParser.class.getName();
    private static final String SIMBA_PRO = "SensBLE";

    static boolean isSimbaProRecord(RxBleScanResult result) {
        //FirebaseCrash.logcat(Log.VERBOSE, TAG, "isSimbaProRecord()");
        return SIMBA_PRO.equals(result.getBleDevice().getName());
    }

    static SimbaProDevice parse(RxBleScanResult result) {
        //FirebaseCrash.logcat(Log.VERBOSE, TAG, "parse()");
        return new SimbaProDevice().setSimbaBleDevice(result).setTimestamp(System.currentTimeMillis());
    }
}
