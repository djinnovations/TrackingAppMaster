package com.example.mohamedbahaaeddine.trackingappmaster;

/**
 * Created by MohamedBahaaEddine on 22/04/2016.
 */
public class Itinerary {
    private String idUser;
    private Coords objective;
    private String status;

    public Itinerary(){
        //Blank
    }

    public Itinerary(String idUser, Coords Objective,String Status){
        this.idUser = idUser;
        this.objective = Objective;
        this.status = Status;
    }

    public String getIdUser(){
        return idUser;
    }

    public void setIdUser(String idUser){
        this.idUser = idUser;
    }

    public Coords getObjective(){
        return objective;
    }

    public void setObjective(Coords objective){
        this.objective = objective;
    }

    public String getStatus(){
        return status;
    }

    public void setStatus(String status){
        this.status = status;
    }
}
