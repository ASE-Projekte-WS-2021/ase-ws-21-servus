package de.ur.servus.utils;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.content.SharedPreferences;

import java.util.UUID;

import de.ur.servus.core.UserProfile;

public class UserAccountHelpers {
    private final SharedPreferences sharedPreferences;

    public UserAccountHelpers(Activity activity) {
        this.sharedPreferences = activity.getSharedPreferences(UserAccountKeys.ACCOUNT.key, MODE_PRIVATE);
    }

    public void saveNewUserIdIfNotExisting() {
        if (readStringValue(UserAccountKeys.ACCOUNT_ITEM_ID, null) == null) {
            var uuid = UUID.randomUUID();
            saveStringValue(UserAccountKeys.ACCOUNT_ITEM_ID, uuid.toString());
        }
    }

    public void saveStringValue(UserAccountKeys key, String value) {
        sharedPreferences.edit().putString(key.key, value).apply();
    }

    public void saveBooleanValue(UserAccountKeys key, boolean value) {
        sharedPreferences.edit().putBoolean(key.key, value).apply();
    }

    public String readStringValue(UserAccountKeys key, String defaultValue) {
        var value = sharedPreferences.getString(key.key, "none");
        return !value.equals("none") ? value : defaultValue;
    }

    public boolean readBooleanValue(UserAccountKeys key, boolean defaultValue) {
        var value = sharedPreferences.getBoolean(key.key, false);
        return sharedPreferences.contains(key.key) ? value : defaultValue;
    }

    public UserProfile getOwnProfile(AvatarEditor avatarEditor) {
        return new UserProfile(
                readStringValue(UserAccountKeys.ACCOUNT_ITEM_ID, null),
                readStringValue(UserAccountKeys.ACCOUNT_ITEM_NAME, ""),
                readStringValue(UserAccountKeys.ACCOUNT_ITEM_GENDER, ""),
                readStringValue(UserAccountKeys.ACCOUNT_ITEM_AGE, ""),
                readStringValue(UserAccountKeys.ACCOUNT_ITEM_COURSE, ""),
                avatarEditor.loadProfilePicture()
        );
    }

}
