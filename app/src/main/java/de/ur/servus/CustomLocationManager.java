package de.ur.servus;

import static android.content.Context.LOCATION_SERVICE;
import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.maps.model.LatLng;
import java.util.Calendar;
import javax.annotation.Nullable;

public class CustomLocationManager {
    private final static String KEY_LAT = "lastLat";
    private final static String KEY_LNG = "lastLng";
    private final static String KEY_TIMESTAMP = "timestamp";
    private final static String PREFS_NAME = "camera";


    public CustomLocationManager(Context context) {
        startListeningForNewLocation(context);
    }

    /**
     * Subscribe to location changes and save to shared preferences.
     *
     * @param context
     */
    private void startListeningForNewLocation(@NonNull Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        Criteria locationCriteria = new Criteria();
        locationCriteria.setAccuracy(Criteria.ACCURACY_COARSE);

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: User didn't give permission initially. Ask again and try this again.
            return;
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 200, 2, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                saveLatLng(context, latLng);
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        });
    }

    private void saveLatLng(@NonNull Context context, @NonNull LatLng latLng) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        long currentTime = Calendar.getInstance().getTimeInMillis();

        editor.putLong(KEY_LAT, Double.doubleToRawLongBits(latLng.latitude));
        editor.putLong(KEY_LNG, Double.doubleToRawLongBits(latLng.longitude));
        editor.putLong(KEY_TIMESTAMP, currentTime);
        editor.apply();
    }

    /**
     * Can return null, if no location was saved.
     *
     * @param context The context, that should read the location.
     * @return The last saved location or null, if none was found.
     */
    @Nullable
    private SavedLocation readLastSavedLocation(@NonNull Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        if (!preferences.contains(KEY_LAT) || !preferences.contains(KEY_LNG) || !preferences.contains(KEY_TIMESTAMP)) {
            return null;
        }

        double lat = Double.longBitsToDouble(preferences.getLong(KEY_LAT, 0));
        double lng = Double.longBitsToDouble(preferences.getLong(KEY_LNG, 0));
        long timestamp = preferences.getLong(KEY_TIMESTAMP, 0);
        return new SavedLocation(lat, lng, timestamp);
    }

    /**
     * Returns the last observed location considering the last known location by android and out last saved location.
     *
     * @param context
     * @return Last observed location as LatLng or null, if no permissions where given or no location was saved.
     */
    @Nullable
    public LatLng getLastObservedLocation(@NonNull Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: User didn't give permission initially. Ask again and try this again.
            return null;
        }

        var lastKnownLocationLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        var lastSavedLocation = readLastSavedLocation(context);

        if (lastKnownLocationLocation != null && lastSavedLocation != null) {
            if (lastKnownLocationLocation.getTime() > lastSavedLocation.timestamp) {
                return new LatLng(lastKnownLocationLocation.getLatitude(), lastKnownLocationLocation.getLongitude());
            } else {
                return lastSavedLocation.getLatLng();
            }
        } else if (lastKnownLocationLocation != null) {
            return new LatLng(lastKnownLocationLocation.getLatitude(), lastKnownLocationLocation.getLongitude());
        } else if (lastSavedLocation != null) {
            return lastSavedLocation.getLatLng();
        } else {
            return null;
        }
    }


    private static class SavedLocation {
        public final double latitude;
        public final double longitude;
        public final long timestamp;

        public SavedLocation(double latitude, double longitude, long timestamp) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.timestamp = timestamp;
        }

        public LatLng getLatLng() {
            return new LatLng(latitude, longitude);
        }
    }
}
