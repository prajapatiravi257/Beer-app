package com.planetnoobs.rxjava.network.model;

public class Beer {
    int id;
    String name;
    String style;
    String timestamp;
    String ounces;
    String abv;

    public String getOunces() {
        return ounces;
    }

    public void setOunces(String ounces) {
        this.ounces = ounces;
    }

    public String getAbv() {
        return abv;
    }

    public void setAbv(String abv) {
        this.abv = abv;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
