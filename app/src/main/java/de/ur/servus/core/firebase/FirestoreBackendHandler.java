package de.ur.servus.core.firebase;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.stream.Collectors;

import de.ur.servus.core.BackendHandler;
import de.ur.servus.core.Event;
import de.ur.servus.core.EventListener;
import de.ur.servus.core.ListenerRegistration;

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
                        EventPOJO eventPOJO = doc.toObject(EventPOJO.class);
                        assert eventPOJO != null;

                        eventPOJO.setId(doc.getId());
                        return eventPOJO.toEvent();
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

                    EventPOJO eventPOJO = value.toObject(EventPOJO.class);

                    if(eventPOJO == null) {
                        throw new Exception("Event not found");
                    }

                    eventPOJO.setId(value.getId());

                    listener.onEvent(eventPOJO.toEvent());
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

    public Task<String> createNewEvent(Event event) {
        EventPOJO pojo = new EventPOJO(event);
        return db.collection(COLLECTION).add(pojo).continueWith(task -> {
            var doc = task.getResult();
            return doc.getId();
        });
    }

    public Task<Void> updateEvent(String eventId, Event event) {
        // FIXME Can create new event, if id does not exist. Add a check.
        return db.collection(COLLECTION).document(eventId).set(event);
    }

    // TODO delete event

}
