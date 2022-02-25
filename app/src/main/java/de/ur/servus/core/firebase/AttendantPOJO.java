package de.ur.servus.core.firebase;

import de.ur.servus.core.Attendant;

public class AttendantPOJO implements POJO<Attendant> {
    private String userId;
    private boolean isCreator;

    public AttendantPOJO() {
    }

    public AttendantPOJO(Attendant attendant){
        this.userId = attendant.getUserId();
        this.isCreator = attendant.isCreator();
    }

    public boolean isCreator() {
        return isCreator;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setCreator(boolean creator) {
        isCreator = creator;
    }

    @Override
    public Attendant toObject() {
        return new Attendant(userId, isCreator);
    }
}
