package com.arrow.jmyiotgateway.fragments;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.arrow.acn.api.AcnApiService;
import com.arrow.acn.api.listeners.CommonRequestListener;
import com.arrow.acn.api.listeners.MessageStatusListener;
import com.arrow.acn.api.models.ApiError;
import com.arrow.acn.api.models.CommonResponse;
import com.arrow.acn.api.models.MessageStatusResponse;
import com.arrow.acn.api.models.NewDeviceStateTransactionRequest;
import com.arrow.acn.api.models.StateModel;
import com.arrow.jmyiotgateway.AcnServiceHolder;
import com.arrow.jmyiotgateway.R;
import com.arrow.jmyiotgateway.device.DeviceType;
import com.arrow.jmyiotgateway.device.TelemetriesNames;
import com.arrow.jmyiotgateway.device.thunderboard.ThunderBoard;
import com.google.firebase.crash.FirebaseCrash;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.Map;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.arrow.jmyiotgateway.Util.getFormattedDateTime;
import static com.arrow.jmyiotgateway.device.TelemetriesNames.LED_0;
import static com.arrow.jmyiotgateway.device.TelemetriesNames.LED_1;

/**
 * Created by osminin on 7/26/2016.
 */

public final class ThunderBoardDetailsFragment extends AbstractDetailsFragment {
    private static final String TAG = ThunderBoardDetailsFragment.class.getCanonicalName();

    @BindView(R.id.thunderboard_details_temperature)
    TextView mTemperature;
    @BindView(R.id.thunderboard_details_humidity)
    TextView mHumidity;
    @BindView(R.id.thunderboard_details_uv)
    TextView mUv;
    @BindView(R.id.thunderboard_details_light)
    TextView mLight;
    @BindView(R.id.thunderboard_details_device_id)
    TextView mId;
    @BindView(R.id.thunderboard_details_accelerometerX)
    TextView mAccelerometerX;
    @BindView(R.id.thunderboard_details_accelerometerY)
    TextView mAccelerometerY;
    @BindView(R.id.thunderboard_details_accelerometerZ)
    TextView mAccelerometerZ;
    @BindView(R.id.thunderboard_details_orientationX)
    TextView mOrientationX;
    @BindView(R.id.thunderboard_details_orientationY)
    TextView mOrientationY;
    @BindView(R.id.thunderboard_details_orientationZ)
    TextView mOrientationZ;
    @BindView(R.id.thunderboard_details_led_0)
    TextView mLed0;
    @BindView(R.id.thunderboard_details_led_1)
    TextView mLed1;

    private boolean isLed0On;
    private boolean isLed1On;

