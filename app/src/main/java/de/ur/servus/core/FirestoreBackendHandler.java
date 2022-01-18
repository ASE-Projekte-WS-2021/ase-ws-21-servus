package de.ur.servus.core;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class FirestoreBackendHandler implements BackendHandler {

    private static final String COLLECTION = "mvp";

    private static BackendHandler instance = null;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public static BackendHandler getInstance() {
        if (FirestoreBackendHandler.instance == null) {
            FirestoreBackendHandler.instance = new FirestoreBackendHandler();
        }
        return FirestoreBackendHandler.instance;
    }

    private FirestoreBackendHandler() {
    }

    public ListenerRegistration subscribeEvents(EventListener<List<Event>> listener) {
        var registration = db.collection(COLLECTION).addSnapshotListener((value, error) -> {
            if (error != null) {
                listener.onError(error);
            } else {
                try {
                    // TODO save event ids in Event objects
                    assert value != null;
                    List<Event> events = value.toObjects(Event.class);
                    listener.onEvent(events);
                } catch (Exception e) {
                    listener.onError(e);
                }
            }
        });

        return registration::remove;
    }

    public Event fetchEvent(String eventId) {
        // fetch a specific event document by its id
        return new Event();
    }

    public void incrementEventAttendants(String eventId) {
        // increment attendant counter of event
    }

    public void decrementEventAttendants(String eventId) {
        // decrement attendant counter of event
    }

}
