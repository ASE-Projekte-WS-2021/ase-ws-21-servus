package de.ur.servus.core;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;

public class Event {
    private final String name;
    private final String description;
    private @Nullable
    String id;
    private final LatLng location;
    private final String genre;
    private final List<Attendant> attendants;
    private final String maxAttendees;

    public Event(String name, String description, LatLng location, List<Attendant> attendants, String genre, String maxAttendees) {
        this.name = name;
        this.description = description;
        this.location = location;
        this.attendants = attendants;
        this.genre = genre;
        this.maxAttendees = maxAttendees;
    }

    public LatLng getLocation() {
        return location;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getGenre() {
        return genre;
    }

    public String getMaxAttendees(){
        return maxAttendees;
    }

    @Nullable
    public String getId() {
        return id;
    }

    public List<Attendant> getAttendants() {
        return attendants;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isUserOwner(String userId) {
        var user = attendants.stream().filter(attendant -> Objects.equals(attendant.getUserId(), userId)).findFirst();
        return user.isPresent() && user.get().isCreator();
    }

    public boolean isUserAttending(String userId) {
        var user = attendants.stream().filter(attendant -> Objects.equals(attendant.getUserId(), userId)).findFirst();
        return user.isPresent();
    }
}
