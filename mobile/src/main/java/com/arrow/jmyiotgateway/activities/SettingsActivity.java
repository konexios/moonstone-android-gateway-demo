package com.arrow.jmyiotgateway.activities;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.arrow.jmyiotgateway.Constant;
import com.arrow.jmyiotgateway.R;
import com.arrow.jmyiotgateway.device.msband.MsBand;
import com.microsoft.band.sensors.HeartRateConsentListener;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by osminin on 4/4/2016.
 */
public class SettingsActivity extends ActivityAbstract implements CompoundButton.OnCheckedChangeListener,
        HeartRateConsentListener {
    private final static String TAG = SettingsActivity.class.getSimpleName();
    private static final int MAX_DIGITS_COUNT = 4;
    @BindView(R.id.settings_sending_rate_edit)
    TextView mSendingRate;

    @BindView((R.id.settings_gps_switch))
    SwitchCompat mGpsSwitch;

    @BindView(R.id.settings_heart_rate_switch)
    SwitchCompat mHeartRateSwitch;

    @BindView(R.id.settings_coordinator)
    CoordinatorLayout mCoordinatorLayout;

    @BindView(R.id.settings_heartbeat_interval_edit)
    TextView mHeartbeatInterval;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    private Subscription mHrRequestSubscription;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        hideImageLogo();

        setInitialStates();
        mGpsSwitch.setOnCheckedChangeListener(this);
        mHeartRateSwitch.setOnCheckedChangeListener(this);
    }

    private void setInitialStates() {
        Log.d(TAG, "setInitialStates");
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        mGpsSwitch.setChecked(sp.getBoolean(Constant.SP_LOCATION_SERVICE, false));

        mSendingRate.setText(Integer.toString(sp.getInt(Constant.SP_SENDING_RATE, Constant.DEFAULT_DEVICE_POLLING_INTERVAL)));

        mHeartbeatInterval.setText(Integer.toString(sp.getInt(Constant.SP_CLOUD_HERATBEAT_INTERVAL, Constant.HEART_BEAT_INTERVAL)));

        boolean isChecked = sp.getBoolean(Constant.SP_MSBAND_HEART_RATE, false);
        mHeartRateSwitch.setChecked(isChecked);
        mHeartRateSwitch.setEnabled(!isChecked);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Log.d(TAG, "onCheckedChanged");
        switch (buttonView.getId()) {
            case R.id.settings_gps_switch:
                putBooleanToPreferences(Constant.SP_LOCATION_SERVICE, isChecked);
                break;
            case R.id.settings_heart_rate_switch:
                if (isChecked) {
                    mHeartRateSwitch.setEnabled(false);
                    enableMsBandHeartRate();
                }
                break;
        }
    }

    @OnClick(R.id.settings_sending_rate_edit)
    void sendingRateClicked() {
        Log.d(TAG, "sendingRateClicked");
        showInputDialog(mSendingRate, Constant.SP_SENDING_RATE, R.string.settings_sending_rate_label);
    }

    @OnClick(R.id.settings_heartbeat_interval_edit)
    void heartbeatClicked() {
        showInputDialog(mHeartbeatInterval, Constant.SP_CLOUD_HERATBEAT_INTERVAL,
                R.string.settings_heartbeat_interval_label);
    }

    private void showInputDialog(final TextView textView, final String key, int title) {
        final EditText input = new EditText(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MAX_DIGITS_COUNT)});
        input.setLayoutParams(lp);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setText(textView.getText());
        AlertDialog alertDialog = new AlertDialog.Builder(this, R.style.AppTheme_AlertDialog)
                .setTitle(title)
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Integer value = Integer.parseInt(input.getText().toString());
                        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this);
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putInt(key, value);
                        editor.commit();
                        textView.setText(value.toString());
                    }
                })
                .create();
        alertDialog.setView(input);
        alertDialog.show();
    }

    private void putBooleanToPreferences(String key, boolean value) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    @Override
    public void userAccepted(boolean isAccepted) {
        Log.d(TAG, "userAccepted");
        putBooleanToPreferences(Constant.SP_MSBAND_HEART_RATE, isAccepted);
        mHeartRateSwitch.setChecked(isAccepted);
        mHeartRateSwitch.setEnabled(!isAccepted);
    }

    public void onHeartRateRequestFailed() {
        Log.d(TAG, "onHeartRateRequestFailed");
        showErrorMessage(R.string.settings_failed_to_connect);
        mHeartRateSwitch.setChecked(false);
        mHeartRateSwitch.setEnabled(true);
    }

    public void heartRateAlreadyAccepted() {
        Log.d(TAG, "heartRateAlreadyAccepted");
        showErrorMessage(R.string.settings_heart_rate_accepted);
    }

    private void showErrorMessage(int messageId) {
        Snackbar snackbar = Snackbar.make(mCoordinatorLayout, messageId, Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    private void enableMsBandHeartRate() {
        mHrRequestSubscription = Observable.create(new Observable.OnSubscribe<MsBand>() {
            @Override
            public void call(Subscriber<? super MsBand> subscriber) {
                MsBand msBand = new MsBand(SettingsActivity.this, 0, false, "");
                msBand.requestHeartRateConsent(SettingsActivity.this);
                subscriber.onNext(msBand);
                subscriber.onCompleted();
            }
        })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<MsBand>() {

                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "onCompleted");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "onError");
                        mHeartRateSwitch.setChecked(false);
                        mHeartRateSwitch.setEnabled(true);
                    }

                    @Override
                    public void onNext(MsBand msBand) {
                        msBand.disable(0);
                    }

                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mHrRequestSubscription != null && !mHrRequestSubscription.isUnsubscribed()) {
            mHrRequestSubscription.unsubscribe();
        }
    }
}
