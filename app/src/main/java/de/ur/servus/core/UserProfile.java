package de.ur.servus.core;

import android.graphics.Bitmap;

public class UserProfile {

    private final String userID;
    private final String name;
    private final String gender;
    private final String birthdate;
    private final String course;
    private final Bitmap picture;

    public UserProfile(String userID, String name, String gender, String birthdate, String course, Bitmap picture) {
        this.userID = userID;
        this.name = name;
        this.gender = gender;
        this.birthdate = birthdate;
        this.course = course;
        this.picture = picture;
    }

    public String getUserID() {
        return userID;
    }

    public String getUserName() {
        return name;
    }

    public String getUserGender() {
        return gender;
    }

    public String getUserBirthdate() {
        return birthdate;
    }

    public String getUserCourse() {
        return course;
    }

    public Bitmap getUserPicture() {
        return picture;
    }
}
