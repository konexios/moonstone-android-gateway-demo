package com.arrow.jmyiotgateway.device.msband;

import android.content.Context;

import com.arrow.jmyiotgateway.R;
import com.arrow.jmyiotgateway.device.DevicePropertiesAbstract;

import static com.arrow.jmyiotgateway.device.TelemetriesNames.*;

/**
 * Created by osminin on 5/27/2016.
 */

public class MsBandProperties extends DevicePropertiesAbstract {
    public static final String SP_PREFIX_KEY = "com.arrow.jmyiotgateway.msband_properties";

    public MsBandProperties(Context context) {
        super(context, new String[]{ACCELEROMETER_Y,
                ACCELEROMETER_Z,
                TEMPERATURE,
                GYROSCOPE_X,
                GYROSCOPE_Y,
                GYROSCOPE_Z,
                HEART_RATE,
                SKIN_TEMP,
                UV});
        loadProperties();
        loadInfo();
    }

    @Override
    public String getSpPrefixKey() {
        return SP_PREFIX_KEY;
    }

    @Override
    public PropertyKeys[] getPropertyKeys() {
        return MsBandPropertiesKeys.values();
    }

    @Override
    public PropertyKeys[] getInfoKeys() {
        return MsBandInfoKeys.values();
    }

    public boolean isSensorEnabled(MsBandPropertiesKeys key) {
        return (Boolean) getProperties().get(key);
    }

    public enum MsBandPropertiesKeys implements DevicePropertiesAbstract.PropertyKeys {
        SKIN_TEMPERATURE_SENSOR_ENABLED("SkinTemperatureSensor/enabled", PropertyKeyType.BOOLEAN, R.string.ms_band_skin_temperature_sensor),
        ACCELEROMETER_SENSOR_ENABLED("AccelerometerSensor/enabled", PropertyKeyType.BOOLEAN, R.string.ms_band_accelerometer_sensor),
        GYROSCOPE_SENSOR_ENABLED("GyroscopeSensor/enabled", PropertyKeyType.BOOLEAN, R.string.ms_band_gyroscope_sensor),
        UV_SENSOR_ENABLED("UVSensor/enabled", PropertyKeyType.BOOLEAN, R.string.ms_band_uv_sensor),
        PEDOMETER_SENSOR_ENABLED("PedometerSensor/enabled", PropertyKeyType.BOOLEAN, R.string.ms_band_pedometer_sensor),
        DISTANCE_SENSOR_ENABLED("DistanceSensor/enabled", PropertyKeyType.BOOLEAN, R.string.ms_band_distance_sensor),
        HEART_RATE_SENSOR_ENABLED("HeartRateSensor/enabled", PropertyKeyType.BOOLEAN, R.string.ms_band_heart_rate_sensor);

        final String mStringKey;
        final PropertyKeyType mType;
        final int mStringResourceId;

        MsBandPropertiesKeys(String key, PropertyKeyType type, int resourceId) {
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

    public enum MsBandInfoKeys implements DevicePropertiesAbstract.PropertyKeys {
        UID("uid", PropertyKeyType.STRING),
        NAME("name", PropertyKeyType.STRING),
        BLE_ADDRESS("bleAddress", PropertyKeyType.STRING),
        TYPE("type", PropertyKeyType.STRING);


        private final String mStringKey;
        private final PropertyKeyType mType;

        MsBandInfoKeys(String key, PropertyKeyType type) {
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
            //doesn't have string resource
            return 0;
        }


    }
}
