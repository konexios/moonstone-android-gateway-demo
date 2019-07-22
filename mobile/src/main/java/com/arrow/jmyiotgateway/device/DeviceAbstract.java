package com.arrow.jmyiotgateway.device;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.arrow.acn.api.AcnApiService;
import com.arrow.acn.api.listeners.RegisterDeviceListener;
import com.arrow.acn.api.models.ApiError;
import com.arrow.acn.api.models.DeviceRegistrationModel;
import com.arrow.acn.api.models.DeviceRegistrationResponse;
import com.arrow.acn.api.models.GatewayEventModel;
import com.arrow.jmyiotgateway.AcnServiceHolder;
import com.arrow.jmyiotgateway.Config;
import com.arrow.jmyiotgateway.Constant;
import com.arrow.jmyiotgateway.cloud.iot.IotConstant;
import com.arrow.jmyiotgateway.cloud.iot.IotDataLoad;
import com.arrow.jmyiotgateway.cloud.iot.IotParameter;
import com.arrow.jmyiotgateway.fragments.AbstractDetailsFragment;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.crash.FirebaseCrash;
import com.google.gson.JsonObject;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.arrow.jmyiotgateway.device.TelemetriesNames.LATITUDE;
import static com.arrow.jmyiotgateway.device.TelemetriesNames.LAT_LONG;
import static com.arrow.jmyiotgateway.device.TelemetriesNames.LONGITUDE;

