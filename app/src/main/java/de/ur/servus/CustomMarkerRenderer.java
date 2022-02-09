package de.ur.servus;

import android.content.Context;
import com.google.android.gms.maps.GoogleMap;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

public class CustomMarkerRenderer extends DefaultClusterRenderer<MarkerClusterItem> {

    private final Context context;

    public CustomMarkerRenderer(Context context, GoogleMap map, ClusterManager<MarkerClusterItem> clusterManager) {
        super(context, map, clusterManager);
        this.context = context;
        clusterManager.setRenderer(this);
    }

    @Override
    protected boolean shouldRenderAsCluster(Cluster<MarkerClusterItem> cluster){
        return cluster.getSize() > 1;
    }
}
