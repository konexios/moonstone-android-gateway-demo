package com.arrow.jmyiotgateway.miramonti.acn.models.eventModel;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by batrakov on 18.01.18.
 */

public class SocialEventListResponse implements Parcelable {

    @SerializedName("data")
    private ArrayList<SocialEventResponse> data;
    @SerializedName("size")
    private int size;

    @SuppressWarnings("unused")
    public static final Creator<SocialEventListResponse> CREATOR = new Creator<SocialEventListResponse>() {
        @Override
        public SocialEventListResponse createFromParcel(Parcel in) {
            return new SocialEventListResponse(in);
        }

        @Override
        public SocialEventListResponse[] newArray(int size) {
            return new SocialEventListResponse[size];
        }
    };

    private SocialEventListResponse(Parcel in) {

        Gson gson = new Gson();
        Type listType = new TypeToken<List<SocialEventResponse>>(){}.getType();
        List<SocialEventResponse> posts = gson.fromJson(in.readString(), listType);

        for (SocialEventResponse response :
                posts) {
            System.out.println(response.getName());
        }
        size = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }

    public ArrayList<SocialEventResponse> getData() {
        return data;
    }

    public void setData(ArrayList<SocialEventResponse> aData) {
        data = aData;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int aSize) {
        size = aSize;
    }
}
