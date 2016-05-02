package com.example.mohamedbahaaeddine.trackingappmaster;

/**
 * Created by MohamedBahaaEddine on 04/04/2016.
 */
public class Locations {
    private  String  idUser;
    private Coords coords;
    private DateTime time;

    public Locations(){

    }

    public Locations(String idUser, Coords coords, DateTime time){
        this.idUser = idUser;
        this.coords = coords;
        this.time = time;
    }


    public String getIdUser() {
        return idUser;
    }

    public void setIdUser(String id){
        this.idUser = id;
    }

    public DateTime getTime(){
        return time;
    }
    public void setTime(DateTime dt){
        this.time = dt;
    }
    public Coords getCoords() {
        return coords;
    }
    public void setCoords(Coords cd){
        this.coords = cd;
    }
}

