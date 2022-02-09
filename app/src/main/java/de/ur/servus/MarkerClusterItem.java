package de.ur.servus;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;

class MarkerClusterItem implements ClusterItem, com.google.maps.android.clustering.ClusterItem {
    private final LatLng latLng;
    private final String title;
    private final String mSnippet;
    private final String eventId;

    public MarkerClusterItem(LatLng latLng, String title, String mSnippet, String eventId){
        this.latLng = latLng;
        this.title = title;
        this.mSnippet = mSnippet;
        this.eventId = eventId;
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
