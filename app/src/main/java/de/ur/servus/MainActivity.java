package de.ur.servus;

import static de.ur.servus.Helpers.ifSubscribedToEvent;
import static de.ur.servus.Helpers.removeAttendingEvent;
import static de.ur.servus.Helpers.saveAttendingEvent;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import de.ur.servus.core.BackendHandler;
import de.ur.servus.core.Event;
import de.ur.servus.core.EventListener;
import de.ur.servus.core.ListenerRegistration;
import de.ur.servus.core.firebase.FirestoreBackendHandler;


public class MainActivity extends FragmentActivity implements OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback {

    private static final int REQUEST_LOCATION_PERMISSION = 1;

    private final BackendHandler backendHandler = FirestoreBackendHandler.getInstance();
    private SharedPreferences sharedPreferences;
    private CustomLocationManager customLocationManager;
    @Nullable
    private GoogleMap mMap;
    @Nullable
    private ListenerRegistration allEventsListenerRegistration;
    @Nullable
    private ListenerRegistration singleEventListenerRegistration;
    MarkerManager markerManager;

    @Nullable
    DetailsBottomSheetFragment detailsBottomSheetFragment;

    View c_bottomSheet;
    View s_bottomSheet;
    View f_bottomSheet;
    BottomSheetBehavior<View> c_bottomSheetBehavior;
    BottomSheetBehavior<View> s_bottomSheetBehavior;
    BottomSheetBehavior<View> f_bottomSheetBehavior;

    ShapeableImageView btn_settings;
    Button btn_creator;
    ShapeableImageView btn_filter;

    Button btn_create_event;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkAndAskPermissions();

