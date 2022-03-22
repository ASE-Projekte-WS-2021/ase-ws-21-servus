package de.ur.servus.eventgenres;

import java.io.Serializable;

public class Genre implements Serializable {

    private String name;
    private final int image;

    public Genre(String name, int image){
        this.name = name;
        this.image = image;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getName(){
        return name;
    }

    public int getImage(){
        return image;
    }

}
