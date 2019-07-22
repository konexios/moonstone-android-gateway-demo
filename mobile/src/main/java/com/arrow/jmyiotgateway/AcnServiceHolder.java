package com.arrow.jmyiotgateway;

import android.content.Context;
import android.text.TextUtils;

import com.arrow.acn.api.AcnApi;
import com.arrow.acn.api.AcnApiService;

import static com.arrow.jmyiotgateway.Constant.DEFAULT_API_KEY;
import static com.arrow.jmyiotgateway.Constant.DEFAULT_API_SECRET;

/**
 * Created by Alex on 26.04.2017.
 */

public class AcnServiceHolder {

    private static AcnApiService mService;
    private static String sApiKey = DEFAULT_API_KEY;
    private static String sApiSecretKey = DEFAULT_API_SECRET;

    public static AcnApiService createService(Context context, Config config) {
        String defaultMode = context.getString(R.string.settings_demo_mode);
        if (TextUtils.isEmpty(config.getServerEnvironment())) {
            config.setServerEnvironment(defaultMode);
        }
        String endpointUrl = defaultMode.equals(config.getServerEnvironment()) ? Constant.IOT_CONNECT_URL_DEMO : Constant.IOT_CONNECT_URL_DEV;
        String host = defaultMode.equals(config.getServerEnvironment()) ? Constant.MQTT_CONNECT_URL_DEMO :
                Constant.MQTT_CONNECT_URL_DEV;
        String mqttPrefix = defaultMode.equals(config.getServerEnvironment()) ? Constant.MQTT_CLIENT_PREFIX_DEMO :
                Constant.MQTT_CLIENT_PREFIX_DEV;
        if (!TextUtils.isEmpty(config.getZoneSystemName())) {
            if (endpointUrl.contains("<zone-name>")) {
                endpointUrl = endpointUrl.replace("<zone-name>", config.getZoneSystemName());
            }
            if (host.contains("<zone-name>")) {
                host = host.replace("<zone-name>", config.getZoneSystemName());
            }
            mService = new AcnApi.Builder()
                    .setRestEndpoint(endpointUrl, sApiKey, sApiSecretKey)
                    .setMqttEndpoint(host, mqttPrefix)
                    .setDebug(true)
                    .build();
            return mService;
        } else {
            endpointUrl = defaultMode.equals(config.getServerEnvironment()) ? Constant.ARROW_CONNECT_URL_DEMO : Constant.ARROW_CONNECT_URL_DEV;
            mService = new AcnApi.Builder()
                    .setRestEndpoint(endpointUrl, DEFAULT_API_KEY, DEFAULT_API_SECRET)
                    .setMqttEndpoint(host, mqttPrefix)
                    .setDebug(true)
                    .build();
            return mService;
        }


    }

    public static AcnApiService getService() {
        return mService;
    }

    public static void setApiKey(String aApiKey) {
        sApiKey = aApiKey;
    }

    public static void setApiSecretKey(String aApiSecretKey) {
        sApiSecretKey = aApiSecretKey;
    }
}
