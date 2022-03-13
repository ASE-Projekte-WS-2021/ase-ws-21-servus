package de.ur.servus.core;

public class Attendant {
    private final String userId;
    private final boolean isCreator;
    private final String userName;
    private final String userGender;
    private final String userBirthdate;
    private final String userCourse;
    private final String userPicturePath;

    public Attendant(String userId, boolean isCreator, String userName, String userGender, String userBirthdate, String userCourse, String userPicturePath) {
        this.userId = userId;
        this.isCreator = isCreator;
        this.userName = userName;
        this.userGender = userGender;
        this.userBirthdate = userBirthdate;
        this.userCourse = userCourse;
        this.userPicturePath = userPicturePath;
    }

    public static Attendant fromUserProfile(UserProfile userProfile, boolean isCreator, String userPicturePath) {
        return new Attendant(
                userProfile.getUserID(),
                isCreator,
                userProfile.getUserName(),
                userProfile.getUserGender(),
                userProfile.getUserBirthdate(),
                userProfile.getUserCourse(),
                userPicturePath
        );
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
}
