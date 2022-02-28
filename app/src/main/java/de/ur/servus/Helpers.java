package de.ur.servus;

import static android.content.Context.MODE_PRIVATE;
import static de.ur.servus.SettingsBottomSheetFragment.ACCOUNT;
import static de.ur.servus.SettingsBottomSheetFragment.ACCOUNT_ITEM_ID;

import android.app.Activity;

import java.util.Optional;
import java.util.UUID;

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
}
