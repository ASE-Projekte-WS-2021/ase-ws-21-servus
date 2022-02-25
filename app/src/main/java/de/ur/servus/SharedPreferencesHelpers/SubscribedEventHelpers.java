package de.ur.servus.SharedPreferencesHelpers;


import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.function.Consumer;

import de.ur.servus.CustomLocationManager;
import de.ur.servus.EventCreationBottomSheet.EventCreationData;
import de.ur.servus.Helpers;
import de.ur.servus.core.Attendant;
import de.ur.servus.core.Event;
import de.ur.servus.core.EventListener;
import de.ur.servus.core.firebase.FirestoreBackendHandler;

/**
 * Contains helper functions to save events to the database and manage shared preferences.
 */
public class SubscribedEventHelpers {
    public static final String SP_ATTENDING_EVENT = "subscribedToEvent";
    public static final String SUBSCRIBED_TO_EVENT = "subscribedToEvent";
    private final String NO_EVENT = "none";

    private final Activity activity;
    private final SharedPreferences sharedPreferences;

    public SubscribedEventHelpers(Activity activity) {
        this.activity = activity;
        this.sharedPreferences = activity.getSharedPreferences(SP_ATTENDING_EVENT, MODE_PRIVATE);
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
        String eventId = sharedPreferences.getString(SUBSCRIBED_TO_EVENT, NO_EVENT);

        return new CurrentSubscribedEventData(eventId.equals(NO_EVENT) ? null : eventId);
    }

    /**
     * Saves event to shared preferences as currently attending event.
     */
    public void saveAttendingEvent(CurrentSubscribedEventData currentSubscribedEventData) {
        var editor = sharedPreferences.edit();
        editor.putString(SUBSCRIBED_TO_EVENT, currentSubscribedEventData.eventId);
        editor.apply();
    }

    /**
     * Removes currently attending event from shared preferences.
     */
    public void removeAttendingEvent() {
        var editor = sharedPreferences.edit();
        editor.putString(SUBSCRIBED_TO_EVENT, NO_EVENT);
        editor.apply();
    }

    public void createEvent(CustomLocationManager locationManager, EventCreationData inputEventData, EventListener<String> afterCreationListener) {
        locationManager.getLastObservedLocation(latLng -> {
            if (!latLng.isPresent()) {
                Log.e("eventCreation", "No location was provided.");
                return;
            }
            var userId = Helpers.readOwnUserId(activity);

            if (!userId.isPresent()) {
                Log.e("eventCreation", "No own user id found.");
                return;
            }

            var owner = new Attendant(userId.get(), true);
            var attendants = new ArrayList<Attendant>();
            attendants.add(owner);

            Event event = new Event(inputEventData.name, inputEventData.description, latLng.get(), attendants, inputEventData.genre);

            FirestoreBackendHandler.getInstance().createNewEvent(event, afterCreationListener);

        });
    }

}
