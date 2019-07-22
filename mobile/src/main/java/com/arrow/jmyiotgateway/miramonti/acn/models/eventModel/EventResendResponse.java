package com.arrow.jmyiotgateway.miramonti.acn.models.eventModel;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * Created by batrakov on 19.01.18.
 */

public class EventResendResponse implements Parcelable {

    @SerializedName("status")
    private String status;
    @SerializedName("message")
    private String message;

    @SuppressWarnings("unused")
    public static final Creator<EventResendResponse> CREATOR = new Creator<EventResendResponse>() {
        @Override
        public EventResendResponse createFromParcel(Parcel in) {
            return new EventResendResponse(in);
        }

        @Override
        public EventResendResponse[] newArray(int size) {
            return new EventResendResponse[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    private EventResendResponse(Parcel in) {
        status = in.readString();
        message = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(status);
        dest.writeString(message);
    }
}
