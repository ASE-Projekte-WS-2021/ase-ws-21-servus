package de.ur.servus;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;

import de.ur.servus.SharedPreferencesHelpers.SubscribedEventHelpers;

public class CustomMarkerRenderer extends DefaultClusterRenderer<MarkerClusterItem> {

    private final SubscribedEventHelpers subscribedEventHelpers;
    private final IconGenerator iconGenerator;
    private final ImageView imageView;
    private final int markerWidth;
    private final int markerHeight;

    public CustomMarkerRenderer(Activity activity, SharedPreferences sharedPreferences, GoogleMap map, ClusterManager<MarkerClusterItem> clusterManager) {
        super(activity, map, clusterManager);
        this.subscribedEventHelpers = new SubscribedEventHelpers(activity);

        iconGenerator = new IconGenerator(activity.getApplicationContext());
        imageView = new ImageView(activity.getApplicationContext());
        markerWidth = (int) activity.getResources().getDimension(R.dimen.custom_marker_image);
        markerHeight = (int) activity.getResources().getDimension(R.dimen.custom_marker_image);
        imageView.setLayoutParams(new ViewGroup.LayoutParams(markerWidth, markerHeight));
        int padding = (int) activity.getResources().getDimension(R.dimen.custom_marker_padding);
        imageView.setPadding(padding, padding, padding, padding);
        iconGenerator.setBackground(activity.getDrawable(R.drawable.style_cluster_marker_bg));

        iconGenerator.setContentView(imageView);

        clusterManager.setRenderer(this);

    }

    @Override
    protected void onBeforeClusterItemRendered(@NonNull MarkerClusterItem item, @NonNull MarkerOptions markerOptions) {
        super.onBeforeClusterItemRendered(item, markerOptions);

        subscribedEventHelpers.ifSubscribedToEvent(currentSubscribedEventData -> {
            if(!item.getEventId().equals(currentSubscribedEventData.eventId)){
                markerOptions.alpha(0.5f);
            }
        }, null);

        imageView.setImageResource(item.getGenrePicture());
        Bitmap icon = iconGenerator.makeIcon();
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon)).title(item.getTitle());

    }



    @Override
    protected boolean shouldRenderAsCluster(Cluster<MarkerClusterItem> cluster) {
        return cluster.getSize() > 1;
    }

    @Override
    protected int getColor(int clusterSize) {
       if(clusterSize < 10 ){
            return Color.rgb(108, 91, 123);
        } else {
            return Color.rgb(53, 92, 125);
        }

    }
}
