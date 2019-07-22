package com.arrow.jmyiotgateway.miramonti.acn.models.eventModel;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * Created by batrakov on 19.01.18.
 */

public class EventVerifyResponse implements Parcelable {

    @SerializedName("applicationHid")
    private String applicationHid;
    @SerializedName("userHid")
    private String userHid;
    @SerializedName("companyHid")
    private String companyHid;

    @SuppressWarnings("unused")
    public static final Creator<EventVerifyResponse> CREATOR = new Creator<EventVerifyResponse>() {
        @Override
        public EventVerifyResponse createFromParcel(Parcel in) {
            return new EventVerifyResponse(in);
        }

        @Override
        public EventVerifyResponse[] newArray(int size) {
            return new EventVerifyResponse[size];
        }
    };


    private EventVerifyResponse(Parcel in) {
        applicationHid = in.readString();
        userHid = in.readString();
        companyHid = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(applicationHid);
        dest.writeString(userHid);
        dest.writeString(companyHid);
    }

    public String getApplicationHid() {
        return applicationHid;
    }

    public void setApplicationHid(String aApplicationHid) {
        applicationHid = aApplicationHid;
    }

    public String getUserHid() {
        return userHid;
    }

    public void setUserHid(String aUserHid) {
        userHid = aUserHid;
    }

    public String getCompanyHid() {
        return companyHid;
    }

    public void setCompanyHid(String aCompanyHid) {
        companyHid = aCompanyHid;
    }
}
