package com.arrow.jmyiotgateway;

public class Constant {
    public final static boolean DEBUG = false;

    public final static boolean DEV_ENVIRONMENT = true;

    public final static String SOFTWARE_NAME = "JMyIotGateway";

    public final static String SENSEABILITY_NAME = "Senseability 2";

    public final static String SP_LOCATION_SERVICE = "com.arrow.jmyiotgateway.location_service";
    public final static String SP_SENDING_RATE = "com.arrow.jmyiotgateway.sending_rate";
    public final static String SP_MSBAND_HEART_RATE = "com.arrow.jmyiotgateway.msband_heart_rate";
    public final static String SP_CLOUD_HERATBEAT_INTERVAL = "com.arrow.jmyiotgateway.cloud_service_heartbeat";

    public final static int DEFAULT_DEVICE_POLLING_INTERVAL = 1; // 1 second
    public final static int DEFAULT_BLE_SCAN_TIMEOUT = 10000; // 10 seconds
    public final static int HEART_BEAT_INTERVAL = 60; //1 minute

    public static final String DEFAULT_API_KEY = "PUT-YOUR-API-KEY-HERE";
    public static final String DEFAULT_API_SECRET = "PUT-YOUR-SECRET-KEY-HERE";

    // EVENT SHARED PREFERENCES CONST
    public static final String SHARED_PREFS = "shared_prefs";
    public static final String EMAIL = "email";
    public static final String NAME = "name";
    public static final String CODE = "code";
    public static final String SERVER__ENVIRONMENT = "server_environment";
    public static final String PROFILE_NAME = "profile_name";
    public static final String PASSWORD = "password";
    public static final String SELECTED_EVENT = "selected_event_code";
    public static final String ZONE_SYSTEM_NAME = "zone_system_name";
    public static final String ACTIVE_EVENTS = "active_events";

    // DEV
    public static final String ARROW_CONNECT_URL_DEV = "http://pgsdev01.arrowconnect.io:11003";
    public static final String IOT_CONNECT_URL_DEV = "http://pgsdev01-<zone-name>.arrowconnect.io:12001";
    public static final String MQTT_CONNECT_URL_DEV = "tcp://pgsdev01-<zone-name>.arrowconnect.io:1883";
    public static final String MQTT_CLIENT_PREFIX_DEV = "/themis.dev";

    // DEMO
    public static final String ARROW_CONNECT_URL_DEMO = "https://acs-api.arrowconnect.io";
    public static final String IOT_CONNECT_URL_DEMO = "https://api-<zone-name>.arrowconnect.io";
    public static final String MQTT_CONNECT_URL_DEMO = "ssl://mqtt-<zone-name>.arrowconnect.io:8883";
    public static final String MQTT_CLIENT_PREFIX_DEMO = "/pegasus";

    public static final String ACTION_IOT_DATA_RECEIVED = "com.arrow.jmyiotgateway.action.iot_data_received";
    public static final String ACTION_IOT_DEVICE_POLLING_SERVICE_COMMAND = "com.arrow.jmyiotgateway.action.iot_device_polling_command";
    public static final String ACTION_IOT_DEVICE_STATE_CHANGED = "com.arrow.jmyiotgateway.action.iot_device_state_changed";
    public static final String ACTION_IOT_DEVICE_STATE_REQUEST = "com.arrow.jmyiotgateway.action.iot_device_state_request";
    public static final String ACTION_IOT_DEVICE_REGISTERED = "com.arrow.jmyiotgateway.action.iot_device_registered";

    public static final String CONFIG_EXTRA_INFO = "config_extra_info";

    public static class LocationService {
        public final static int DEFAULT_INTERVAL = 20000; // 20 secs
        public final static int DEFAULT_FASTEST_INTERVAL = 10000; // 10 sec
    }

    public static class Preference {
        public final static String KEY_DEVICE_ID_SUFFIX = "device-id";
        public final static String KEY_DEVICE_EXTERNAL_ID_SUFFIX = "external-device-id";
        public final static String KEY_SELECTED_EVENT = "selected-event-code";
    }
}
