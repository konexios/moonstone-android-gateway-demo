package com.arrow.jmyiotgateway.device.sensortile.data;

import android.os.SystemClock;

import com.arrow.jmyiotgateway.cloud.iot.IotParameter;

public abstract class SensorData<T> {
    private final T data;
    private final long timestamp;

    public SensorData(T data) {
        this.data = data;
        this.timestamp = SystemClock.currentThreadTimeMillis();

    }

    public T getData() {
        return data;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public abstract IotParameter[] toIotParameters();
}
