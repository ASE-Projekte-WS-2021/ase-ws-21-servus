package de.ur.servus.eventgenres;

import java.io.Serializable;

public class Genre implements Serializable {

    private final String name;
    private final int image;

    public Genre(String name, int image){
        this.name = name;
        this.image = image;
    }

    public String getName(){
        return name;
    }

    public int getImage(){
        return image;
    }

}
