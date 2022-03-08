package de.ur.servus;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;

import de.ur.servus.eventgenres.Genre;
import de.ur.servus.eventgenres.GenreData;

import de.ur.servus.core.Event;

public class MarkerClusterItem implements ClusterItem, com.google.maps.android.clustering.ClusterItem {
    private final Genre genre;
    private final Event event;


    public MarkerClusterItem(@NonNull Event event){
        this.event = event;
        this.genre = GenreData.getGenreFromName(event.getGenre());
    }

    public int getGenrePicture(){
        return genre.getImage();
    }

    public Event getEvent() {
        return event;
    }

    @NonNull
    @Override
    public LatLng getPosition() {
        return event.getLocation();
    }

    @Nullable
    @Override
    public String getTitle() {
        return event.getName();
    }

    @Nullable
    @Override
    public String getSnippet() {
        return "";
    }

}
