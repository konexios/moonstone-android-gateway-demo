package com.arrow.jmyiotgateway;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;

import com.arrow.jmyiotgateway.device.DeviceType;
import com.google.firebase.crash.FirebaseCrash;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import org.parceler.Parcel;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static com.arrow.jmyiotgateway.ConfigContentProvider.ConfigContentContract.ConfigEntry.ADDED_DEVICES;
import static com.arrow.jmyiotgateway.ConfigContentProvider.ConfigContentContract.ConfigEntry.API_SECURITY_KEY;
import static com.arrow.jmyiotgateway.ConfigContentProvider.ConfigContentContract.ConfigEntry.APPLICATION_HID;
import static com.arrow.jmyiotgateway.ConfigContentProvider.ConfigContentContract.ConfigEntry.CODE;
import static com.arrow.jmyiotgateway.ConfigContentProvider.ConfigContentContract.ConfigEntry.EMAIL;
import static com.arrow.jmyiotgateway.ConfigContentProvider.ConfigContentContract.ConfigEntry.EXTERNAL_ID;
import static com.arrow.jmyiotgateway.ConfigContentProvider.ConfigContentContract.ConfigEntry.GATEWAY_HID;
import static com.arrow.jmyiotgateway.ConfigContentProvider.ConfigContentContract.ConfigEntry.GATEWAY_UID;
import static com.arrow.jmyiotgateway.ConfigContentProvider.ConfigContentContract.ConfigEntry.IS_ACTIVE;
import static com.arrow.jmyiotgateway.ConfigContentProvider.ConfigContentContract.ConfigEntry.NAME;
import static com.arrow.jmyiotgateway.ConfigContentProvider.ConfigContentContract.ConfigEntry.PASSWORD;
import static com.arrow.jmyiotgateway.ConfigContentProvider.ConfigContentContract.ConfigEntry.PROFILE_NAME;
import static com.arrow.jmyiotgateway.ConfigContentProvider.ConfigContentContract.ConfigEntry.SELECTED_EVENT;
import static com.arrow.jmyiotgateway.ConfigContentProvider.ConfigContentContract.ConfigEntry.SERVER__ENVIRONMENT;
import static com.arrow.jmyiotgateway.ConfigContentProvider.ConfigContentContract.ConfigEntry.USER_ID;
import static com.arrow.jmyiotgateway.ConfigContentProvider.ConfigContentContract.ConfigEntry.ZONE_SYSTEM_NAME;
import static com.arrow.jmyiotgateway.ConfigContentProvider.URI_ACCOUNTS;

@Parcel
public class Config {
    private final static String TAG = Config.class.getSimpleName();

    String gatewayId;
    String userId;
    String name;
    String email;
    String apiSecurityKey;
    String code;
    String applicationHid;
    String externalId;
    String gatewayUid;
    String password;
    String selectedEvent;
    String zoneSystemName;

    String serverEnvironment;
    boolean isActive;
    String profileName;

    List<ConfigDeviceModel> addedDevices;

    public Config() {
    }

    public Config(String gatewayId, String userId, String name, String email, String apiSecurityKey,
                  String code, String applicationHid, String externalId, String gatewayUid,
                  String serverEnvironment, boolean isActive, String profileName,
                  List<ConfigDeviceModel> addedDevices) {
        this.gatewayId = gatewayId;
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.apiSecurityKey = apiSecurityKey;
        this.code = code;
        this.applicationHid = applicationHid;
        this.externalId = externalId;
        this.serverEnvironment = serverEnvironment;
        this.isActive = isActive;
        this.profileName = profileName;
        this.addedDevices = addedDevices;
        this.gatewayUid = gatewayUid;
        this.selectedEvent = "none";
        this.zoneSystemName = "";
    }

