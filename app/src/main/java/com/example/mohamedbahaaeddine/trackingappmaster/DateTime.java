package com.example.mohamedbahaaeddine.trackingappmaster;

/**
 * Created by MohamedBahaaEddine on 11/04/2016.
 */
public class DateTime {

    private String hm;
    private String date;

    public DateTime(){
        //blank
    }
    public DateTime(String HM, String date){
        this.hm = HM;
        this.date = date;
    }

    public String getHM(){
        return hm;
    }
    public void setHm(String hm) {
        this.hm = hm;
    }

    public String getDate(){

        return date;
    }
    public void setDate(String date){
        this.date = date;
    }
}
