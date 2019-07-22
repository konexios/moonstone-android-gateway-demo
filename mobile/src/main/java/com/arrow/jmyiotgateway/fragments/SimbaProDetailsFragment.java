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
import com.arrow.jmyiotgateway.device.simbapro.SimbaPro;

import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by osminin on 12.01.2018.
 */

public final class SimbaProDetailsFragment extends AbstractDetailsFragment {

    @BindView(R.id.simba_pro_details_temperature)
    TextView mTemperature;
    @BindView(R.id.simba_pro_details_humidity)
    TextView mHumidity;
    @BindView(R.id.simba_pro_details_light)
    TextView mLight;
    @BindView(R.id.simba_pro_details_pressure)
    TextView mPressure;
    @BindView(R.id.simba_pro_details_mic_lvl)
    TextView mMicLevel;
    @BindView(R.id.simba_pro_details_device_id)
    TextView mId;
    @BindView(R.id.simba_pro_details_accelerometer_x)
    TextView mAccelerometerX;
    @BindView(R.id.simba_pro_details_accelerometer_y)
    TextView mAccelerometerY;
    @BindView(R.id.simba_pro_details_accelerometer_z)
    TextView mAccelerometerZ;
    @BindView(R.id.simba_pro_details_gyroscope_x)
    TextView mGyroscopeX;
    @BindView(R.id.simba_pro_details_gyroscope_y)
    TextView mGyroscopeY;
    @BindView(R.id.simba_pro_details_gyroscope_z)
    TextView mGyroscopeZ;
    @BindView(R.id.simba_pro_details_magnetometer_x)
    TextView mMagnetometerX;
    @BindView(R.id.simba_pro_details_magnetometer_y)
    TextView mMagnetometerY;
    @BindView(R.id.simba_pro_details_magnetometer_z)
    TextView mMagnetometerZ;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_simba_pro_details, container, false);
        mUnbinder = ButterKnife.bind(this, mRootView);
        setInitialStates();
        return mRootView;
    }

    @Override
    public String getTitle(Context context) {
        return mContext.getString(R.string.cards_simba_pro_title);
    }
/*
    this method is used for showing OTA btn
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        View settings = ButterKnife.findById(mRootView.getRootView(), R.id.bottom_sheet_config);
        View actions = ButterKnife.findById(mRootView.getRootView(), R.id.bottom_sheet_action);
        RelativeLayout.LayoutParams actionsLp = (RelativeLayout.LayoutParams) actions.getLayoutParams();
        actionsLp.removeRule(CENTER_IN_PARENT);
        actionsLp.addRule(RIGHT_OF, settings.getId());
        actions.setLayoutParams(actionsLp);
        View dashboard = ButterKnife.findById(mRootView.getRootView(), R.id.bottom_sheet_stat);
        RelativeLayout.LayoutParams dashboardLp = (RelativeLayout.LayoutParams) dashboard.getLayoutParams();
        dashboardLp.removeRule(ALIGN_PARENT_END);
        dashboardLp.removeRule(ALIGN_PARENT_RIGHT);
        dashboardLp.addRule(RIGHT_OF, actions.getId());
        dashboard.setLayoutParams(dashboardLp);
        View ota = ButterKnife.findById(mRootView.getRootView(), R.id.bottom_sheet_ota);
        ota.setVisibility(View.VISIBLE);
        ota.setOnClickListener(v -> openOtaScreen());
    }*/

