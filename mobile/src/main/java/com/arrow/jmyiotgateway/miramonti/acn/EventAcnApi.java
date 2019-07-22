package com.arrow.jmyiotgateway.miramonti.acn;

import android.support.annotation.Keep;

import com.arrow.jmyiotgateway.miramonti.acn.eventImpl.EventAcnApiServiceImpl;
import com.arrow.jmyiotgateway.miramonti.common.EventRetrofitHolderImpl;

/**
 * Created by batrakov on 18.01.18.
 */

public class EventAcnApi {

    @Keep
    public static final class Builder {
        private String mEndpoint;
        private String mApiKey;
        private String mApiSecret;

        public EventAcnApi.Builder setRestEndpoint(String endpoint, String apiKey, String apiSecret) {
            mEndpoint = endpoint;
            mApiKey = apiKey;
            mApiSecret = apiSecret;
            return this;
        }


        public EventAcnApiService build() {
            EventAcnApiServiceImpl service = new EventAcnApiServiceImpl(new EventRetrofitHolderImpl(null));
            service.setRestEndpoint(mEndpoint, mApiKey, mApiSecret);
            return service;
        }
    }
}
