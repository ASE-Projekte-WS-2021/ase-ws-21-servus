package de.ur.servus;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import javax.annotation.Nullable;

public class CustomLocationManager {
    public final static int REQUEST_CHECK_SETTINGS = 46485216;
    public static final int REQUEST_LOCATION_PERMISSION = 1;

    private final static String KEY_LAT = "lastLat";
    private final static String KEY_LNG = "lastLng";
    private final static String KEY_TIMESTAMP = "timestamp";
    private final static String PREFS_NAME = "camera";
    private static final LocationRequest LOCATION_REQUEST = createLocationRequest();

    private final FusedLocationProviderClient fusedLocationClient;
    private final LocationManager locationManager;
    private final Activity activity;
    private final List<Consumer<LatLng>> locationListeners = new ArrayList<>();
    private final List<Runnable> providerDisabledListeners = new ArrayList<>();
    private final LocationCallback locationCallback = getLocationCallback();
    private final LocationListener providerDisabledCallback = getProviderDisabledCallback();

    private static LocationRequest createLocationRequest() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }

    public CustomLocationManager(@NonNull Activity activity) {
        this.activity = activity;
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);
        this.locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);

        locationListeners.add(this::saveLatLng);
    }

    private LocationCallback getLocationCallback() {
        return new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                var location = locationResult.getLastLocation();
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

                locationListeners.stream().filter(Objects::nonNull).forEach(listener -> listener.accept(latLng));
            }
        };
    }

    private LocationListener getProviderDisabledCallback() {
        return new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
            }

            @Override
            public void onProviderEnabled(@NonNull String provider) {
            }

            @Override
            public void onProviderDisabled(@NonNull String provider) {
                Log.d("loc", "gps disabled");
                providerDisabledListeners.stream().filter(Objects::nonNull).forEach(Runnable::run);
            }
        };
    }

    /**
     * Start listening to location changes.
     */
    public void startListeningForLocationUpdates() {

        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: User didn't give permission initially. Ask again and try this again.
            return;
        }

        fusedLocationClient.requestLocationUpdates(LOCATION_REQUEST, locationCallback, Looper.getMainLooper());
    }

    /**
     * Stop listening to location changes.
     */
    public void stopListeningForLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    /**
     * Start listening, if provider gets disabled.
     */
    public void startListeningProviderDisabled() {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: User didn't give permission initially. Ask again and try this again.
            return;
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 200, providerDisabledCallback);
    }

    /**
     * Stop listening, if provider gets disabled.
     */
    public void stopListeningProviderDisabled() {
        locationManager.removeUpdates(providerDisabledCallback);
    }

    private void saveLatLng(@NonNull LatLng latLng) {
        SharedPreferences preferences = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
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
     * @return The last saved location or null, if none was found.
     */
    private Optional<CustomLocation> readLastSavedLocation() {
        SharedPreferences preferences = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        if (!preferences.contains(KEY_LAT) || !preferences.contains(KEY_LNG) || !preferences.contains(KEY_TIMESTAMP)) {
            return Optional.empty();
        }

        double lat = Double.longBitsToDouble(preferences.getLong(KEY_LAT, 0));
        double lng = Double.longBitsToDouble(preferences.getLong(KEY_LNG, 0));
        long timestamp = preferences.getLong(KEY_TIMESTAMP, 0);
        return Optional.of(new CustomLocation(lat, lng, timestamp));
    }

    /**
     * Returns the last observed location considering the last known location by android and our last saved location.
     * The value in the listener might be null, if no location was provided by android and we have saved no loacation yet.
     */
    public void getLastObservedLocation(Consumer<Optional<LatLng>> listener) {

        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Handle case, where user wont give permission. Ask again?
            return;
        }

        // Try to get best last location. If the service returns null, look at shared preferences. If this is null to return null .
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            var lastKnownLocations = new CustomLocation[]{
                    CustomLocation.fromLocation(location).orElse(null),
                    readLastSavedLocation().orElse(null),
            };

            var newestLastKnownLocation = Arrays.stream(lastKnownLocations).filter(Objects::nonNull).max(Comparator.comparing(CustomLocation::getTimestamp));

            var latLng = newestLastKnownLocation.map(CustomLocation::getLatLng);

            listener.accept(latLng);
        });
    }

    public void addOnProviderDisabledListener(Runnable onProviderDisabledListener) {
        providerDisabledListeners.add(onProviderDisabledListener);
    }

    public void removeOnProviderDisabledListener(Runnable onProviderDisabledListener) {
        providerDisabledListeners.remove(onProviderDisabledListener);
    }

    public void showEnableGpsDialogIfNecessary() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(LOCATION_REQUEST)
                .setAlwaysShow(true);

        LocationServices.getSettingsClient(activity).checkLocationSettings(builder.build())
                .addOnFailureListener(e -> {
                    if (e instanceof ResolvableApiException) {
                        // Location settings are not satisfied, but this can be fixed by showing the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            ResolvableApiException resolvable = (ResolvableApiException) e;
                            resolvable.startResolutionForResult(activity, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException sendEx) {
                            // Ignore the error.
                        }
                    }
                });
    }


    private static class CustomLocation {
        public final double latitude;
        public final double longitude;
        public final long timestamp;

        public static Optional<CustomLocation> fromLocation(@Nullable Location location) {
            if (location == null) {
                return Optional.empty();
            }

            return Optional.of(new CustomLocation(location.getLatitude(), location.getLongitude(), location.getTime()));
        }

        public CustomLocation(double latitude, double longitude, long timestamp) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.timestamp = timestamp;
        }

        public LatLng getLatLng() {
            return new LatLng(latitude, longitude);
        }

        public long getTimestamp() {
            return this.timestamp;
        }
    }
}