    private AcnApiService mService;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_thunderboard_details, container, false);

        mUnbinder = ButterKnife.bind(this, mRootView);
        setInitialStates();
        mService = AcnServiceHolder.getService();
        return mRootView;
    }

    @Override
    protected void onTelemetryDataChanged(Map<String, String> parametersMap) {
        formatAndSetText(parametersMap.get(TelemetriesNames.TEMPERATURE), mDegreeSign + "F", mTemperature);
        formatAndSetText(parametersMap.get(TelemetriesNames.HUMIDITY), "%RH", mHumidity);
        formatAndSetText(parametersMap.get(TelemetriesNames.UV), "", mUv);
        formatAndSetText(parametersMap.get(TelemetriesNames.LIGHT), mLux, mLight);

        formatAndSetTextWithDefault(parametersMap.get(TelemetriesNames.ACCELEROMETER_X), mMetersPerSquareSecond, DEFAULT_ZERO_LABEL, mAccelerometerX);
        formatAndSetTextWithDefault(parametersMap.get(TelemetriesNames.ACCELEROMETER_Y), mMetersPerSquareSecond, DEFAULT_ZERO_LABEL, mAccelerometerY);
        formatAndSetTextWithDefault(parametersMap.get(TelemetriesNames.ACCELEROMETER_Z), mMetersPerSquareSecond, DEFAULT_ZERO_LABEL, mAccelerometerZ);

        formatAndSetTextWithDefault(parametersMap.get(TelemetriesNames.ORIENTATION_X), "", DEFAULT_ZERO_LABEL, mOrientationX);
        formatAndSetTextWithDefault(parametersMap.get(TelemetriesNames.ORIENTATION_Y), "", DEFAULT_ZERO_LABEL, mOrientationY);
        formatAndSetTextWithDefault(parametersMap.get(TelemetriesNames.ORIENTATION_Z), "", DEFAULT_ZERO_LABEL, mOrientationZ);
    }

    @Override
    protected DeviceType getDeviceType() {
        return DeviceType.ThunderBoard;
    }

    @Override
    protected void updateTimer(String time) {
    }

    @Override
    protected void setTextDeviceId(String id) {
        mId.setText(" " + id);
    }

    @Override
    protected void setInitialStates() {
        mAccelerometerX.setText("-");
        mAccelerometerY.setText("-");
        mAccelerometerZ.setText("-");
        mOrientationX.setText("-");
        mOrientationY.setText("-");
        mOrientationZ.setText("-");
        mLight.setText("0000");
        mUv.setText(DEFAULT_ZERO_LABEL);
        mTemperature.setText(DEFAULT_ZERO_LABEL);
        mHumidity.setText(DEFAULT_DOUBLE_ZERO_LABEL);
        mLed0.setTextColor(Color.WHITE);
        mLed1.setTextColor(Color.WHITE);
        isLed0On = false;
        isLed1On = false;
    }

    @Override
    public String getTitle(Context context) {
        return mContext.getString(R.string.thunderboard_details_title);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        super.onCheckedChanged(buttonView, isChecked);
        if (!isChecked) {
            mPollingService.stopPolling(getDeviceType(), getCardId());
        }
    }

    @Override
    protected BaseFragment getConfigFragment() {
        return new ThunderBoardConfigFragment();
    }

    @OnClick(R.id.thunderboard_details_led_0)
    public void onLed0() {
        isLed0On = !isLed0On;
        boolean result = ((ThunderBoard) mDevice).led0Action(isLed0On);
        if (result) {
            int color = isLed0On ? ContextCompat.getColor(mContext, R.color.thunderboard_led0_on) :
                    Color.WHITE;
            mLed0.setTextColor(color);
        } else {
            isLed0On = !isLed0On;
        }
        changeDeviceState(LED_0, Boolean.toString(isLed0On));
    }

    @OnClick(R.id.thunderboard_details_led_1)
    public void onLed1() {
        isLed1On = !isLed1On;
        boolean result = ((ThunderBoard) mDevice).led1Action(isLed1On);
        if (result) {
            int color = isLed1On ? ContextCompat.getColor(mContext, R.color.thunderboard_led1_on) :
                    Color.WHITE;
            mLed1.setTextColor(color);
        } else {
            isLed1On = !isLed1On;
        }
        changeDeviceState(LED_1, Boolean.toString(isLed1On));
    }

    @Override
    public boolean handleDeviceStateRequest(String state) {
        JsonParser p = new JsonParser();
        Gson gson = new Gson();
        JsonObject result = p.parse(state).getAsJsonObject();
        Set<Map.Entry<String, JsonElement>> entrySet = result.entrySet();
        for (Map.Entry<String, JsonElement> entry : entrySet) {
            String key = entry.getKey();
            if (key.equals(LED_0)) {
                StateModel stateModel = gson.fromJson(entry.getValue(), StateModel.class);
                boolean led0 = Boolean.parseBoolean(stateModel.getValue());
                if (isLed0On != led0) {
                    onLed0();
                }
                continue;
            }
            if (key.equals(LED_1)) {
                StateModel stateModel = gson.fromJson(entry.getValue(), StateModel.class);
                boolean led1 = Boolean.parseBoolean(stateModel.getValue());
                if (isLed1On != led1) {
                    onLed1();
                }
                continue;
            }
        }
        return true;
    }

    private void changeDeviceState(String key, String value) {
        NewDeviceStateTransactionRequest request = new NewDeviceStateTransactionRequest();
        request.addState(key, value);
        request.setTimestamp(getFormattedDateTime(System.currentTimeMillis()));
        mService.createNewDeviceStateTransaction(mDevice.getDeviceHid(), request, new CommonRequestListener() {
            @Override
            public void onRequestSuccess(CommonResponse commonResponse) {
                mService.deviceStateTransactionSucceeded(mDevice.getDeviceHid(),
                        commonResponse.getHid(),
                        new MessageStatusListener() {
                            @Override
                            public void onResponse(MessageStatusResponse messageStatusResponse) {
                                FirebaseCrash.logcat(Log.INFO, TAG, "deviceStateTransactionComplete ok");
                            }

                            @Override
                            public void onError(ApiError apiError) {
                                FirebaseCrash.logcat(Log.ERROR, TAG, "deviceStateTransactionComplete error");
                            }
                        });
            }

            @Override
            public void onRequestError(ApiError apiError) {
                FirebaseCrash.logcat(Log.ERROR, TAG, "createNewDeviceStateTransaction error");
            }
        });
    }
}
