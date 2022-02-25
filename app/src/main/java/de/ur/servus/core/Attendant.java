package de.ur.servus.core;

public class Attendant {
    private final String userId;
    private final boolean isCreator;

    public Attendant(String userId, boolean isCreator) {
        this.userId = userId;
        this.isCreator = isCreator;
    }

    public boolean isCreator() {
        return isCreator;
    }

    public String getUserId() {
        return userId;
    }
}
