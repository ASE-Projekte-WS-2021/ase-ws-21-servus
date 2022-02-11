package de.ur.servus;


import android.app.Activity;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.Optional;
import java.util.function.Consumer;

import de.ur.servus.core.Event;
import de.ur.servus.core.EventListener;
import de.ur.servus.core.firebase.FirestoreBackendHandler;

// TODO better name/structuring of this stuff
public class Helpers {
    public static final String SUBSCRIBED_TO_EVENT = "subscribedToEvent";

    /**
     * Helper, to check, if a event is saved in preferences and react to it.
     *
     * @param then Consumer, that gets an event id. Can be null.
     * @param els  Runnable, that is called when no subscribed to an event. Can be null.
     */
    public static void ifSubscribedToEvent(SharedPreferences sharedPreferences, @Nullable Consumer<String> then, @Nullable Runnable els) {
        var subscribed = tryGetSubscribedEvent(sharedPreferences);

        if (subscribed.isPresent()) {
            if (then != null) {
                then.accept(subscribed.get());
            }
        } else {
            if (els != null) {
                els.run();
            }
        }
    }

    /**
     * Tries to get the currently attending event from shared preferences.
     * @param sharedPreferences
     * @return An object, that may be an event id, or may be null. Needs to be checked before using.
     */
    public static Optional<String> tryGetSubscribedEvent(SharedPreferences sharedPreferences) {
        final String NONE = "none";
        String subscribed = sharedPreferences.getString(SUBSCRIBED_TO_EVENT, NONE);

        if (!subscribed.equals(NONE) ) {
            return Optional.of(subscribed);
        } else {
            return Optional.empty();
        }
    }

    /**
     * Saves event to shared preferences as currently attending event.
     *
     * @param sharedPreferences
     */
    public static void saveAttendingEvent(SharedPreferences sharedPreferences, String eventId) {
        var editor = sharedPreferences.edit();
        editor.putString(SUBSCRIBED_TO_EVENT, eventId);
        editor.apply();
    }

    /**
     * Removes currently attending event from shared preferences.
     *
     * @param sharedPreferences
     */
    public static void removeAttendingEvent(SharedPreferences sharedPreferences) {
        var editor = sharedPreferences.edit();
        editor.putString(SUBSCRIBED_TO_EVENT, "none");
        editor.apply();
    }

    public static void createEvent(CustomLocationManager locationManager, EventCreationData inputEventData,  EventListener<String> afterCreationListener){
        locationManager.getLastObservedLocation(latLng -> {
            if(!latLng.isPresent()){
                Log.e("eventCreation", "Could not create event.");
                return;
            }

            Event event = new Event(inputEventData.name, inputEventData.description, latLng.get(), 0);

            FirestoreBackendHandler.getInstance().createNewEvent(event, afterCreationListener);

        });
    }

    /**
     * Informs the backend about the attendance of an Event and saves its id in the SharedPreferences.
     *
     * @param eventId The ID of the Event to attend to.
     */
    public static void attendEvent(Activity activity, String eventId) {
        // call backend to increment attendant counter
        // trigger subscription to Event (and remove any old subscriptions to specific Events), after first call to backend is finished
        // save eventId in SharedPreferences as currentEventId

        /*

        SharedPreferences sharedPreferences = activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        final String SUBSCRIBED_TO_EVENT = "subscribedToEvent";

        Button attend_withdraw_meetup = activity.findViewById(R.id.event_details_button);

        BackendHandler bh_attend_withdraw = FirestoreBackendHandler.getInstance();
        bh_attend_withdraw.incrementEventAttendants(eventId);

        attend_withdraw_meetup.setText(R.string.event_details_button_withdraw);
        attend_withdraw_meetup.setBackgroundResource(R.drawable.style_btn_roundedcorners_clicked);
        attend_withdraw_meetup.setTextColor(activity.getResources().getColor(R.color.servus_pink, activity.getTheme()));

        editor.putString(SUBSCRIBED_TO_EVENT, eventId);
        editor.apply();

        */
    }

    /**
     * Informs the backend about the leave of an Event and clears the SharedPreferences.
     */
    public static void leaveEvent(Activity activity, String eventId) {
        // check, if currently in event
        // call backend to decrement attendant counter
        // remove any old subscriptions to specific Events
        // set currentEventId in SharedPreferences to a null value

        /*

        SharedPreferences sharedPreferences = activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        final String SUBSCRIBED_TO_EVENT = "subscribedToEvent";

        Button attend_withdraw_meetup = activity.findViewById(R.id.event_details_button);

        BackendHandler bh_attend_withdraw = FirestoreBackendHandler.getInstance();

        bh_attend_withdraw.decrementEventAttendants(eventId);

        attend_withdraw_meetup.setText(R.string.event_details_button_attend);
        attend_withdraw_meetup.setBackgroundResource(R.drawable.style_btn_roundedcorners);
        attend_withdraw_meetup.setTextColor(activity.getResources().getColor(R.color.servus_white, activity.getTheme()));

        editor.putString(SUBSCRIBED_TO_EVENT, "none");
        editor.apply();

        */
    }
}
