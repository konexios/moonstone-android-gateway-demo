package com.arrow.jmyiotgateway.miramonti.acn.models.eventModel;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

/**
 * Created by batrakov on 18.01.18.
 */

public class EventAccountRequest implements Parcelable {

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<EventAccountRequest> CREATOR = new Parcelable.Creator<EventAccountRequest>() {
        @NonNull
        @Override
        public EventAccountRequest createFromParcel(@NonNull Parcel in) {
            return new EventAccountRequest(in);
        }

        @NonNull
        @Override
        public EventAccountRequest[] newArray(int size) {
            return new EventAccountRequest[size];
        }
    };


    @SerializedName("name")
    private String name;
    @SerializedName("email")
    private String email;
    @SerializedName("password")
    private String password;
    @SerializedName("eventCode")
    private String eventCode;
    @SerializedName("socialEventHid")
    private String socialEventHid;

    private EventAccountRequest(@NonNull Parcel in) {
        name = in.readString();
        email = in.readString();
        password = in.readString();
        eventCode = in.readString();
        socialEventHid = in.readString();
    }

    public EventAccountRequest() {

    }

    public String getEventCode() {
        return eventCode;
    }

    public void setApplicationCode(String code) {
        this.eventCode = code;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setSocialEventHid(String aSocialEventHid) {
        socialEventHid = aSocialEventHid;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(email);
        dest.writeString(password);
        dest.writeString(eventCode);
        dest.writeString(socialEventHid);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EventAccountRequest that = (EventAccountRequest) o;

        return (name != null ? name.equals(that.name) : that.name == null)
                && (email != null ? email.equals(that.email) : that.email == null)
                && (password != null ? password.equals(that.password) : that.password == null)
                && (eventCode != null ? eventCode.equals(that.eventCode) : that.eventCode == null);

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (email != null ? email.hashCode() : 0);
        result = 31 * result + (password != null ? password.hashCode() : 0);
        result = 31 * result + (eventCode != null ? eventCode.hashCode() : 0);
        return result;
    }
}
