package com.arrow.jmyiotgateway.device;

import org.parceler.Parcel;

@Parcel
public enum DeviceState {
    Disconnected,
    Disconnecting,
    Bound,
    Connecting,
    Connected,
    Monitoring,
    Reconnecting,
    Error
}
