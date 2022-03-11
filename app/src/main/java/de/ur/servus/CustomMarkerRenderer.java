package de.ur.servus;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

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
    private final Activity activity;

    public CustomMarkerRenderer(Activity activity, SharedPreferences sharedPreferences, GoogleMap map, ClusterManager<MarkerClusterItem> clusterManager) {
        super(activity, map, clusterManager);
        this.subscribedEventHelpers = new SubscribedEventHelpers(activity);
        this.activity = activity;

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

        imageView.setBackgroundResource(item.getGenrePicture());
        Bitmap icon = iconGenerator.makeIcon();
        drawOnCanvas(item, icon);
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
    }

    private void drawOnCanvas (MarkerClusterItem item, Bitmap icon){
        int numberOfAttendees = item.getAttendeesNumber();
        //Create a canvas to draw the circle to display the number of attendees on the icon
        Canvas canvas = new Canvas(icon);
        //Paint for the circle
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        //Paint for the text
        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(ContextCompat.getColor(activity.getApplicationContext(), R.color.white));
        textPaint.setTextSize(35);
        textPaint.setColor(ContextCompat.getColor(activity.getApplicationContext(), R.color.white));
        paint.setColor(ContextCompat.getColor(activity.getApplicationContext(), R.color.servus_blue));
        canvas.drawCircle(canvas.getWidth() - 30,canvas.getHeight() - 30,30,paint);
        //Saves the number of attendees as String so that the length can be determined to position the number in the center of the circle
        String attendeesString = String.valueOf(numberOfAttendees);
        Rect bounds = new Rect();
        paint.getTextBounds(attendeesString, 0, attendeesString.length(), bounds);
        int x = (canvas.getWidth() - bounds.width())/6;
        int y = (canvas.getHeight() + bounds.height())/5;

        if(attendeesString.length() > 1){
            canvas.drawText(attendeesString, x * 4.3f, y *4.1f , textPaint);
        } else {
            canvas.drawText(attendeesString, x * 4.6f, y *4.1f , textPaint);
        }
    }

    @Override
    protected boolean shouldRenderAsCluster(Cluster<MarkerClusterItem> cluster) {
        return cluster.getSize() > 1;
    }

    @Override
    protected int getColor(int clusterSize) {
       if(clusterSize < 10 ){
            return ContextCompat.getColor(activity.getApplicationContext(), R.color.servus_violet);
        } else {
            return ContextCompat.getColor(activity.getApplicationContext(), R.color.servus_blue);
        }

    }
}
