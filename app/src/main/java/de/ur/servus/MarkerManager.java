package de.ur.servus;

import android.content.Context;

import com.google.android.gms.maps.GoogleMap;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.algo.NonHierarchicalDistanceBasedAlgorithm;

public class MarkerManager {
    private ClusterManager<MarkerClusterItem> clusterManager;
    private NonHierarchicalDistanceBasedAlgorithm<MarkerClusterItem> algorithm;


    public ClusterManager<MarkerClusterItem> getClusterManager() {
        return clusterManager;
    }

    public void setClusterAlgorithm() {
        this.algorithm = new NonHierarchicalDistanceBasedAlgorithm<>();
        this.algorithm.setMaxDistanceBetweenClusteredItems(20);
        clusterManager.setAlgorithm(this.algorithm);
    }

    public void setUpClusterManager(ClusterManagerContext context, GoogleMap map) {
        this.clusterManager = new ClusterManager<>((Context) context, map);
        this.clusterManager.setOnClusterClickListener((ClusterManager.OnClusterClickListener<MarkerClusterItem>) context);
        this.clusterManager.setOnClusterItemClickListener((ClusterManager.OnClusterItemClickListener<MarkerClusterItem>) context);
        map.setOnCameraIdleListener(clusterManager);
        map.setOnMarkerClickListener(clusterManager);
        clusterManager.setAnimation(true);
    }

}
