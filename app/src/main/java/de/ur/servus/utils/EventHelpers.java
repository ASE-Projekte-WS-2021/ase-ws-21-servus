package de.ur.servus.utils;


import android.app.Activity;
import android.util.Log;

import java.util.ArrayList;

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
    private final Activity activity;

    public EventHelpers(Activity activity) {
        this.activity = activity;
    }

    public void createEvent(CustomLocationManager locationManager, EventCreationData inputEventData, EventListener<String> afterCreationListener) {
        var avatarEditor = new AvatarEditor(activity);
        var userAccountHelpers = new UserAccountHelpers(activity);

        locationManager.getLastObservedLocation(latLng -> {
            if (!latLng.isPresent()) {
                Log.e("eventCreation", "No location was provided.");
                return;
            }

            //get USER ID
            var profile = userAccountHelpers.getOwnProfile(avatarEditor);


            if (profile.getUserID() == null) {
                Log.e("eventCreation", "No own user id found.");
                return;
            }

            var attendants = new ArrayList<Attendant>();

            Event event = new Event(inputEventData.name, inputEventData.description, latLng.get(), attendants, inputEventData.genre);

            FirestoreBackendHandler.getInstance().createNewEvent(event, afterCreationListener);

        });
    }

}
