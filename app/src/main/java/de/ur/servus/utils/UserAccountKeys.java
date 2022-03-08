package de.ur.servus.utils;

public enum UserAccountKeys {
    ACCOUNT("userAccount"),
    ACCOUNT_ITEM_NAME("Username"),
    ACCOUNT_ITEM_AGE("Birthday"),
    ACCOUNT_ITEM_GENDER("Gender"),
    ACCOUNT_ITEM_COURSE("CourseOfStudy"),
    ACCOUNT_ITEM_ID("UserId"),
    ACCOUNT_EXISTS("accountExists");

    public final String key;

    UserAccountKeys(String key) {
        this.key = key;
    }
}
