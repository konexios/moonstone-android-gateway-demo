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
 * Created by osminin on 9/6/2016.
 */

public final class SensorTileDetailsFragment extends AbstractDetailsFragment {
    @BindView(R.id.sensor_tile_details_device_id)
    TextView mId;
    @BindView(R.id.sensor_tile_details_accelerometerX)
    TextView mAccelerometerX;
    @BindView(R.id.sensor_tile_details_accelerometerY)
    TextView mAccelerometerY;
    @BindView(R.id.sensor_tile_details_accelerometerZ)
    TextView mAccelerometerZ;
    @BindView(R.id.sensor_tile_details_magnetometer_x)
    TextView mMagnometerX;
    @BindView(R.id.sensor_tile_details_magnetometer_y)
    TextView mMagnometerY;
    @BindView(R.id.sensor_tile_details_magnetometer_z)
    TextView mMagnometerZ;
    @BindView(R.id.sensor_tile_details_gyroscope_x)
    TextView mGyrometerX;
    @BindView(R.id.sensor_tile_details_gyroscope_y)
    TextView mGyrometerY;
    @BindView(R.id.sensor_tile_details_gyroscope_z)
    TextView mGyrometerZ;

    @BindView(R.id.sensor_tile_details_amb_temp)
    TextView mAmbientTemperature;
    @BindView(R.id.sensor_tile_details_surf_temp)
    TextView mSurfaceTemperature;
    @BindView(R.id.sensor_tile_details_hum)
    TextView mHumidity;
    @BindView(R.id.sensor_tile_details_pressure)
    TextView mPressure;
    @BindView(R.id.sensor_tile_details_mic_lvl)
    TextView mMicLevel;
    @BindView(R.id.sensor_tile_details_switch)
    TextView mSwitchTelemetry;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_sensor_tile_details, container, false);
        mUnbinder = ButterKnife.bind(this, mRootView);
        setInitialStates();
        return mRootView;
    }

    @Override
    protected void onTelemetryDataChanged(Map<String, String> parametersMap) {
        formatAndSetTextWithDefault(parametersMap.get(TelemetriesNames.ACCELEROMETER_X), mMetersPerSquareSecond, DEFAULT_ZERO_LABEL, mAccelerometerX);
        formatAndSetTextWithDefault(parametersMap.get(TelemetriesNames.ACCELEROMETER_Y), mMetersPerSquareSecond, DEFAULT_ZERO_LABEL, mAccelerometerY);
        formatAndSetTextWithDefault(parametersMap.get(TelemetriesNames.ACCELEROMETER_Z), mMetersPerSquareSecond, DEFAULT_ZERO_LABEL, mAccelerometerZ);

        formatAndSetTextWithDefault(parametersMap.get(TelemetriesNames.MAGNETOMETER_X), mMagnetUnit, DEFAULT_ZERO_LABEL, mMagnometerX);
        formatAndSetTextWithDefault(parametersMap.get(TelemetriesNames.MAGNETOMETER_Y), mMagnetUnit, DEFAULT_ZERO_LABEL, mMagnometerY);
        formatAndSetTextWithDefault(parametersMap.get(TelemetriesNames.MAGNETOMETER_Z), mMagnetUnit, DEFAULT_ZERO_LABEL, mMagnometerZ);

        formatAndSetTextWithDefault(parametersMap.get(TelemetriesNames.GYROMETER_X), mGyroUnit, DEFAULT_ZERO_LABEL, mGyrometerX);
        formatAndSetTextWithDefault(parametersMap.get(TelemetriesNames.GYROMETER_Y), mGyroUnit, DEFAULT_ZERO_LABEL, mGyrometerY);
        formatAndSetTextWithDefault(parametersMap.get(TelemetriesNames.GYROMETER_Z), mGyroUnit, DEFAULT_ZERO_LABEL, mGyrometerZ);

        formatAndSetTextWithDefault(parametersMap.get(TelemetriesNames.TEMPERATURE), mDegreeSign, DEFAULT_ZERO_LABEL, mAmbientTemperature);
        formatAndSetTextWithDefault(parametersMap.get(TelemetriesNames.IR_TEMPERATURE), mDegreeSign, DEFAULT_ZERO_LABEL, mSurfaceTemperature);
        formatAndSetTextWithDefault(parametersMap.get(TelemetriesNames.HUMIDITY), "%", DEFAULT_ZERO_LABEL, mHumidity);

        formatAndSetTextWithDefault(parametersMap.get(TelemetriesNames.PRESSURE), mPressureUnit, DEFAULT_ZERO_LABEL, mPressure);
        formatAndSetTextWithDefault(parametersMap.get(TelemetriesNames.MIC_LEVEL), "", DEFAULT_ZERO_LABEL, mMicLevel);
        formatAndSetTextWithDefault(parametersMap.get(TelemetriesNames.SWITCH), "", DEFAULT_ZERO_LABEL, mSwitchTelemetry);
    }

    @Override
    protected DeviceType getDeviceType() {
        return DeviceType.SensorTile;
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
        mMagnometerX.setText("-");
        mMagnometerY.setText("-");
        mMagnometerZ.setText("-");
        mGyrometerX.setText("-");
        mGyrometerY.setText("-");
        mGyrometerZ.setText("-");
        mAmbientTemperature.setText("-");
        mSurfaceTemperature.setText("-");
        mHumidity.setText("-");
        mPressure.setText("-");
        mMicLevel.setText("-");
        mSwitchTelemetry.setText("-");
    }

    @Override
    public String getTitle(Context context) {
        return mContext.getString(R.string.cards_sensor_tile_title);
    }

    @Override
    protected BaseFragment getConfigFragment() {
        return new SensorTileConfigFragment();
    }
}
