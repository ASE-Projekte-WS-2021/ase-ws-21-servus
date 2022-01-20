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
     * @return A Task to view the asynchronous operation.
     */
    Task<Void> incrementEventAttendants(String eventId);

    /**
     * Decrements the attendance counter of an Event in the database by 1.
     *
     * @param eventId The id of the Event.
     * @return A Task to view the asynchronous operation.
     */
    Task<Void> decrementEventAttendants(String eventId);
}
