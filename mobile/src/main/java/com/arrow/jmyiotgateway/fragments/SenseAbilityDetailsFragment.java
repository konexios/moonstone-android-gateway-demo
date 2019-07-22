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

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by osminin on 22.07.2016.
 */

public class SenseAbilityDetailsFragment extends AbstractDetailsFragment {
    @BindView(R.id.senseability_details_temperature)
    TextView mTemperature;
    @BindView(R.id.senseability_details_humidity)
    TextView mHumidity;
    @BindView(R.id.senseability_details_magnet)
    TextView mMagnet;
    @BindView(R.id.senseability_details_pressure)
    TextView mPressure;
    @BindView(R.id.senseability_details_airflow)
    TextView mAirflow;
    @BindView(R.id.senseability_details_device_id)
    TextView mId;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_senseability_details, container, false);
        mUnbinder = ButterKnife.bind(this, mRootView);
        setInitialStates();
        return mRootView;
    }

    @Override
    protected void onTelemetryDataChanged(Map<String, String> parametersMap) {
        formatAndSetTextWithDefault(parametersMap.get(TelemetriesNames.TEMPERATURE), mDegreeSign, DEFAULT_DOUBLE_ZERO_LABEL, mTemperature);
        formatAndSetTextWithDefault(parametersMap.get(TelemetriesNames.HUMIDITY), "%", DEFAULT_DOUBLE_ZERO_LABEL, mHumidity);
        formatAndSetTextWithDefault(parametersMap.get(TelemetriesNames.PRESSURE), mPressureUnit, DEFAULT_DOUBLE_ZERO_LABEL, mPressure);
        formatAndSetTextWithDefault(parametersMap.get(TelemetriesNames.MAGNET), mMagnetUnit, DEFAULT_ZERO_LABEL, mMagnet);
        formatAndSetTextWithDefault(parametersMap.get(TelemetriesNames.AIRFLOW), "", DEFAULT_ZERO_LABEL, mAirflow);
    }

    @Override
    protected DeviceType getDeviceType() {
        return DeviceType.SenseAbilityKit;
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
        mHumidity.setText(DEFAULT_ZERO_LABEL);
        mMagnet.setText(DEFAULT_ZERO_LABEL);

        mPressure.setText(DEFAULT_ZERO_LABEL);
        mAirflow.setText(DEFAULT_ZERO_LABEL);
        mTemperature.setText(DEFAULT_ZERO_LABEL);
    }

    @Override
    public String getTitle(Context context) {
        return mContext.getString(R.string.senseability_details_title);
    }
}