public abstract class DeviceAbstract implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private final static String TAG = DeviceAbstract.class.getSimpleName();


    protected final Context mContext;
    protected boolean mIsMonitoring;
    protected DeviceState mLastState;
    protected AbstractDetailsFragment mDetailsFragment;
    protected long mCardId;
    protected DevicePropertiesAbstract mProperties;
    private Map<String, IotParameter> mIotParamsMap = new HashMap<>();
    private AcnApiService mConnectService;
    private GoogleApiClient mGoogleApiClient;
    private String mDeviceHid;
    private String mExternalId;
    private Long mOnlineSince;
    private boolean mIsLocationNeeded;
    private Intent mRegistrationIntent;

    public DeviceAbstract(Context context, long cardId, boolean isLocationNeeded, String deviceHid) {
        this.mContext = context;
        mCardId = cardId;
        mDeviceHid = deviceHid;
        mConnectService = AcnServiceHolder.getService();
        mIsLocationNeeded = isLocationNeeded;
        if (isLocationNeeded) {
            initializeLocationService();
        }
        mLastState = DeviceState.Disconnected;
    }

    private void initializeLocationService() {
        mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    public synchronized void putIotParams(IotParameter... params) {
        for (IotParameter param : params) {
            mIotParamsMap.put(param.getKey(), param);
        }
    }

    private synchronized List<IotParameter> getIotParams() {
        if (mIotParamsMap.isEmpty())
            return Collections.emptyList();
        List<IotParameter> result = new ArrayList<>(mIotParamsMap.values());
        mIotParamsMap.clear();
        return result;
    }

    public void saveDeviceId(String deviceId, String externalId) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext.getApplicationContext());
        SharedPreferences.Editor editor = prefs.edit();
        String key = String.format("%s/%s", getDeviceType().name(), Constant.Preference.KEY_DEVICE_EXTERNAL_ID_SUFFIX);
        editor.putString(key, externalId);
        editor.commit();
        Intent intent = new Intent(Constant.ACTION_IOT_DEVICE_REGISTERED);
        intent.putExtra(IotConstant.EXTRA_DATA_LABEL_DEVICE_ID, Parcels.wrap(mDeviceHid));
        intent.putExtra(IotConstant.EXTRA_DATA_LABEL_DEVICE_TYPE, Parcels.wrap(getDeviceType()));
        intent.putExtra(IotConstant.EXTRA_DATA_LABEL_CARD_ID, mCardId);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
        FirebaseCrash.logcat(Log.INFO, TAG, "device hid: " + mDeviceHid);
        FirebaseCrash.logcat(Log.INFO, TAG, String.format("saveDeviceId() key: %s, value: %s", key, deviceId));
    }

    public Context getContext() {
        return mContext;
    }


    public void checkAndRegisterDevice() {
        if (TextUtils.isEmpty(mDeviceHid)) {
            if (!getDeviceUId().contains("null")) {
                FirebaseCrash.logcat(Log.INFO, TAG, "checkAndRegisterDevice() launching registration ...");
                DeviceRegistrationModel payload = getRegisterPayload();
                mConnectService.registerDevice(payload, new RegisterDeviceListener() {
                    @Override
                    public void onDeviceRegistered(DeviceRegistrationResponse response) {
                        mDeviceHid = response.getHid();
                        Config config = new Config().loadActive(mContext);
                        config.updateDevice(new Config.ConfigDeviceModel()
                                .setDeviceHid(mDeviceHid)
                                .setDeviceType(getDeviceType())
                                .setDeviceName(getDeviceTypeName())
                                .setIndex(mCardId));
                        config.save(mContext);
                        mExternalId = response.getExternalId();
                        saveDeviceId(mDeviceHid, mExternalId);
                        FirebaseCrash.logcat(Log.INFO, TAG, "checkAndRegisterDevice() ok");
                    }

                    @Override
                    public void onDeviceRegistrationFailed(ApiError error) {
                        FirebaseCrash.logcat(Log.ERROR, TAG, "checkAndRegisterDevice, code: "
                                + error.getStatus() + ", mesage: " + error.getMessage());
                    }
                });
            }
        } else {
            mRegistrationIntent = new Intent(Constant.ACTION_IOT_DEVICE_REGISTERED);
            mRegistrationIntent.putExtra(IotConstant.EXTRA_DATA_LABEL_DEVICE_ID, Parcels.wrap(mDeviceHid));
            mRegistrationIntent.putExtra(IotConstant.EXTRA_DATA_LABEL_DEVICE_TYPE, Parcels.wrap(getDeviceType()));
            mRegistrationIntent.putExtra(IotConstant.EXTRA_DATA_LABEL_CARD_ID, mCardId);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(mRegistrationIntent);
            FirebaseCrash.logcat(Log.INFO, TAG, "device hid: " + mDeviceHid);
        }
    }

    protected void notifyDeviceStateChanged() {
        notifyDeviceStateChanged(getDeviceState());
    }

    public void notifyDeviceStateChanged(DeviceState state) {
        FirebaseCrash.logcat(Log.DEBUG, TAG, "notifyDeviceStatusChanged(), device: " + getDeviceType().name() + " new state: "
                + state.name());
        Intent intent = new Intent(Constant.ACTION_IOT_DEVICE_STATE_CHANGED);
        intent.putExtra(IotConstant.EXTRA_DATA_LABEL_DEVICE_STATE, Parcels.wrap(state));
        intent.putExtra(IotConstant.EXTRA_DATA_LABEL_DEVICE_ID, Parcels.wrap(getDeviceUId()));
        intent.putExtra(IotConstant.EXTRA_DATA_LABEL_DEVICE_TYPE, Parcels.wrap(getDeviceType()));
        intent.putExtra(IotConstant.EXTRA_DATA_LABEL_CARD_ID, mCardId);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
        mLastState = state;
    }

    public boolean isMonitoring() {
        return mIsMonitoring;
    }

    public abstract DeviceType getDeviceType();

    public abstract String getDeviceUId();

    protected abstract void enable(long cardId);

    protected abstract void disable(long cardId);

    public abstract DeviceState getDeviceState();

    public abstract String getDeviceTypeName();

    public abstract AbstractDetailsFragment getDetailsFragment(long cardId);

    public void updateProperties() {
    }

    protected void handleDeviceStateRequestInternal(String payload) {}

    protected void updateRegistrationModel(){
        if (mDetailsFragment != null && mRegistrationIntent != null) {
            Bundle bundle = mDetailsFragment.getArguments() == null ? new Bundle() : mDetailsFragment.getArguments();
            bundle.putParcelable(Constant.ACTION_IOT_DEVICE_REGISTERED, mRegistrationIntent);
            mDetailsFragment.setArguments(bundle);
        }
    }

    public Runnable handleDeviceStateRequest(final GatewayEventModel eventModel) {
        return new Runnable() {
            @Override
            public void run() {
                Handler handler = new Handler(mContext.getMainLooper());
                final String payload = eventModel.getParameters().get("payload");
                if (mDetailsFragment != null) {
                    if (!TextUtils.isEmpty(payload)) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                mDetailsFragment.handleDeviceStateRequest(payload);
                            }
                        });
                    }
                } else {
                    handleDeviceStateRequestInternal(payload);
                }
            }
        };
    }

    public String[] getTelemetryNames() {
        return mProperties != null ? mProperties.getTelemetryNames() : null;
    }

    public void detachDevice() {
        mDetailsFragment = null;
    }

    public boolean isMultipleDevice() {
        return false;
    }

    protected DeviceRegistrationModel getRegisterPayload() {
        Config config = Config.loadActive(mContext);
        DeviceRegistrationModel payload = new DeviceRegistrationModel();
        payload.setUid(getDeviceUId());
        if (getDeviceType().name().equals("SimbaPro")) {
            payload.setName("SIMBA-PRO");
            payload.setType("simba-pro");
        } else {
            payload.setType(getDeviceTypeName());
            payload.setName(getDeviceType().name());
        }
        payload.setGatewayHid(config.getGatewayId());
        payload.setUserHid(config.getUserId());
        payload.setEnabled(true);
        return payload;
    }

    public Runnable getEnableTask(final long cardId) {
        return new Runnable() {
            @Override
            public void run() {
                preEnable();
                enable(cardId);
                if (mIsLocationNeeded) {
                    mGoogleApiClient.connect();
                    // create location request
                    LocationRequest locationRequest = new LocationRequest();
                    locationRequest.setInterval(Constant.LocationService.DEFAULT_INTERVAL);
                    locationRequest.setFastestInterval(Constant.LocationService.DEFAULT_FASTEST_INTERVAL);
                    locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                }
            }
        };
    }

    public Runnable getDisableTask(final long cardId) {
        return new Runnable() {
            @Override
            public void run() {
                disable(cardId);
                if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                    mGoogleApiClient.disconnect();
                }
                postDisable();
            }
        };
    }

    public Runnable getPollingTask() {
        return new Runnable() {
            @Override
            public void run() {
                if (mIsLocationNeeded) {
                    putNewLocation();
                }
                pollingTask();
            }
        };
    }

    public void pollingTask() {
        try {
            if (mIsLocationNeeded) {
                putNewLocation();
            }
            final List<IotParameter> params = getIotParams();
            if (params.isEmpty()) {
                mIsMonitoring = false;
                if (mLastState != getDeviceState()) {
                    notifyDeviceStateChanged();
                }
                return;
            }

            mIsMonitoring = true;
            IotDataLoad load = new IotDataLoad();
            load.setDeviceId(mDeviceHid);
            load.setTimestamp(System.currentTimeMillis());
            load.setParameters(params);
            Intent telemetryIntent = new Intent(Constant.ACTION_IOT_DATA_RECEIVED);
            Bundle bundle = new Bundle();
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty(TelemetriesNames.TIMESTAMP, load.getTimestamp());
            jsonObject.addProperty(TelemetriesNames.DEVICE_HID, mDeviceHid);

            for (IotParameter param : load.getParameters()) {
                jsonObject.addProperty(param.getKey(), param.getValue());
            }
            bundle.putString(IotConstant.EXTRA_DATA_LABEL_TELEMETRY, jsonObject.toString());
            bundle.putParcelable(IotConstant.EXTRA_DATA_LABEL_DEVICE_TYPE, Parcels.wrap(getDeviceType()));
            if (mOnlineSince == null) {
                mOnlineSince = System.currentTimeMillis();
            }
            bundle.putLong(IotConstant.EXTRA_DATA_LABEL_DEVICE_ONLINE, mOnlineSince);
            bundle.putString(IotConstant.EXTRA_DATA_LABEL_DEVICE_ID, getDeviceUId());
            bundle.putString(IotConstant.EXTRA_DATA_LABEL_EXTERNAL_DEVICE_ID, mExternalId);
            bundle.putLong(IotConstant.EXTRA_DATA_LABEL_CARD_ID, mCardId);
            telemetryIntent.putExtra(IotConstant.EXTRA_DATA_LABEL_TELEMETRY_BUNDLE, bundle);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(telemetryIntent);
            updateUi(telemetryIntent);
            FirebaseCrash.logcat(Log.VERBOSE, TAG, "PollingTask completed");
        } catch (Exception e) {
            FirebaseCrash.logcat(Log.ERROR, TAG, "getPollingTask");
            FirebaseCrash.report(e);
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        putNewLocation();
    }

    private void putNewLocation() {
        if (mGoogleApiClient != null) {
            Location currentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (currentLocation != null) {
                Double lat = currentLocation.getLatitude();
                Double lon = currentLocation.getLongitude();
                putIotParams(new IotParameter(LONGITUDE, lon.toString()));
                putIotParams(new IotParameter(LATITUDE, lat.toString()));
                putIotParams(new IotParameter(LAT_LONG, String.format("%s|%s", lat, lon)));
            } else {
                FirebaseCrash.logcat(Log.INFO, TAG, "putNewLocation() no last known location found");
            }
        }
    }

    private void updateUi(Intent intent) {
        if (mDetailsFragment != null) {
            mDetailsFragment.updateUi(intent);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        //TODO: handle
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        //TODO: handle
    }

    @Override
    public void onLocationChanged(Location location) {
        putNewLocation();
    }

    public void preEnable() {
        mOnlineSince = System.currentTimeMillis();
        if (mIsLocationNeeded) {
            initializeLocationService();
        }
    }

    public void postDisable() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
            mGoogleApiClient = null;
        }
        mIsMonitoring = false;
        mOnlineSince = null;
    }

    public String getDeviceHid() {
        return mDeviceHid;
    }
}
