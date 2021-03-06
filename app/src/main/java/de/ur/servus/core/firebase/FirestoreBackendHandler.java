package de.ur.servus.core.firebase;

import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import de.ur.servus.core.Attendant;
import de.ur.servus.core.BackendHandler;
import de.ur.servus.core.Event;
import de.ur.servus.core.EventListener;
import de.ur.servus.core.ListenerRegistration;

public class FirestoreBackendHandler implements BackendHandler {

    private static final String COLLECTION = "mvp";

    private static BackendHandler instance = null;
    //Firestore for documents aka events
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    //Storage for folder with user pictures
    private final FirebaseStorage storage = FirebaseStorage.getInstance();

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
                        // casting may fail, e.g. if it's an old event.
                        try {
                            EventPOJO eventPOJO = doc.toObject(EventPOJO.class);
                            assert eventPOJO != null;

                            eventPOJO.setId(doc.getId());
                            return eventPOJO.toObject();
                        } catch (Exception e) {
                            Log.e("eventSubscription", "For event with id:" + doc.getId() + ". " + e.getLocalizedMessage());
                            return null;
                        }

                    }).filter(Objects::nonNull).collect(Collectors.toList());

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

                    if (eventPOJO == null) {
                        throw new Exception("Event not found");
                    }

                    eventPOJO.setId(value.getId());

                    listener.onEvent(eventPOJO.toObject());
                } catch (Exception e) {
                    listener.onError(e);
                }
            }
        });

        return registration::remove;
    }

    public Task<Void> addEventAttendant(String eventId, Attendant attendant, Bitmap profilePicture) {
        var pojo = new AttendantPOJO(attendant);
        var userID = pojo.getUserId();

        return createNewEventStorageFolder(eventId, userID, profilePicture)
                .continueWithTask(taskSnapshot -> getDownloadUrlFromStorage(eventId, userID)
                        .continueWithTask(task -> {
                            pojo.setUserPicturePath(task.getResult().toString());
                            return db.collection(COLLECTION).document(eventId).update("attendants", FieldValue.arrayUnion(pojo));
                        }));
    }

    public Task<Void> removeEventAttendantById(String eventId, String attendantId) {
        return db.collection(COLLECTION).document(eventId).get().addOnSuccessListener(documentSnapshot -> {
            var event = documentSnapshot.toObject(EventPOJO.class);
            if (event != null) {
                var attendant = event.getAttendants().stream().filter(attendantPOJO -> attendantPOJO.getUserId().equals(attendantId)).findFirst();

                if (attendant.isPresent()) {
                    db.collection(COLLECTION).document(eventId).update("attendants", FieldValue.arrayRemove(attendant.get()));
                    deleteStorageFolderEntry(eventId, attendantId);
                }
            }
        }).continueWith(runnable -> null);
    }

    public void createNewEvent(Event event, @Nullable EventListener<String> listener) {
        EventPOJO pojo = new EventPOJO(event);
        db.collection(COLLECTION).add(pojo)
                .addOnSuccessListener(doc -> {
                    if (listener != null) {
                        listener.onEvent(doc.getId());
                    }
                })
                .addOnFailureListener(e -> {
                    if (listener != null) {
                        listener.onError(e);
                    }
                });
    }

    @Override
    public Task<Void> deleteEvent(String eventId) {
        return db.collection(COLLECTION).document(eventId).delete();
    }

    public UploadTask createNewEventStorageFolder(String eventID, String creatorID, Bitmap creatorPicture) {
        //creates a new folder in storage with eventID as folder-name and adds creator's picture as attendant picture (filename is userID)
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        creatorPicture.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();

        StorageReference eventRef = storage.getReference().child(String.format("%s/%s.png", eventID, creatorID));
        return eventRef.putBytes(data);
    }

    public Task<Uri> getDownloadUrlFromStorage(String eventID, String userID) {
        StorageReference fileRef = storage.getReference().child(String.format("%s/%s.png", eventID, userID));
        return fileRef.getDownloadUrl();
    }

    public void deleteStorageFolderEntry(String eventID, String userID) {
        StorageReference fileRef = storage.getReference().child(String.format("%s/%s.png", eventID, userID));
        fileRef.delete().addOnSuccessListener(unused -> Log.d("DELETE", String.format("UserPicture deleted: %s", userID)));
    }

    public void updateEvent(String eventId, Map<String, Object> newEventData, @Nullable Runnable listener) {
        db.collection(COLLECTION).document(eventId).update(newEventData)
                .addOnSuccessListener(doc -> {
                    if (listener != null) {
                        listener.run();
                    }
                });
    }

}
