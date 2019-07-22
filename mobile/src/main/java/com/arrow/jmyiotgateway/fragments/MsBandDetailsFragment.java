package com.arrow.jmyiotgateway.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.arrow.jmyiotgateway.R;
import com.arrow.jmyiotgateway.device.DeviceType;
import com.arrow.jmyiotgateway.device.TelemetriesNames;

import java.util.Map;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by osminin on 4/1/2016.
 */
public class MsBandDetailsFragment extends AbstractDetailsFragment {
    @BindView(R.id.ms_band_details_steps)
    TextView mSteps;
    @BindView(R.id.ms_band_details_distance)
    TextView mDistance;

    @BindView(R.id.ms_band_details_accelerometerX)
    TextView mAccelerometerX;
    @BindView(R.id.ms_band_details_accelerometerY)
    TextView mAccelerometerY;
    @BindView(R.id.ms_band_details_accelerometerZ)
    TextView mAccelerometerZ;

    @BindView(R.id.ms_band_details_gyroscopeX)
    TextView mGyroscopeX;
    @BindView(R.id.ms_band_details_gyroscopeY)
    TextView mGyroscopeY;
    @BindView(R.id.ms_band_details_gyroscopeZ)
    TextView mGyroscopeZ;

    @BindView(R.id.ms_band_details_heart_rate)
    TextView mHeartRate;
    /*    @BindView(R.id.ms_band_details_timer)
        TextView mTimer;*/
    @BindView(R.id.ms_band_details_skin_temp)
    TextView mSkinTemp;
    @BindView(R.id.ms_band_details_uv)
    TextView mUv;
    @BindView(R.id.ms_band_details_device_id)
    TextView mId;

    @BindString(R.string.device_details_foots)
    String mFootsUnit;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_ms_band_details, container, false);
        mUnbinder = ButterKnife.bind(this, mRootView);
        setInitialStates();
        return mRootView;
    }

    @Override
    protected void setInitialStates() {
        mSteps.setText("-");
        mDistance.setText("-");

        mAccelerometerX.setText("-");
        mAccelerometerY.setText("-");
        mAccelerometerZ.setText("-");

        mGyroscopeX.setText("-");
        mGyroscopeY.setText("-");
        mGyroscopeZ.setText("-");

        mHeartRate.setText(DEFAULT_ZERO_LABEL);
        mSkinTemp.setText(DEFAULT_ZERO_LABEL);
        mUv.setText(DEFAULT_NONE_LABEL);
    }

    @Override
    protected void onTelemetryDataChanged(Map<String, String> parametersMap) {
        formatAndSetTextWithDefault(parametersMap.get(TelemetriesNames.STEPS), mStepsUnit, DEFAULT_ZERO_LABEL, mSteps);
        formatAndSetTextWithDefault(parametersMap.get(TelemetriesNames.DISTANCE), mFootsUnit, DEFAULT_ZERO_LABEL, mDistance);

        formatAndSetTextWithDefault(parametersMap.get(TelemetriesNames.ACCELEROMETER_X), mMetersPerSquareSecond, DEFAULT_ZERO_LABEL, mAccelerometerX);
        formatAndSetTextWithDefault(parametersMap.get(TelemetriesNames.ACCELEROMETER_Y), mMetersPerSquareSecond, DEFAULT_ZERO_LABEL, mAccelerometerY);
        formatAndSetTextWithDefault(parametersMap.get(TelemetriesNames.ACCELEROMETER_Z), mMetersPerSquareSecond, DEFAULT_ZERO_LABEL, mAccelerometerZ);

        formatAndSetTextWithDefault(parametersMap.get(TelemetriesNames.GYROSCOPE_X), mGyroUnit, DEFAULT_ZERO_LABEL, mGyroscopeX);
        formatAndSetTextWithDefault(parametersMap.get(TelemetriesNames.GYROSCOPE_Y), mGyroUnit, DEFAULT_ZERO_LABEL, mGyroscopeY);
        formatAndSetTextWithDefault(parametersMap.get(TelemetriesNames.GYROSCOPE_Z), mGyroUnit, DEFAULT_ZERO_LABEL, mGyroscopeZ);

        formatAndSetTextWithDefault(parametersMap.get(TelemetriesNames.HEART_RATE), "", DEFAULT_ZERO_LABEL, mHeartRate);
        if (!TextUtils.isEmpty(parametersMap.get(TelemetriesNames.SKIN_TEMP))) {
            formatAndSetTextWithDefault(parametersMap.get(TelemetriesNames.SKIN_TEMP), "", DEFAULT_ZERO_LABEL, mSkinTemp);
        }
        formatAndSetTextWithDefault(parametersMap.get(TelemetriesNames.UV), "", DEFAULT_NONE_LABEL, mUv);
    }

    @Override
    protected DeviceType getDeviceType() {
        return DeviceType.MicrosoftBand;
    }

    @Override
    protected void updateTimer(String time) {
        /*if(mTimer != null) {
            mTimer.setText(time);
        }*/
    }

    @Override
    protected void setTextDeviceId(String id) {
        mId.setText(" " + id);
    }

    @Override
    public String getTitle(Context context) {
        return context.getString(R.string.cards_ms_band_title);
    }

    @Override
    protected BaseFragment getConfigFragment() {
        return new MsBandConfigFragment();
    }
}
