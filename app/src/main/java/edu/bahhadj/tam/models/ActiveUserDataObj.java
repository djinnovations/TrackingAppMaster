package edu.bahhadj.tam.models;

/**
 * Created by COMP on 5/3/2016.
 */
public class ActiveUserDataObj {

    private String name;
    private String sigStrength;

    public ActiveUserDataObj(String name, String sigStrength) {
        this.name = name;
        this.sigStrength = sigStrength;
    }


    public String getName() {
        return name;
    }

    public String getSigStrength() {
        return sigStrength;
    }
}
