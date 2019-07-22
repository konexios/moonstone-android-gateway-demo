package com.arrow.jmyiotgateway.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import com.arrow.acn.api.AcnApi;
import com.arrow.acn.api.AcnApiService;
import com.arrow.acn.api.listeners.RegisterAccount2Listener;
import com.arrow.acn.api.models.AccountRequest2;
import com.arrow.acn.api.models.AccountResponse2;
import com.arrow.acn.api.models.ApiError;
import com.arrow.jmyiotgateway.AcnServiceHolder;
import com.arrow.jmyiotgateway.Config;
import com.arrow.jmyiotgateway.Constant;
import com.arrow.jmyiotgateway.R;
import com.arrow.jmyiotgateway.Util;
import com.arrow.jmyiotgateway.activities.MainActivity;
import com.google.firebase.crash.FirebaseCrash;

import org.parceler.Parcels;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.util.Log.VERBOSE;
import static com.arrow.jmyiotgateway.Constant.CONFIG_EXTRA_INFO;
import static com.arrow.jmyiotgateway.Constant.DEFAULT_API_KEY;
import static com.arrow.jmyiotgateway.Constant.DEFAULT_API_SECRET;
import static com.arrow.jmyiotgateway.activities.AccountActivity.IS_LAUNCHED;

/**
 * Created by osminin on 01.11.2016.
 */

public final class NewAccountFragment extends BaseFragment {
    private final static String TAG = NewAccountFragment.class.getSimpleName();

    @BindView((R.id.editTextSignInProfileName))
    TextView mTextViewProfileName;
    @BindView(R.id.editTextSignInName)
    TextView mTextViewName;
    @BindView(R.id.editTextSignInEmail)
    TextView mTextViewEmail;
    @BindView(R.id.editTextSignInPassword)
    TextView mTextViewPassword;
    @BindView(R.id.editTextSignInCode)
    TextView mTextViewCode;
    @BindView(R.id.button_register)
    Button mButtonRegister;
    @BindView(R.id.account_dev_mode_switch)
    Switch mDemoModeSwitch;

    private Config mConfig;
    private boolean mRegistered = false;
    private boolean isLaunched;

