package de.ur.servus.core;

import com.google.android.gms.maps.model.LatLng;

import javax.annotation.Nullable;

public class Event {
    private final String name;
    private final String description;
    private @Nullable String id;
    private final LatLng location;
    private final int attendants;

    public Event(String name, String description, LatLng location, int attendants) {
        this.name = name;
        this.description = description;
        this.location = location;
        this.attendants = attendants;
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


    @Nullable
    public String getId() {
        return id;
    }

    public int getAttendants() {
        return attendants;
    }

    public void setId(String id) {
        this.id = id;
    }
}
