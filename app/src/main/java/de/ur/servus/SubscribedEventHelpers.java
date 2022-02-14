package de.ur.servus;


import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.function.Consumer;

import de.ur.servus.core.Event;
import de.ur.servus.core.EventListener;
import de.ur.servus.core.firebase.FirestoreBackendHandler;

class EventPreferences {
    @Nullable
    public final String eventId;
    public final boolean isCreator;

    EventPreferences(@Nullable String eventId, boolean isCreator) {
        this.eventId = eventId;
        this.isCreator = isCreator;
    }
}

/**
 * Contains helper functions to save events to the database and manage shared preferences.
 */
public class SubscribedEventHelpers {
    public static final String SUBSCRIBED_TO_EVENT = "subscribedToEvent";
    public static final String IS_EVENT_CREATOR = "isEventCreator";
    private final String NO_EVENT = "none";

    private final SharedPreferences sharedPreferences;

    public SubscribedEventHelpers(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    /**
     * Check, if a event is saved in preferences and react to it.
     *
     * @param then Consumer, that gets event information. Can be null.
     * @param els  Runnable, that is called when no subscribed to an event. Can be null.
     */
    public void ifSubscribedToEvent(@Nullable Consumer<EventPreferences> then, @Nullable Runnable els) {
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
    public EventPreferences tryGetSubscribedEvent() {
        String eventId = sharedPreferences.getString(SUBSCRIBED_TO_EVENT, NO_EVENT);
        boolean isCreator = sharedPreferences.getBoolean(IS_EVENT_CREATOR, false);

        return new EventPreferences(eventId.equals(NO_EVENT) ? null : eventId, isCreator);
    }

    /**
     * Saves event to shared preferences as currently attending event.
     */
    public void saveAttendingEvent(EventPreferences eventPreferences) {
        var editor = sharedPreferences.edit();
        editor.putString(SUBSCRIBED_TO_EVENT, eventPreferences.eventId);
        editor.putBoolean(IS_EVENT_CREATOR, eventPreferences.isCreator);
        editor.apply();
    }

    /**
     * Removes currently attending event from shared preferences.
     */
    public void removeAttendingEvent() {
        var editor = sharedPreferences.edit();
        editor.putString(SUBSCRIBED_TO_EVENT, NO_EVENT);
        editor.putBoolean(IS_EVENT_CREATOR, false);
        editor.apply();
    }

    public void createEvent(CustomLocationManager locationManager, EventCreationData inputEventData, EventListener<String> afterCreationListener) {
        locationManager.getLastObservedLocation(latLng -> {
            if (!latLng.isPresent()) {
                Log.e("eventCreation", "Could not create event.");
                return;
            }

            Event event = new Event(inputEventData.name, inputEventData.description, latLng.get(), 0);

            FirestoreBackendHandler.getInstance().createNewEvent(event, afterCreationListener);

        });
    }

}
