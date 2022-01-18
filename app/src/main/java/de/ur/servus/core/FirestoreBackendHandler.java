package de.ur.servus.core;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class FirestoreBackendHandler implements BackendHandler{

    private static final String COLLECTION = "mvp";

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public ListenerRegistration subscribeEvents(EventListener<List<Event>> listener) {
        var registration = db.collection(COLLECTION).addSnapshotListener((value, error) -> {
            if (error != null) {
                listener.onError(error);
            } else {
                try {
                    List<Event> events = value.toObjects(Event.class);
                    listener.onEvent(events);
                } catch (Exception e) {
                    listener.onError(e);
                }
            }
        });

        return registration::remove;
    }


}