        sharedPreferences = this.getPreferences(Context.MODE_PRIVATE);
        markerManager = new MarkerManager();
        customLocationManager = new CustomLocationManager(this);
        detailsBottomSheetFragment = new DetailsBottomSheetFragment(sharedPreferences, this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        /*
         * Initialize all Bottom Sheets, add a corresponding BottomSheetBehavior and referring Callbacks.
         */
        c_bottomSheet = findViewById(R.id.creator_bottomSheet);
        s_bottomSheet = findViewById(R.id.settings_bottomSheet);
        f_bottomSheet = findViewById(R.id.filter_bottomSheet);
        c_bottomSheetBehavior = BottomSheetBehavior.from(c_bottomSheet);
        s_bottomSheetBehavior = BottomSheetBehavior.from(s_bottomSheet);
        f_bottomSheetBehavior = BottomSheetBehavior.from(f_bottomSheet);

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
            ifSubscribedToEvent(sharedPreferences,
                    eventId -> showDetailsBottomSheet(),
                    () -> c_bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED)
            );
        });

        btn_filter = findViewById(R.id.btn_filter);
        btn_filter.setOnClickListener(v -> f_bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED));

        btn_create_event = findViewById(R.id.event_create_button);
        btn_create_event.setOnClickListener(v -> createEvent());

        ifSubscribedToEvent(sharedPreferences,
                this::subscribeEvent,
                null
        );

    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        try {
            googleMap.setMapStyle(new MapStyleOptions(getResources().getString(R.string.map_light_mode)));
        } catch (Resources.NotFoundException e) {
            Log.e("Debug: ", "Can't find style. Error: ", e);
        }

        // on marker click load/show event
        mMap.setOnMarkerClickListener(marker -> {
            var eventId = Objects.requireNonNull(marker.getTag()).toString();
            subscribeEvent(eventId);
            showDetailsBottomSheet();
            return true;
        });

        // This can fail on first run, because permission is not granted. (onRequestPermissionsResult handles this case)
        centerCamera(mMap);

        int presetNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        changeMapStyle(presetNightMode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // If location permission was granted center camera
        if (requestCode == REQUEST_LOCATION_PERMISSION && mMap != null) {
            centerCamera(mMap);
        }
    }

    public void subscribeAllEvents() {
        if (allEventsListenerRegistration != null) {
            allEventsListenerRegistration.unsubscribe();
        }

        this.allEventsListenerRegistration = backendHandler.subscribeEvents(new EventListener<>() {
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
                ifSubscribedToEvent(sharedPreferences,
                        eventId -> {
                            markerManager.showSingleMarker(eventId);
                            setStyleClicked();
                        }, () -> {
                            markerManager.showAllMarkers();
                            setStyleDefault();
                        }
                );
            }

            @Override
            public void onError(Exception e) {
                // TODO error handling here
                Log.e("Data", e.getMessage());
            }

        });
    }

    public void subscribeEvent(String eventId) {
        if (singleEventListenerRegistration != null) {
            singleEventListenerRegistration.unsubscribe();
        }

        singleEventListenerRegistration = backendHandler.subscribeEvent(eventId, new EventListener<>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onEvent(Event e) {
                if (detailsBottomSheetFragment != null) {
                    detailsBottomSheetFragment.setEvent(e);

                    detailsBottomSheetFragment.setOnClickListener(event -> {
                        ifSubscribedToEvent(sharedPreferences,
                                eventId -> leaveEvent(eventId), // leave current event (not necessarily event in bottom sheet)
                                () -> attendEvent(event.getId())
                        );
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e("Data", e.getMessage());
                removeAttendingEvent(sharedPreferences);
                setStyleDefault();
            }
        });
    }

    private void centerCamera(@NonNull GoogleMap mMap) {
        final float ZOOM_FACTOR = 13.0f;

        var latLng = customLocationManager.getLastObservedLocation(this);
        if (latLng != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, ZOOM_FACTOR));
        }
    }

    private void checkAndAskPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Handle case, where user wont give permission. Ask again?
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSION
            );
        }
    }

    private void showDetailsBottomSheet() {
        if (detailsBottomSheetFragment != null) {
            detailsBottomSheetFragment.show(getSupportFragmentManager(), detailsBottomSheetFragment.getTag());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (this.allEventsListenerRegistration != null) {
            this.allEventsListenerRegistration.unsubscribe();
        }

        // TODO unsubscribe single event
    }

    @Override
    protected void onResume() {
        super.onResume();

        subscribeAllEvents();
        // TODO subscribe single event again (which event id?)
    }

    private void attendEvent(String eventId) {
        setStyleClicked();
        saveAttendingEvent(sharedPreferences, eventId);
        markerManager.showSingleMarker(eventId);
        backendHandler.incrementEventAttendants(eventId, null);
    }

    private void leaveEvent(String eventId) {
        setStyleDefault();
        removeAttendingEvent(sharedPreferences);
        markerManager.showAllMarkers();
        backendHandler.decrementEventAttendants(eventId, null);
    }

    private void setStyleClicked() {
        if (btn_creator != null) {
            btn_creator.setText(R.string.event_details_button_withdraw);
            btn_creator.setBackgroundResource(R.drawable.style_btn_roundedcorners_clicked);
            btn_creator.setTextColor(getResources().getColor(R.color.servus_pink, getTheme()));
        }
    }

    private void setStyleDefault() {
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
        int attendants = 0;
        LatLng location = customLocationManager.getLastObservedLocation(this);

        Event event = new Event(event_name, event_description, location, attendants);

        backendHandler.createNewEvent(event, new EventListener<>() {
            @Override
            public void onEvent(String id) {
                attendEvent(id);
            }

            @Override
            public void onError(Exception e) {
                // TODO handle errors
            }
        });

        //close bottomsheet
        c_bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    private void changeMapStyle(int currentNightMode) {
        switch (currentNightMode) {
            case Configuration.UI_MODE_NIGHT_NO:
                Log.d("Debug: ", "Light Mode");
                mMap.setMapStyle(new MapStyleOptions(getResources().getString(R.string.map_light_mode)));
                break;

            case Configuration.UI_MODE_NIGHT_YES:
                Log.d("Debug: ", "Dark Mode");
                mMap.setMapStyle(new MapStyleOptions(getResources().getString(R.string.map_dark_mode)));
                break;
        }
    }
}
