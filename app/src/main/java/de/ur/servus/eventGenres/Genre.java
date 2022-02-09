package de.ur.servus.eventGenres;

import java.io.Serializable;

public class Genre implements Serializable {

    private String name;
    private int image;

    public Genre(){

    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public int getImage(){
        return image;
    }

    public void setImage(int image){
        this.image = image;
    }
}
