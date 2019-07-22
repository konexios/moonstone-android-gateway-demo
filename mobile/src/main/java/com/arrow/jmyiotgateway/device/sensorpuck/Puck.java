package com.arrow.jmyiotgateway.device.sensorpuck;

import android.content.Context;

import com.arrow.jmyiotgateway.device.DeviceAbstract;
import com.arrow.jmyiotgateway.device.DeviceState;
import com.arrow.jmyiotgateway.device.DeviceType;
import com.arrow.jmyiotgateway.fragments.AbstractDetailsFragment;
import com.arrow.jmyiotgateway.fragments.SensorPuckDetailsFragment;

import static com.arrow.jmyiotgateway.device.sensorpuck.SensorPuck.DEVICE_TYPE_NAME;

/**
 * Created by osminin on 6/8/2016.
 */
public class Puck extends DeviceAbstract {
    protected final static String DEVICE_UID_PREFIX = "sl-android-";

    private String mAddress;
    private int mAmbientLight;
    private float mBattery;
    private int mHRM_PrevSample;
    private int mHRM_Rate;
    private int[] mHRM_Sample;
    private int mHRM_State;
    private float mHumidity;
    private int mIdleCount;
    private int mLostAdv;
    private int mLostCount;
    private int mMeasurementMode;
    private String mName;
    private int mPrevCount;
    private int mPrevSequence;
    private int mRecvCount;
    private int mSequence;
    private float mTemperature;
    private int mUV_Index;
    private int mUniqueCount;

    private boolean mIsAttached;

    public Puck(Context mContext, long cardId, boolean isLocationNeeded, String deviceHid) {
        super(mContext, cardId, isLocationNeeded, deviceHid);
    }

    public String getAddress() {
        return mAddress;
    }

    public void setAddress(String address) {
        mAddress = address;
    }

    public int getAmbientLight() {
        return mAmbientLight;
    }

    public void setAmbientLight(int ambientLight) {
        mAmbientLight = ambientLight;
    }

    public float getBattery() {
        return mBattery;
    }

    public void setBattery(float battery) {
        mBattery = battery;
    }

    public int getHRM_PrevSample() {
        return mHRM_PrevSample;
    }

    public void setHRM_PrevSample(int HRM_PrevSample) {
        mHRM_PrevSample = HRM_PrevSample;
    }

    public int getHRM_Rate() {
        return mHRM_Rate;
    }

    public void setHRM_Rate(int HRM_Rate) {
        mHRM_Rate = HRM_Rate;
    }

    public int[] getHRM_Sample() {
        if (mHRM_Sample == null) {
            mHRM_Sample = new int[SensorPuck.SD_UV_LIGHT];
        }
        return mHRM_Sample;
    }

    public void setHRM_Sample(int[] HRM_Sample) {
        mHRM_Sample = HRM_Sample;
    }

    public int getHRM_State() {
        return mHRM_State;
    }

    public void setHRM_State(int HRM_State) {
        mHRM_State = HRM_State;
    }

    public float getHumidity() {
        return mHumidity;
    }

    public void setHumidity(float humidity) {
        mHumidity = humidity;
    }

    public int getIdleCount() {
        return mIdleCount;
    }

    public void setIdleCount(int idleCount) {
        mIdleCount = idleCount;
    }

    public int getLostAdv() {
        return mLostAdv;
    }

    public void setLostAdv(int lostAdv) {
        mLostAdv = lostAdv;
    }

    public int getLostCount() {
        return mLostCount;
    }

    public void setLostCount(int lostCount) {
        mLostCount = lostCount;
    }

    public int getMeasurementMode() {
        return mMeasurementMode;
    }

    public void setMeasurementMode(int measurementMode) {
        mMeasurementMode = measurementMode;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public int getPrevCount() {
        return mPrevCount;
    }

    public void setPrevCount(int prevCount) {
        mPrevCount = prevCount;
    }

    public int getPrevSequence() {
        return mPrevSequence;
    }

    public void setPrevSequence(int prevSequence) {
        mPrevSequence = prevSequence;
    }

    public int getRecvCount() {
        return mRecvCount;
    }

    public void setRecvCount(int recvCount) {
        mRecvCount = recvCount;
    }

    public int getSequence() {
        return mSequence;
    }

    public void setSequence(int sequence) {
        mSequence = sequence;
    }

    public float getTemperature() {
        return mTemperature;
    }

    public void setTemperature(float temperature) {
        mTemperature = temperature;
    }

    public int getUV_Index() {
        return mUV_Index;
    }

    public void setUV_Index(int UV_Index) {
        mUV_Index = UV_Index;
    }

    public int getUniqueCount() {
        return mUniqueCount;
    }

    public void setUniqueCount(int uniqueCount) {
        mUniqueCount = uniqueCount;
    }

    public boolean isAttached() {
        return mIsAttached;
    }

    public void setIsAttached(boolean isAttached) {
        this.mIsAttached = isAttached;
    }

    @Override
    public DeviceType getDeviceType() {
        return DeviceType.SensorPuck;
    }

    @Override
    public String getDeviceUId() {
        if (getAddress() != null) {
            return DEVICE_UID_PREFIX + getAddress().toLowerCase().replace(":", "");
        }
        return "";
    }

    @Override
    protected void enable(long cardId) {
        //nothing
    }

    @Override
    protected void disable(long cardId) {
        //nothing
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
            mDetailsFragment = new SensorPuckDetailsFragment();
            mDetailsFragment.setCardId(cardId);
            mDetailsFragment.setDevice(this);
        }
        return mDetailsFragment;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Puck puck = (Puck) o;

        return mAddress.equals(puck.mAddress);

    }

    @Override
    public int hashCode() {
        return mAddress.hashCode();
    }
}
