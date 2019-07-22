package com.arrow.jmyiotgateway.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.arrow.acn.api.listeners.RegisterAccountListener;
import com.arrow.acn.api.models.AccountResponse;
import com.arrow.acn.api.models.ApiError;
import com.arrow.jmyiotgateway.BuildConfig;
import com.arrow.jmyiotgateway.Constant;
import com.arrow.jmyiotgateway.miramonti.acn.EventSimpleResponseListener;
import com.arrow.jmyiotgateway.miramonti.acn.models.eventModel.EventAccountRequest;
import com.arrow.jmyiotgateway.miramonti.acn.EventAcnApiService;
import com.arrow.jmyiotgateway.miramonti.acn.models.eventModel.SocialEventListResponse;
import com.arrow.jmyiotgateway.miramonti.acn.eventServiceHolders.EventAcnServiceHolder;
import com.arrow.jmyiotgateway.miramonti.acn.eventServiceHolders.EventRegistrationAcnServiceHolder;
import com.arrow.jmyiotgateway.R;
import com.arrow.jmyiotgateway.miramonti.acn.models.eventModel.SocialEventResponse;
import com.arrow.jmyiotgateway.Util;
import com.arrow.jmyiotgateway.activities.EventRegistrationActivity;
import com.google.firebase.crash.FirebaseCrash;

import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.util.Log.VERBOSE;
import static com.arrow.jmyiotgateway.Constant.DEV_ENVIRONMENT;

/**
 * Created by batrakov on 12.01.18.
 */

public class EventRegistrationFragment extends BaseFragment implements View.OnClickListener {

    private static final String TAG = EventRegistrationActivity.class.getName();

    private EventAcnApiService mEventRegistrationAcnApiService;
    private EventAcnApiService mEventAcnApiService;

    @BindView(R.id.confirm_registration_event_button)
    Button mConfirmButton;

    @BindView(R.id.back_to_event_choice_button)
    Button mBackButton;

    @BindView(R.id.name_field_event)
    EditText mNameField;

    @BindView(R.id.email_field_event)
    EditText mEmailField;

    @BindView(R.id.password_field_event)
    EditText mPasswordField;

    @BindView(R.id.repassword_field_event)
    EditText mRePasswordField;

    @BindView(R.id.application_code_field)
    EditText mApplicationCodeField;

    @BindView(R.id.register_error_img)
    ImageView mRegisterErrorImageView;

    @BindView(R.id.register_error_label)
    TextView mRegisterErrorTextView;

