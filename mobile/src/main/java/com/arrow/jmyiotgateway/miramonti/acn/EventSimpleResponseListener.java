package com.arrow.jmyiotgateway.miramonti.acn;

import com.arrow.acn.api.models.ApiError;

public interface EventSimpleResponseListener<T> {
    void onRequestSuccess(T response);

    void onRequestError(ApiError error);
}
