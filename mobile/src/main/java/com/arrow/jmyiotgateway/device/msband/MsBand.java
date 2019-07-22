package com.arrow.jmyiotgateway.device.msband;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.arrow.jmyiotgateway.activities.SettingsActivity;
import com.arrow.jmyiotgateway.cloud.iot.IotParameter;
import com.arrow.jmyiotgateway.device.DeviceAbstract;
import com.arrow.jmyiotgateway.device.DevicePropertiesAbstract;
import com.arrow.jmyiotgateway.device.DeviceState;
import com.arrow.jmyiotgateway.device.DeviceType;
import com.arrow.jmyiotgateway.device.TelemetriesNames;
import com.arrow.jmyiotgateway.fragments.AbstractDetailsFragment;
import com.arrow.jmyiotgateway.fragments.MsBandDetailsFragment;
import com.arrow.acn.api.models.DeviceRegistrationModel;
import com.google.firebase.crash.FirebaseCrash;
import com.microsoft.band.BandClient;
import com.microsoft.band.BandClientManager;
import com.microsoft.band.BandException;
import com.microsoft.band.BandInfo;
import com.microsoft.band.ConnectionState;
import com.microsoft.band.UserConsent;
import com.microsoft.band.sensors.BandAccelerometerEvent;
import com.microsoft.band.sensors.BandAccelerometerEventListener;
import com.microsoft.band.sensors.BandDistanceEvent;
import com.microsoft.band.sensors.BandDistanceEventListener;
import com.microsoft.band.sensors.BandGyroscopeEvent;
import com.microsoft.band.sensors.BandGyroscopeEventListener;
import com.microsoft.band.sensors.BandHeartRateEvent;
import com.microsoft.band.sensors.BandHeartRateEventListener;
import com.microsoft.band.sensors.BandPedometerEvent;
import com.microsoft.band.sensors.BandPedometerEventListener;
import com.microsoft.band.sensors.BandSensorManager;
import com.microsoft.band.sensors.BandSkinTemperatureEvent;
import com.microsoft.band.sensors.BandSkinTemperatureEventListener;
import com.microsoft.band.sensors.BandUVEvent;
import com.microsoft.band.sensors.BandUVEventListener;
import com.microsoft.band.sensors.HeartRateQuality;
import com.microsoft.band.sensors.SampleRate;

import java.util.HashMap;
import java.util.Map;


