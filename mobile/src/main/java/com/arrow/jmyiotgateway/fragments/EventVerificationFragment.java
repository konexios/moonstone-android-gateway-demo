package com.arrow.jmyiotgateway.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextSwitcher;
import android.widget.TextView;

import com.arrow.acn.api.models.ApiError;
import com.arrow.jmyiotgateway.Constant;
import com.arrow.jmyiotgateway.miramonti.acn.EventAcnApiService;
import com.arrow.jmyiotgateway.miramonti.acn.EventSimpleResponseListener;
import com.arrow.jmyiotgateway.R;
import com.arrow.jmyiotgateway.Util;
import com.arrow.jmyiotgateway.activities.EventRegistrationActivity;
import com.arrow.jmyiotgateway.activities.MainActivity;
import com.arrow.jmyiotgateway.miramonti.acn.models.eventModel.EventResendResponse;
import com.arrow.jmyiotgateway.miramonti.acn.models.eventModel.EventVerifyResponse;
import com.arrow.jmyiotgateway.miramonti.acn.eventServiceHolders.EventRegistrationAcnServiceHolder;
import com.google.firebase.crash.FirebaseCrash;

import org.parceler.Parcels;

import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.util.Log.VERBOSE;
import static com.arrow.jmyiotgateway.Constant.CONFIG_EXTRA_INFO;

/**
 * Created by batrakov on 12.01.18.
 */

public class EventVerificationFragment extends BaseFragment implements View.OnClickListener {

    private static final String TAG = EventVerificationFragment.class.getName();

    private EventAcnApiService mEventRegistrationAcnApiService;

    @BindView(R.id.verification_code_field)
    EditText mVerificationCodeField;

    @BindView(R.id.verify_code_button)
    Button mVerifyButton;

    @BindView(R.id.verification_email_field)
    EditText mVerificationEmailField;

    @BindView(R.id.resend_code_button)
    Button mResendCodeButton;

    @BindView(R.id.verify_error_label)
    TextView mVerifyErrorTextView;

    @BindView(R.id.verify_error_img)
    ImageView mVerifyErrorImageView;

