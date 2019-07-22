package com.arrow.jmyiotgateway.device;

import android.content.Context;
import android.hardware.SensorManager;

import com.arrow.jmyiotgateway.device.android.AndroidSensorProperties;
import com.arrow.jmyiotgateway.device.msband.MsBandProperties;
import com.arrow.jmyiotgateway.device.sensortile.SensorTileProperties;
import com.arrow.jmyiotgateway.device.simbapro.SimbaProProperties;
import com.arrow.jmyiotgateway.device.thunderboard.TBProperties;
import com.google.gson.JsonObject;

import org.parceler.Parcel;
import org.parceler.ParcelConstructor;
import org.parceler.Transient;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public abstract class DevicePropertiesAbstract {
    private static final String TAG = DevicePropertiesAbstract.class.getName();
    public static final Boolean DEFAULT_BOOLEAN_VALUE = true;
    public static final Integer DEFAULT_INTEGER_VALUE = 200;
    public static final String DEFAULT_STRING_VALUE = "";

    protected final String[] mTelemetryNames;

    public interface PropertyKeys extends Serializable{
        enum PropertyKeyType {
            INTEGER,
            BOOLEAN,
            STRING
        }

        PropertyKeyType getType();
        String getStringKey();
        int getStringResourceId();
    }

    protected Map<PropertyKeys, Object> mProperties = new HashMap<>();
    private Map<PropertyKeys, Object> mInfo = new HashMap<>();
    private Context mContext;

    public DevicePropertiesAbstract(Context context, String[] telemetryNames) {
        mContext = context;
        mTelemetryNames = telemetryNames;
    }

    public Map<PropertyKeys, Object> getProperties() {
        return mProperties;
    }

    protected void loadProperties() {
        loadFromPreferences(getPropertyKeys(), mProperties);
    }

    protected void loadInfo() {
        loadFromPreferences(getInfoKeys(), mInfo);
    }

    private void loadFromPreferences(PropertyKeys[] propertyKeys, Map<PropertyKeys, Object> map) {
        DevicePropertiesStorage.loadFromPreferences(mContext, getSpPrefixKey(), propertyKeys, map);
    }

    public void saveToPreferences(Map<PropertyKeys, Object> map) {
        DevicePropertiesStorage.saveToPreferences(mContext, getSpPrefixKey(), map);
    }

    public JsonObject getPropertiesAsJson() {
        return convertMapToJson(mProperties);
    }

    public JsonObject getInfoAsJson() {
        return convertMapToJson(mInfo);
    }

    private JsonObject convertMapToJson(Map<PropertyKeys, Object> map) {
        JsonObject jsonObject = new JsonObject();
        for (Map.Entry<PropertyKeys, Object> entry : map.entrySet()) {
            PropertyKeys key = entry.getKey();
            if (key.getType() == PropertyKeys.PropertyKeyType.INTEGER) {
                jsonObject.addProperty(key.getStringKey(), (Integer) entry.getValue());
            } else if (key.getType() == PropertyKeys.PropertyKeyType.BOOLEAN){
                jsonObject.addProperty(key.getStringKey(), (Boolean) entry.getValue());
            } else if (key.getType() == PropertyKeys.PropertyKeyType.STRING) {
                jsonObject.addProperty(key.getStringKey(), (String) entry.getValue());
            }
        }
        return jsonObject;
    }

    public boolean saveToPreferencesJsonMap(Map<String, String> map) {
        Map<PropertyKeys, Object> tmpMap = new HashMap<>();
        PropertyKeys[] propertyKeys = getPropertyKeys();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            for (PropertyKeys propertyKey : propertyKeys) {
                if (entry.getKey().equals(propertyKey.getStringKey())) {
                    Object value = null;
                    if (propertyKey.getType() == PropertyKeys.PropertyKeyType.INTEGER) {
                        String tmpValue = entry.getValue().replaceAll("([^0-9])","");
                        if (tmpValue.isEmpty()) break;
                        value = Integer.parseInt(tmpValue);
                    } else if (propertyKey.getType() == PropertyKeys.PropertyKeyType.BOOLEAN){
                        String tmpValue = entry.getValue().replaceAll("([^falsetrue])","");
                        if (tmpValue.isEmpty()) break;
                        value = Boolean.parseBoolean(tmpValue);
                    } else if (propertyKey.getType() == PropertyKeys.PropertyKeyType.STRING) {
                        value = entry.getValue();
                    }
                    tmpMap.put(propertyKey, value);
                    break;
                }
            }
        }
        saveToPreferences(tmpMap);
        return !tmpMap.isEmpty();
    }

    public abstract String getSpPrefixKey();
    public abstract PropertyKeys[] getPropertyKeys();
    public abstract PropertyKeys[] getInfoKeys();

    public void update() {
        loadProperties();
        loadInfo();
    }

    public String[] getTelemetryNames() {
        return mTelemetryNames;
    }

    public static DevicePropertiesAbstract create(Context context, DeviceType deviceType) {
        DevicePropertiesAbstract result = null;
        switch (deviceType) {
            case MicrosoftBand:
                result = new MsBandProperties(context);
                break;
            case AndroidInternal:
                result = new AndroidSensorProperties(context, (SensorManager) context.getSystemService(Context.SENSOR_SERVICE));
                break;
            case ThunderBoard:
                result = new TBProperties(context);
                break;
            case SensorTile:
                result = new SensorTileProperties(context);
                break;
            case SimbaPro:
                result = new SimbaProProperties(context);
                break;
            case SensorPuck:
            case SenseAbilityKit:
            default:
                break;
            /* nothing to do here */
        }
        return result;
    }
}
