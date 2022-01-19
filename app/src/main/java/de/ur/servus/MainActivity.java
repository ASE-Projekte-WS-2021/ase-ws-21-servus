package de.ur.servus;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.FragmentActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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

    Context context;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    private final String SUBSCRIBED_TO_EVENT = "subscribedToEvent";

    private GoogleMap mMap;
    private ListenerRegistration listenerRegistration;
    private static final int REQUEST_LOCATION_PERMISSION = 1;

    LocationManager locationManager;
    LocationListener locationListener;
    Marker marker;

    View c_bottomSheet;
    View p_bottomSheet;
    View s_bottomSheet;
    View f_bottomSheet;
    BottomSheetBehavior<View> c_bottomSheetBehavior;
    BottomSheetBehavior<View> p_bottomSheetBehavior;
    BottomSheetBehavior<View> s_bottomSheetBehavior;
    BottomSheetBehavior<View> f_bottomSheetBehavior;

    ShapeableImageView btn_settings;
    Button btn_creator;
    ShapeableImageView btn_filter;

    TextView details_eventname;
    TextView details_description;
    TextView details_attendees;
    //TextView details_creator; //Not part of MVP

    Button btn_attend_withdraw;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = getApplicationContext();
        sharedPreferences = this.getPreferences(Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        /*
         * Initialize all Bottom Sheets, add a corresponding BottomSheetBehavior and referring Callbacks.
         */
        c_bottomSheet = findViewById(R.id.creator_bottomSheet);
        p_bottomSheet = findViewById(R.id.participant_bottomSheet);
        s_bottomSheet = findViewById(R.id.settings_bottomSheet);
        f_bottomSheet = findViewById(R.id.filter_bottomSheet);
        c_bottomSheetBehavior = BottomSheetBehavior.from(c_bottomSheet);
        p_bottomSheetBehavior = BottomSheetBehavior.from(p_bottomSheet);
        s_bottomSheetBehavior = BottomSheetBehavior.from(s_bottomSheet);
        f_bottomSheetBehavior = BottomSheetBehavior.from(f_bottomSheet);
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
        btn_settings = findViewById(R.id.btn_settings);
        btn_settings.setOnClickListener(v -> s_bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED));

        btn_creator = findViewById(R.id.btn_meetup);
        btn_creator.setOnClickListener(v -> {
            /*
             * Add behavior for create button, if user is already subscribed to an event as attendant
             */
            String subscribed = sharedPreferences.getString(SUBSCRIBED_TO_EVENT, "none");
            if (subscribed.equals("none")){
                c_bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
            else{
                p_bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });

        btn_filter = findViewById(R.id.btn_filter);
        btn_filter.setOnClickListener(v -> f_bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED));

        btn_attend_withdraw = findViewById(R.id.event_details_button);

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

        /*
         * Initialize all TextViews that will get manipulated by value within the Bottom Sheets
         */
        details_eventname = findViewById(R.id.event_details_eventname);
        details_description = findViewById(R.id.event_details_description);
        details_attendees = findViewById(R.id.event_details_attendees);
        //details_creator = findViewById(R.id.event_details_creator); //Not part of MVP


        /*
         * Initialize Creator button based on an event subscription
         */
        String subscribed = sharedPreferences.getString(SUBSCRIBED_TO_EVENT, "none");
        if (!subscribed.equals("none")){
            loadDataForMarker(subscribed);
            setStyleClicked();
        }
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

                String subscribed = sharedPreferences.getString(SUBSCRIBED_TO_EVENT, "none");

                // Load event data
                if (subscribed.equals("none")){
                    // Iterate through the whole list and add markers without limitation
                    for (Event event : events) {
                        Marker marker = mMap.addMarker(new MarkerOptions().position(event.getLocation()).title(event.getName()));
                        if (marker != null) {
                            marker.setTag(event.getId());
                        }
                    }
                } else {
                    // Iterate through the whole list and only add the event with the subscribed ID
                    for (Event event : events) {
                        if (event.getId().equals(subscribed)){
                            Marker marker = mMap.addMarker(new MarkerOptions().position(event.getLocation()).title(event.getName()));
                            if (marker != null) {
                                marker.setTag(event.getId());
                            }
                        }
                    }
                }

                mMap.setOnMarkerClickListener(marker -> {
                    loadDataForMarker(Objects.requireNonNull(marker.getTag()).toString());
                    p_bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    return true;
                });
            }

            @Override
            public void onError(Exception e) {
                // TODO error handling here
                Log.e("Data", e.getMessage());
            }

        });
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

    private void loadDataForMarker(String eventID) {
        BackendHandler bh_marker = FirestoreBackendHandler.getInstance();
        bh_marker.subscribeEvent(eventID, new EventListener<>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onEvent(Event event) {
                if (event.getName() != null) {
                    details_eventname.setText(event.getName());
                }
                if (event.getDescription() != null){
                    details_description.setText(event.getDescription());
                }
                if (event.getAttendants() != null){
                    details_attendees.setText(Long.toString(event.getAttendants()));
                }
                //TODO details_creator.setText(event.getName());

                /*
                 * Add Button click behavior to attend/withdraw button in participation bottom sheet
                 */
                btn_attend_withdraw.setOnClickListener(v -> {
                    String subscribed = sharedPreferences.getString(SUBSCRIBED_TO_EVENT, "none");
                    if (subscribed.equals("none")) attendEvent(event.getId());
                    else leaveEvent(event.getId());
                });
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

    private void attendEvent(String eventId) {
        BackendHandler bh_attend_withdraw = FirestoreBackendHandler.getInstance();
        bh_attend_withdraw.incrementEventAttendants(eventId);
        setStyleClicked();
        editor.putString(SUBSCRIBED_TO_EVENT, eventId);
        editor.apply();

    }

    private void leaveEvent(String eventId) {
        BackendHandler bh_attend_withdraw = FirestoreBackendHandler.getInstance();
        bh_attend_withdraw.decrementEventAttendants(eventId);
        setStyleDefault();
        editor.putString(SUBSCRIBED_TO_EVENT, "none");
        editor.apply();
    }

    private void setStyleClicked(){
        if (btn_attend_withdraw != null) {
            btn_attend_withdraw.setText(R.string.event_details_button_withdraw);
            btn_attend_withdraw.setBackgroundResource(R.drawable.style_btn_roundedcorners_clicked);
            btn_attend_withdraw.setTextColor(getResources().getColor(R.color.servus_pink, getTheme()));
        }

        if (btn_creator != null) {
            btn_creator.setText(R.string.event_details_button_withdraw);
            btn_creator.setBackgroundResource(R.drawable.style_btn_roundedcorners_clicked);
            btn_creator.setTextColor(getResources().getColor(R.color.servus_pink, getTheme()));
        }
    }

    private void setStyleDefault(){
        if (btn_attend_withdraw != null) {
            btn_attend_withdraw.setText(R.string.event_details_button_attend);
            btn_attend_withdraw.setBackgroundResource(R.drawable.style_btn_roundedcorners);
            btn_attend_withdraw.setTextColor(getResources().getColor(R.color.servus_white, getTheme()));
        }

        if (btn_creator != null) {
            btn_creator.setText(R.string.content_create_meetup);
            btn_creator.setBackgroundResource(R.drawable.style_btn_roundedcorners);
            btn_creator.setTextColor(getResources().getColor(R.color.servus_white, getTheme()));
        }
    }
}
