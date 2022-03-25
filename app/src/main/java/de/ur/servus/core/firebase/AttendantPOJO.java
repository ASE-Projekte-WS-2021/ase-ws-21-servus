package de.ur.servus.core.firebase;

import de.ur.servus.core.Attendant;

public class AttendantPOJO implements POJO<Attendant> {
    private String userId;
    private boolean isCreator;
    private String userName;
    private String userGender;
    private String userBirthdate;
    private String userCourse;
    private String userPicturePath;

    public AttendantPOJO() {
    }

    public AttendantPOJO(Attendant attendant){
        this.userId = attendant.getUserId();
        this.isCreator = attendant.isCreator();
        this.userName = attendant.getUserName();
        this.userGender = attendant.getUserGender();
        this.userBirthdate = attendant.getUserBirthdate();
        this.userCourse = attendant.getUserCourse();
        this.userPicturePath = "";
    }

    public boolean isCreator() {
        return isCreator;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserGender() {
        return userGender;
    }

    public String getUserBirthdate() {
        return userBirthdate;
    }

    public String getUserCourse() {
        return userCourse;
    }

    public String getUserPicturePath() {
        return userPicturePath;
    }

    public void setCreator(boolean creator) {
        this.isCreator = creator;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setUserName(String username) {
        this.userName = username;
    }

    public void setUserGender(String gender) {
        this.userGender = gender;
    }

    public void setUserBirthdate(String birthdate) {
        this.userBirthdate = birthdate;
    }

    public void setUserCourse(String course) {
        this.userCourse = course;
    }

    public void setUserPicturePath(String path) {
        this.userPicturePath = path;
    }

    @Override
    public Attendant toObject() {
        return new Attendant(userId, isCreator, userName, userGender, userBirthdate, userCourse, userPicturePath);
    }
}
