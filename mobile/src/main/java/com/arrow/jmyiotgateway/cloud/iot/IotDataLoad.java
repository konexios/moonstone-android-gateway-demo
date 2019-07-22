package com.arrow.jmyiotgateway.cloud.iot;

import java.util.List;

public class IotDataLoad {
    private String deviceId;
    private long timestamp;
    private Double longitude;
    private Double latitude;
    private List<IotParameter> parameters;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public List<IotParameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<IotParameter> parameters) {
        this.parameters = parameters;
    }
}