    public static List<Config> loadAll(Context context) {
        Cursor cursor = context.getContentResolver().query(
                URI_ACCOUNTS,
                null,
                null,
                null,
                null);
        int accountsCount = cursor.getCount();
        ArrayList<Config> accounts = new ArrayList<>();
        if (accountsCount > 0) {
            cursor.moveToFirst();
            do {
                Config config = new Config();
                config.fillData(context, cursor);
                accounts.add(config);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return accounts;
    }

    public static Config loadActive(Context context) {
        String where = IS_ACTIVE + " =?";
        String[] args = new String[]{" 1"};
        Cursor cursor = context.getContentResolver().query(
                URI_ACCOUNTS,
                null,
                where,
                args,
                null);
        int accountsCount = cursor.getCount();
        Config config = new Config();
        if (accountsCount > 0) {
            cursor.moveToFirst();
            config.fillData(context, cursor);
        }
        cursor.close();
        return config;
    }

    public void save(Context context) {
        ContentValues values = new ContentValues();
        values.put(EMAIL, getEmail());
        values.put(GATEWAY_HID, getGatewayId());
        values.put(USER_ID, getUserId());
        values.put(NAME, getName());
        values.put(CODE, getCode());
        values.put(API_SECURITY_KEY, getApiSecurityKey());
        values.put(APPLICATION_HID, getApplicationHid());
        values.put(EXTERNAL_ID, getExternalId());
        values.put(SERVER__ENVIRONMENT, getServerEnvironment());
        values.put(ADDED_DEVICES, prepareAddedDevices());
        values.put(IS_ACTIVE, isActive ? 1 : 0);
        values.put(PROFILE_NAME, getProfileName());
        values.put(GATEWAY_UID, getGatewayUid());
        values.put(PASSWORD, getPassword());
        values.put(SELECTED_EVENT, getSelectedEvent());
        values.put(ZONE_SYSTEM_NAME, getZoneSystemName());
        context.getContentResolver().insert(URI_ACCOUNTS, values);
        FirebaseCrash.logcat(Log.INFO, TAG, "save, appHid: " + getApplicationHid());
    }

    public String getGatewayId() {
        return gatewayId;
    }

    public void setGatewayId(String gatewayId) {
        this.gatewayId = gatewayId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getApiSecurityKey() {
        return apiSecurityKey;
    }

    public void setApiSecurityKey(String apiSecurityKey) {
        this.apiSecurityKey = apiSecurityKey;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getApplicationHid() {
        return applicationHid;
    }

    public void setApplicationHid(String applicationHid) {
        this.applicationHid = applicationHid;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getServerEnvironment() {
        return serverEnvironment;
    }

    public String getZoneSystemName() {
        return zoneSystemName;
    }

    public void setZoneSystemName(String aZoneSystemName) {
        zoneSystemName = aZoneSystemName;
    }

    public void setServerEnvironment(String serverEnvironment) {
        this.serverEnvironment = serverEnvironment;
    }

    public List<ConfigDeviceModel> getAddedDevices(Context context) {
        if (addedDevices == null) {
            parseAddedDevicesString(context, "");
        }
        return addedDevices;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public String getProfileName() {
        return profileName;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    public String getGatewayUid() {
        return gatewayUid;
    }

    public void setGatewayUid(String gatewayUid) {
        this.gatewayUid = gatewayUid;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSelectedEvent() {
        return selectedEvent;
    }

    public void setSelectedEvent(String selectedEvent) {
        this.selectedEvent = selectedEvent;
    }

    public void addDevice(ConfigDeviceModel model) {
        addedDevices.add(model);
    }

    public void removeDevice(ConfigDeviceModel model) {
        addedDevices.remove(model);
    }

    public void clearAllDevices() {
        for (int i = 0; i < this.addedDevices.size(); i++) {
            this.addedDevices.get(i).setDeviceHid("");
        }
    }

    public void updateDevice(ConfigDeviceModel model) {
        for (ConfigDeviceModel deviceModel : addedDevices) {
            if (deviceModel.getDeviceType().equals(model.getDeviceType())
                    && deviceModel.getIndex() == model.getIndex()) {
                deviceModel.setDeviceHid(model.getDeviceHid());
                deviceModel.setDeviceName(model.getDeviceName());
            }
        }
    }

    private String prepareAddedDevices() {
        if (addedDevices == null) {
            return null;
        }

        return new Gson().toJson(addedDevices);
    }

    private void parseAddedDevicesString(Context context, String devicesJson) {
        addedDevices = new ArrayList<>();
        if (TextUtils.isEmpty(devicesJson)) {
            ConfigDeviceModel model = new ConfigDeviceModel()
                    .setDeviceHid("")
                    .setDeviceName(getDefaultDeviceName(context, DeviceType.AndroidInternal))
                    .setDeviceType(DeviceType.AndroidInternal);
            addedDevices.add(model);
        } else {
            Type type = new TypeToken<List<ConfigDeviceModel>>() {
            }.getType();
            try {
                addedDevices = new Gson().fromJson(devicesJson, type);
            } catch (IllegalStateException e) {
                FirebaseCrash.logcat(Log.ERROR, TAG, "parseAddedDevicesString: " + devicesJson);
                FirebaseCrash.report(e);
            }
        }
    }

    private void fillData(Context context, Cursor cursor) {
        email = cursor.getString(cursor.getColumnIndex(EMAIL));
        gatewayId = cursor.getString(cursor.getColumnIndex(GATEWAY_HID));
        userId = cursor.getString(cursor.getColumnIndex(USER_ID));
        name = cursor.getString(cursor.getColumnIndex(NAME));
        code = cursor.getString(cursor.getColumnIndex(CODE));
        apiSecurityKey = cursor.getString(cursor.getColumnIndex(API_SECURITY_KEY));
        applicationHid = cursor.getString(cursor.getColumnIndex(APPLICATION_HID));
        externalId = cursor.getString(cursor.getColumnIndex(EXTERNAL_ID));
        serverEnvironment = cursor.getString(cursor.getColumnIndex(SERVER__ENVIRONMENT));
        isActive = cursor.getInt(cursor.getColumnIndex(IS_ACTIVE)) != 0;
        profileName = cursor.getString(cursor.getColumnIndex(PROFILE_NAME));
        gatewayUid = cursor.getString(cursor.getColumnIndex(GATEWAY_UID));
        password = cursor.getString(cursor.getColumnIndex(PASSWORD));
        selectedEvent = cursor.getString(cursor.getColumnIndex(SELECTED_EVENT));
        zoneSystemName = cursor.getString(cursor.getColumnIndex(ZONE_SYSTEM_NAME));
        parseAddedDevicesString(context, cursor.getString(cursor.getColumnIndex(ADDED_DEVICES)));
    }

    private String getDefaultDeviceName(Context context, DeviceType type) {
        String result = "";
        switch (type) {
            case MicrosoftBand:
                result = context.getString(R.string.cards_ms_band_title);
                break;
            case SensorPuck:
                result = context.getString(R.string.cards_sensor_puck_title);
                break;
            case AndroidInternal:
                result = context.getString(R.string.cards_android_internal_title);
                break;
            case SenseAbilityKit:
                result = context.getString(R.string.cards_senseability_kit_title);
                break;
            case ThunderBoard:
                result = context.getString(R.string.thunderboard_details_title);
                break;
            case SensorTile:
                result = context.getString(R.string.cards_sensor_tile_title);
                break;
            case SimbaPro:
                result = context.getString(R.string.cards_simba_pro_title);
                break;
        }
        return result;
    }

    public static class ConfigDeviceModel implements Parcelable {
        @SerializedName("deviceType")
        private DeviceType deviceType;
        @SerializedName("deviceHid")
        private String deviceHid;
        @SerializedName("deviceName")
        private String deviceName;
        @SerializedName("index")
        private long index;

        public ConfigDeviceModel() {
        }

        public long getIndex() {
            return index;
        }

        public ConfigDeviceModel setIndex(long index) {
            this.index = index;
            return this;
        }

        public DeviceType getDeviceType() {
            return deviceType;
        }

        public ConfigDeviceModel setDeviceType(DeviceType deviceType) {
            this.deviceType = deviceType;
            return this;
        }

        public String getDeviceHid() {
            return deviceHid;
        }

        public ConfigDeviceModel setDeviceHid(String deviceHid) {
            this.deviceHid = deviceHid;
            return this;
        }

        public String getDeviceName() {
            return deviceName;
        }

        public ConfigDeviceModel setDeviceName(String deviceName) {
            this.deviceName = deviceName;
            return this;
        }

        protected ConfigDeviceModel(android.os.Parcel in) {
            deviceType = (DeviceType) in.readValue(DeviceType.class.getClassLoader());
            deviceHid = in.readString();
            deviceName = in.readString();
            index = in.readLong();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ConfigDeviceModel that = (ConfigDeviceModel) o;

            if (getIndex() != that.getIndex()) return false;
            if (getDeviceType() != that.getDeviceType()) return false;
            if (getDeviceHid() != null ? !getDeviceHid().equals(that.getDeviceHid()) : that.getDeviceHid() != null)
                return false;
            return getDeviceName() != null ? getDeviceName().equals(that.getDeviceName()) : that.getDeviceName() == null;

        }

        @Override
        public int hashCode() {
            int result = deviceType != null ? deviceType.hashCode() : 0;
            result = 31 * result + (deviceHid != null ? deviceHid.hashCode() : 0);
            result = 31 * result + (deviceName != null ? deviceName.hashCode() : 0);
            result = 31 * result + (int) (index ^ (index >>> 32));
            return result;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(android.os.Parcel dest, int flags) {
            dest.writeValue(deviceType);
            dest.writeString(deviceHid);
            dest.writeString(deviceName);
            dest.writeLong(index);
        }

        @SuppressWarnings("unused")
        public static final Parcelable.Creator<ConfigDeviceModel> CREATOR = new Parcelable.Creator<ConfigDeviceModel>() {
            @Override
            public ConfigDeviceModel createFromParcel(android.os.Parcel source) {
                return new ConfigDeviceModel(source);
            }

            @Override
            public ConfigDeviceModel[] newArray(int size) {
                return new ConfigDeviceModel[size];
            }
        };
    }
}
