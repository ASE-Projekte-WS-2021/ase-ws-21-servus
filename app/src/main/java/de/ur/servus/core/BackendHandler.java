package de.ur.servus.core;

import java.util.List;

import javax.annotation.Nullable;

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
     * @param listener A listener to react, when the server has finished.
     */
    void incrementEventAttendants(String eventId, @Nullable EventListener<Void> listener);

    /**
     * Decrements the attendance counter of an Event in the database by 1.
     *
     * @param eventId The id of the Event.
     * @param listener A listener to react, when the server has finished.
     */
    void decrementEventAttendants(String eventId, @Nullable EventListener<Void> listener);

    /**
     * Saves a new event to the database and returns its ID.
     * @param event The event to be saved.
     * @param eventListener A listener to get the Event's ID when it is created.
     */
    void createNewEvent(Event event, EventListener<String> eventListener);

    /**
     * Updates a event in the database.
     * @param eventId The ID of the event to update.
     * @param event The new event to be saved.
     */
    void updateEvent(String eventId, Event event, Runnable listener);
}