    private AcnApiService mAcnApiService;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FirebaseCrash.logcat(VERBOSE, TAG, "onCreateView");
        mRootView = inflater.inflate(R.layout.fragment_new_account, container, false);
        mUnbinder = ButterKnife.bind(this, mRootView);
        return mRootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        FirebaseCrash.logcat(VERBOSE, TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);
        mConfig = Parcels.unwrap(getArguments().getParcelable(CONFIG_EXTRA_INFO));
        isLaunched = getArguments().getBoolean(IS_LAUNCHED, false);
        createAcnApiService();
        initView();
    }

    @Override
    public void onDestroyView() {
        InputMethodManager imm = (InputMethodManager)
                mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mRootView.getWindowToken(), 0);
        super.onDestroyView();
    }

    @Override
    public String getTitle(Context context) {
        return context.getString(R.string.title_activity_account);
    }

    @OnClick(R.id.button_register)
    public void buttonRegisterClicked() {
        FirebaseCrash.logcat(Log.INFO, TAG, "buttonRegisterClicked() ...");

        if (mRegistered && !mConfig.getActive()) {
            AcnServiceHolder.setApiKey(DEFAULT_API_KEY);
            AcnServiceHolder.setApiSecretKey(DEFAULT_API_SECRET);
            Config current = new Config().loadActive(mContext);
            current.setActive(false);
            current.save(mContext);
            FirebaseCrash.logcat(Log.INFO, TAG, "save: " + current.getEmail() + current.getActive());
            mConfig.setActive(true);
            mConfig.save(mContext);
            FirebaseCrash.logcat(Log.INFO, TAG, "save: " + mConfig.getEmail() + mConfig.getActive());
            Intent intent = new Intent();
            intent.putExtra(CONFIG_EXTRA_INFO, Parcels.wrap(mConfig));
            ((Activity) mContext).setResult(Activity.RESULT_OK, intent);
            ((Activity) mContext).finish();
            return;
        }

        boolean validated = true;

        final String name = mTextViewName.getText().toString();
        if (TextUtils.isEmpty((name))) {
            mTextViewName.setError("Name is required");
            validated = false;
        }

        final String email = mTextViewEmail.getText().toString();
        if (TextUtils.isEmpty(email)) {
            mTextViewEmail.setError("Email is required");
            validated = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mTextViewEmail.setError("Email is invalid");
            validated = false;
        }

        final String password = mTextViewPassword.getText().toString();
        if (TextUtils.isEmpty(password)) {
            mTextViewPassword.setError("Password is required");
            validated = false;
        }

        final String code = mTextViewCode.getText().toString();
        if (TextUtils.isEmpty(code)) {
            mTextViewCode.setError("Application Code is required");
            validated = false;
        }

        if (validated) {
            String dialogText = mRegistered ? getString(R.string.fragment_new_account_logging) :
                    getString(R.string.fragment_new_account_registering);
            final ProgressDialog dialog = ProgressDialog.show(mContext, null, dialogText, false);
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            AccountRequest2 model = new AccountRequest2();
            model.setUsername(email.toLowerCase());
            model.setPassword(password);
            model.setApplicationCode(code);
            mAcnApiService.registerAccount2(model, new RegisterAccount2Listener() {
                @Override
                public void onAccountRegistered(AccountResponse2 accountResponse) {
                    Config config = new Config().loadActive(mContext);
                    if (!TextUtils.isEmpty(config.getUserId())) {
                        config.setActive(false);
                        config.save(mContext);
                        FirebaseCrash.logcat(Log.INFO, TAG, "save: " + config.getEmail() + config.getActive());
                    }
                    mConfig.setUserId(accountResponse.getUserHid());
                    mConfig.setName(name);
                    mConfig.setEmail(email);
                    mConfig.setCode(code);
                    mConfig.setPassword(password);
                    mConfig.setApplicationHid(accountResponse.getApplicationHid());
                    mConfig.setZoneSystemName(accountResponse.getZoneSystemName());
                    mConfig.setActive(true);

                    String profileName = mTextViewProfileName.getText().toString();
                    if (TextUtils.isEmpty(profileName)) {
                        profileName = mContext.getString(R.string.fragment_new_account_default_profile);
                    }
                    mConfig.setProfileName(profileName);
                    String environment;
                    if (mDemoModeSwitch.isChecked()) {
                        environment = "Production";
                    } else {
                        environment = "Development";
                    }
                    mConfig.setServerEnvironment(environment);

                    String newEndpoint;
                    if (mConfig.getServerEnvironment().equals("Development")) {
                        newEndpoint = Constant.IOT_CONNECT_URL_DEV.replace("<zone-name>"
                                , accountResponse.getZoneSystemName());
                    } else {
                        newEndpoint = Constant.IOT_CONNECT_URL_DEMO.replace("<zone-name>"
                                , accountResponse.getZoneSystemName());
                    }
                    AcnApi.Builder.resetRestEndpoint(newEndpoint);


                    mConfig.save(mContext);
                    FirebaseCrash.logcat(Log.INFO, TAG, "save: " + mConfig.getEmail() + mConfig.getActive());
                    Intent intent = new Intent(mContext, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(CONFIG_EXTRA_INFO, Parcels.wrap(mConfig));
                    dialog.dismiss();
                    if (isLaunched) {
                        startActivity(intent);
                    } else {
                        intent = new Intent();
                        intent.putExtra(CONFIG_EXTRA_INFO, Parcels.wrap(mConfig));
                        ((Activity) mContext).setResult(Activity.RESULT_OK, intent);
                    }
                    FirebaseCrash.logcat(Log.INFO, TAG, "account registered, ApplicationHid: " +
                            accountResponse.getApplicationHid());
                    ((Activity) mContext).finish();
                }

                @Override
                public void onAccountRegisterFailed(ApiError error) {
                    FirebaseCrash.logcat(VERBOSE, TAG, "onFailure");
                    if  (mContext == null) {
                        return;
                    }
                    dialog.dismiss();
                    Util.showSimpleAlertDialog(mContext, "Registration Failed", "Code: " + error.getStatus() +
                            ", " + error.getMessage());
                }
            });
        }
    }

    private void initView() {
        FirebaseCrash.logcat(VERBOSE, TAG, "initView");
        mRegistered = !TextUtils.isEmpty(mConfig.getUserId());
        if (mRegistered) {
            mTextViewName.setText(mConfig.getName());
            mTextViewEmail.setText(mConfig.getEmail());
            mTextViewPassword.setText(mConfig.getPassword());
            mTextViewCode.setText(mConfig.getCode());
            mTextViewProfileName.setText(mConfig.getProfileName());
            if (mConfig.getServerEnvironment().equals("Production")) {
                mDemoModeSwitch.setChecked(true);
            } else {
                mDemoModeSwitch.setChecked(false);
            }
            if (!mConfig.getActive()) {
                mButtonRegister.setText(R.string.fragment_accounts_set_active);
            }
            mDemoModeSwitch.setClickable(false);
            if (isLaunched) {
                buttonRegisterClicked();
            } else {
                mTextViewName.setEnabled(false);
                mTextViewEmail.setEnabled(false);
                mTextViewPassword.setEnabled(false);
                mTextViewCode.setEnabled(false);
                mTextViewProfileName.setEnabled(false);
                mButtonRegister.setEnabled(!mConfig.getActive());
                if (mConfig.getActive()) {
                    mButtonRegister.setVisibility(View.GONE);
                } else {
                    mButtonRegister.setVisibility(View.VISIBLE);
                }
            }
        } else {
            mRegistered = false;
            mTextViewName.setEnabled(true);
            mTextViewName.requestFocus();
            mTextViewEmail.setEnabled(true);
            mTextViewPassword.setEnabled(true);
            mTextViewCode.setEnabled(true);
            mButtonRegister.setEnabled(true);
            mDemoModeSwitch.setClickable(true);
            mTextViewProfileName.setEnabled(true);
            mTextViewProfileName.setText(getString(R.string.fragment_new_account_default_profile)
                    + " " + (Config.loadAll(mContext).size() + 1));
        }
    }

    private void createAcnApiService() {
        mAcnApiService = AcnServiceHolder.createService(mContext.getApplicationContext(), mConfig);
    }

    @OnClick(R.id.account_dev_mode_switch)
    public void onSwitched() {
        if (mDemoModeSwitch.isChecked()) {
            mConfig.setServerEnvironment("Production");
        } else {
            mConfig.setServerEnvironment("Development");
        }
        createAcnApiService();
    }
}
