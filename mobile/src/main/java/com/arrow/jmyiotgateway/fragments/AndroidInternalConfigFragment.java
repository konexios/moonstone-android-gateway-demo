package com.arrow.jmyiotgateway.fragments;

import android.content.Context;
import android.os.Bundle;

import com.arrow.jmyiotgateway.R;
import com.arrow.jmyiotgateway.device.DevicePropertiesAbstract;
import com.arrow.jmyiotgateway.device.android.AndroidSensorProperties;
import com.arrow.jmyiotgateway.device.android.AndroidSensorUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.hardware.Sensor.TYPE_ACCELEROMETER;
import static android.hardware.Sensor.TYPE_AMBIENT_TEMPERATURE;
import static android.hardware.Sensor.TYPE_GYROSCOPE;
import static android.hardware.Sensor.TYPE_HEART_RATE;
import static android.hardware.Sensor.TYPE_LIGHT;
import static android.hardware.Sensor.TYPE_MAGNETIC_FIELD;
import static android.hardware.Sensor.TYPE_PRESSURE;
import static android.hardware.Sensor.TYPE_RELATIVE_HUMIDITY;
import static android.hardware.Sensor.TYPE_STEP_COUNTER;

public final class AndroidInternalConfigFragment extends BaseConfigFragment {

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        init();
    }

    private void init() {
        List<Integer> availableSensors = Arrays.asList(AndroidSensorUtils.getAvailableSensors(mContext));
        List<DevicePropertiesAbstract.PropertyKeys> list = new ArrayList<>();
        for (AndroidSensorProperties.AndroidSensorPropertiesKeys key : AndroidSensorProperties.AndroidSensorPropertiesKeys.values()) {
            if (availableSensors.contains(getSensorTypeByKey(key))) {
                list.add(key);
            }
        }
        init(list.toArray(new DevicePropertiesAbstract.PropertyKeys[list.size()]));
    }

    private int getSensorTypeByKey(AndroidSensorProperties.AndroidSensorPropertiesKeys key) {
        int result = -1;
        switch (key) {
            case TEMPERATURE_SENSOR_ENABLED:
                result = TYPE_AMBIENT_TEMPERATURE;
                break;
            case ACCELEROMETER_SENSOR_ENABLED:
                result = TYPE_ACCELEROMETER;
                break;
            case GYROSCOPE_SENSOR_ENABLED:
                result = TYPE_GYROSCOPE;
                break;
            case PEDOMETER_SENSOR_ENABLED:
                result = TYPE_STEP_COUNTER;
                break;
            case LIGHTSENSOR_ENABLED:
                result = TYPE_LIGHT;
                break;
            case HUMIDITYSENSOR_ENABLED:
                result = TYPE_RELATIVE_HUMIDITY;
                break;
            case MAGNETOMETERSENSOR_ENABLED:
                result = TYPE_MAGNETIC_FIELD;
                break;
            case PRESSURESENSOR_ENABLED:
                result = TYPE_PRESSURE;
                break;
            case HEART_RATE_SENSOR_ENABLED:
                result = TYPE_HEART_RATE;
                break;
        }
        return result;
    }

    @Override
    protected String getSPPrefix() {
        return AndroidSensorProperties.SP_PREFIX_KEY;
    }

    @Override
    public String getTitle(Context context) {
        return context.getString(R.string.android_internal_config_title);
    }
}
