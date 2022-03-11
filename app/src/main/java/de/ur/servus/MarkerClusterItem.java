package de.ur.servus;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;

import de.ur.servus.eventgenres.Genre;
import de.ur.servus.eventgenres.GenreData;

public class MarkerClusterItem implements ClusterItem, com.google.maps.android.clustering.ClusterItem {
    private final LatLng latLng;
    private final String title;
    private final String mSnippet;
    private final String eventId;
    private final Genre genre;
    private final int attendeesNumber;

    public MarkerClusterItem(LatLng latLng, String title, String mSnippet, String eventId, String genreName, int attendeesNumber){
        this.latLng = latLng;
        this.title = title;
        this.mSnippet = mSnippet;
        this.eventId = eventId;
        this.genre = GenreData.getGenreFromName(genreName);
        this.attendeesNumber = attendeesNumber;
    }

    public int getAttendeesNumber() {
        return attendeesNumber;
    }

    public int getGenrePicture(){
        return genre.getImage();
    }

    public String getEventId(){
        return eventId;
    }

    @NonNull
    @Override
    public LatLng getPosition() {
        return latLng;
    }

    @Nullable
    @Override
    public String getTitle() {
        return title;
    }

    @Nullable
    @Override
    public String getSnippet() {
        return mSnippet;
    }
}
