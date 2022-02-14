package de.ur.servus;

import static de.ur.servus.CustomLocationManager.REQUEST_LOCATION_PERMISSION;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.ConnectivityManager;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.maps.android.clustering.Cluster;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import de.ur.servus.core.BackendHandler;
import de.ur.servus.core.Event;
import de.ur.servus.core.EventListener;
import de.ur.servus.core.ListenerRegistration;
import de.ur.servus.core.firebase.FirestoreBackendHandler;


public class MainActivity extends FragmentActivity implements OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback, ClusterManagerContext {

    public static final String SUBSCRIBED_TO_EVENT = "subscribedToEvent";
    private static final String TUTORIAL_PREFS_ITEM = "tutorialSeen";

    private final BackendHandler backendHandler = FirestoreBackendHandler.getInstance();
    private SubscribedEventHelpers subscribedEventHelpers;
    Context context;
    SharedPreferences sharedPreferences;
    CustomLocationManager customLocationManager;
    MarkerManager markerManager;
    CustomMarkerRenderer customMarkerRenderer;

    @Nullable
    private GoogleMap mMap;
    @Nullable
    private ListenerRegistration allEventsListenerRegistration;
    @Nullable
    private ListenerRegistration singleEventListenerRegistration;

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

    BroadcastReceiver networkReceiver;
    IntentFilter networkFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = getApplicationContext();
        sharedPreferences = this.getPreferences(Context.MODE_PRIVATE);
        markerManager = new MarkerManager();
        customLocationManager = new CustomLocationManager(this);
        subscribedEventHelpers = new SubscribedEventHelpers(sharedPreferences);

        // when GPS is turned off, ask to turn it on. Starting to listen needs to be done in onCreate
        customLocationManager.addOnProviderDisabledListener(customLocationManager::showEnableGpsDialogIfNecessary);

        if (!sharedPreferences.getBoolean(TUTORIAL_PREFS_ITEM, false)) {
            sharedPreferences.edit().putBoolean(TUTORIAL_PREFS_ITEM, true).apply();

            Intent intent = new Intent(MainActivity.this, TutorialActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

        checkAndAskPermissions();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

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
            subscribedEventHelpers.ifSubscribedToEvent(
                    preferences -> {
                        subscribeEvent(preferences.eventId);
                        showBottomSheet(detailsBottomSheetFragment);
                    },
                    () -> showBottomSheet(eventCreationBottomSheetFragment)
            );
        });

        btn_filter = findViewById(R.id.btn_filter);
        btn_filter.setOnClickListener(v -> showBottomSheet(filterBottomSheetFragment));

        eventCreationBottomSheetFragment.update(this::onEventCreationCreateClicked);

        subscribedEventHelpers.ifSubscribedToEvent(
                eventPreferences -> this.subscribeEvent(eventPreferences.eventId),
                null
        );

