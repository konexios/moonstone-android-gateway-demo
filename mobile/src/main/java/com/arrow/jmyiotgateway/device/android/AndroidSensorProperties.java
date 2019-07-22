package com.arrow.jmyiotgateway.device.android;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;

import com.arrow.jmyiotgateway.R;
import com.arrow.jmyiotgateway.device.DevicePropertiesAbstract;

import java.util.ArrayList;
import java.util.List;

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
import static com.arrow.jmyiotgateway.device.TelemetriesNames.*;
import static com.arrow.jmyiotgateway.device.android.AndroidSensorProperties.AndroidSensorPropertiesKeys.ACCELEROMETER_SENSOR_ENABLED;
import static com.arrow.jmyiotgateway.device.android.AndroidSensorProperties.AndroidSensorPropertiesKeys.GYROSCOPE_SENSOR_ENABLED;
import static com.arrow.jmyiotgateway.device.android.AndroidSensorProperties.AndroidSensorPropertiesKeys.HEART_RATE_SENSOR_ENABLED;
import static com.arrow.jmyiotgateway.device.android.AndroidSensorProperties.AndroidSensorPropertiesKeys.HUMIDITYSENSOR_ENABLED;
import static com.arrow.jmyiotgateway.device.android.AndroidSensorProperties.AndroidSensorPropertiesKeys.LIGHTSENSOR_ENABLED;
import static com.arrow.jmyiotgateway.device.android.AndroidSensorProperties.AndroidSensorPropertiesKeys.MAGNETOMETERSENSOR_ENABLED;
import static com.arrow.jmyiotgateway.device.android.AndroidSensorProperties.AndroidSensorPropertiesKeys.PEDOMETER_SENSOR_ENABLED;
import static com.arrow.jmyiotgateway.device.android.AndroidSensorProperties.AndroidSensorPropertiesKeys.PRESSURESENSOR_ENABLED;
import static com.arrow.jmyiotgateway.device.android.AndroidSensorProperties.AndroidSensorPropertiesKeys.TEMPERATURE_SENSOR_ENABLED;

/**
 * TODO: Add a class header comment!
 */

public final class AndroidSensorProperties extends DevicePropertiesAbstract {
    public static final String SP_PREFIX_KEY = "com.arrow.jmyiotgateway.android_internal_properties";

    public enum AndroidSensorPropertiesKeys implements DevicePropertiesAbstract.PropertyKeys {
        TEMPERATURE_SENSOR_ENABLED("TemperatureSensor/enabled", PropertyKeyType.BOOLEAN, R.string.config_temperature_sensor),
        ACCELEROMETER_SENSOR_ENABLED("AccelerometerSensor/enabled", PropertyKeyType.BOOLEAN, R.string.ms_band_accelerometer_sensor),
        GYROSCOPE_SENSOR_ENABLED("GyroscopeSensor/enabled", PropertyKeyType.BOOLEAN, R.string.ms_band_gyroscope_sensor),
        PEDOMETER_SENSOR_ENABLED("PedometerSensor/enabled", PropertyKeyType.BOOLEAN, R.string.ms_band_pedometer_sensor),
        LIGHTSENSOR_ENABLED("LightSensor/enabled", PropertyKeyType.BOOLEAN, R.string.config_light_sensor),
        HUMIDITYSENSOR_ENABLED("HumiditySensor/enabled", PropertyKeyType.BOOLEAN, R.string.config_humidity_sensor),
        MAGNETOMETERSENSOR_ENABLED("Magnetometer/enabled", PropertyKeyType.BOOLEAN, R.string.device_details_magnetometer),
        PRESSURESENSOR_ENABLED("PressureSensor/enabled", PropertyKeyType.BOOLEAN, R.string.device_pressure_sensor),
        HEART_RATE_SENSOR_ENABLED("HeartRateSensor/enabled", PropertyKeyType.BOOLEAN, R.string.ms_band_heart_rate_sensor);

        private final String mStringKey;
        private final PropertyKeyType mType;
        private final int mStringResourceId;

        AndroidSensorPropertiesKeys(String key, PropertyKeyType type, int resourceId) {
            mStringKey = key;
            mType = type;
            mStringResourceId = resourceId;
        }

        @Override
        public PropertyKeyType getType() {
            return mType;
        }

        @Override
        public String getStringKey() {
            return mStringKey;
        }

