package de.ur.servus;

import static de.ur.servus.utils.UserAccountKeys.ACCOUNT_ITEM_ID;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

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
    private final Activity activity;
    private final EventList eventList;

    public CustomMarkerRenderer(Activity activity, GoogleMap map, ClusterManager<MarkerClusterItem> clusterManager, EventList eventList) {
        super(activity, map, clusterManager);
        this.eventHelpers = new EventHelpers(activity);
        this.userAccountHelpers = new UserAccountHelpers(activity);
        this.activity = activity;
        this.eventList = eventList;

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

        var alpha = getAlphaForClusterItem(item);
        markerOptions.alpha(alpha);

        imageView.setBackgroundResource(item.getGenrePicture());
        Bitmap icon = iconGenerator.makeIcon();
        drawOnCanvas(item, icon);

        // Based on MaxAttendee count, apply a greyscale to the icon - otherwise leave it pink
        if (item.getEvent().getMaxAttendees() != null && item.getEvent().getAttendants().size() >= Integer.parseInt(item.getEvent().getMaxAttendees())) {
            Bitmap btm = toGrayscale(icon);
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(btm));
        } else {
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
        }
    }

    @Override
    protected void onClusterItemUpdated(@NonNull MarkerClusterItem item, @NonNull Marker marker) {
        super.onClusterItemUpdated(item, marker);

        var alpha = getAlphaForClusterItem(item);

        marker.setAlpha(alpha);
    }

    private void drawOnCanvas (MarkerClusterItem item, Bitmap icon){
        int numberOfAttendees = item.getEvent().getAttendants().size();

        //Create a canvas to draw the circle to display the number of attendees on the icon
        Canvas canvas = new Canvas(icon);

        //Paint for the circle
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        //Paint for the text
        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

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
            canvas.drawText(attendeesString, x * 4.4f, y *4.1f , textPaint);
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

    /**
     * Generate alpha value for marker, depending on the user attending the event.
     *
     * @param item
     * @return
     */
    private float getAlphaForClusterItem(MarkerClusterItem item) {
        var event = item.getEvent();
        var userId = userAccountHelpers.readStringValue(ACCOUNT_ITEM_ID, "");
        var isAttendingCurrentEvent = eventList.getEventsAttendedByUser(userId).stream().anyMatch(e -> Objects.equals(e.getId(), event.getId()));

        // event does not exist!? just don't show it. Should not happen.
        if (event.getId() == null) {
            return 0f;
        }

        // if user is subscribed to an event, return every other half transparent
        if (eventList.isUserAttendingAnyEvent(userId)) {
            if (isAttendingCurrentEvent) {
                return 1f;
            } else {
                return 0.5f;
            }
        }

        return 1f;
    }

    public Bitmap toGrayscale(Bitmap bmpOriginal)
    {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
    }
}