    private String mSocialEventHid = "none";
    private String mSelectedEvent = "none";
    private String mZoneSystemName = "";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_event_registration, container, false);
        mUnbinder = ButterKnife.bind(this, mRootView);
        mBackButton.setOnClickListener(this);
        mConfirmButton.setOnClickListener(this);
        return mRootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mRegisterErrorImageView.setVisibility(View.GONE);
        mRegisterErrorTextView.setVisibility(View.GONE);
        if (DEV_ENVIRONMENT) {
            EventRegistrationActivity.sEventConfig.setServerEnvironment("Development");
        } else {
            EventRegistrationActivity.sEventConfig.setServerEnvironment("Production");
        }

        mSelectedEvent = getActivity().getPreferences(Context.MODE_PRIVATE)
                .getString(Constant.Preference.KEY_SELECTED_EVENT, "nothing");
        createAcnApiService();

        mEventAcnApiService.findSocialEvents(new EventSimpleResponseListener<SocialEventListResponse>() {
            @Override
            public void onRequestSuccess(SocialEventListResponse response) {
                FirebaseCrash.logcat(VERBOSE, TAG, "onSuccess");
                for (SocialEventResponse socialEventResponse :
                        response.getData()) {
                    if (socialEventResponse.getName().equals(mSelectedEvent)) {
                        mSocialEventHid = socialEventResponse.getHid();
                        mZoneSystemName = socialEventResponse.getZoneSystemName();
                        EventRegistrationActivity.sEventConfig.setZoneSystemName(mZoneSystemName);
                        mEventRegistrationAcnApiService = EventRegistrationAcnServiceHolder.createService(mContext, EventRegistrationActivity.sEventConfig);
                        mConfirmButton.setEnabled(true);
                    }
                }
            }

            @Override
            public void onRequestError(ApiError error) {
                FirebaseCrash.logcat(VERBOSE, TAG, "onFailure");
                if  (mContext == null) {
                    return;
                }
                Util.showSimpleAlertDialog(mContext, "Event choosing failed", "Code: " + error.getStatus() +
                        ", " + error.getMessage());
            }
        });
    }

    @Override
    public String getTitle(Context context) {
        return "";
    }

    @Override
    public void onClick(View aView) {
        mRegisterErrorImageView.setVisibility(View.GONE);
        mRegisterErrorTextView.setVisibility(View.GONE);
        switch (aView.getId()) {
            case R.id.back_to_event_choice_button:
                ((AppCompatActivity) mContext).onBackPressed();
                break;
            case R.id.confirm_registration_event_button:
                boolean validated = true;
                final String name = mNameField.getText().toString();
                if (TextUtils.isEmpty((name))) {
                    mNameField.setError("Name is required");
                    validated = false;
                }

                final String email = mEmailField.getText().toString();
                if (TextUtils.isEmpty(email)) {
                    mEmailField.setError("Email is required");
                    validated = false;
                } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    mEmailField.setError("Email is invalid");
                    validated = false;
                }

                final String password = mPasswordField.getText().toString();
                final String rePassword = mRePasswordField.getText().toString();
                if (TextUtils.isEmpty(password)) {
                    mPasswordField.setError("Password is required");
                    validated = false;
                } else if (!password.equals(rePassword)) {
                    mRePasswordField.setError("Passwords do not match ");
                    validated = false;
                }

                final String code = mApplicationCodeField.getText().toString();
                if (TextUtils.isEmpty(code)) {
                    mApplicationCodeField.setError("Event Code is required");
                    validated = false;
                }

                if (validated && mEventRegistrationAcnApiService != null) {
                    String dialogText = getString(R.string.fragment_new_account_registering);
                    final ProgressDialog dialog = ProgressDialog.show(mContext, null, dialogText, false);
                    dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);

                    EventAccountRequest model = new EventAccountRequest();
                    model.setName(name);
                    model.setEmail(email.toLowerCase());
                    model.setPassword(password);
                    model.setApplicationCode(code);
                    model.setSocialEventHid(mSocialEventHid);

                    final WeakReference<EventRegistrationFragment> currentFragmentReference = new WeakReference<>(this);
                    mEventRegistrationAcnApiService.registerEventAccount(model, new RegisterAccountListener() {
                        @Override
                        public void onAccountRegistered(AccountResponse accountResponse) {
                            FirebaseCrash.logcat(VERBOSE, TAG, "onSuccess");

                            SharedPreferences preferences = getActivity().getPreferences(Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = preferences.edit();

                            EventRegistrationActivity.sEventConfig.setName(name);
                            editor.putString(Constant.NAME, name);
                            EventRegistrationActivity.sEventConfig.setEmail(email);
                            editor.putString(Constant.EMAIL, email);
                            EventRegistrationActivity.sEventConfig.setCode(code);
                            editor.putString(Constant.CODE, code);
                            EventRegistrationActivity.sEventConfig.setPassword(password);
                            editor.putString(Constant.PASSWORD, password);
                            EventRegistrationActivity.sEventConfig.setActive(true);
                            EventRegistrationActivity.sEventConfig.setSelectedEvent(getActivity().getPreferences(Context.MODE_PRIVATE)
                                    .getString(Constant.Preference.KEY_SELECTED_EVENT, "nothing"));
                            editor.putString(Constant.SELECTED_EVENT, EventRegistrationActivity.sEventConfig.getSelectedEvent());
                            String profileName = mContext.getString(R.string.fragment_new_account_default_profile);
                            EventRegistrationActivity.sEventConfig.setProfileName(profileName);
                            editor.putString(Constant.PROFILE_NAME, profileName);
                            EventRegistrationActivity.sEventConfig.setZoneSystemName(mZoneSystemName);
                            editor.putString(Constant.ZONE_SYSTEM_NAME, mZoneSystemName);
                            editor.putString(Constant.SERVER__ENVIRONMENT, EventRegistrationActivity.sEventConfig.getServerEnvironment());

                            editor.apply();

                            FirebaseCrash.logcat(Log.INFO, TAG, "save: "
                                    + EventRegistrationActivity.sEventConfig.getEmail()
                                    + EventRegistrationActivity.sEventConfig.getActive());
                            BaseFragment fragment = new EventVerificationFragment();
                            android.support.v4.app.FragmentManager fragmentManager = ((AppCompatActivity) mContext).
                                    getSupportFragmentManager();
                            FragmentTransaction transaction = fragmentManager.beginTransaction();
                            transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
                            transaction.replace(R.id.event_frame, fragment);
                            transaction.addToBackStack(currentFragmentReference.get().getTitle(mContext));
                            transaction.commit();
                            dialog.dismiss();
                        }

                        @Override
                        public void onAccountRegisterFailed(ApiError error) {
                            FirebaseCrash.logcat(VERBOSE, TAG, "onFailure");
                            dialog.dismiss();
                            if (error.getMessage().equals(getString(R.string.existing_account_msg))) {
                                BaseFragment fragment = new EventAccountExistFragment();
                                FragmentManager fragmentManager = ((AppCompatActivity) mContext).
                                        getSupportFragmentManager();
                                FragmentTransaction transaction = fragmentManager.beginTransaction();
                                transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
                                transaction.add(R.id.event_frame, fragment);
                                transaction.addToBackStack(currentFragmentReference.get().getTitle(mContext));
                                transaction.commit();
                            } else {
                                mRegisterErrorTextView.setText(error.getMessage());
                                mRegisterErrorImageView.setVisibility(View.VISIBLE);
                                mRegisterErrorTextView.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                }
                break;
        }
    }

    private void createAcnApiService() {
        mEventAcnApiService = EventAcnServiceHolder.createService(mContext, EventRegistrationActivity.sEventConfig);
    }
}
