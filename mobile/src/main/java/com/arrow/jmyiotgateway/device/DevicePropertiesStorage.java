package com.arrow.jmyiotgateway.device;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.arrow.jmyiotgateway.Config;
import com.arrow.jmyiotgateway.Constant;
import com.google.firebase.crash.FirebaseCrash;

import java.util.List;
import java.util.Map;

import static com.arrow.jmyiotgateway.device.DevicePropertiesAbstract.DEFAULT_BOOLEAN_VALUE;
import static com.arrow.jmyiotgateway.device.DevicePropertiesAbstract.DEFAULT_INTEGER_VALUE;
import static com.arrow.jmyiotgateway.device.DevicePropertiesAbstract.DEFAULT_STRING_VALUE;

/**
 * Created by osminin on 6/15/2016.
 */

public final class DevicePropertiesStorage {
    private static final String TAG = DevicePropertiesAbstract.class.getName();

    public static void loadFromPreferences(Context context, String prefixKey,
                                           DevicePropertiesAbstract.PropertyKeys[] propertyKeys,
                                           Map<DevicePropertiesAbstract.PropertyKeys, Object> map) {
        FirebaseCrash.logcat(Log.DEBUG, TAG, "loadFromPreferences");
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        for (DevicePropertiesAbstract.PropertyKeys key : propertyKeys) {
            if (key.getType() == DevicePropertiesAbstract.PropertyKeys.PropertyKeyType.INTEGER) {
                Integer value = sp.getInt(prefixKey + key.getStringKey(), DEFAULT_INTEGER_VALUE);
                map.put(key, value);
            } else if (key.getType() == DevicePropertiesAbstract.PropertyKeys.PropertyKeyType.BOOLEAN) {
                Boolean value = sp.getBoolean(prefixKey + key.getStringKey(), DEFAULT_BOOLEAN_VALUE);
                map.put(key, value);
            } else if (key.getType() == DevicePropertiesAbstract.PropertyKeys.PropertyKeyType.STRING) {
                String value = sp.getString(prefixKey + key.getStringKey(), DEFAULT_STRING_VALUE);
                map.put(key, value);
            }
        }
    }

    public static void saveToPreferences(Context context, String prefixKey,
                                         Map<DevicePropertiesAbstract.PropertyKeys, Object> map) {
        FirebaseCrash.logcat(Log.DEBUG, TAG, "saveToPreferences");
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        for (DevicePropertiesAbstract.PropertyKeys key : map.keySet()) {
            if (key.getType() == DevicePropertiesAbstract.PropertyKeys.PropertyKeyType.INTEGER) {
                editor.putInt(prefixKey + key.getStringKey(), (int) map.get(key));
            } else if (key.getType() == DevicePropertiesAbstract.PropertyKeys.PropertyKeyType.BOOLEAN) {
                editor.putBoolean(prefixKey + key.getStringKey(), (boolean) map.get(key));
            } else if (key.getType() == DevicePropertiesAbstract.PropertyKeys.PropertyKeyType.STRING) {
                editor.putString(prefixKey + key.getStringKey(), (String) map.get(key));
            }
        }
        editor.commit();
    }

    public static void saveProperty(Context context, String prefix,
                                    DevicePropertiesAbstract.PropertyKeys key, Boolean value) {
        FirebaseCrash.logcat(Log.DEBUG, TAG, "saveProperty");
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(prefix + key.getStringKey(), value);
        editor.commit();
    }

    public static void saveProperty(Context context, String prefix,
                             DevicePropertiesAbstract.PropertyKeys key, Integer value) {
        FirebaseCrash.logcat(Log.DEBUG, TAG, "saveProperty");
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(prefix + key.getStringKey(), value);
        editor.commit();
    }

    public static DeviceKey getDeviceKeyByHid(Context context, String deviceHid, Config config) {
        FirebaseCrash.logcat(Log.DEBUG, TAG, "getDeviceKeyByHid");
        List<Config.ConfigDeviceModel> deviceModelList = config.getAddedDevices(context);
        for (Config.ConfigDeviceModel model : deviceModelList) {
            if (deviceHid.equals(model.getDeviceHid())) {
                return new DeviceKey(model.getDeviceType(), model.getIndex());
            }
        }
        return null;
    }
}
