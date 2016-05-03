package edu.bahhadj.tam.utils;

/**
 * Created by MohamedBahaaEddine on 05/04/2016.
 */
public class Coords {

    private double longitude;
    private double latitude;

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {

        return longitude;
    }

    public void setLongitude(double lng){

        this.longitude = lng;
    }

    public double getLatitude() {

        return latitude;
    }


    private Coords(){

    }

    public Coords(double latitude, double longitude){
        this.latitude = latitude;
        this.longitude = longitude;
    }

}

