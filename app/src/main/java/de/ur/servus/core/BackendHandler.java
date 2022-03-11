package de.ur.servus.core;

import android.graphics.Bitmap;

import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.UploadTask;

import java.util.List;
import java.util.Map;

public interface BackendHandler {

    /**
     * Subscribes to Event updates from the database. Provides a list of all Events.
     *
     * @param listener A listener, that can react to updates and errors.
     * @return A listener registration, that is used to stop listening for updates.
     */
    ListenerRegistration subscribeEvents(EventListener<List<Event>> listener);

    /**
     * Subscribes to a single Event updates from the database. Provides a single Event.
     *
     * @param listener A listener, that can react to updates and errors.
     * @return A listener registration, that is used to stop listening for updates.
     */
    ListenerRegistration subscribeEvent(String eventId, EventListener<Event> listener);

    /**
     * Adds an Attendant to an Event.
     *
     * @param eventId   The id of the Event.
     * @param attendant The attendant to add.
     * @param profilePicture Bitmap of a user's picture
     * @return A Task to react, when the attendant was added.
     */
    Task<Void> addEventAttendant(String eventId, Attendant attendant, Bitmap profilePicture);

    /**
     * Removes an Attendant from an Event.
     *
     * @param eventId     The id of the Event.
     * @param attendantId The id of the attendant to remove.
     * @return A Task to react, when the attendant was removed.
     */
    Task<Void> removeEventAttendantById(String eventId, String attendantId);

    /**
     * Saves a new event to the database and returns its ID.
     *
     * @param event         The event to be saved.
     * @param eventListener A listener to get the Event's ID when it is created.
     */
    void createNewEvent(Event event, EventListener<String> eventListener);

    /**
     * Updates a event in the database.
     *
     * @param eventId The ID of the event to update.
     * @param newEventData   The data to be updated.
     */
    void updateEvent(String eventId, Map<String, Object> newEventData, Runnable listener);

    /**
     * Creates a new folder in firebase storage with creator's profile picture in it
     * @param eventID     event ID of corresponding firestore doc - used as folder name
     * @param creatorID   UUID of creator, used as profile picture name
     * @param creatorPicture Bitmap of user picture
     * @return
     */
    UploadTask createNewEventStorageFolder(String eventID, String creatorID, Bitmap creatorPicture);


}
