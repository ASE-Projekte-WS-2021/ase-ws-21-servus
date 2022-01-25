package de.ur.servus.core;

import com.google.android.gms.tasks.Task;

import java.util.List;

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
     * Increments the attendance counter of an Event in the database by 1.
     *
     * @param eventId The id of the Event.
     * @return A Task to await the asynchronous operation.
     */
    Task<Void> incrementEventAttendants(String eventId);

    /**
     * Decrements the attendance counter of an Event in the database by 1.
     *
     * @param eventId The id of the Event.
     * @return A Task to await the asynchronous operation.
     */
    Task<Void> decrementEventAttendants(String eventId);

    /**
     * Saves a new event to the database and returns its ID.
     * @param event The event to be saved.
     * @return The event's ID.
     */
    Task<String> createNewEvent(Event event);

    /**
     * Updates a event in the database.
     * @param eventId The ID of the event to update.
     * @param event The event to be saved.
     * @return A Task to await the asynchronous operation.
     */
    Task<Void> updateEvent(String eventId, Event event);
}