        /*
         * Initialize network broadcast receiver
         */
        networkReceiver = new NetworkChangeReceiver(this);
        networkFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
    }

    @Override
    protected void onResume() {
        super.onResume();

        customLocationManager.startListeningForLocationUpdates();
        customLocationManager.startListeningProviderDisabled();
        customLocationManager.showEnableGpsDialogIfNecessary();

        subscribeAllEvents();
        // TODO subscribe single event again (which event id?)
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
                markerManager.getClusterManager().clearItems();

                // create markers
                events.forEach(event -> {
                    MarkerClusterItem marker = new MarkerClusterItem(event.getLocation(), event.getName(), event.getName(), event.getId());
                    markerManager.getClusterManager().addItem(marker);
                });

                markerManager.getClusterManager().cluster();

                // style bottom button
                subscribedEventHelpers.ifSubscribedToEvent(
                        eventId -> setStyleClicked(),
                        () -> setStyleDefault()
                );
            }

            @Override
            public void onError(Exception e) {
                // TODO error handling here
                Log.e("Data", e.getMessage());
                // TODO leave current event
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
                var eventPreferences = subscribedEventHelpers.tryGetSubscribedEvent();
                var attending = eventPreferences.eventId != null && eventPreferences.eventId.equals(event.getId());

                // update details sheet
                if (detailsBottomSheetFragment != null) {
                    detailsBottomSheetFragment.update(event, attending, (e, a) -> onDetailsAttendWithdrawClicked(e, a));
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e("Data", e.getMessage());
                subscribedEventHelpers.removeAttendingEvent();
                setStyleDefault();
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

        mMap.setMapStyle(getMapStyle(currentNightMode));

        // This can fail on first run, because permission is not granted. (onRequestPermissionsResult handles this case)
        centerCamera(mMap);

        markerManager = new MarkerManager();
        markerManager.setUpClusterManager((ClusterManagerContext) this, mMap);
        markerManager.setClusterAlgorithm();
        customMarkerRenderer = new CustomMarkerRenderer(this, sharedPreferences, mMap, markerManager.getClusterManager());
    }


    void animateZoomInCamera(LatLng latLng) {
        if (mMap != null) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10f));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // If location permission was granted center camera
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (mMap != null) {
                centerCamera(mMap);
            }
        } else {
            // User doesn't have permission again as Permission is not granted by user
            // Now further, we need to check if the used denied permanently or not
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                // User has denied permission once but he didn't click on "Never Show again" check box
                // Recursively ask the user again for the permission

                checkAndAskPermissions(); // Might never be called atm
            } else {
                // User denied the permission and also clicked on the "Never Show again" check box. Permission denied permanently.
                // Open Permission denied activity with link to the setting's page from here

                Intent intent = new Intent(MainActivity.this, PermissionDeniedActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }
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
            attendEvent(event.getId(), false);
        }
    }

    private void onEventCreationCreateClicked(EventCreationData inputEventData) {
        subscribedEventHelpers.createEvent(customLocationManager, inputEventData, new EventListener<>() {
            @Override
            public void onEvent(String id) {
                attendEvent(id, true);
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

        customLocationManager.getLastObservedLocation(latLng -> latLng.ifPresent(lng -> mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lng, ZOOM_FACTOR))));
    }

    private void checkAndAskPermissions() {
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // User doesn't have permission. Now we need to check further if permission was shown before or not
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                // User has denied permission once but he didn't clicked on "Never Show again" check box

                Intent intent = new Intent(MainActivity.this, PermissionDeniedActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } else {
                // User has never seen the permission Dialog. Request for permission.
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_LOCATION_PERMISSION
                );
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (this.allEventsListenerRegistration != null) {
            this.allEventsListenerRegistration.unsubscribe();
        }

        customLocationManager.stopListeningForLocationUpdates();
        customLocationManager.stopListeningProviderDisabled();

        // TODO unsubscribe single event
    }

    private void attendEvent(String eventId, boolean isCreator) {
        setStyleClicked();
        var subscribedEventInfos = new EventPreferences(eventId, isCreator);
        subscribedEventHelpers.saveAttendingEvent(subscribedEventInfos);
        markerManager.getClusterManager().cluster();
        backendHandler.incrementEventAttendants(eventId, null);
    }

    private void leaveEvent(String eventId) {
        setStyleDefault();
        subscribedEventHelpers.removeAttendingEvent();
        markerManager.getClusterManager().cluster();
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

    private MapStyleOptions getMapStyle(int currentNightMode) {
        try {
            switch (currentNightMode) {
                case Configuration.UI_MODE_NIGHT_NO:
                    Log.d("Debug: ", "Light Mode");
                    return new MapStyleOptions(getResources().getString(R.string.map_light_mode));
                case Configuration.UI_MODE_NIGHT_YES:
                    Log.d("Debug: ", "Dark Mode");
                    return new MapStyleOptions(getResources().getString(R.string.map_dark_mode));
                default:
                    return new MapStyleOptions(getResources().getString(R.string.map_light_mode));
            }
        } catch (Resources.NotFoundException e) {
            Log.e("Debug: ", "Can't find style. Error: ", e);
            return new MapStyleOptions(getResources().getString(R.string.map_light_mode));
        }
    }

    @Override
    public boolean onClusterClick(Cluster cluster) {
        animateZoomInCamera(cluster.getPosition());
        return false;
    }

    @Override
    public boolean onClusterItemClick(MarkerClusterItem markerClusterItem) {
        var eventId = Objects.requireNonNull(markerClusterItem.getEventId());
        subscribeEvent(eventId);
        // TODO wait before initial data was fetched before showing bottom sheet
        showBottomSheet(detailsBottomSheetFragment);
        return false;
    }
}
