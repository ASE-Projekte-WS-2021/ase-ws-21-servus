package de.ur.servus.core;

import android.location.Location;
import android.location.LocationManager;

import java.util.List;

public class Event {
    // TODO Find a way to force set these values. Currently they could be null.

    public String name;
    public String description;
    public String eventId;
    private Location location;

    public Location getLocation() {
        return location;
    }

    /**
     * Create a location from a list with two elements. This is how they are saved in Firestore.
     *
     * @param latlng List with two elements: Latitude and Longitude.
     */
    public void setLocation(List<Double> latlng) {
        var location = new Location(LocationManager.GPS_PROVIDER);
        location.setLatitude(latlng.get(0));
        location.setLongitude(latlng.get(1));
        this.location = location;
    }

    public Event() {
    }

}
