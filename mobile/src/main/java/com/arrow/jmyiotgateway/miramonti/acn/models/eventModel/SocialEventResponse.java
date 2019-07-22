package com.arrow.jmyiotgateway.miramonti.acn.models.eventModel;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;

/**
 * Created by batrakov on 18.01.18.
 */

public class SocialEventResponse implements Parcelable {

    @SerializedName("hid")
    private String hid;
    @SerializedName("links")
    private JsonElement links;
    @SerializedName("name")
    private String name;
    @SerializedName("startDate")
    private String startDate;
    @SerializedName("endDate")
    private String endDate;
    @SerializedName("zoneHid")
    private String zoneHid;
    @SerializedName("zoneSystemName")
    private String zoneSystemName;


    @SuppressWarnings("unused")
    public static final Creator<SocialEventResponse> CREATOR = new Creator<SocialEventResponse>() {
        @Override
        public SocialEventResponse createFromParcel(Parcel in) {
            return new SocialEventResponse(in);
        }

        @Override
        public SocialEventResponse[] newArray(int size) {
            return new SocialEventResponse[size];
        }
    };

    private SocialEventResponse(Parcel in) {
        hid = in.readString();
        JsonParser parser = new JsonParser();
        links = parser.parse(in.readString()).getAsJsonObject();
        name = in.readString();
        startDate = in.readString();
        endDate = in.readString();
        zoneHid = in.readString();
        zoneSystemName = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(hid);
        String str = new Gson().toJson(getLinks());
        dest.writeString(str);
        dest.writeString(name);
        dest.writeString(startDate);
        dest.writeString(endDate);
        dest.writeString(zoneHid);
        dest.writeString(zoneSystemName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SocialEventResponse response = (SocialEventResponse) o;

        return (hid != null ? hid.equals(response.hid) : response.hid == null)
                && (name != null ? name.equals(response.name) : response.name == null)
                && (links != null ? links.equals(response.links) : response.links == null)
                && (startDate != null ? startDate.equals(response.startDate) : response.startDate == null)
                && (endDate != null ? endDate.equals(response.endDate) : response.endDate == null)
                && (zoneHid != null ? zoneHid.equals(response.zoneHid) : response.zoneHid == null)
                && (zoneSystemName != null ? zoneSystemName.equals(response.zoneSystemName) : response.zoneSystemName == null);
    }

    @Override
    public int hashCode() {
        int result = hid != null ? hid.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (links != null ? links.hashCode() : 0);
        result = 31 * result + (startDate != null ? startDate.hashCode() : 0);
        result = 31 * result + (endDate != null ? endDate.hashCode() : 0);
        result = 31 * result + (zoneHid != null ? zoneHid.hashCode() : 0);
        result = 31 * result + (zoneSystemName != null ? zoneSystemName.hashCode() : 0);
        return result;
    }


    public String getHid() {
        return hid;
    }

    public void setHid(String aHid) {
        hid = aHid;
    }

    public JsonElement getLinks() {
        return links;
    }

    public void setLinks(JsonElement aLinks) {
        links = aLinks;
    }

    public String getName() {
        return name;
    }

    public void setName(String aName) {
        name = aName;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String aStartDate) {
        startDate = aStartDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String aEndDate) {
        endDate = aEndDate;
    }

    public String getZoneHid() {
        return zoneHid;
    }

    public void setZoneHid(String aZoneHid) {
        zoneHid = aZoneHid;
    }

    public String getZoneSystemName() {
        return zoneSystemName;
    }

    public void setZoneSystemName(String aZoneSystemName) {
        zoneSystemName = aZoneSystemName;
    }
}
