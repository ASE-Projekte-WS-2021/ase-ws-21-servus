package de.ur.servus;

import android.content.Context;

import com.google.android.gms.maps.GoogleMap;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.algo.NonHierarchicalDistanceBasedAlgorithm;

public class MarkerManager {
    private final ClusterManager<MarkerClusterItem> clusterManager;

    public MarkerManager(ClusterManagerContext context, GoogleMap map) {
        this.clusterManager = new ClusterManager<>((Context) context, map);

        this.clusterManager.setOnClusterClickListener(context);
        this.clusterManager.setOnClusterItemClickListener(context);
        map.setOnCameraIdleListener(clusterManager);
        map.setOnMarkerClickListener(clusterManager);
        clusterManager.setAnimation(true);
    }

    public ClusterManager<MarkerClusterItem> getClusterManager() {
        return clusterManager;
    }

    public void setClusterAlgorithm() {
        NonHierarchicalDistanceBasedAlgorithm<MarkerClusterItem> algorithm = new NonHierarchicalDistanceBasedAlgorithm<>();
        algorithm.setMaxDistanceBetweenClusteredItems(20);
        clusterManager.setAlgorithm(algorithm);
    }

}
