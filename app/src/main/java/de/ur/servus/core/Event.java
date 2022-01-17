package de.ur.servus.core;

import android.location.Location;
import android.location.LocationManager;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class Event implements Parcelable {
    // TODO Find a way to force set these values. Currently they could be null.

    // When adding properties, remember to add them to the parcel to
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


    /* Below here is the implementation for Parcelable */
    @Override
    public void writeToParcel(Parcel out, int flags) {
        // This order is important, the data needs to be unpacked in the same order as it was packed
        out.writeString(name);
        out.writeString(description);
        out.writeString(eventId);
        location.writeToParcel(out, flags);
    }

    private Event(Parcel in) {
        // This order is important, the data needs to be unpacked in the same order as it was packed
        this.name = in.readString();
        this.description = in.readString();
        this.eventId = in.readString();
        this.location = Location.CREATOR.createFromParcel(in);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<Event> CREATOR = new Parcelable.Creator<>() {
        public Event createFromParcel(Parcel in) {
            return new Event(in);
        }

        public Event[] newArray(int size) {
            return new Event[size];
        }
    };
}
