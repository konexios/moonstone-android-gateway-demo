package com.arrow.jmyiotgateway.fragments;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.arrow.jmyiotgateway.Constant;
import com.arrow.jmyiotgateway.R;
import com.arrow.jmyiotgateway.activities.ActivityAbstract;
import com.arrow.jmyiotgateway.cloud.iot.IotConstant;
import com.arrow.jmyiotgateway.device.DeviceAbstract;
import com.arrow.jmyiotgateway.device.DevicePollingService;
import com.arrow.jmyiotgateway.device.DeviceState;
import com.arrow.jmyiotgateway.device.DeviceType;
import com.arrow.jmyiotgateway.device.TelemetriesNames;
import com.google.firebase.crash.FirebaseCrash;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.parceler.Parcels;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;

import static android.app.Activity.RESULT_OK;
import static android.util.Log.ERROR;
import static com.arrow.jmyiotgateway.Constant.ACTION_IOT_DEVICE_REGISTERED;
import static com.arrow.jmyiotgateway.cloud.iot.IotConstant.EXTRA_DATA_LABEL_CARD_ID;
import static com.arrow.jmyiotgateway.fragments.ActionsFragment.DEVICE_HID_BUNDLE;
import static com.arrow.jmyiotgateway.fragments.ActionsFragment.DEVICE_NAME_BUNDLE;

/**
 * Created by osminin on 3/31/2016.
 */