public class MsBand extends DeviceAbstract implements BandHeartRateEventListener,
        BandSkinTemperatureEventListener, BandAccelerometerEventListener, BandGyroscopeEventListener,
        BandUVEventListener, BandPedometerEventListener, BandDistanceEventListener {
    private final static String TAG = MsBand.class.getSimpleName();

    private final static String DEVICE_TYPE_NAME = "ms-band";
    private final static String DEVICE_UID_PREFIX = "band-android-";

    private BandInfo mBandInfo;
    private BandClient mClient;

    public MsBand(Context context, long cardId, boolean isLocationNeeded, String deviceHid) {
        super(context, cardId, isLocationNeeded, deviceHid);
        mProperties = new MsBandProperties(context);
    }

    @Override
    public DeviceType getDeviceType() {
        return DeviceType.MicrosoftBand;
    }

    @Override
    public String getDeviceTypeName() {
        return DEVICE_TYPE_NAME;
    }

    @Override
    public AbstractDetailsFragment getDetailsFragment(long cardId) {
        if (mDetailsFragment == null) {
            mDetailsFragment = new MsBandDetailsFragment();
        }
        updateRegistrationModel();
        return mDetailsFragment;
    }

    @Override
    public String getDeviceUId() {
        return mBandInfo == null ? null : DEVICE_UID_PREFIX + mBandInfo.getMacAddress().toLowerCase().replace(":", "");
    }

    public void requestHeartRateConsent(final SettingsActivity activity) {
        Handler handler = new Handler(activity.getMainLooper());
        try {
            if (silentConnect()) {
                if (!isHeartRateConsentAccepted()) {
                    mClient.getSensorManager().requestHeartRateConsent(activity, activity);
                } else {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            activity.heartRateAlreadyAccepted();
                        }
                    });
                }
            } else {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        activity.onHeartRateRequestFailed();
                    }
                });
            }
        } catch (Exception e) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    activity.onHeartRateRequestFailed();
                }
            });
            FirebaseCrash.logcat(Log.ERROR, TAG, "requestHeartRateConsent");
            FirebaseCrash.report(e);
        }
    }

    @Override
    protected void enable(long cardId) {
        FirebaseCrash.logcat(Log.INFO, TAG, "enable() ...");
        try {
            if (connect()) {
                FirebaseCrash.logcat(Log.INFO, TAG, "band is connected");
                notifyDeviceStateChanged();
                // check current consent
                if (!isHeartRateConsentAccepted()) {
                    FirebaseCrash.logcat(Log.INFO, TAG, "requestHeartRateConsent ...");
                }
                registerListeners();
            } else {
                notifyDeviceStateChanged();
                FirebaseCrash.logcat(Log.INFO, TAG, "band is not connected");
            }
        } catch (Exception e) {
            notifyDeviceStateChanged(DeviceState.Error);
            FirebaseCrash.logcat(Log.ERROR, TAG, "enable");
            FirebaseCrash.report(e);
        }

    }

    @Override
    public void disable(long cardId) {
        FirebaseCrash.logcat(Log.INFO, TAG, "disable() ...");
        notifyDeviceStateChanged(DeviceState.Disconnecting);
        try {
            if (mClient != null) {
                FirebaseCrash.logcat(Log.INFO, TAG, "disable() unregistering all listeners ...");
                mClient.getSensorManager().unregisterAllListeners();
                FirebaseCrash.logcat(Log.INFO, TAG, "disable() disconnecting ...");
                mClient.disconnect().await();
                notifyDeviceStateChanged(DeviceState.Disconnected);
                mClient = null;
            }
        } catch (Exception e) {
            FirebaseCrash.logcat(Log.ERROR, TAG, e.getMessage());
            FirebaseCrash.report(e);
        }
    }

    @Override
    public DeviceState getDeviceState() {
        DeviceState state = DeviceState.Disconnected;
        if (mClient != null) {
            switch (mClient.getConnectionState()) {
                case DISPOSED:
                case UNBOUND:
                    break;
                case INVALID_SDK_VERSION:
                    state = DeviceState.Error;
                    break;
                case BINDING:
                    state = DeviceState.Connecting;
                    break;
                case UNBINDING:
                    state = DeviceState.Disconnecting;
                    break;
                case BOUND:
                    state = DeviceState.Bound;
                    break;
                case CONNECTED:
                    state = DeviceState.Connected;
                    break;
            }
        }
        return mIsMonitoring ? DeviceState.Monitoring : state;
    }

    private boolean connect(boolean isSilent) throws InterruptedException, BandException {
        if (mClient == null) {
            if (!isSilent) {
                notifyDeviceStateChanged(DeviceState.Connecting);
            }
            BandInfo[] devices = BandClientManager.getInstance().getPairedBands();
            if (devices.length == 0) {
                FirebaseCrash.logcat(Log.INFO, TAG, "connect() band not found");
                return false;
            }
            mBandInfo = devices[0];
            FirebaseCrash.logcat(Log.INFO, TAG, "connect() band found: " + mBandInfo.getName() + " / " + mBandInfo.getMacAddress());
            mClient = BandClientManager.getInstance().create(getContext(), mBandInfo);

            // mBandInfo registration
            checkAndRegisterDevice();
        } else if (mClient.getConnectionState() == ConnectionState.CONNECTED) {
            if (!isSilent) {
                notifyDeviceStateChanged();
            }
            return true;
        }

        FirebaseCrash.logcat(Log.INFO, TAG, "connect() trying ...");
        ConnectionState state = mClient.connect().await();
        return ConnectionState.CONNECTED == state;
    }

    private boolean silentConnect() throws InterruptedException, BandException {
        return connect(true);
    }

    private boolean connect() throws InterruptedException, BandException {
        return connect(false);
    }

    @Override
    public void onBandAccelerometerChanged(BandAccelerometerEvent bandAccelerometerEvent) {
        String x = Double.toString(bandAccelerometerEvent.getAccelerationX() * 9.81);
        String y = Double.toString(bandAccelerometerEvent.getAccelerationY() * 9.81);
        String z = Double.toString(bandAccelerometerEvent.getAccelerationZ() * 9.81);
        FirebaseCrash.logcat(Log.VERBOSE, TAG, "onBandAccelerometerChanged() " + x + " / " + y + " / " + z);
        putIotParams(
                new IotParameter(TelemetriesNames.ACCELEROMETER_X, x),
                new IotParameter(TelemetriesNames.ACCELEROMETER_Y, y),
                new IotParameter(TelemetriesNames.ACCELEROMETER_Z, z),
                new IotParameter(TelemetriesNames.ACCELEROMETER_XYZ, String.format("%s|%s|%s", x, y, z)));
    }

    @Override
    public void onBandDistanceChanged(BandDistanceEvent bandDistanceEvent) {
        long distance = (long) (bandDistanceEvent.getTotalDistance() * 0.0328084); // cm -> ft
        FirebaseCrash.logcat(Log.VERBOSE, TAG, "onBandDistanceChanged() " + distance);
        putIotParams(new IotParameter(TelemetriesNames.DISTANCE, Long.toString(distance)));
    }

    @Override
    public void onBandGyroscopeChanged(BandGyroscopeEvent bandGyroscopeEvent) {
        String x = Double.toString(bandGyroscopeEvent.getAngularVelocityX());
        String y = Double.toString(bandGyroscopeEvent.getAngularVelocityY());
        String z = Double.toString(bandGyroscopeEvent.getAngularVelocityZ());
        FirebaseCrash.logcat(Log.VERBOSE, TAG, "onBandGyroscopeChanged() " + x + " / " + y + " / " + z);
        putIotParams(
                new IotParameter(TelemetriesNames.GYROSCOPE_X, x),
                new IotParameter(TelemetriesNames.GYROSCOPE_Y, y),
                new IotParameter(TelemetriesNames.GYROSCOPE_Z, z),
                new IotParameter(TelemetriesNames.GYROMETER_XYZ, String.format("%s|%s|%s", x, y, z)));
    }

    @Override
    public void onBandHeartRateChanged(BandHeartRateEvent bandHeartRateEvent) {
        HeartRateQuality quality = bandHeartRateEvent.getQuality();
        int heartRate = bandHeartRateEvent.getHeartRate();
        FirebaseCrash.logcat(Log.VERBOSE, TAG, "onBandHeartRateChanged() heartRate: " + quality.toString() + ": " + heartRate);
        if (quality == HeartRateQuality.LOCKED) {
            putIotParams(new IotParameter(TelemetriesNames.HEART_RATE, Integer.toString(heartRate)));
        }
    }

    @Override
    public void onBandPedometerChanged(BandPedometerEvent bandPedometerEvent) {
        long steps = bandPedometerEvent.getTotalSteps();
        FirebaseCrash.logcat(Log.VERBOSE, TAG, "onBandPedometerChanged() " + steps);
        putIotParams(new IotParameter(TelemetriesNames.STEPS, Long.toString(steps)));
    }

    @Override
    public void onBandSkinTemperatureChanged(BandSkinTemperatureEvent bandSkinTemperatureEvent) {
        int temperature = (int) ((bandSkinTemperatureEvent.getTemperature() * 9) / 5) + 32;
        FirebaseCrash.logcat(Log.VERBOSE, TAG, "onBandSkinTemperatureChanged() temperature: " + temperature);
        putIotParams(new IotParameter(TelemetriesNames.SKIN_TEMP, Integer.toString(temperature)));
    }

    @Override
    public void onBandUVChanged(BandUVEvent bandUVEvent) {
        String level = bandUVEvent.getUVIndexLevel().toString();
        FirebaseCrash.logcat(Log.VERBOSE, TAG, "onBandUVChanged() " + level);
        putIotParams(new IotParameter(TelemetriesNames.UV, level));
    }

    private void registerListeners() {
        if (mClient == null) return;

        try {
            BandSensorManager manager = mClient.getSensorManager();

            manager.unregisterAllListeners();
            MsBandProperties msBandProperties = (MsBandProperties) mProperties;
            if (isHeartRateConsentAccepted() &&
                    msBandProperties.isSensorEnabled(MsBandProperties.MsBandPropertiesKeys.HEART_RATE_SENSOR_ENABLED)) {
                manager.registerHeartRateEventListener(this);
            }

            if (msBandProperties.isSensorEnabled(MsBandProperties.MsBandPropertiesKeys.SKIN_TEMPERATURE_SENSOR_ENABLED)) {
                FirebaseCrash.logcat(Log.INFO, TAG, "registering skinTemperatureEventListener ...");
                manager.registerSkinTemperatureEventListener(this);
            }

            if (msBandProperties.isSensorEnabled(MsBandProperties.MsBandPropertiesKeys.ACCELEROMETER_SENSOR_ENABLED)) {
                FirebaseCrash.logcat(Log.INFO, TAG, "registering accelerometerEventListener ... ");
                manager.registerAccelerometerEventListener(this, SampleRate.MS128);
            }

            if (msBandProperties.isSensorEnabled(MsBandProperties.MsBandPropertiesKeys.GYROSCOPE_SENSOR_ENABLED)) {
                FirebaseCrash.logcat(Log.INFO, TAG, "registering gyroscopeEventListener ... ");
                manager.registerGyroscopeEventListener(this, SampleRate.MS128);
            }

            if (msBandProperties.isSensorEnabled(MsBandProperties.MsBandPropertiesKeys.UV_SENSOR_ENABLED)) {
                FirebaseCrash.logcat(Log.INFO, TAG, "registering uvEventListener ...");
                manager.registerUVEventListener(this);
            }

            if (msBandProperties.isSensorEnabled(MsBandProperties.MsBandPropertiesKeys.PEDOMETER_SENSOR_ENABLED)) {
                FirebaseCrash.logcat(Log.INFO, TAG, "registering pedometerEventListener ...");
                manager.registerPedometerEventListener(this);
            }

            if (msBandProperties.isSensorEnabled(MsBandProperties.MsBandPropertiesKeys.DISTANCE_SENSOR_ENABLED)) {
                FirebaseCrash.logcat(Log.INFO, TAG, "registering distanceEventListener ...");
                manager.registerDistanceEventListener(this);
            }
        } catch (BandException e) {
            String exceptionMessage = "";
            switch (e.getErrorType()) {
                case UNSUPPORTED_SDK_VERSION_ERROR:
                    exceptionMessage = "Microsoft Health BandService doesn't support your SDK Version. Please update to latest SDK.";
                    break;
                case SERVICE_ERROR:
                    exceptionMessage = "Microsoft Health BandService is not available. Please make sure Microsoft Health is installed and that you have the correct permissions.";
                    break;
                default:
                    exceptionMessage = "Unknown error occurred: " + e.getMessage();
                    break;
            }
            FirebaseCrash.logcat(Log.INFO, TAG, exceptionMessage);
            FirebaseCrash.report(e);
        } catch (Exception e) {
            FirebaseCrash.logcat(Log.ERROR, TAG, e.getMessage());
            FirebaseCrash.report(e);
        }
    }

    private boolean isHeartRateConsentAccepted() {
        return mClient != null && mClient.getSensorManager().getCurrentHeartRateConsent() == UserConsent.GRANTED;
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
        map.put(MsBandProperties.MsBandInfoKeys.UID, getDeviceUId());
        map.put(MsBandProperties.MsBandInfoKeys.NAME, getDeviceType().name());
        map.put(MsBandProperties.MsBandInfoKeys.TYPE, getDeviceTypeName());
        mProperties.saveToPreferences(map);
    }

    @Override
    public void updateProperties() {
        mProperties.update();
        registerListeners();
    }
}
