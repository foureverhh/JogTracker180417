package com.nackademin.foureverhh.jogtracker180417;

import android.os.Parcel;
import android.os.Parcelable;

public class MyLatLng implements Parcelable{

    private double latitude;
    private double longitude;

    public MyLatLng(){
    }

    public MyLatLng(double latitude, double longitude){
        this.latitude = latitude;
        this.longitude = longitude;
    }

    protected MyLatLng(Parcel in) {
        latitude = in.readDouble();
        longitude = in.readDouble();
    }

    public static final Creator<MyLatLng> CREATOR = new Creator<MyLatLng>() {
        @Override
        public MyLatLng createFromParcel(Parcel in) {
            return new MyLatLng(in);
        }

        @Override
        public MyLatLng[] newArray(int size) {
            return new MyLatLng[size];
        }
    };

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
    }
}