    private String mZoneSystemName;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_event_verification, container, false);
        mUnbinder = ButterKnife.bind(this, mRootView);
        mVerifyErrorImageView.setVisibility(View.GONE);
        mVerifyErrorTextView.setVisibility(View.GONE);
        SharedPreferences sharedPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);
        mZoneSystemName = sharedPreferences.getString(Constant.ZONE_SYSTEM_NAME, "");
        return mRootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        restoreConfigFromSharedPref();
        createAcnApiService();
        mVerifyButton.setOnClickListener(this);
        mResendCodeButton.setOnClickListener(this);

    }

    @Override
    public String getTitle(Context context) {
        return "";
    }

    @Override
    public void onClick(View aView) {
        final WeakReference<EventVerificationFragment> currentFragmentReference = new WeakReference<>(this);
        EventVerificationFragment fragment = currentFragmentReference.get();
        if (fragment != null) {
            fragment.mVerifyErrorTextView.setVisibility(View.GONE);
            fragment.mVerifyErrorImageView.setVisibility(View.GONE);
        }

        switch (aView.getId()) {
            case R.id.verify_code_button:
                String verificationCode = mVerificationCodeField.getText().toString();
                if (TextUtils.isEmpty(verificationCode)) {
                    if (fragment != null) {
                        fragment.mVerifyErrorTextView.setText(R.string.please_type_your_verification_code);
                        fragment.mVerifyErrorTextView.setVisibility(View.VISIBLE);
                        fragment.mVerifyErrorImageView.setVisibility(View.VISIBLE);
                    }
                } else {
                    String dialogText = getString(R.string.fragment_verification_process);
                    final ProgressDialog dialog = ProgressDialog.show(mContext, null, dialogText, false);
                    dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    mVerifyErrorImageView.setVisibility(View.GONE);
                    mVerifyErrorTextView.setVisibility(View.GONE);
                    mEventRegistrationAcnApiService.verifyEventAccount(verificationCode, new EventSimpleResponseListener<EventVerifyResponse>() {
                        @Override
                        public void onRequestSuccess(EventVerifyResponse response) {
                            EventRegistrationActivity.sEventConfig.setUserId(response.getUserHid());
                            EventRegistrationActivity.sEventConfig.setApplicationHid(response.getApplicationHid());
                            EventRegistrationActivity.sEventConfig.setActive(true);
                            EventRegistrationActivity.sEventConfig.save(mContext);

                            Intent intent = new Intent(mContext, MainActivity.class);
                            intent.putExtra(CONFIG_EXTRA_INFO, Parcels.wrap(EventRegistrationActivity.sEventConfig));
                            dialog.dismiss();
                            startActivity(intent);
                            ((Activity) mContext).finish();
                        }

                        @Override
                        public void onRequestError(ApiError error) {
                            FirebaseCrash.logcat(VERBOSE, TAG, "onFailure");
                            dialog.dismiss();
                            EventVerificationFragment fragment = currentFragmentReference.get();
                            if (fragment != null) {
                                fragment.mVerifyErrorTextView.setText(error.getMessage());
                                fragment.mVerifyErrorTextView.setVisibility(View.VISIBLE);
                                fragment.mVerifyErrorImageView.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                }
                break;
            case R.id.resend_code_button:
                String email = mVerificationEmailField.getText().toString();
                if (TextUtils.isEmpty(email)) {
                    mVerificationEmailField.setError("Email required");
                } else {
                    String dialogText = getString(R.string.fragment_resending_process);
                    final ProgressDialog dialog = ProgressDialog.show(mContext, null, dialogText, false);
                    dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    mEventRegistrationAcnApiService.resendVerificationCode(email, new EventSimpleResponseListener<EventResendResponse>() {
                        @Override
                        public void onRequestSuccess(EventResendResponse response) {
                            dialog.dismiss();
                            if (fragment != null) {
                                fragment.mVerifyErrorTextView.setText(R.string.verification_code_has_been_sent);
                                fragment.mVerifyErrorTextView.setVisibility(View.VISIBLE);
                            }
                        }

                        @Override
                        public void onRequestError(ApiError error) {
                            FirebaseCrash.logcat(VERBOSE, TAG, "onFailure");
                            dialog.dismiss();
                            EventVerificationFragment fragment = currentFragmentReference.get();
                            if (fragment != null) {
                                fragment.mVerifyErrorTextView.setText(error.getMessage());
                                fragment.mVerifyErrorTextView.setVisibility(View.VISIBLE);
                                fragment.mVerifyErrorImageView.setVisibility(View.VISIBLE);
                            }

                        }
                    });
                    break;
                }
        }
    }

    private void restoreConfigFromSharedPref() {
        SharedPreferences sharedPreferences = getActivity().getPreferences(Context.MODE_PRIVATE);

        String name = sharedPreferences.getString(Constant.NAME, "");
        if (!name.equals("")) {
            EventRegistrationActivity.sEventConfig.setName(name);
        }

        String email = sharedPreferences.getString(Constant.EMAIL, "");
        if (!email.equals("")) {
            EventRegistrationActivity.sEventConfig.setEmail(email);
        }

        String code = sharedPreferences.getString(Constant.CODE, "");
        if (!code.equals("")) {
            EventRegistrationActivity.sEventConfig.setCode(code);
        }

        String password = sharedPreferences.getString(Constant.PASSWORD, "");
        if (!password.equals("")) {
            EventRegistrationActivity.sEventConfig.setPassword(password);
        }

        String selectedEvent = sharedPreferences.getString(Constant.SELECTED_EVENT, "");
        if (!selectedEvent.equals("")) {
            EventRegistrationActivity.sEventConfig.setSelectedEvent(selectedEvent);
        }

        String profileName = sharedPreferences.getString(Constant.PROFILE_NAME, "");
        if (!profileName.equals("")) {
            EventRegistrationActivity.sEventConfig.setProfileName(profileName);
        }

        String serverEnvironment = sharedPreferences.getString(Constant.SERVER__ENVIRONMENT, "");
        if (!serverEnvironment.equals("")) {
            EventRegistrationActivity.sEventConfig.setServerEnvironment(serverEnvironment);
        }

        if (!mZoneSystemName.equals("")) {
            EventRegistrationActivity.sEventConfig.setZoneSystemName(mZoneSystemName);
        }
    }

    private void createAcnApiService() {
        mEventRegistrationAcnApiService = EventRegistrationAcnServiceHolder.createService(mContext, EventRegistrationActivity.sEventConfig);
    }
}
