package com.arrow.jmyiotgateway.miramonti.acn;

import android.support.annotation.NonNull;

import com.arrow.acn.api.models.AccountResponse;
import com.arrow.jmyiotgateway.miramonti.acn.models.eventModel.EventAccountRequest;
import com.arrow.jmyiotgateway.miramonti.acn.models.eventModel.EventResendResponse;
import com.arrow.jmyiotgateway.miramonti.acn.models.eventModel.EventVerifyResponse;
import com.arrow.jmyiotgateway.miramonti.acn.models.eventModel.SocialEventListResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface EventIotConnectApiService{
    //Account api
    @NonNull
    @POST("/api/v1/kronos/socialevent/registrations/register")
    Call<AccountResponse> registerEventAccount(@Body EventAccountRequest aEventAccountRequest);

    @NonNull
    @POST("/api/v1/kronos/socialevent/registrations/verify")
    Call<EventVerifyResponse> verifyEventAccount(@Query("verificationCode") String verificationCode);

    @NonNull
    @POST("/api/v1/kronos/socialevent/registrations/resend")
    Call<EventResendResponse> resendVerificationCode(@Query("email") String email);

    @NonNull
    @GET("/api/v1/pegasus/social-events")
    Call<SocialEventListResponse> findSocialEvents();
}
