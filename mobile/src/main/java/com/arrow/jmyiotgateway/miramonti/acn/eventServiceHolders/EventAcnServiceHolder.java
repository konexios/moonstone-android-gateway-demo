package com.arrow.jmyiotgateway.miramonti.acn.eventServiceHolders;

import android.content.Context;
import android.text.TextUtils;

import com.arrow.jmyiotgateway.Config;
import com.arrow.jmyiotgateway.R;
import com.arrow.jmyiotgateway.miramonti.acn.EventAcnApi;
import com.arrow.jmyiotgateway.miramonti.acn.EventAcnApiService;

import static com.arrow.jmyiotgateway.Constant.ARROW_CONNECT_URL_DEMO;
import static com.arrow.jmyiotgateway.Constant.ARROW_CONNECT_URL_DEV;
import static com.arrow.jmyiotgateway.Constant.DEFAULT_API_KEY;
import static com.arrow.jmyiotgateway.Constant.DEFAULT_API_SECRET;

/**
 * Created by batrakov on 18.01.18.
 */

public class EventAcnServiceHolder {

    private static EventAcnApiService mService;

    public static EventAcnApiService createService(Context context, Config config) {
        String defaultMode = context.getString(R.string.settings_demo_mode);
        if (TextUtils.isEmpty(config.getServerEnvironment())) {
            config.setServerEnvironment(defaultMode);
        }

        String endpoitURL = config.getServerEnvironment().equals(defaultMode) ?
                ARROW_CONNECT_URL_DEMO :  ARROW_CONNECT_URL_DEV;

        mService = new EventAcnApi.Builder()
                .setRestEndpoint(endpoitURL, DEFAULT_API_KEY, DEFAULT_API_SECRET)
                .build();
        return mService;
    }

    public static EventAcnApiService getService() {
        return mService;
    }
}
