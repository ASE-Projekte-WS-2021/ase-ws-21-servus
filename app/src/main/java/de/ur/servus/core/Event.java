package de.ur.servus.core;

import android.location.Location;
import android.location.LocationManager;

import java.util.List;

public class Event {
    // TODO Find a way to force set these values. Currently they could be null.

    private String name;
    private String description;
    private String id;
    private LatLng location;

    public Event() {
    }

    public LatLng getLocation() {
        return location;
    }

    /**
     * Create a location from a list with two elements. This is how they are saved in Firestore.
     *
     * @param latlng List with two elements: Latitude and Longitude.
     */
    public void setLocation(List<Double> latlng) {
        this.location = new LatLng(latlng.get(0), latlng.get(1));
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
