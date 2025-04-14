package com.example.coffee2.Domain;

public class Beverages {
    private int Id;
    private String Loc;
    public Beverages(){

    }

    @Override
    public String toString() {
        return Loc ;
    }

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public String getLoc() {
        return Loc;
    }

    public void setLoc(String loc) {
        Loc = loc;
    }
}
