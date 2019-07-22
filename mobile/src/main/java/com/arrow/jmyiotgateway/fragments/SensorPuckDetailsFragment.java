package com.arrow.jmyiotgateway.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
 * Created by osminin on 6/9/2016.
 */
public class SensorPuckDetailsFragment extends AbstractDetailsFragment {

    @BindView(R.id.sensor_puck_details_temp)
    TextView mTemperature;
    @BindView(R.id.sensor_puck_details_humidity)
    TextView mHumidity;
    @BindView(R.id.sensor_puck_details_light)
    TextView mLight;
    @BindView(R.id.sensor_puck_details_uv)
    TextView mUv;
    @BindView(R.id.sensor_puck_details_heart)
    TextView mHeartRate;
    @BindView(R.id.sensor_puck_details_device_id)
    TextView mId;

    @BindString(R.string.device_details_percents)
    String mPercents;
    @BindString(R.string.device_details_lx)
    String mLx;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_sensor_puck_details, container, false);
        mUnbinder = ButterKnife.bind(this, mRootView);
        setInitialStates();
        return mRootView;
    }

    @Override
    protected void onTelemetryDataChanged(Map<String, String> parametersMap) {
        formatAndSetTextWithDefault(parametersMap.get(TelemetriesNames.HEART_RATE), "", DEFAULT_ZERO_LABEL, mHeartRate);
        formatAndSetTextWithDefault(parametersMap.get(TelemetriesNames.UV), "", DEFAULT_NONE_LABEL, mUv);
        formatAndSetTextWithDefault(parametersMap.get(TelemetriesNames.LIGHT), mLx, DEFAULT_QUATRO_ZERO_LABEL, mLight);

        formatAndSetTextWithDefault(parametersMap.get(TelemetriesNames.TEMPERATURE), mDegreeSign, DEFAULT_DOUBLE_ZERO_LABEL, mTemperature);
        formatAndSetTextWithDefault(parametersMap.get(TelemetriesNames.HUMIDITY), mPercents, DEFAULT_DOUBLE_ZERO_LABEL, mHumidity);
    }

    @Override
    protected DeviceType getDeviceType() {
        return DeviceType.SensorPuck;
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
        mTemperature.setText(String.format("00%s", mDegreeSign));
        mHumidity.setText("00");
        mLight.setText("0000");
        mHeartRate.setText("0");
        mUv.setText("NONE");
    }

    @Override
    public String getTitle(Context context) {
        return mContext.getString(R.string.cards_sensor_puck_title);
    }
}
