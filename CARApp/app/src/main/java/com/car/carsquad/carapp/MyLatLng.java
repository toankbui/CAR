package com.car.carsquad.carapp;

import com.google.android.gms.maps.model.LatLng;

public class MyLatLng {
    public Double latitude;
    public Double longitude;

    public MyLatLng() {
        //default constructor
    }
    public MyLatLng(Double latitude, Double longitude){
        this.latitude = latitude;
        this.longitude = longitude;
    }
    public Double getLatitude() {
        return latitude;
    }
    public Double getLongitude() {
        return longitude;
    }
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
}
