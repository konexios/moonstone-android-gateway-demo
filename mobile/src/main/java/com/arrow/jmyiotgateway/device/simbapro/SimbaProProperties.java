package com.arrow.jmyiotgateway.device.simbapro;

import android.content.Context;

import com.arrow.jmyiotgateway.R;
import com.arrow.jmyiotgateway.device.DevicePropertiesAbstract;

import java.util.UUID;

import static com.arrow.jmyiotgateway.device.TelemetriesNames.ACCELEROMETER_X;
import static com.arrow.jmyiotgateway.device.TelemetriesNames.ACCELEROMETER_XYZ;
import static com.arrow.jmyiotgateway.device.TelemetriesNames.ACCELEROMETER_Y;
import static com.arrow.jmyiotgateway.device.TelemetriesNames.ACCELEROMETER_Z;
import static com.arrow.jmyiotgateway.device.TelemetriesNames.GYROMETER_X;
import static com.arrow.jmyiotgateway.device.TelemetriesNames.GYROMETER_XYZ;
import static com.arrow.jmyiotgateway.device.TelemetriesNames.GYROMETER_Y;
import static com.arrow.jmyiotgateway.device.TelemetriesNames.GYROMETER_Z;
import static com.arrow.jmyiotgateway.device.TelemetriesNames.HUMIDITY;
import static com.arrow.jmyiotgateway.device.TelemetriesNames.LIGHT;
import static com.arrow.jmyiotgateway.device.TelemetriesNames.MAGNETOMETER_X;
import static com.arrow.jmyiotgateway.device.TelemetriesNames.MAGNETOMETER_XYZ;
import static com.arrow.jmyiotgateway.device.TelemetriesNames.MAGNETOMETER_Y;
import static com.arrow.jmyiotgateway.device.TelemetriesNames.MAGNETOMETER_Z;
import static com.arrow.jmyiotgateway.device.TelemetriesNames.MIC_LEVEL;
import static com.arrow.jmyiotgateway.device.TelemetriesNames.PRESSURE;
import static com.arrow.jmyiotgateway.device.TelemetriesNames.TEMPERATURE;
import static com.arrow.jmyiotgateway.device.simbapro.SimbaProProperties.SimbaProPropertyKeys.ENVIRONMENTSENSOR_ENABLED;
import static com.arrow.jmyiotgateway.device.simbapro.SimbaProProperties.SimbaProPropertyKeys.LIGHTSENSOR_ENABLED;
import static com.arrow.jmyiotgateway.device.simbapro.SimbaProProperties.SimbaProPropertyKeys.MICLEVELSENSOR_ENABLED;
import static com.arrow.jmyiotgateway.device.simbapro.SimbaProProperties.SimbaProPropertyKeys.MOVEMENTSENSOR_ENABLED;

/**
 * Created by osminin on 17.01.2018.
 */

public final class SimbaProProperties extends DevicePropertiesAbstract {
    public static final String SP_PREFIX_KEY = "com.arrow.jmyiotgateway.simba_pro_properties";

    public SimbaProProperties(Context context) {
        super(context, new String[]{ACCELEROMETER_X,
                ACCELEROMETER_Y,
                ACCELEROMETER_Z,
                ACCELEROMETER_XYZ,
                MAGNETOMETER_X,
                MAGNETOMETER_Y,
                MAGNETOMETER_Z,
                MAGNETOMETER_XYZ,
                GYROMETER_X,
                GYROMETER_Y,
                GYROMETER_Z,
                GYROMETER_XYZ,
                TEMPERATURE,
                HUMIDITY,
                PRESSURE,
                MIC_LEVEL,
                LIGHT});
        loadProperties();
        loadInfo();
    }

    @Override
    public String getSpPrefixKey() {
        return SP_PREFIX_KEY;
    }

    @Override
    public PropertyKeys[] getPropertyKeys() {
        return SimbaProPropertyKeys.values();
    }

    @Override
    public PropertyKeys[] getInfoKeys() {
        return SimbaProInfoKeys.values();
    }

    public enum SimbaProPropertyKeys implements DevicePropertiesAbstract.PropertyKeys {
        ENVIRONMENTSENSOR_ENABLED("EnvironmentSensor/enabled", PropertyKeyType.BOOLEAN, R.string.sensor_tile_environment_sensor),

        LIGHTSENSOR_ENABLED("LightSensor/enabled", PropertyKeyType.BOOLEAN, R.string.simba_pro_light_sensor),
        MOVEMENTSENSOR_ENABLED("MovementSensor/enabled", PropertyKeyType.BOOLEAN, R.string.sensor_tile_movement_sensor),
        MICLEVELSENSOR_ENABLED("MicLevelSensor/enabled", PropertyKeyType.BOOLEAN, R.string.sensor_tile_mic_level_sensor);

        final int mStringResourceId;
        private final String mStringKey;
        private final PropertyKeyType mType;

        SimbaProPropertyKeys(String key, PropertyKeyType type, int resourceId) {
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

    enum SimbaProInfoKeys implements DevicePropertiesAbstract.PropertyKeys {
        UID("uid", PropertyKeyType.STRING),
        NAME("name", PropertyKeyType.STRING),
        BLE_ADDRESS("bleAddress", PropertyKeyType.STRING),
        TYPE("type", PropertyKeyType.STRING);


        private final String mStringKey;
        private final PropertyKeyType mType;

        SimbaProInfoKeys(String key, PropertyKeyType type) {
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

    public boolean isSensorEnabled(UUID uuid) {
        SimbaProPropertyKeys key = null;
        if (SimbaProMovementSensor.SENSOR_UUID.equals(uuid)) {
            key = MOVEMENTSENSOR_ENABLED;
        } else if (SimbaProEnvironmentSensor.SENSOR_UUID.equals(uuid)) {
            key = ENVIRONMENTSENSOR_ENABLED;
        } else if (SimbaProMicSensor.SENSOR_UUID.equals(uuid)) {
            key = MICLEVELSENSOR_ENABLED;
        } else if (SimbaProLightSensor.SENSOR_UUID.equals(uuid)) {
            key = LIGHTSENSOR_ENABLED;
        }

        return key == null ? false : (Boolean) mProperties.get(key);
    }
}
