package de.ur.servus;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
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
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.imageview.ShapeableImageView;

import de.ur.servus.core.BackendHandler;
import de.ur.servus.core.Event;
import de.ur.servus.core.EventListener;
import de.ur.servus.core.firebase.FirestoreBackendHandler;
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

    MarkerManager markerManager;

    double latitude;
    double longitude;

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
    Button btn_create_event;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = getApplicationContext();
        sharedPreferences = this.getPreferences(Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        markerManager = new MarkerManager();

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
            if (subscribed.equals("none")) {
                c_bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            } else {
                p_bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });

        btn_filter = findViewById(R.id.btn_filter);
        btn_filter.setOnClickListener(v -> f_bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED));

        btn_attend_withdraw = findViewById(R.id.event_details_button);

        btn_create_event = findViewById(R.id.event_create_button);
        btn_create_event.setOnClickListener(v -> createEvent());

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

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
        if (!subscribed.equals("none")) {
            loadDataForEvent(subscribed);
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

                // save new markers
                var markers = events.stream().map(event -> {
                    Marker marker = mMap.addMarker(new MarkerOptions().position(event.getLocation()).title(event.getName()));
                    if (marker != null) {
                        marker.setTag(event.getId());
                    }
                    return marker;
                }).filter(Objects::nonNull).collect(Collectors.toList());
                markerManager.setMarkers(markers);

                // show relevant markers
                String subscribed = sharedPreferences.getString(SUBSCRIBED_TO_EVENT, "none");

                if (subscribed.equals("none")) {
                    markerManager.showAllMarkers();
                    setStyleDefault();
                } else {
                    // TODO check if event really exists...
                    markerManager.showSingleMarker(subscribed);
                    setStyleClicked();
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
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // on marker click load/show event
        mMap.setOnMarkerClickListener(marker -> {
            loadDataForEvent(Objects.requireNonNull(marker.getTag()).toString());
            p_bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            return true;
        });

        // get inital position and move camera
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                //getting the latitude and longitude of the user's position
                latitude = location.getLatitude();
                longitude = location.getLongitude();

                Geocoder geocoder = new Geocoder(getApplicationContext());
                try {
                    List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                    LatLng latLng = new LatLng(latitude, longitude);
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

        //asking for the users permission to use the location
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION
            );
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
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

    private void loadDataForEvent(String eventID) {
        BackendHandler bh_marker = FirestoreBackendHandler.getInstance();
        bh_marker.subscribeEvent(eventID, new EventListener<>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onEvent(Event event) {
                details_eventname.setText(event.getName());
                details_description.setText(event.getDescription());
                details_attendees.setText(Long.toString(event.getAttendants()));
                //TODO details_creator.setText(event.getName());

                /*
                 * Add Button click behavior to attend/withdraw button in participation bottom sheet
                 */
                btn_attend_withdraw.setOnClickListener(v -> {
                    String subscribed = sharedPreferences.getString(SUBSCRIBED_TO_EVENT, "none");
                    var bh = FirestoreBackendHandler.getInstance();

                    if (subscribed.equals("none")) {
                        attendEvent(event.getId());
                        bh.incrementEventAttendants(event.getId());
                    } else {
                        leaveEvent(event.getId());
                        bh.decrementEventAttendants(event.getId());
                    }
                });

            }

            @Override
            public void onError(Exception e) {
                // TODO error handling here
                Log.e("Data", e.getMessage());

                editor.putString(SUBSCRIBED_TO_EVENT, "none");
                editor.apply();

                setStyleDefault();
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
        setStyleClicked();
        editor.putString(SUBSCRIBED_TO_EVENT, eventId);
        editor.apply();
        markerManager.showSingleMarker(eventId);
    }

    private void leaveEvent(String eventId) {
        setStyleDefault();
        editor.putString(SUBSCRIBED_TO_EVENT, "none");
        editor.apply();
        markerManager.showAllMarkers();
    }

    private void setStyleClicked() {
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

    private void setStyleDefault() {
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

    private void createEvent() {
        //MVP: create POJO for firebase backend handler and use corresponding method
        EditText et_event_name = findViewById(R.id.event_creation_eventname);
        String event_name = et_event_name.getText().toString();

        EditText et_event_description = findViewById(R.id.event_creation_description);
        String event_description = et_event_description.getText().toString();
        int attendants = 1;

        //we assume that the user doesn't move to much, the latLon of the user updates ONCE on startup of app


        Event event = new Event(event_name, event_description, new LatLng(latitude, longitude), attendants);
        var bh = FirestoreBackendHandler.getInstance();
        bh.createNewEvent(event).addOnSuccessListener(id ->
                bh.subscribeEvent(id, new EventListener<>() {
                    @Override
                    public void onEvent(Event event) {
                        attendEvent(id);
                    }

                    @Override
                    public void onError(Exception e) {
                        // TODO handle errors
                    }
                }));

        //close bottomsheet
        c_bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }
}