        @Override
        public int getStringResourceId() {
            return mStringResourceId;
        }
    }

    public enum AndroidSensorInfoKeys implements DevicePropertiesAbstract.PropertyKeys {
        UID("uid", PropertyKeyType.STRING),
        NAME("name", PropertyKeyType.STRING),
        BLE_ADDRESS("bleAddress", PropertyKeyType.STRING),
        TYPE("type", PropertyKeyType.STRING);


        private final String mStringKey;
        private final PropertyKeyType mType;

        AndroidSensorInfoKeys(String key, PropertyKeyType type) {
            mStringKey = key;
            mType = type;
        }

        @Override
        public PropertyKeyType getType() {
            return mType;
        }

        @Override
        public String getStringKey() {
            return mStringKey;
        }

        @Override
        public int getStringResourceId() {
            return 0;
        }


    }

    public AndroidSensorProperties(Context context, SensorManager sensorManager) {
        super(context, availableSensors(sensorManager));
        update();
    }

    @Override
    public String getSpPrefixKey() {
        return SP_PREFIX_KEY;
    }

    @Override
    public PropertyKeys[] getPropertyKeys() {
        return AndroidSensorPropertiesKeys.values();
    }

    @Override
    public PropertyKeys[] getInfoKeys() {
        return AndroidSensorInfoKeys.values();
    }

    public boolean isSensorEnabled(int sensorType) {
        AndroidSensorPropertiesKeys key = null;
        switch (sensorType) {
            case TYPE_ACCELEROMETER:
                key = ACCELEROMETER_SENSOR_ENABLED;
                break;
            case TYPE_AMBIENT_TEMPERATURE:
                key = TEMPERATURE_SENSOR_ENABLED;
                break;
            case TYPE_GYROSCOPE:
            case TYPE_GYROSCOPE_UNCALIBRATED:
                key = GYROSCOPE_SENSOR_ENABLED;
                break;
            case TYPE_HEART_RATE:
                key = HEART_RATE_SENSOR_ENABLED;
                break;
            case TYPE_LIGHT:
                key = LIGHTSENSOR_ENABLED;
                break;
            case TYPE_MAGNETIC_FIELD:
            case TYPE_MAGNETIC_FIELD_UNCALIBRATED:
                key = MAGNETOMETERSENSOR_ENABLED;
                break;
            case TYPE_PRESSURE:
                key = PRESSURESENSOR_ENABLED;
                break;
            case TYPE_RELATIVE_HUMIDITY:
                key = HUMIDITYSENSOR_ENABLED;
                break;
            case TYPE_STEP_COUNTER:
                key = PEDOMETER_SENSOR_ENABLED;
                break;
        }
        return (Boolean) mProperties.get(key);
    }

    private static String[] availableSensors(SensorManager sensorManager) {
        List<Sensor> sensors = AndroidSensorUtils.getSensorsList(sensorManager);
        List<String> result = new ArrayList<>();
        for (Sensor sensor : sensors) {
            switch (sensor.getType()) {
                case TYPE_ACCELEROMETER:
                    result.add(ACCELEROMETER_X);
                    result.add(ACCELEROMETER_Y);
                    result.add(ACCELEROMETER_Z);
                    break;
                case TYPE_AMBIENT_TEMPERATURE:
                    result.add(TEMPERATURE);
                    break;
                case TYPE_GYROSCOPE:
                case TYPE_GYROSCOPE_UNCALIBRATED:
                    result.add(GYROSCOPE_X);
                    result.add(GYROSCOPE_Y);
                    result.add(GYROSCOPE_Z);
                    break;
                case TYPE_HEART_RATE:
                    result.add(HEART_RATE);
                    break;
                case TYPE_LIGHT:
                    result.add(LIGHT);
                    break;
                case TYPE_MAGNETIC_FIELD:
                case TYPE_MAGNETIC_FIELD_UNCALIBRATED:
                    result.add(MAGNETOMETER_X);
                    result.add(MAGNETOMETER_Y);
                    result.add(MAGNETOMETER_Z);
                    break;
                case TYPE_PRESSURE:
                    result.add(PRESSURE);
                    break;
                case TYPE_RELATIVE_HUMIDITY:
                    result.add(HUMIDITY);
                    break;
                case TYPE_STEP_COUNTER:
                    result.add(STEPS);
                    break;
            }
        }
        return result.toArray(new String[]{});
    }
}