public abstract class AbstractDetailsFragment extends BaseFragment implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {
    protected static final String DEFAULT_QUATRO_ZERO_LABEL = "0000";
    protected static final String DEFAULT_DOUBLE_ZERO_LABEL = "00";
    protected static final String DEFAULT_ZERO_LABEL = "0";
    protected static final String DEFAULT_NONE_LABEL = "none";
    protected static final Integer REQ_BT_ENABLE = 1;
    private static final int TELEMETRY_VALUE_MAX_LENGTH = 11;
    private final static String TAG = AbstractDetailsFragment.class.getSimpleName();
    protected DevicePollingService mPollingService;
    protected DeviceAbstract mDevice;
    @BindString(R.string.device_details_degree_sign)
    String mDegreeSign;
    @BindString(R.string.device_details_meters_per_square_second)
    String mMetersPerSquareSecond;
    @BindString(R.string.device_details_steps)
    String mStepsUnit;
    @BindString(R.string.device_details_lx)
    String mLux;
    @BindString(R.string.device_details_gyro)
    String mGyroUnit;
    @BindString(R.string.device_details_magnet_unit)
    String mMagnetUnit;
    @BindString(R.string.device_details_pressure_unit)
    String mPressureUnit;
    @BindView(R.id.device_details_switch)
    SwitchCompat mSwitch;
    @BindView(R.id.details_header)
    TextView mHeader;
    private Map<String, String> mParametersMap = new HashMap<>();
    private Gson mGson = new Gson();
    private Timer mTimer;
    private Long mStartTime;
    private Handler mUiHandler;
    private long mCardId;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Constant.ACTION_IOT_DEVICE_STATE_CHANGED)
                    || intent.getAction().equals(ACTION_IOT_DEVICE_REGISTERED)) {
                handleIncomingIotData(intent);
            }
        }
    };

    protected abstract void onTelemetryDataChanged(Map<String, String> parametersMap);

    protected abstract DeviceType getDeviceType();

    protected abstract void updateTimer(String time);

    protected abstract void setTextDeviceId(String id);

    protected abstract void setInitialStates();

    public long getCardId() {
        return mCardId;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null && bundle.containsKey(EXTRA_DATA_LABEL_CARD_ID)) {
            mCardId = bundle.getLong(EXTRA_DATA_LABEL_CARD_ID);
        } else {
            FirebaseCrash.logcat(ERROR, TAG, "no id found");
        }
        registerReceiver();
        mUiHandler = new Handler(mContext.getMainLooper());
        if (mDevice == null && mPollingService != null) {
            mDevice = mPollingService.getDevice(getDeviceType(), mCardId);
        }
        View settings = ButterKnife.findById(mRootView.getRootView(), R.id.bottom_sheet_config);
        settings.setOnClickListener(this);
        View actions = ButterKnife.findById(mRootView.getRootView(), R.id.bottom_sheet_action);
        actions.setOnClickListener(this);
        View dashboard = ButterKnife.findById(mRootView.getRootView(), R.id.bottom_sheet_stat);
        dashboard.setOnClickListener(this);
        /*if (bundle != null && bundle.containsKey(ACTION_IOT_DEVICE_REGISTERED)) {
            Intent intent = getArguments().getParcelable(ACTION_IOT_DEVICE_REGISTERED);
            String deviceHid = Parcels.unwrap(intent.getParcelableExtra(IotConstant.EXTRA_DATA_LABEL_DEVICE_ID));
            setTextDeviceId(deviceHid);
            Config config = new Config().loadActive(mContext);
            for (Config.ConfigDeviceModel deviceModel : config.getAddedDevices(mContext)) {
                if (deviceHid.equals(deviceModel.getDeviceHid())) {
                    mHeader.setText(deviceModel.getDeviceName());
                    break;
                }
            }
        } else {
            Config config = new Config().loadActive(mContext);
            if (!TextUtils.isEmpty(mDevice.getDeviceHid())) {
                setTextDeviceId(mDevice.getDeviceHid());
                for (Config.ConfigDeviceModel deviceModel : config.getAddedDevices(mContext)) {
                    if (mDevice.getDeviceHid().equals(deviceModel.getDeviceHid())) {
                        mHeader.setText(deviceModel.getDeviceName());
                        break;
                    }
                }

            } else {
                //sets default name
                for (Config.ConfigDeviceModel deviceModel : config.getAddedDevices(mContext)) {
                    if (mDevice.getDeviceType().equals(deviceModel.getDeviceType())) {
                        mHeader.setText(deviceModel.getDeviceName());
                        break;
                    }
                }
            }
        }*/
        if (TextUtils.isEmpty(mHeader.getText())) {
            mHeader.setText(getTitle(mContext));
        }
        if (!TextUtils.isEmpty(mDevice.getDeviceUId())) {
            setTextDeviceId(mDevice.getDeviceUId());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mDevice == null && mPollingService == null) {
            ((Activity) mContext).onBackPressed();
        } else {
            mSwitch.setOnCheckedChangeListener(this);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mSwitch.setOnCheckedChangeListener(null);
    }

    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mReceiver);
        if (mDevice != null) {
            mDevice.detachDevice();
            mDevice = null;
        }
        super.onDestroy();
    }

    public void setDevice(DeviceAbstract device) {
        mDevice = device;
    }

    protected void formatAndSetTextWithDefault(String value, String unit, String defaultVal, TextView textView) {
        if (textView != null) {
            if (!TextUtils.isEmpty(value)) {
                value = value.substring(0, Math.min(TELEMETRY_VALUE_MAX_LENGTH, value.length()));
                textView.setText(String.format("%s %s", value, unit));
            } else {
                textView.setText(String.format("%s %s", defaultVal, unit));
            }
        }
    }

    protected void formatAndSetText(String value, String unit, TextView textView) {
        if (textView != null) {
            if (!TextUtils.isEmpty(value)) {
                value = value.substring(0, Math.min(TELEMETRY_VALUE_MAX_LENGTH, value.length()));
                textView.setText(String.format("%s %s", value, unit));
            }
        }
    }

    public void setCardId(long cardId) {
        mCardId = cardId;
    }

    public void updateUi(final Intent intent) {
        mUiHandler.post(new Runnable() {
            @Override
            public void run() {
                handleIncomingIotData(intent);
            }
        });
    }

    private void handleIncomingIotData(Intent intent) {
        if (mContext == null) {
            return;
        }
        if (intent != null && intent.hasExtra(IotConstant.EXTRA_DATA_LABEL_TELEMETRY_BUNDLE)) {
            Bundle bundle = intent.getBundleExtra(IotConstant.EXTRA_DATA_LABEL_TELEMETRY_BUNDLE);
            DeviceType deviceType = Parcels.unwrap(bundle.getParcelable(IotConstant.EXTRA_DATA_LABEL_DEVICE_TYPE));
            String deviceId = bundle.getString(IotConstant.EXTRA_DATA_LABEL_DEVICE_ID);
            if (deviceType == getDeviceType()) {
                if (mTimer == null) {
                    mTimer = new Timer();
                    mStartTime = bundle.getLong(IotConstant.EXTRA_DATA_LABEL_DEVICE_ONLINE);
                    mTimer.schedule(new UpdateTimeTask(), 100, 200);
                    if (!mSwitch.isChecked()) {
                        mSwitch.setOnCheckedChangeListener(null);
                        mSwitch.setChecked(true);
                        mSwitch.setOnCheckedChangeListener(this);
                    }
                }
                String jsonData = bundle.getString(IotConstant.EXTRA_DATA_LABEL_TELEMETRY);
                Type type = new TypeToken<Map<String, String>>() {
                }.getType();
                mParametersMap = mGson.fromJson(jsonData, type);
                onTelemetryDataChanged(mParametersMap);
                mParametersMap.clear();
            }
        } else if (intent != null && intent.getAction().equals(Constant.ACTION_IOT_DEVICE_STATE_CHANGED)) {
            DeviceState deviceState = Parcels.unwrap(intent.getParcelableExtra(IotConstant.EXTRA_DATA_LABEL_DEVICE_STATE));
            DeviceType deviceType = Parcels.unwrap(intent.getParcelableExtra(IotConstant.EXTRA_DATA_LABEL_DEVICE_TYPE));
            if (deviceType == getDeviceType()) {
                boolean isChecked = deviceState != DeviceState.Disconnected &&
                        deviceState != DeviceState.Bound &&
                        deviceState != DeviceState.Disconnecting &&
                        deviceState != DeviceState.Error;
                mSwitch.setOnCheckedChangeListener(null);
                mSwitch.setChecked(isChecked);
                mSwitch.setOnCheckedChangeListener(this);
                if (!isChecked) {
                    if (mTimer != null) {
                        mTimer.cancel();
                        mTimer.purge();
                        mTimer = null;
                    }
                    setInitialStates();
                }
            }
        } else if (intent.getAction().equals(ACTION_IOT_DEVICE_REGISTERED)) {
            DeviceType deviceType = Parcels.unwrap(intent.getParcelableExtra(IotConstant.EXTRA_DATA_LABEL_DEVICE_TYPE));
            String deviceHid = Parcels.unwrap(intent.getParcelableExtra(IotConstant.EXTRA_DATA_LABEL_DEVICE_ID));
            long cardId = intent.getLongExtra(EXTRA_DATA_LABEL_CARD_ID, -1);
            if (!TextUtils.isEmpty(mDevice.getDeviceUId())) {
                setTextDeviceId(mDevice.getDeviceUId());
            }
            if (deviceType == getDeviceType() && cardId == mCardId) {
                //TODO disable this part of code due to we don't have to get names from cloud
                /*AcnApiService apiService = AcnServiceHolder.getService();
                apiService.findDeviceByHid(deviceHid, new FindDeviceListener() {

                    @Override
                    public void onDeviceFindSuccess(DeviceModel device) {
                        if (mRootView != null) {
                            mHeader.setText(device.getName());
                        }
                    }

                    @Override
                    public void onDeviceFindFailed(ApiError error) {
                        FirebaseCrash.logcat(ERROR, TAG, "onDeviceFindFailed");
                    }
                });*/
            }
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        FirebaseCrash.logcat(Log.INFO, TAG, "checked: " + isChecked);
        if (isChecked && !isAbleToConnect()) {
            mSwitch.setChecked(false);
            return;
        }
        if (isChecked) {
            mPollingService.startDevice(getDeviceType(), mCardId);
            mPollingService.startPolling(getDeviceType(), mCardId);
        } else {
            mPollingService.stopDevice(getDeviceType(), mCardId);
        }
    }

    protected boolean isAbleToConnect() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        boolean result = true;
        if (mBluetoothAdapter == null) {
            result = false;
        } else {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQ_BT_ENABLE);
                result = false;
            }
        }
        return result;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_BT_ENABLE && resultCode == RESULT_OK) {
            mSwitch.setChecked(true);
        }
    }

    protected BaseFragment getConfigFragment() {
        return null;
    }

    public void setDevicePollingService(DevicePollingService service) {
        mPollingService = service;
    }

    public boolean handleDeviceStateRequest(String state) {
        return false;
    }

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter(Constant.ACTION_IOT_DATA_RECEIVED);
        filter.addAction(Constant.ACTION_IOT_DEVICE_STATE_CHANGED);
        filter.addAction(ACTION_IOT_DEVICE_REGISTERED);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mReceiver, filter);
    }

    @Override
    public void onClick(View v) {
        BaseFragment fragment = null;
        Bundle bundle = new Bundle();
        switch (v.getId()) {
            case R.id.bottom_sheet_config:
                fragment = getConfigFragment();
                if (fragment != null) {
                    bundle.putParcelable(IotConstant.EXTRA_DATA_LABEL_DEVICE_TYPE, Parcels.wrap(getDeviceType()));
                    bundle.putLong(IotConstant.EXTRA_DATA_LABEL_CARD_ID, mCardId);
                }
                break;
            case R.id.bottom_sheet_action:
                if (mDevice != null) {
                    String deviceHid = mDevice.getDeviceHid();
                    if (!TextUtils.isEmpty(deviceHid)) {
                        fragment = new ActionsFragment();
                        bundle.putString(TelemetriesNames.DEVICE_HID, deviceHid);
                    }
                }
                break;
            case R.id.bottom_sheet_stat:
                if (mDevice != null) {
                    String deviceHid = mDevice.getDeviceHid();
                    if (!TextUtils.isEmpty(deviceHid)) {
                        fragment = new TelemetryDashboardFragment();
                        bundle.putStringArray(IotConstant.EXTRA_DATA_LABEL_DEVICE_TELEMETRY_KEYS, mDevice.getTelemetryNames());
                        bundle.putString(DEVICE_HID_BUNDLE, deviceHid);
                        bundle.putString(DEVICE_NAME_BUNDLE, getTitle(mContext));
                    }
                }
                break;
        }
        openFragment(fragment, bundle);
    }

    protected void openFragment(BaseFragment fragment, Bundle bundle) {
        FragmentManager fragmentManager = ((AppCompatActivity) mContext).
                getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        if (fragment != null &&
                fragmentManager.findFragmentByTag(fragment.getFragmentTag()) == null) {
            if (bundle != null) {
                fragment.setArguments(bundle);
            }
            String tag = fragment.getFragmentTag();
            BaseFragment lastFragment = ((ActivityAbstract) mContext).getLastBaseFragment();
            if (lastFragment instanceof BaseConfigFragment ||
                    lastFragment instanceof ActionsFragment ||
                    lastFragment instanceof TelemetryDashboardFragment ||
                    lastFragment instanceof NewActionFragment ||
                    lastFragment instanceof HistoricalActionsFragment ||
                    lastFragment instanceof SimbaOTAFragment) {
                fragmentManager.beginTransaction()
                        .remove(lastFragment)
                        .commit();
                fragmentManager.popBackStack();
            }
            transaction.add(R.id.content_frame, fragment, tag);
            transaction.addToBackStack(tag);
            transaction.commit();
            InputMethodManager imm = (InputMethodManager)
                    mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mRootView.getWindowToken(), 0);
        }
    }

    class UpdateTimeTask extends TimerTask {
        private UpdateUiTimerTask mUpdateTimerTask = new UpdateUiTimerTask();

        public void run() {
            long millis = System.currentTimeMillis() - mStartTime;
            int seconds = (int) (millis / 1000) % 60;
            int minutes = (int) ((millis / (1000 * 60)) % 60);
            int hours = (int) ((millis / (1000 * 60 * 60)) % 24);

            final String ms = String
                    .format("%02d:%02d:%02d", hours, minutes, seconds);

            mUpdateTimerTask.setTimeString(ms);
            mUiHandler.post(mUpdateTimerTask);
        }

        private class UpdateUiTimerTask implements Runnable {
            private String mTimeString;

            public void setTimeString(String mTimeString) {
                this.mTimeString = mTimeString;
            }

            @Override
            public void run() {
                updateTimer(mTimeString);
            }
        }
    }
}
