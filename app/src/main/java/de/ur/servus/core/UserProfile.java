package de.ur.servus.core;

import android.graphics.Bitmap;

public class UserProfile {

    private final String name;
    private final String gender;
    private final String birthdate;
    private final String course;
    private final Bitmap picture;

    public UserProfile(String name, String gender, String birthdate, String course, Bitmap picture) {
        this.name = name;
        this.gender = gender;
        this.birthdate = birthdate;
        this.course = course;
        this.picture = picture;
    }

    public String getName() {
        return name;
    }

    public String getGender() {
        return gender;
    }

    public String getBirthdate() {
        return birthdate;
    }

    public String getCourse() {
        return course;
    }

    public Bitmap getPicture() {
        return picture;
    }
}
