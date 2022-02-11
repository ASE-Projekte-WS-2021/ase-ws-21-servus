package de.ur.servus;

import static de.ur.servus.MainActivity.SUBSCRIBED_TO_EVENT;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

public class CustomMarkerRenderer extends DefaultClusterRenderer<MarkerClusterItem> {

    private final Context context;
    private final SharedPreferences sharedPreferences;

    public CustomMarkerRenderer(Context context, SharedPreferences sharedPreferences, GoogleMap map, ClusterManager<MarkerClusterItem> clusterManager) {
        super(context, map, clusterManager);
        this.context = context;
        this.sharedPreferences = sharedPreferences;
        clusterManager.setRenderer(this);
    }

    @Override
    protected void onBeforeClusterItemRendered(@NonNull MarkerClusterItem item, @NonNull MarkerOptions markerOptions) {
        super.onBeforeClusterItemRendered(item, markerOptions);

        String currentEventId = sharedPreferences.getString(SUBSCRIBED_TO_EVENT, "none");

        if (!currentEventId.equals("none") && !item.getEventId().equals(currentEventId)) {
            // Style currently not attended event
            markerOptions.alpha(0.5f);
        }
    }



    @Override
    protected boolean shouldRenderAsCluster(Cluster<MarkerClusterItem> cluster) {
        return cluster.getSize() > 1;
    }
}
