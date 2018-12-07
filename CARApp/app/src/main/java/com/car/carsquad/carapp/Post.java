package com.car.carsquad.carapp;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

public class Post {
    String userID;
    String postID;
    String startPt;
    String endPt;
    String date;
    String time;
    String cost;
    MyLatLng startLatLgn;
    MyLatLng endLatLgn;
    int availableSeats;
    //Location startLoc;
    //Location endLoc;

    public Post(){
        //default constructor
    }

    public Post(String userID, String postID, String startPt, String endPt, String date, String time, String cost,
                MyLatLng startLatLgn, MyLatLng endLatLgn){
        this.userID = userID;
        this.postID = postID;
        this.startPt = startPt;
        this.endPt = endPt;
        this.date = date;
        this.time = time;
        this.cost = cost;
        this.startLatLgn = startLatLgn;
        this.endLatLgn = endLatLgn;
        //this.startLoc = startLoc;
        //this.endLoc = endLoc;
    }

    public Post(String userID, String postID, String startPt, String endPt, String date, String time, String cost,
                MyLatLng startLatLgn, MyLatLng endLatLgn, int availableSeats){
        this.userID = userID;
        this.postID = postID;
        this.startPt = startPt;
        this.endPt = endPt;
        this.date = date;
        this.time = time;
        this.cost = cost;
        this.startLatLgn = startLatLgn;
        this.endLatLgn = endLatLgn;
        this.availableSeats = availableSeats;
        //this.startLoc = startLoc;
        //this.endLoc = endLoc;
    }

    public int getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(int availableSeats) {
        this.availableSeats = availableSeats;
    }

    public String getUserID() {
        return userID;
    }
    public String getPostID() {
        return postID;
    }
    public String getDate() {
        return date;
    }
    public String getTime() {
        return time;
    }
    public String getEndPt() {
        return endPt;
    }
    public String getStartPt() {
        return startPt;
    }
    public String getCost() {
        return cost;
    }

    public void setStartLatLgn(MyLatLng startLatLgn) {
        this.startLatLgn = startLatLgn;
    }

    public void setEndLatLgn(MyLatLng endLatLgn) {
        this.endLatLgn = endLatLgn;
    }

    public MyLatLng getStartLatLgn() {
        return startLatLgn;
    }

    public MyLatLng getEndLatLgn() {
        return endLatLgn;
    }

    /*
    public LatLng getEndLatLgn() {
        return endLatLgn;
    }
    public LatLng getStartLatLgn() {
        return startLatLgn;
    }*/
/*
    public void setEndLatLgn(MyLatLng endLatLgn) {
        this.endLatLgn = endLatLgn;
    }
    public void setStartLatLgn(MyLatLng startLatLgn) {
        this.startLatLgn = startLatLgn;
    }
*/

/*
    public MyLatLng getEndLoc() {
        return endLoc;
    }

    public MyLatLng getStartLoc() {
        return startLoc;
    }

    public void setEndLoc(Location endLoc) {
        this.endLoc = endLoc;
    }
    public void setStartLoc(Location startLoc){
        this.startLoc = startLoc;
    }*/
}
