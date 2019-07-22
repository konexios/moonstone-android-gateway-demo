package com.arrow.jmyiotgateway.device.android;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.arrow.jmyiotgateway.cloud.iot.IotParameter;
import com.arrow.jmyiotgateway.device.DeviceAbstract;
import com.arrow.jmyiotgateway.device.DevicePropertiesAbstract;
import com.arrow.jmyiotgateway.device.DeviceState;
import com.arrow.jmyiotgateway.device.DeviceType;
import com.arrow.jmyiotgateway.device.TelemetriesNames;
import com.arrow.jmyiotgateway.fragments.AbstractDetailsFragment;
import com.arrow.jmyiotgateway.fragments.AndroidInternalDetailsScreen;
import com.arrow.acn.api.models.DeviceRegistrationModel;
import com.google.firebase.crash.FirebaseCrash;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
 * Created by osminin on 6/27/2016.
 */

public final class DeviceAndroid extends DeviceAbstract implements SensorEventListener {
    private final static String TAG = DeviceAndroid.class.getSimpleName();

    private final static String DEVICE_TYPE_NAME = "android";
    private final static String DEVICE_UID_PREFIX = "android-";

    private SensorManager mSensorManager;
    private List<Sensor> mSensors;


    public DeviceAndroid(Context context, long cardId, boolean isLocationNeeded, String deviceHid) {
        super(context, cardId, isLocationNeeded, deviceHid);
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mProperties = new AndroidSensorProperties(context, mSensorManager);
        mSensors = new ArrayList<>();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        handleSensorChangedEvent(event);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public DeviceType getDeviceType() {
        return DeviceType.AndroidInternal;
    }

    @Override
    public String getDeviceUId() {
        final TelephonyManager tm = (TelephonyManager) mContext.
                getSystemService(Context.TELEPHONY_SERVICE);
        final String tmDevice, tmSerial, androidId;
        tmDevice = "" + tm.getDeviceId();
        tmSerial = "" + tm.getSimSerialNumber();
        androidId = "" + android.provider.Settings.Secure.getString(mContext.getContentResolver(),
                android.provider.Settings.Secure.ANDROID_ID);

        UUID deviceUuid = new UUID(androidId.hashCode(), ((long) tmDevice.hashCode() << 32) | tmSerial.hashCode());
        String deviceId = deviceUuid.toString().replace("00000000-", "").replace("ffffffff-", "");
        return DEVICE_UID_PREFIX + deviceId;
    }

    @Override
    protected void enable(long cardId) {
        notifyDeviceStateChanged(DeviceState.Connecting);
        List<Sensor> sensors = AndroidSensorUtils.getSensorsList(mSensorManager);
        for (Sensor sensor : sensors) {
            if (((AndroidSensorProperties) mProperties).isSensorEnabled(sensor.getType())) {
                mSensors.add(sensor);
                mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
                FirebaseCrash.logcat(Log.INFO, TAG, "enable() sensor: " + sensor.getName());
            }
        }
        checkAndRegisterDevice();
        notifyDeviceStateChanged(DeviceState.Connected);
    }

    @Override
    protected void disable(long cardId) {
        notifyDeviceStateChanged(DeviceState.Disconnecting);
        for (Sensor sensor : mSensors) {
            mSensorManager.unregisterListener(this, sensor);
        }
        notifyDeviceStateChanged(DeviceState.Disconnected);
    }

    @Override
    public DeviceState getDeviceState() {
        return mLastState;
    }

    @Override
    public String getDeviceTypeName() {
        return DEVICE_TYPE_NAME;
    }

    @Override
    public AbstractDetailsFragment getDetailsFragment(long cardId) {
        if (mDetailsFragment == null) {
            mDetailsFragment = new AndroidInternalDetailsScreen();
        }
        updateRegistrationModel();
        return mDetailsFragment;
    }

    @Override
    public void updateProperties() {
        mProperties.update();
        for (Iterator<Sensor> iterator = mSensors.iterator(); iterator.hasNext(); ) {
            Sensor current = iterator.next();
            if (!((AndroidSensorProperties) mProperties).isSensorEnabled(current.getType())) {
                mSensorManager.unregisterListener(this, current);
                iterator.remove();
            }
        }
        List<Sensor> sensors = AndroidSensorUtils.getSensorsList(mSensorManager);
        for (Sensor sensor : sensors) {
            if (((AndroidSensorProperties) mProperties).isSensorEnabled(sensor.getType()) && !mSensors.contains(sensor)) {
                mSensors.add(sensor);
                mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
            }
        }
    }

    private void handleSensorChangedEvent(SensorEvent event) {
        int sensorType = event.sensor.getType();
        String x = "", y = "", z = "";
        if (event.values.length >= 1) {
            x = Float.toString(event.values[0]);
        }
        if (event.values.length >= 2) {
            y = Float.toString(event.values[1]);
        }
        if (event.values.length == 3) {
            z = Float.toString(event.values[2]);
        }
        switch (sensorType) {
            case TYPE_ACCELEROMETER:
                putIotParams(new IotParameter(TelemetriesNames.ACCELEROMETER_X, x));
                putIotParams(new IotParameter(TelemetriesNames.ACCELEROMETER_Y, y));
                putIotParams(new IotParameter(TelemetriesNames.ACCELEROMETER_Z, z));
                putIotParams(new IotParameter(TelemetriesNames.ACCELEROMETER_XYZ, String.format("%s|%s|%s", x, y, z)));
                break;
            case TYPE_AMBIENT_TEMPERATURE:
                Float value = Float.valueOf(x)  * 1.8f + 32;
                putIotParams(new IotParameter(TelemetriesNames.TEMPERATURE, value.toString()));
                break;
            case TYPE_GYROSCOPE:
            case TYPE_GYROSCOPE_UNCALIBRATED:
                putIotParams(new IotParameter(TelemetriesNames.GYROSCOPE_X, x));
                putIotParams(new IotParameter(TelemetriesNames.GYROSCOPE_Y, y));
                putIotParams(new IotParameter(TelemetriesNames.GYROSCOPE_Z, z));
                putIotParams(new IotParameter(TelemetriesNames.GYROMETER_XYZ, String.format("%s|%s|%s", x, y, z)));
                break;
            case TYPE_HEART_RATE:
                putIotParams(new IotParameter(TelemetriesNames.HEART_RATE, x));
                break;
            case TYPE_LIGHT:
                putIotParams(new IotParameter(TelemetriesNames.LIGHT, x));
                break;
            case TYPE_MAGNETIC_FIELD:
            case TYPE_MAGNETIC_FIELD_UNCALIBRATED:
                putIotParams(new IotParameter(TelemetriesNames.MAGNETOMETER_X, x));
                putIotParams(new IotParameter(TelemetriesNames.MAGNETOMETER_Y, y));
                putIotParams(new IotParameter(TelemetriesNames.MAGNETOMETER_Z, z));
                putIotParams(new IotParameter(TelemetriesNames.MAGNETOMETER_XYZ, String.format("%s|%s|%s", x, y, z)));
                break;
            case TYPE_PRESSURE:
                putIotParams(new IotParameter(TelemetriesNames.PRESSURE, x));
                break;
            case TYPE_RELATIVE_HUMIDITY:
                putIotParams(new IotParameter(TelemetriesNames.HUMIDITY, x));
                break;
            case TYPE_STEP_COUNTER:
                putIotParams(new IotParameter(TelemetriesNames.STEPS, x));
                break;
        }
    }

    @Override
    protected DeviceRegistrationModel getRegisterPayload() {
        saveDeviceInfo();
        mProperties.update();
        DeviceRegistrationModel payload = super.getRegisterPayload();
        payload.setProperties(mProperties.getPropertiesAsJson());
        payload.setInfo(mProperties.getInfoAsJson());
        return payload;
    }

    private void saveDeviceInfo() {
        Map<DevicePropertiesAbstract.PropertyKeys, Object> map = new HashMap<>();
        map.put(AndroidSensorProperties.AndroidSensorInfoKeys.UID, getDeviceUId());
        map.put(AndroidSensorProperties.AndroidSensorInfoKeys.NAME, getDeviceType().name());
        map.put(AndroidSensorProperties.AndroidSensorInfoKeys.TYPE, getDeviceTypeName());
        mProperties.saveToPreferences(map);
    }
}
