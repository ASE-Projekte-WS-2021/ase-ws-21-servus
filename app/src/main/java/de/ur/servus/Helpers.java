package de.ur.servus;

// TODO better name/structuring of this stuff
public class Helpers {

    /**
     * Informs the backend about the attendance of an Event and saves its id in the SharedPreferences.
     * @param eventId The ID of the Event to attend to.
     */
    public static void attendEvent(String eventId) {
        // call backend to increment attendant counter
        // trigger subscription to Event (and remove any old subscriptions to specific Events), after first call to backend is finished
        // save eventId in SharedPreferences as currentEventId
    }

    /**
     * Informs the backend about the leave of an Event and clears the SharedPreferences.
     */
    public static void leaveCurrentEvent() {
        // check, if currently in event
        // call backend to decrement attendant counter
        // remove any old subscriptions to specific Events
        // set currentEventId in SharedPreferences to a null value
    }
}
