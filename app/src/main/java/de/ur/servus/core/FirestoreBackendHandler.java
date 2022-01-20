package de.ur.servus.core;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.stream.Collectors;

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
                    assert value != null;

                    List<Event> events = value.getDocuments().stream().map(doc -> {
                        Event event = doc.toObject(Event.class);
                        assert event != null;

                        event.setId(doc.getId());
                        return event;
                    }).collect(Collectors.toList());

                    listener.onEvent(events);
                } catch (Exception e) {
                    listener.onError(e);
                }
            }
        });

        return registration::remove;
    }

    public ListenerRegistration subscribeEvent(String eventId, EventListener<Event> listener) {
        var registration = db.collection(COLLECTION).document(eventId).addSnapshotListener((value, error) -> {
            if (error != null) {
                listener.onError(error);
            } else {
                try {
                    assert value != null;

                    Event event = value.toObject(Event.class);
                    assert event != null;

                    event.setId(value.getId());

                    listener.onEvent(event);
                } catch (Exception e) {
                    listener.onError(e);
                }
            }
        });

        return registration::remove;
    }

    public Task<Void> incrementEventAttendants(String eventId) {
        return db.collection(COLLECTION).document(eventId).update("attendants", FieldValue.increment(1));
    }

    public Task<Void> decrementEventAttendants(String eventId) {
        return db.collection(COLLECTION).document(eventId).update("attendants", FieldValue.increment(-1));
    }

}
