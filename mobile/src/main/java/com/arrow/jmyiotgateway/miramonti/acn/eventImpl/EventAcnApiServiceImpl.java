package com.arrow.jmyiotgateway.miramonti.acn.eventImpl;

import android.support.annotation.NonNull;

import com.arrow.acn.api.listeners.RegisterAccountListener;
import com.arrow.acn.api.models.AccountResponse;
import com.arrow.acn.api.models.ApiError;
import com.arrow.jmyiotgateway.miramonti.acn.EventAcnApiService;
import com.arrow.jmyiotgateway.miramonti.acn.EventIotConnectApiService;
import com.arrow.jmyiotgateway.miramonti.acn.EventSimpleResponseListener;
import com.arrow.jmyiotgateway.miramonti.acn.models.eventModel.EventAccountRequest;
import com.arrow.jmyiotgateway.miramonti.acn.models.eventModel.EventResendResponse;
import com.arrow.jmyiotgateway.miramonti.acn.models.eventModel.EventVerifyResponse;
import com.arrow.jmyiotgateway.miramonti.acn.models.eventModel.SocialEventListResponse;
import com.arrow.jmyiotgateway.miramonti.common.ErrorUtils;
import com.arrow.jmyiotgateway.miramonti.common.EventRetrofitHolder;

import java.net.HttpURLConnection;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

/**
 * Created by batrakov on 18.01.18.
 */

public class EventAcnApiServiceImpl implements EventAcnApiService {

    private final EventRetrofitHolder mRetrofitHolder;
    private EventIotConnectApiService mRestService;

    public EventAcnApiServiceImpl(EventRetrofitHolder retrofitHolder) {
        mRetrofitHolder = retrofitHolder;
    }

    public void setRestEndpoint(@NonNull String endpoint, @NonNull String apiKey, @NonNull String apiSecret) {
        Timber.d("setRestEndpoint");
        mRetrofitHolder.setDefaultApiKey(apiKey);
        mRetrofitHolder.setDefaultApiSecret(apiSecret);
        mRetrofitHolder.setSecretKey(null);
        mRetrofitHolder.setApiKey(null);
        mRestService = mRetrofitHolder.getIotConnectAPIService(endpoint);
    }


    @Override
    public void registerEventAccount(EventAccountRequest aEventAccountRequest, @NonNull final RegisterAccountListener listener) {
        Timber.d("registerAccount() email: " + aEventAccountRequest.getEmail()
                + ", code: " + aEventAccountRequest.getEventCode());
        Call<AccountResponse> call = mRestService.registerEventAccount(aEventAccountRequest);
        call.enqueue(new Callback<AccountResponse>() {
            @Override
            public void onResponse(Call<AccountResponse> call, @NonNull Response<AccountResponse> response) {
                Timber.d("registerAccount: " + response.code());
                try {
                    if (response.body() != null && response.code() == HttpURLConnection.HTTP_OK) {
                        listener.onAccountRegistered(response.body());
                    } else {
                        ApiError error = mRetrofitHolder.convertToApiError(response);
                        listener.onAccountRegisterFailed(error);
                    }
                } catch (Exception e) {
                    listener.onAccountRegisterFailed(ErrorUtils.parseError(e));
                    e.printStackTrace();
                    Timber.e(e);
                }
            }

            @Override
            public void onFailure(Call<AccountResponse> call, Throwable t) {
                listener.onAccountRegisterFailed(ErrorUtils.parseError(t));
                Timber.e("registerAccount() failed");
                Timber.e(t);
            }
        });
    }

    @Override
    public void verifyEventAccount(String verificationCode, final EventSimpleResponseListener<EventVerifyResponse> listener) {
        Call<EventVerifyResponse> call = mRestService.verifyEventAccount(verificationCode);
        call.enqueue(new Callback<EventVerifyResponse>() {
            @Override
            public void onResponse(Call<EventVerifyResponse> call, @NonNull Response<EventVerifyResponse> response) {
                Timber.d("registerAccount: " + response.code());
                try {
                    if (response.body() != null && response.code() == HttpURLConnection.HTTP_OK) {
                        listener.onRequestSuccess(response.body());
                    } else {
                        ApiError error = mRetrofitHolder.convertToApiError(response);
                        listener.onRequestError(error);
                    }
                } catch (Exception e) {
                    listener.onRequestError(ErrorUtils.parseError(e));
                    e.printStackTrace();
                    Timber.e(e);
                }
            }

            @Override
            public void onFailure(Call<EventVerifyResponse> call, Throwable t) {
                listener.onRequestError(ErrorUtils.parseError(t));
                Timber.e("verifyAccount() failed");
                Timber.e(t);
            }
        });
    }

    @Override
    public void resendVerificationCode(String email, final EventSimpleResponseListener<EventResendResponse> listener) {
        Call<EventResendResponse> call = mRestService.resendVerificationCode(email);
        call.enqueue(new Callback<EventResendResponse>() {
            @Override
            public void onResponse(Call<EventResendResponse> call, @NonNull Response<EventResendResponse> response) {
                Timber.d("registerAccount: " + response.code());
                try {
                    if (response.body() != null && response.code() == HttpURLConnection.HTTP_OK) {
                        listener.onRequestSuccess(response.body());
                    } else {
                        ApiError error = mRetrofitHolder.convertToApiError(response);
                        listener.onRequestError(error);
                    }
                } catch (Exception e) {
                    listener.onRequestError(ErrorUtils.parseError(e));
                    e.printStackTrace();
                    Timber.e(e);
                }
            }

            @Override
            public void onFailure(Call<EventResendResponse> call, Throwable t) {
                listener.onRequestError(ErrorUtils.parseError(t));
                Timber.e("verifyAccount() failed");
                Timber.e(t);
            }
        });
    }


    @Override
    public void findSocialEvents(@NonNull final EventSimpleResponseListener<SocialEventListResponse> aEventsRequestListener) {
        Call<SocialEventListResponse> call = mRestService.findSocialEvents();
        call.enqueue(new Callback<SocialEventListResponse>() {
            @Override
            public void onResponse(Call<SocialEventListResponse> call, @NonNull Response<SocialEventListResponse> response) {
                Timber.d("registerAccount: " + response.code());
                try {
                    if (response.body() != null && response.code() == HttpURLConnection.HTTP_OK) {
                        aEventsRequestListener.onRequestSuccess(response.body());
                    } else {
                        ApiError error = mRetrofitHolder.convertToApiError(response);
                        aEventsRequestListener.onRequestError(error);
                    }
                } catch (Exception e) {
                    aEventsRequestListener.onRequestError(ErrorUtils.parseError(e));
                    e.printStackTrace();
                    Timber.e(e);
                }
            }

            @Override
            public void onFailure(Call<SocialEventListResponse> call, Throwable t) {
                aEventsRequestListener.onRequestError(ErrorUtils.parseError(t));
                Timber.e("registerAccount() failed");
                Timber.e(t);
            }
        });
    }
}