/*
    this method is used for hiding OTA button
    @Override
    public void onDestroyView() {
        View actions = ButterKnife.findById(mRootView.getRootView(), R.id.bottom_sheet_action);
        RelativeLayout.LayoutParams actionsLp = (RelativeLayout.LayoutParams) actions.getLayoutParams();
        actionsLp.removeRule(RIGHT_OF);
        actionsLp.addRule(CENTER_IN_PARENT, TRUE);
        actions.setLayoutParams(actionsLp);

        View dashboard = ButterKnife.findById(mRootView.getRootView(), R.id.bottom_sheet_stat);
        RelativeLayout.LayoutParams dashboardLp = (RelativeLayout.LayoutParams) dashboard.getLayoutParams();
        dashboardLp.removeRule(RIGHT_OF);
        dashboardLp.addRule(ALIGN_PARENT_END, TRUE);
        dashboardLp.addRule(ALIGN_PARENT_RIGHT, TRUE);
        dashboard.setLayoutParams(dashboardLp);
        View ota = ButterKnife.findById(mRootView.getRootView(), R.id.bottom_sheet_ota);
        ota.setVisibility(View.GONE);
        ota.setOnClickListener(null);
        super.onDestroyView();
    }*/

    @Override
    protected void onTelemetryDataChanged(Map<String, String> parametersMap) {
        formatAndSetText(parametersMap.get(TelemetriesNames.LIGHT), mLux, mLight);
        formatAndSetText(parametersMap.get(TelemetriesNames.TEMPERATURE), mDegreeSign + "F", mTemperature);
        formatAndSetText(parametersMap.get(TelemetriesNames.HUMIDITY), "%RH", mHumidity);
        formatAndSetTextWithDefault(parametersMap.get(TelemetriesNames.PRESSURE), mPressureUnit, DEFAULT_DOUBLE_ZERO_LABEL, mPressure);
        formatAndSetTextWithDefault(parametersMap.get(TelemetriesNames.MIC_LEVEL), "", DEFAULT_ZERO_LABEL, mMicLevel);

        formatAndSetTextWithDefault(parametersMap.get(TelemetriesNames.ACCELEROMETER_X), mMetersPerSquareSecond, DEFAULT_ZERO_LABEL, mAccelerometerX);
        formatAndSetTextWithDefault(parametersMap.get(TelemetriesNames.ACCELEROMETER_Y), mMetersPerSquareSecond, DEFAULT_ZERO_LABEL, mAccelerometerY);
        formatAndSetTextWithDefault(parametersMap.get(TelemetriesNames.ACCELEROMETER_Z), mMetersPerSquareSecond, DEFAULT_ZERO_LABEL, mAccelerometerZ);

        formatAndSetTextWithDefault(parametersMap.get(TelemetriesNames.MAGNETOMETER_X), mMagnetUnit, DEFAULT_ZERO_LABEL, mMagnetometerX);
        formatAndSetTextWithDefault(parametersMap.get(TelemetriesNames.MAGNETOMETER_Y), mMagnetUnit, DEFAULT_ZERO_LABEL, mMagnetometerY);
        formatAndSetTextWithDefault(parametersMap.get(TelemetriesNames.MAGNETOMETER_Z), mMagnetUnit, DEFAULT_ZERO_LABEL, mMagnetometerZ);

        formatAndSetTextWithDefault(parametersMap.get(TelemetriesNames.GYROMETER_X), mGyroUnit, DEFAULT_ZERO_LABEL, mGyroscopeX);
        formatAndSetTextWithDefault(parametersMap.get(TelemetriesNames.GYROMETER_Y), mGyroUnit, DEFAULT_ZERO_LABEL, mGyroscopeY);
        formatAndSetTextWithDefault(parametersMap.get(TelemetriesNames.GYROMETER_Z), mGyroUnit, DEFAULT_ZERO_LABEL, mGyroscopeZ);
    }

    @Override
    protected DeviceType getDeviceType() {
        return DeviceType.SimbaPro;
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
        mGyroscopeX.setText("-");
        mGyroscopeY.setText("-");
        mGyroscopeZ.setText("-");
        mMagnetometerX.setText("-");
        mMagnetometerY.setText("-");
        mMagnetometerZ.setText("-");
        mLight.setText("0000");
        mTemperature.setText(DEFAULT_ZERO_LABEL);
        mPressure.setText(DEFAULT_ZERO_LABEL);
        mMicLevel.setText(DEFAULT_ZERO_LABEL);
        mHumidity.setText(DEFAULT_DOUBLE_ZERO_LABEL);
    }

    private void openOtaScreen() {
        if (!mDevice.isMonitoring()) {
            return;
        }
        SimbaOTAFragment fragment = new SimbaOTAFragment();
        fragment.setDevice((SimbaPro) mDevice);
        openFragment(fragment, null);
    }

    @Override
    protected BaseFragment getConfigFragment() {
        return new SimbaProConfigFragment();
    }
}
