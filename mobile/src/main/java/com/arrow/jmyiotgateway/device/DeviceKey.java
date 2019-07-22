package com.arrow.jmyiotgateway.device;

public final class DeviceKey {
    private final DeviceType mDeviceType;
    private final long mIndex;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DeviceKey deviceKey = (DeviceKey) o;

        if (mIndex != deviceKey.mIndex) return false;
        return mDeviceType == deviceKey.mDeviceType;
    }

    @Override
    public int hashCode() {
        int result = mDeviceType != null ? mDeviceType.hashCode() : 0;
        result = 31 * result + (int) (mIndex ^ (mIndex >>> 32));
        return result;
    }

    public DeviceType getDeviceType() {

        return mDeviceType;
    }

    public long getIndex() {
        return mIndex;
    }

    public DeviceKey(DeviceType deviceType, long index) {
        mDeviceType = deviceType;
        mIndex = index;
    }
}
