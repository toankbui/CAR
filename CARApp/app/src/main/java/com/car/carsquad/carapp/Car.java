package com.car.carsquad.carapp;

public class Car {

    String userID;
    String carID;
    int numSeats;
    int originalNumSeats;
    String model;
    String licensePlate;

    public Car(){
        //default constructor
    }

    public Car(int originalNumSeats, String carID, int numSeats, String model, String licensePlate){
        this.originalNumSeats = originalNumSeats;
        this.userID = userID;
        this.carID = carID;
        this.numSeats = numSeats;
        this.model = model;
        this.licensePlate = licensePlate;
    }

    public int getOriginalNumSeats() {
        return originalNumSeats;
    }
    public void setOriginalNumSeats(int originalNumSeats) {
        this.originalNumSeats = originalNumSeats;
    }
    public String getUserID() {
        return userID;
    }
    public String getCarID() {
        return carID;
    }
    public int getNumSeats() {
        return numSeats;
    }
    public String getModel() {
        return model;
    }
    public String getLicensePlate() {
        return licensePlate;
    }
}
