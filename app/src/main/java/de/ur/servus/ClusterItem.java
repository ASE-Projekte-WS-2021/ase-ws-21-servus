package de.ur.servus;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.gms.maps.model.LatLng;
public interface ClusterItem {
    @NonNull LatLng getPosition();

    @Nullable String getTitle();

    @Nullable String getSnippet();
}
