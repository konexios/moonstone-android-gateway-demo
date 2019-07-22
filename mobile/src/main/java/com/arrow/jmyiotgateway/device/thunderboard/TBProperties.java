package com.arrow.jmyiotgateway.device.thunderboard;

import android.content.Context;

import com.arrow.jmyiotgateway.R;
import com.arrow.jmyiotgateway.device.DevicePropertiesAbstract;

import java.util.UUID;

import static com.arrow.jmyiotgateway.device.TelemetriesNames.*;

/**
 * Created by osminin on 8/23/2016.
 */

public final class TBProperties extends DevicePropertiesAbstract {
    public static final String SP_PREFIX_KEY = "com.arrow.jmyiotgateway.thunder_board_properties";

    public enum TBPropertiesKeys implements DevicePropertiesAbstract.PropertyKeys {
        TEMPERATURE_SENSOR_ENABLED("TemperatureSensor/enabled", PropertyKeyType.BOOLEAN, R.string.config_temperature_sensor),
        ACCELEROMETER_SENSOR_ENABLED("AccelerometerSensor/enabled", PropertyKeyType.BOOLEAN, R.string.ms_band_accelerometer_sensor),
        LIGHT_SENSOR_ENABLED("AmbientLightSensor/enabled", PropertyKeyType.BOOLEAN, R.string.config_light_sensor),
        HUMIDITY_SENSOR_ENABLED("HumiditySensor/enabled", PropertyKeyType.BOOLEAN, R.string.config_humidity_sensor),
        ORIENTATION_SENSOR_ENABLED("OrientationSensor/enabled", PropertyKeyType.BOOLEAN, R.string.device_orientation_sensor),
        UVSENSOR_ENABLED("UVSensor/enabled", PropertyKeyType.BOOLEAN, R.string.ms_band_uv_sensor);

        private final String mStringKey;
        private final PropertyKeyType mType;
        final int mStringResourceId;

        TBPropertiesKeys(String key, PropertyKeyType type, int resourceId) {
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

    public enum TBInfoKeys implements DevicePropertiesAbstract.PropertyKeys {
        UID("uid", PropertyKeyType.STRING),
        NAME("name", PropertyKeyType.STRING),
        BLE_ADDRESS("bleAddress", PropertyKeyType.STRING),
        TYPE("type", PropertyKeyType.STRING);


        private final String mStringKey;
        private final PropertyKeyType mType;

        TBInfoKeys(String key, PropertyKeyType type) {
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

    public TBProperties(Context context) {
        super(context, new String[] {TEMPERATURE,
                HUMIDITY,
                UV,
                LIGHT,
                ACCELEROMETER_X,
                ACCELEROMETER_Y,
                ACCELEROMETER_Z,
                ORIENTATION_X,
                ORIENTATION_Y,
                ORIENTATION_Z});
        loadProperties();
        loadInfo();
    }

    public boolean isSensorEnabled(UUID uuid) {
        TBPropertiesKeys key = null;

        if (uuid.compareTo(ThunderBoardUuids.UUID_CHARACTERISTIC_TEMPERATURE) == 0) {
            key = TBPropertiesKeys.TEMPERATURE_SENSOR_ENABLED;
        } else if (uuid.compareTo(ThunderBoardUuids.UUID_CHARACTERISTIC_HUMIDITY) == 0) {
            key = TBPropertiesKeys.HUMIDITY_SENSOR_ENABLED;
        } else if (uuid.compareTo(ThunderBoardUuids.UUID_CHARACTERISTIC_AMBIENT_LIGHT) == 0) {
            key = TBPropertiesKeys.LIGHT_SENSOR_ENABLED;
        } else if (uuid.compareTo(ThunderBoardUuids.UUID_CHARACTERISTIC_UV_INDEX) == 0) {
            key = TBPropertiesKeys.UVSENSOR_ENABLED;
        } else if (uuid.compareTo(ThunderBoardUuids.UUID_CHARACTERISTIC_ACCELERATION) == 0) {
            key = TBPropertiesKeys.ACCELEROMETER_SENSOR_ENABLED;
        } else if (uuid.compareTo(ThunderBoardUuids.UUID_CHARACTERISTIC_ORIENTATION) == 0) {
            key = TBPropertiesKeys.ORIENTATION_SENSOR_ENABLED;
        }
        return mProperties.get(key) == null ? false : (Boolean) mProperties.get(key);
    }

    @Override
    public String getSpPrefixKey() {
        return SP_PREFIX_KEY;
    }

    @Override
    public PropertyKeys[] getPropertyKeys() {
        return TBPropertiesKeys.values();
    }

    @Override
    public PropertyKeys[] getInfoKeys() {
        return TBInfoKeys.values();
    }
}
