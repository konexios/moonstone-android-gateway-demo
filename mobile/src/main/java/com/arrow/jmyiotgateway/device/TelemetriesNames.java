package com.arrow.jmyiotgateway.device;

/**
 * Created by osminin on 4/21/2016.
 */
public interface TelemetriesNames {
    String STEPS = "i|steps";
    String DISTANCE = "i|distance";

    String ACCELEROMETER_X = "f|accelerometerX";
    String ACCELEROMETER_Y = "f|accelerometerY";
    String ACCELEROMETER_Z = "f|accelerometerZ";
    String ACCELEROMETER_XYZ = "f3|accelerometerXYZ";

    String ORIENTATION_X = "f|orientationX";
    String ORIENTATION_Y = "f|orientationY";
    String ORIENTATION_Z = "f|orientationZ";
    String ORIENTATION_XYZ = "f3|orientationXYZ";

    String GYROSCOPE_X = "f|gyroscopeX";
    String GYROSCOPE_Y = "f|gyroscopeY";
    String GYROSCOPE_Z = "f|gyroscopeZ";

    String HEART_RATE = "i|heartRate";
    String UV = "s|uvLevel";
    String SKIN_TEMP = "i|skinTemperature";

    String PRESSURE = "f|pressure";
    String LIGHT = "f|light";
    String TEMPERATURE = "f|temperature";
    String IR_TEMPERATURE = "f|surfaceTemperature";
    String HUMIDITY = "f|humidity";

    String MAGNETOMETER_X = "f|magnetometerX";
    String MAGNETOMETER_Y = "f|magnetometerY";
    String MAGNETOMETER_Z = "f|magnetometerZ";
    String MAGNETOMETER_XYZ = "f3|magnetometerXYZ";

    String GYROMETER_X = "f|gyrometerX";
    String GYROMETER_Y = "f|gyrometerY";
    String GYROMETER_Z = "f|gyrometerZ";
    String GYROMETER_XYZ = "f3|gyrometerXYZ";

    String TIMESTAMP = "_|timestamp";
    String DEVICE_HID = "_|deviceHid";

    String MAGNET = "b|magnet";
    String STATUS = "s|status";
    String AIRFLOW = "f|airflow";

    String MIC_LEVEL = "f|micLevel";
    String SWITCH = "i|switch";

    String LAT_LONG = "f2|latlong";
    String LATITUDE = "f|latitude";
    String LONGITUDE = "f|longitude";

    String LED_0 = "b|led1status";
    String LED_1 = "b|led2status";
}
