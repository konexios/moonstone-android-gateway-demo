package com.arrow.jmyiotgateway.miramonti.acn;

import com.arrow.acn.api.listeners.RegisterAccountListener;
import com.arrow.jmyiotgateway.miramonti.acn.models.eventModel.EventAccountRequest;
import com.arrow.jmyiotgateway.miramonti.acn.models.eventModel.EventResendResponse;
import com.arrow.jmyiotgateway.miramonti.acn.models.eventModel.EventVerifyResponse;
import com.arrow.jmyiotgateway.miramonti.acn.models.eventModel.SocialEventListResponse;

/**
 * Created by batrakov on 18.01.18.
 */

public interface EventAcnApiService {
    void registerEventAccount(EventAccountRequest aEventAccountRequest, RegisterAccountListener listener);

    void verifyEventAccount(String verificationCode, EventSimpleResponseListener<EventVerifyResponse> listener);

    void resendVerificationCode(String email, EventSimpleResponseListener<EventResendResponse> listener);

    void findSocialEvents(EventSimpleResponseListener<SocialEventListResponse> aEventsRequestListener);
}
