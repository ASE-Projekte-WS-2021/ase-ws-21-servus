package de.ur.servus;

import static de.ur.servus.Helpers.ifSubscribedToEvent;
import static de.ur.servus.Helpers.removeAttendingEvent;
import static de.ur.servus.Helpers.saveAttendingEvent;
import static de.ur.servus.Helpers.tryGetSubscribedEvent;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
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

    DetailsBottomSheetFragment detailsBottomSheetFragment = new DetailsBottomSheetFragment();
    EventCreationBottomSheetFragment eventCreationBottomSheetFragment = new EventCreationBottomSheetFragment();
    SettingsBottomSheetFragment settingsBottomSheetFragment = new SettingsBottomSheetFragment();
    FilterBottomSheetFragment filterBottomSheetFragment = new FilterBottomSheetFragment();

    /*
     * Views
     */
    ShapeableImageView btn_settings;
    Button btn_creator;
    ShapeableImageView btn_filter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = this.getPreferences(Context.MODE_PRIVATE);
        markerManager = new MarkerManager();
        customLocationManager = new CustomLocationManager(this);

        checkAndAskPermissions();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        /*
         * Setup views
         */
        btn_settings = findViewById(R.id.btn_settings);
        btn_settings.setOnClickListener(v -> showBottomSheet(settingsBottomSheetFragment));

        btn_creator = findViewById(R.id.btn_meetup);
        btn_creator.setOnClickListener(v -> {
            /*
             * Add behavior for create button, if user is already subscribed to an event as attendant
             */
            ifSubscribedToEvent(sharedPreferences,
                    eventId -> showBottomSheet(detailsBottomSheetFragment),
                    () -> showBottomSheet(eventCreationBottomSheetFragment)
            );
        });

        btn_filter = findViewById(R.id.btn_filter);
        btn_filter.setOnClickListener(v -> showBottomSheet(filterBottomSheetFragment));

        eventCreationBottomSheetFragment.update(this::onEventCreationCreateClicked);

        ifSubscribedToEvent(sharedPreferences,
                this::subscribeEvent,
                null
        );

    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

        try {
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
        } catch (Resources.NotFoundException e) {
            Log.e("Debug: ", "Can't find style. Error: ", e);
        }

        // on marker click load/show event
        mMap.setOnMarkerClickListener(marker -> {
            var eventId = Objects.requireNonNull(marker.getTag()).toString();
            subscribeEvent(eventId);
            // TODO wait before initial data was fetched before showing bottom sheet
            showBottomSheet(detailsBottomSheetFragment);
            return true;
        });

        // This can fail on first run, because permission is not granted. (onRequestPermissionsResult handles this case)
        centerCamera(mMap);
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
            public void onEvent(Event event) {
                boolean attending = tryGetSubscribedEvent(sharedPreferences)
                        .map(eventId -> eventId.equals(event.getId()))
                        .orElse(false);

                // update details sheet
                if (detailsBottomSheetFragment != null) {
                    detailsBottomSheetFragment.update(event, attending, (e, a) -> onDetailsAttendWithdrawClicked(e, a));
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

    private void showBottomSheet(@Nullable BottomSheetDialogFragment bottomSheet) {
        if (bottomSheet != null) {
            bottomSheet.show(getSupportFragmentManager(), bottomSheet.getTag());
        }
    }

    private void onDetailsAttendWithdrawClicked(Event event, boolean attending) {
        if (attending) {
            leaveEvent(event.getId());
        } else {
            attendEvent(event.getId());
        }
    }

    private void onEventCreationCreateClicked(EventCreationData inputEventData) {
        Helpers.createEvent(this, customLocationManager, inputEventData, new EventListener<>() {
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
        if (eventCreationBottomSheetFragment != null) {
            eventCreationBottomSheetFragment.dismiss();
        }
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
}
