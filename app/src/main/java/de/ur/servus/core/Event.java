package de.ur.servus.core;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

import javax.annotation.Nullable;

public class Event {
    private final String name;
    private final String description;
    private @Nullable String id;
    private final LatLng location;
    private final String genre;
    private final List<Attendant> attendants;

    public Event(String name, String description, LatLng location, List<Attendant> attendants, String genre) {
        this.name = name;
        this.description = description;
        this.location = location;
        this.attendants = attendants;
        this.genre = genre;
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

    public String getGenre(){return genre;}

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
}
