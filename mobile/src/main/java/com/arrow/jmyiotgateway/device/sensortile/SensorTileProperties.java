package com.arrow.jmyiotgateway.device.sensortile;

import android.content.Context;

import com.arrow.jmyiotgateway.R;
import com.arrow.jmyiotgateway.device.DevicePropertiesAbstract;

import java.util.UUID;

import static com.arrow.jmyiotgateway.device.TelemetriesNames.*;

/**
 * TODO: Add a class header comment!
 */

public final class SensorTileProperties extends DevicePropertiesAbstract {
    public static final String SP_PREFIX_KEY = "com.arrow.jmyiotgateway.sensortile_properties";

    public enum SensorTilePropertyKeys implements DevicePropertiesAbstract.PropertyKeys {
        MOVEMENTSENSOR_ENABLED ("MovementSensor/enabled", PropertyKeyType.BOOLEAN, R.string.sensor_tile_movement_sensor),
        ENVIRONMENTSENSOR_ENABLED ("EnvironmentSensor/enabled", PropertyKeyType.BOOLEAN, R.string.sensor_tile_environment_sensor),

        MICLEVELSENSOR_ENABLED ("MicLevelSensor/enabled", PropertyKeyType.BOOLEAN, R.string.sensor_tile_mic_level_sensor),
        SWITCHSENSOR_ENABLED ("SwitchSensor/enabled", PropertyKeyType.BOOLEAN, R.string.sensor_tile_switch_sensor);

        private final String mStringKey;
        private final PropertyKeyType mType;
        final int mStringResourceId;

        SensorTilePropertyKeys(String key, PropertyKeyType type, int resourceId) {
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

    enum SensorTileInfoKeys implements DevicePropertiesAbstract.PropertyKeys {
        UID ("uid", PropertyKeyType.STRING),
        NAME ("name", PropertyKeyType.STRING),
        BLE_ADDRESS ("bleAddress", PropertyKeyType.STRING),
        TYPE ("type", PropertyKeyType.STRING);


        private final String mStringKey;
        private final PropertyKeyType mType;

        SensorTileInfoKeys(String key, PropertyKeyType type) {
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

    public SensorTileProperties(Context context) {
        super(context, new String[] {ACCELEROMETER_X,
                ACCELEROMETER_Y,
                ACCELEROMETER_Z,
                MAGNETOMETER_X,
                MAGNETOMETER_Y,
                MAGNETOMETER_Z,
                GYROMETER_X,
                GYROMETER_Y,
                GYROMETER_Z,
                TEMPERATURE,
                IR_TEMPERATURE,
                HUMIDITY,
                PRESSURE,
                MIC_LEVEL,
                SWITCH});
        update();
    }

    @Override
    public String getSpPrefixKey() {
        return SP_PREFIX_KEY;
    }

    @Override
    public PropertyKeys[] getPropertyKeys() {
        return SensorTilePropertyKeys.values();
    }

    @Override
    public PropertyKeys[] getInfoKeys() {
        return SensorTileInfoKeys.values();
    }

    public boolean isSensorEnabled(UUID uuid) {
        SensorTilePropertyKeys key = null;
        if (STileMovementSensor.SENSOR_UUID.equals(uuid)) {
            key = SensorTilePropertyKeys.MOVEMENTSENSOR_ENABLED;
        } else if (STileEnvironmentSensor.SENSOR_UUID.equals(uuid)) {
            key = SensorTilePropertyKeys.ENVIRONMENTSENSOR_ENABLED;
        } else if (STileMicLevelSensor.SENSOR_UUID.equals(uuid)) {
            key = SensorTilePropertyKeys.MICLEVELSENSOR_ENABLED;
        } else if (STileSwitchSensor.SENSOR_UUID.equals(uuid)) {
            key = SensorTilePropertyKeys.SWITCHSENSOR_ENABLED;
        }

        return key == null ? false : (Boolean) mProperties.get(key);
    }
}
