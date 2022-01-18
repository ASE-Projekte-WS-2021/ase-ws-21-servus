package de.ur.servus;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Marker;

import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationListener;
import android.location.LocationManager;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;
import java.util.stream.Collectors;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.imageview.ShapeableImageView;

import de.ur.servus.core.BackendHandler;
import de.ur.servus.core.Event;
import de.ur.servus.core.EventListener;
import de.ur.servus.core.FirestoreBackendHandler;
import de.ur.servus.core.ListenerRegistration;

import java.io.IOException;


// Tutorial on how to set a marker on the user's current location from: https://github.com/mohsinulkabir14/An-Android-Application-to-Show-Your-Position-On-The-Map-Using-Google-Maps-API

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ListenerRegistration listenerRegistration;
    private static final int REQUEST_LOCATION_PERMISSION = 1;

    LocationManager locationManager;
    LocationListener locationListener;
    Marker marker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        /*
         * Initialize all Bottom Sheets, add a corresponding BottomSheetBehavior and referring Callbacks.
         */
        View c_bottomSheet = findViewById(R.id.creator_bottomSheet);
        View p_bottomSheet = findViewById(R.id.participant_bottomSheet);
        View s_bottomSheet = findViewById(R.id.settings_bottomSheet);
        View f_bottomSheet = findViewById(R.id.filter_bottomSheet);
        BottomSheetBehavior<View> c_bottomSheetBehavior = BottomSheetBehavior.from(c_bottomSheet);
        BottomSheetBehavior<View> p_bottomSheetBehavior = BottomSheetBehavior.from(p_bottomSheet);
        BottomSheetBehavior<View> s_bottomSheetBehavior = BottomSheetBehavior.from(s_bottomSheet);
        BottomSheetBehavior<View> f_bottomSheetBehavior = BottomSheetBehavior.from(f_bottomSheet);
        addBottomSheetCallbacks(c_bottomSheetBehavior, p_bottomSheetBehavior, s_bottomSheetBehavior, f_bottomSheetBehavior);

        /*
         * Add functionality to the BottomNav buttons.
         * Each button should be able to expand its pre-loaded bottomsheet.
         *
         * This explicitly EXCLUDES a button for the participant bottom sheet due to the fact,
         * that this sheet will only be expanded when clicked on a marker.
         *
         * This will be added in a future release.
         */
        ShapeableImageView btn_settings = findViewById(R.id.btn_settings);
        btn_settings.setOnClickListener(v -> s_bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED));

        Button btn_creator = findViewById(R.id.btn_meetup);
        btn_creator.setOnClickListener(v -> c_bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED));

        ShapeableImageView btn_filter = findViewById(R.id.btn_filter);
        btn_filter.setOnClickListener(v -> f_bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED));

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        //asking for the users permission to use the location
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]
                            {Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
        }

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                //getting the latitude and longitude of the user's position
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();

                Geocoder geocoder = new Geocoder(getApplicationContext());
                try {
                    List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                    String adress = addresses.get(0).getLocality() + ":";
                    adress += addresses.get(0).getCountryName();

                    LatLng latLng = new LatLng(latitude, longitude);
                    if (marker != null) {
                        marker.remove();
                    }
                    //marker = mMap.addMarker(new MarkerOptions().position(latLng).title(adress));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13.0f));
                    locationManager.removeUpdates(locationListener);

                } catch (IOException e) {
                    e.printStackTrace();
                }

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
        };

        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

    }

    @Override
    protected void onResume() {
        super.onResume();

        BackendHandler bh = FirestoreBackendHandler.getInstance();
        this.listenerRegistration = bh.subscribeEvents(new EventListener<>() {
            @Override
            public void onEvent(List<Event> events) {
                // Log all event names to console
                Log.d("Data", events.stream().map(event -> event.getName() + ": " + event.getId()).collect(Collectors.joining(", ")));
                mMap.clear();
                // Load event data
                for (Event event : events) {
                    mMap.addMarker(new MarkerOptions().position(event.getLocation()).title(event.getName()));
                }
            }

            @Override
            public void onError(Exception e) {
                // TODO error handling here
                Log.e("Data", e.getMessage());
            }

        });
    }


    @Override
    protected void onPause() {
        super.onPause();

        if (this.listenerRegistration != null) {
            this.listenerRegistration.unsubscribe();
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        locationManager.removeUpdates(locationListener);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
    }

    /*
     * Initialize all BottomSheetBehaviors.
     * This adds functionality to trigger any time a specific state of the BottomSheet was triggered.
     *   -> Code will be extended and cleaned once all specifications are clear to the core.
     */
    private void addBottomSheetCallbacks(BottomSheetBehavior<View> c, BottomSheetBehavior<View> p, BottomSheetBehavior<View> s, BottomSheetBehavior<View> f) {
        c.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        //tbd
                        break;

                    case BottomSheetBehavior.STATE_EXPANDED:
                        //tbd
                        break;

                    case BottomSheetBehavior.STATE_DRAGGING:
                        //tbd
                        break;

                    case BottomSheetBehavior.STATE_HIDDEN:
                        //tbd
                        break;

                    case BottomSheetBehavior.STATE_SETTLING:
                        //tbd
                        break;

                    case BottomSheetBehavior.STATE_HALF_EXPANDED:
                        //tbd
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                //tbd
                // potentially empty (?)
            }
        });

        p.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        //tbd
                        break;

                    case BottomSheetBehavior.STATE_EXPANDED:
                        //tbd
                        break;

                    case BottomSheetBehavior.STATE_DRAGGING:
                        //tbd
                        break;

                    case BottomSheetBehavior.STATE_HIDDEN:
                        //tbd
                        break;

                    case BottomSheetBehavior.STATE_SETTLING:
                        //tbd
                        break;

                    case BottomSheetBehavior.STATE_HALF_EXPANDED:
                        //tbd
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                //tbd
                // potentially empty (?)
            }
        });

        s.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        //tbd
                        break;

                    case BottomSheetBehavior.STATE_EXPANDED:
                        //tbd
                        break;

                    case BottomSheetBehavior.STATE_DRAGGING:
                        //tbd
                        break;

                    case BottomSheetBehavior.STATE_HIDDEN:
                        //tbd
                        break;

                    case BottomSheetBehavior.STATE_SETTLING:
                        //tbd
                        break;

                    case BottomSheetBehavior.STATE_HALF_EXPANDED:
                        //tbd
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                //tbd
                // potentially empty (?)
            }
        });

        f.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        //tbd
                        break;

                    case BottomSheetBehavior.STATE_EXPANDED:
                        //tbd
                        break;

                    case BottomSheetBehavior.STATE_DRAGGING:
                        //tbd
                        break;

                    case BottomSheetBehavior.STATE_HIDDEN:
                        //tbd
                        break;

                    case BottomSheetBehavior.STATE_SETTLING:
                        //tbd
                        break;

                    case BottomSheetBehavior.STATE_HALF_EXPANDED:
                        //tbd
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                //tbd
                // potentially empty (?)
            }
        });
    }

}
