package de.ur.servus;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;

import java.util.Objects;

import de.ur.servus.utils.EventHelpers;
import de.ur.servus.utils.UserAccountHelpers;

public class CustomMarkerRenderer extends DefaultClusterRenderer<MarkerClusterItem> {

    private final EventHelpers eventHelpers;
    private final UserAccountHelpers userAccountHelpers;
    private final IconGenerator iconGenerator;
    private final ImageView imageView;
    private final int markerWidth;
    private final int markerHeight;

    public CustomMarkerRenderer(Activity activity, SharedPreferences sharedPreferences, GoogleMap map, ClusterManager<MarkerClusterItem> clusterManager) {
        super(activity, map, clusterManager);
        this.eventHelpers = new EventHelpers(activity);
        this.userAccountHelpers = new UserAccountHelpers(activity);

        iconGenerator = new IconGenerator(activity.getApplicationContext());
        imageView = new ImageView(activity.getApplicationContext());
        markerWidth = (int) activity.getResources().getDimension(R.dimen.custom_marker_image);
        markerHeight = (int) activity.getResources().getDimension(R.dimen.custom_marker_image);
        imageView.setLayoutParams(new ViewGroup.LayoutParams(markerWidth, markerHeight));
        int padding = (int) activity.getResources().getDimension(R.dimen.custom_marker_padding);
        imageView.setPadding(padding, padding, padding, padding);
        iconGenerator.setBackground(AppCompatResources.getDrawable(activity, R.drawable.style_cluster_marker_bg));

        iconGenerator.setContentView(imageView);

        clusterManager.setRenderer(this);
    }

    @Override
    protected void onBeforeClusterItemRendered(@NonNull MarkerClusterItem item, @NonNull MarkerOptions markerOptions) {
        super.onBeforeClusterItemRendered(item, markerOptions);

        var alpha = getAlphaForClusterItem(item);
        markerOptions.alpha(alpha);

        imageView.setImageResource(item.getGenrePicture());
        Bitmap icon = iconGenerator.makeIcon();
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon)).title(item.getTitle());

    }

    @Override
    protected void onClusterItemUpdated(@NonNull MarkerClusterItem item, @NonNull Marker marker) {
        super.onClusterItemUpdated(item, marker);

        var alpha = getAlphaForClusterItem(item);

        marker.setAlpha(alpha);
    }

    @Override
    protected boolean shouldRenderAsCluster(Cluster<MarkerClusterItem> cluster) {
        return cluster.getSize() > 1;
    }

    @Override
    protected int getColor(int clusterSize) {
        if (clusterSize < 10) {
            return Color.rgb(108, 91, 123);
        } else {
            return Color.rgb(53, 92, 125);
        }

    }

    /**
     * Generate alpha value for marker, depending on the user attending the event.
     *
     * @param item
     * @return
     */
    private float getAlphaForClusterItem(MarkerClusterItem item) {
        var event = item.getEvent();

        var currentSubscribedEventData = eventHelpers.tryGetSubscribedEvent();

        // event does not exist!? just don't show it. Should not happen.
        if (event.getId() == null) {
            return 0f;
        }

        // if user is subscribed to an event, return every other half transparent
        if (currentSubscribedEventData.eventId != null) {
            if (Objects.equals(currentSubscribedEventData.eventId, event.getId())) {
                return 1f;
            } else {
                return 0.5f;
            }
        }

        return 1f;
    }
}
