package de.ur.servus;

import android.app.Activity;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

import de.ur.servus.utils.EventHelpers;

public class CustomMarkerRenderer extends DefaultClusterRenderer<MarkerClusterItem> {

    private final EventHelpers eventHelpers;

    public CustomMarkerRenderer(Activity activity, SharedPreferences sharedPreferences, GoogleMap map, ClusterManager<MarkerClusterItem> clusterManager) {
        super(activity, map, clusterManager);
        this.eventHelpers = new EventHelpers(activity);
        clusterManager.setRenderer(this);
    }

    @Override
    protected void onBeforeClusterItemRendered(@NonNull MarkerClusterItem item, @NonNull MarkerOptions markerOptions) {
        super.onBeforeClusterItemRendered(item, markerOptions);

        eventHelpers.ifSubscribedToEvent(currentSubscribedEventData -> {
            if(!item.getEventId().equals(currentSubscribedEventData.eventId)){
                markerOptions.alpha(0.5f);
            }
        }, null);
    }



    @Override
    protected boolean shouldRenderAsCluster(Cluster<MarkerClusterItem> cluster) {
        return cluster.getSize() > 1;
    }
}
