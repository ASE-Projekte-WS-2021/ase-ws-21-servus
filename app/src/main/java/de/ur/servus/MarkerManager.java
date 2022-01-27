package de.ur.servus;

import com.google.android.gms.maps.model.Marker;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class MarkerManager {
    private List<Marker> markers;


    public List<Marker> getMarkers() {
        return markers;
    }

    public Optional<Marker> getMarkerForId(String eventId) {
        return markers.stream().filter(marker -> {
            String markerId = Objects.requireNonNull(marker.getTag()).toString();
            return markerId.equals(eventId);
        }).findFirst();
    }

    public void setMarkers(List<Marker> markers) {
        this.markers = markers;
    }

    public void showAllMarkers() {
        markers.forEach(marker -> marker.setVisible(true));
    }

    public void hideAllMarkers() {
        markers.forEach(marker -> marker.setVisible(false));
    }

    public void showSingleMarker(String eventId) {
        var marker = getMarkerForId(eventId);
        if (marker.isPresent()) {
            hideAllMarkers();
            marker.get().setVisible(true);
        }
    }

}
