package de.ur.servus.core;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

public class Event {
    // TODO Find a way to force set these values. Currently they could be null.

    private String name;
    private String description;
    private String id;
    private LatLng latLng;

    public Event() {
    }

    public LatLng getLatLng() {
        return latLng;
    }

    /**
     * Create a location from a list with two elements. This is how they are saved in Firestore.
     *
     * @param latlng List with two elements: Latitude and Longitude.
     */
    public void setLatLng(List<Double> latlng) {
        this.latLng = new LatLng(latlng.get(0), latlng.get(1));
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
