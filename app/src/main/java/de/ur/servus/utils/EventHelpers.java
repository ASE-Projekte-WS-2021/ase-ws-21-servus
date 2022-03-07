package de.ur.servus.utils;


import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.function.Consumer;

import de.ur.servus.CustomLocationManager;
import de.ur.servus.core.Attendant;
import de.ur.servus.core.Event;
import de.ur.servus.core.EventListener;
import de.ur.servus.core.firebase.FirestoreBackendHandler;
import de.ur.servus.eventcreationbottomsheet.EventCreationData;

/**
 * Contains helper functions to save events to the database and manage shared preferences.
 */
public class EventHelpers {
    public static final String CURRENT_EVENT = "currentEvent";
    public static final String CURRENT_EVENT_ITEM_ID = "id";

    private final Activity activity;
    private final SharedPreferences sharedPreferences;

    public EventHelpers(Activity activity) {
        this.activity = activity;
        this.sharedPreferences = activity.getSharedPreferences(CURRENT_EVENT, MODE_PRIVATE);
    }

    /**
     * Check, if a event is saved in preferences and react to it.
     *
     * @param then Consumer, that gets event information. Can be null.
     * @param els  Runnable, that is called when no subscribed to an event. Can be null.
     */
    public void ifSubscribedToEvent(@Nullable Consumer<CurrentSubscribedEventData> then, @Nullable Runnable els) {
        var eventPreferences = tryGetSubscribedEvent();

        if (eventPreferences.eventId != null) {
            if (then != null) {
                then.accept(eventPreferences);
            }
        } else {
            if (els != null) {
                els.run();
            }
        }
    }

    /**
     * Tries to get the currently attending event from shared preferences.
     *
     * @return An object, that may be an event id, or may be null. Needs to be checked before using.
     */
    public CurrentSubscribedEventData tryGetSubscribedEvent() {
        String eventId = sharedPreferences.getString(CURRENT_EVENT_ITEM_ID, null);

        return new CurrentSubscribedEventData(eventId);
    }

    /**
     * Saves event to shared preferences as currently attending event.
     */
    public void saveAttendingEvent(CurrentSubscribedEventData currentSubscribedEventData) {
        var editor = sharedPreferences.edit();
        editor.putString(CURRENT_EVENT_ITEM_ID, currentSubscribedEventData.eventId);
        editor.apply();
    }

    /**
     * Removes currently attending event from shared preferences.
     */
    public void removeAttendingEvent() {
        var editor = sharedPreferences.edit();
        editor.remove(CURRENT_EVENT_ITEM_ID);
        editor.apply();
    }

    public void createEvent(CustomLocationManager locationManager, EventCreationData inputEventData, EventListener<String> afterCreationListener) {
        var avatarEditor = new AvatarEditor(activity);
        var userAccountHelpers = new UserAccountHelpers(activity);

        locationManager.getLastObservedLocation(latLng -> {
            if (!latLng.isPresent()) {
                Log.e("eventCreation", "No location was provided.");
                return;
            }
            var profile = userAccountHelpers.getOwnProfile(avatarEditor);

            if (profile.getUserID() == null) {
                Log.e("eventCreation", "No own user id found.");
                return;
            }

            // TODO add profile picture path
            var owner = Attendant.fromUserProfile(profile, true, "tbd");
            var attendants = new ArrayList<Attendant>();
            attendants.add(owner);

            Event event = new Event(inputEventData.name, inputEventData.description, latLng.get(), attendants, inputEventData.genre);

            FirestoreBackendHandler.getInstance().createNewEvent(event, afterCreationListener);
        });
    }

}
