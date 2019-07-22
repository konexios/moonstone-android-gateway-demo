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
import com.arrow.jmyiotgateway.device.android.AndroidSensorUtils;

import java.util.HashSet;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.hardware.Sensor.TYPE_ACCELEROMETER;
import static android.hardware.Sensor.TYPE_AMBIENT_TEMPERATURE;
import static android.hardware.Sensor.TYPE_GYROSCOPE;
import static android.hardware.Sensor.TYPE_GYROSCOPE_UNCALIBRATED;
import static android.hardware.Sensor.TYPE_HEART_RATE;
import static android.hardware.Sensor.TYPE_LIGHT;
import static android.hardware.Sensor.TYPE_MAGNETIC_FIELD;
import static android.hardware.Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED;
import static android.hardware.Sensor.TYPE_PRESSURE;
import static android.hardware.Sensor.TYPE_RELATIVE_HUMIDITY;
import static android.hardware.Sensor.TYPE_STEP_COUNTER;

/**
 * Created by osminin on 6/29/2016.
 */

public final class AndroidInternalDetailsScreen extends AbstractDetailsFragment {

    @BindView(R.id.android_internal_details_device_id)
    TextView mId;

    @BindView(R.id.android_internal_telemetry_layout)
    ViewGroup mTelemetryContainer;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_internal_details, container, false);
        mUnbinder = ButterKnife.bind(this, mRootView);
        return mRootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        addTelemetryFields((LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE));
        setInitialStates();
    }

    @Override
    protected void onTelemetryDataChanged(Map<String, String> parametersMap) {
        for (int i = 0; i < mTelemetryContainer.getChildCount(); ++i) {
            View container = mTelemetryContainer.getChildAt(i);
            int sensorType = (int) container.getTag();
            TextView val1 = ButterKnife.findById(container, R.id.telemetry_val_1);
            TextView val2 = ButterKnife.findById(container, R.id.telemetry_val_2);
            TextView val3 = ButterKnife.findById(container, R.id.telemetry_val_3);
            switch (sensorType) {
                case TYPE_ACCELEROMETER:
                    formatAndSetTextWithDefault(parametersMap.get(TelemetriesNames.ACCELEROMETER_X), mMetersPerSquareSecond, DEFAULT_ZERO_LABEL, val1);
                    formatAndSetTextWithDefault(parametersMap.get(TelemetriesNames.ACCELEROMETER_Y), mMetersPerSquareSecond, DEFAULT_ZERO_LABEL, val2);
                    formatAndSetTextWithDefault(parametersMap.get(TelemetriesNames.ACCELEROMETER_Z), mMetersPerSquareSecond, DEFAULT_ZERO_LABEL, val3);
                    break;
                case TYPE_AMBIENT_TEMPERATURE:
                    formatAndSetTextWithDefault(parametersMap.get(TelemetriesNames.TEMPERATURE), mDegreeSign + "F", DEFAULT_DOUBLE_ZERO_LABEL, val1);
                    break;
                case TYPE_GYROSCOPE:
                case TYPE_GYROSCOPE_UNCALIBRATED:
                    formatAndSetTextWithDefault(parametersMap.get(TelemetriesNames.GYROSCOPE_X), mGyroUnit, DEFAULT_ZERO_LABEL, val1);
                    formatAndSetTextWithDefault(parametersMap.get(TelemetriesNames.GYROSCOPE_Y), mGyroUnit, DEFAULT_ZERO_LABEL, val2);
                    formatAndSetTextWithDefault(parametersMap.get(TelemetriesNames.GYROSCOPE_Z), mGyroUnit, DEFAULT_ZERO_LABEL, val3);
                    break;
                case TYPE_HEART_RATE:
                    formatAndSetTextWithDefault(parametersMap.get(TelemetriesNames.HEART_RATE), "", DEFAULT_ZERO_LABEL, val1);
                    break;
                case TYPE_LIGHT:
                    formatAndSetTextWithDefault(parametersMap.get(TelemetriesNames.LIGHT), mLux, DEFAULT_QUATRO_ZERO_LABEL, val1);
                    break;
                case TYPE_MAGNETIC_FIELD:
                case TYPE_MAGNETIC_FIELD_UNCALIBRATED:
                    formatAndSetTextWithDefault(parametersMap.get(TelemetriesNames.MAGNETOMETER_X), mMagnetUnit, DEFAULT_ZERO_LABEL, val1);
                    formatAndSetTextWithDefault(parametersMap.get(TelemetriesNames.MAGNETOMETER_Y), mMagnetUnit, DEFAULT_ZERO_LABEL, val2);
                    formatAndSetTextWithDefault(parametersMap.get(TelemetriesNames.MAGNETOMETER_Z), mMagnetUnit, DEFAULT_ZERO_LABEL, val3);
                    break;
                case TYPE_PRESSURE:
                    formatAndSetTextWithDefault(parametersMap.get(TelemetriesNames.PRESSURE), mPressureUnit, DEFAULT_DOUBLE_ZERO_LABEL, val1);
                    break;
                case TYPE_RELATIVE_HUMIDITY:
                    formatAndSetTextWithDefault(parametersMap.get(TelemetriesNames.HUMIDITY), "", DEFAULT_DOUBLE_ZERO_LABEL, val1);
                    break;
                case TYPE_STEP_COUNTER:
                    formatAndSetTextWithDefault(parametersMap.get(TelemetriesNames.STEPS), mStepsUnit, DEFAULT_ZERO_LABEL, val1);
                    break;
            }
        }
    }

    @Override
    protected DeviceType getDeviceType() {
        return DeviceType.AndroidInternal;
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
        for (int i = 0; i < mTelemetryContainer.getChildCount(); ++i) {
            View container = mTelemetryContainer.getChildAt(i);
            TextView val1 = ButterKnife.findById(container, R.id.telemetry_val_1);
            TextView val2 = ButterKnife.findById(container, R.id.telemetry_val_2);
            TextView val3 = ButterKnife.findById(container, R.id.telemetry_val_3);
            val1.setText("0");
            if (val2 != null && val3 != null) {
                val2.setText("0");
                val3.setText("0");
            }
        }
    }

    private void addTelemetryFields(LayoutInflater inflater) {
        Integer[] availableSensors = AndroidSensorUtils.getAvailableSensors(mContext);
        mTelemetryContainer.removeAllViews();
        HashSet<Integer> sensorTypes = new HashSet<>();
        for (int sensorType : availableSensors) {
            View container;
            int layoutResId = 0;
            int labelResId = 0;
            switch (sensorType) {
                case TYPE_ACCELEROMETER:
                    layoutResId = R.layout.layout_telemetry_3_values;
                    labelResId = R.string.device_details_accelerometer;
                    break;
                case TYPE_AMBIENT_TEMPERATURE:
                    layoutResId = R.layout.layout_telemetry_1_value;
                    labelResId = R.string.device_details_temperature;
                    break;
                case TYPE_GYROSCOPE:
                case TYPE_GYROSCOPE_UNCALIBRATED:
                    layoutResId = R.layout.layout_telemetry_3_values;
                    labelResId = R.string.device_details_gyroscope;
                    break;
                case TYPE_HEART_RATE:
                    layoutResId = R.layout.layout_telemetry_1_value;
                    labelResId = R.string.device_details_heart_rate;
                    break;
                case TYPE_LIGHT:
                    layoutResId = R.layout.layout_telemetry_1_value;
                    labelResId = R.string.telemetry_param_light;
                    break;
                case TYPE_MAGNETIC_FIELD:
                case TYPE_MAGNETIC_FIELD_UNCALIBRATED:
                    layoutResId = R.layout.layout_telemetry_3_values;
                    labelResId = R.string.device_details_magnetometer;
                    break;
                case TYPE_PRESSURE:
                    layoutResId = R.layout.layout_telemetry_1_value;
                    labelResId = R.string.device_details_pressure;
                    break;
                case TYPE_RELATIVE_HUMIDITY:
                    layoutResId = R.layout.layout_telemetry_1_value;
                    labelResId = R.string.telemetry_param_humidity;
                    break;
                case TYPE_STEP_COUNTER:
                    layoutResId = R.layout.layout_telemetry_1_value;
                    labelResId = R.string.device_details_total_steps;
                    break;
            }
            if ((sensorType == TYPE_GYROSCOPE_UNCALIBRATED && mTelemetryContainer.findViewWithTag(TYPE_GYROSCOPE) != null) ||
                    (sensorType == TYPE_MAGNETIC_FIELD_UNCALIBRATED && mTelemetryContainer.findViewWithTag(TYPE_MAGNETIC_FIELD) != null)) {
                continue;
            }
            if (sensorTypes.add(sensorType)) {
                container = inflater.inflate(layoutResId, null);
                TextView label = ButterKnife.findById(container, R.id.telemetry_label);
                label.setText(labelResId);
                container.setTag(sensorType);
                mTelemetryContainer.addView(container);
            }
        }
    }

    @Override
    public String getTitle(Context context) {
        return context.getString(R.string.cards_android_internal_title);
    }

    @Override
    protected BaseFragment getConfigFragment() {
        return new AndroidInternalConfigFragment();
    }

    @Override
    protected boolean isAbleToConnect() {
        return true;
    }
}
