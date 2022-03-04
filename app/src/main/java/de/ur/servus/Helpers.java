package de.ur.servus;

import static android.content.Context.MODE_PRIVATE;
import static de.ur.servus.SettingsBottomSheetFragment.ACCOUNT;
import static de.ur.servus.SettingsBottomSheetFragment.ACCOUNT_ITEM_AGE;
import static de.ur.servus.SettingsBottomSheetFragment.ACCOUNT_ITEM_COURSE;
import static de.ur.servus.SettingsBottomSheetFragment.ACCOUNT_ITEM_GENDER;
import static de.ur.servus.SettingsBottomSheetFragment.ACCOUNT_ITEM_ID;
import static de.ur.servus.SettingsBottomSheetFragment.ACCOUNT_ITEM_NAME;

import android.app.Activity;
import android.util.Log;

import java.util.Optional;
import java.util.UUID;

import de.ur.servus.core.UserProfile;
import de.ur.servus.utils.AvatarEditor;

public class Helpers {

    private static void saveOwnUserId(Activity activity, String id) {
        var sharedPreferences = activity.getSharedPreferences(ACCOUNT, MODE_PRIVATE);
        var editor = sharedPreferences.edit();
        editor.putString(ACCOUNT_ITEM_ID, id);
        editor.apply();
    }

    public static Optional<String> readOwnUserId(Activity activity) {
        var sharedPreferences = activity.getSharedPreferences(ACCOUNT, MODE_PRIVATE);
        var userId = sharedPreferences.getString(ACCOUNT_ITEM_ID, "none");

        if (!userId.equals("none")) {
            return Optional.of(userId);
        } else {
            return Optional.empty();
        }
    }

    public static void saveNewUserIdIfNotExisting(Activity activity) {
        var uuid = UUID.randomUUID();

        if (!readOwnUserId(activity).isPresent()) {
            saveOwnUserId(activity, uuid.toString());
        }
    }

    public static UserProfile loadLocalAccountDataIntoUserProfile(Activity activity){
        var sharedPreferences = activity.getSharedPreferences(ACCOUNT, MODE_PRIVATE);
        AvatarEditor avatarEditor = new AvatarEditor(activity);

        UserProfile localProfile = new UserProfile(
                sharedPreferences.getString(ACCOUNT_ITEM_ID, "DEF_ID"),
                sharedPreferences.getString(ACCOUNT_ITEM_NAME, "DEF_NAME"),
                sharedPreferences.getString(ACCOUNT_ITEM_GENDER, "DEF_GENDER"),
                sharedPreferences.getString(ACCOUNT_ITEM_AGE, "DEF_BIRTHDATE"),
                sharedPreferences.getString(ACCOUNT_ITEM_COURSE, "DEF_COURSE"),
                avatarEditor.loadProfilePicture());

        Log.d("Profile", localProfile.toString());
        Log.d("Profile", localProfile.getUserID());
        Log.d("Profile", localProfile.getUserName());
        Log.d("Profile", localProfile.getUserGender());
        Log.d("Profile", localProfile.getUserBirthdate());
        Log.d("Profile", localProfile.getUserCourse());
        Log.d("Profile", localProfile.getUserPicture().toString());

        return localProfile;
    }
}
