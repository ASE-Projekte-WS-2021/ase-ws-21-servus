package de.ur.servus;

import static de.ur.servus.Helpers.ifSubscribedToEvent;
import static de.ur.servus.Helpers.removeAttendingEvent;
import static de.ur.servus.Helpers.saveAttendingEvent;
import static de.ur.servus.Helpers.tryGetSubscribedEvent;
import static de.ur.servus.SettingsBottomSheetFragment.PICK_IMAGE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
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
import de.ur.servus.core.UserProfile;
import de.ur.servus.core.firebase.FirestoreBackendHandler;
import de.ur.servus.utils.AvatarEditor;


public class MainActivity extends FragmentActivity implements OnMapReadyCallback, ClusterManagerContext {

    public static final String SUBSCRIBED_TO_EVENT = "subscribedToEvent";
    private static final String TUTORIAL_PREFS_ITEM = "tutorialSeen";

    private final BackendHandler backendHandler = FirestoreBackendHandler.getInstance();
    Context context;
    SharedPreferences sharedPreferences;
    CustomLocationManager customLocationManager;
    MarkerManager markerManager;
    CustomMarkerRenderer customMarkerRenderer;

    AvatarEditor avatarEditor;

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
    LinearLayout error_message;



    /**
     *
     *
     *      App state handling
     *
     *
     *
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = getApplicationContext();
        sharedPreferences = this.getPreferences(Context.MODE_PRIVATE);
        markerManager = new MarkerManager();
        customLocationManager = new CustomLocationManager(this);
        avatarEditor = new AvatarEditor(this);

        // when GPS is turned off, ask to turn it on. Starting to listen needs to be done in onCreate
        customLocationManager.addOnProviderDisabledListener(customLocationManager::showEnableGpsDialogIfNecessary);

        if (!sharedPreferences.getBoolean(TUTORIAL_PREFS_ITEM, false)) {
            sharedPreferences.edit().putBoolean(TUTORIAL_PREFS_ITEM, true).apply();

            Intent intent = new Intent(MainActivity.this, TutorialActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

        checkAndAskLocationPermission();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Setup views
        btn_settings = findViewById(R.id.btn_settings);
        btn_settings.setOnClickListener(v -> showBottomSheet(settingsBottomSheetFragment));

        btn_creator = findViewById(R.id.btn_meetup);
        btn_creator.setOnClickListener(v -> {
            // Add behavior for create button, if user is already subscribed to an event as attendant
            ifSubscribedToEvent(sharedPreferences,
                    eventId -> showBottomSheet(detailsBottomSheetFragment),
                    () -> showBottomSheet(eventCreationBottomSheetFragment)
            );
        });

        btn_filter = findViewById(R.id.btn_filter);
        btn_filter.setOnClickListener(v -> showBottomSheet(filterBottomSheetFragment));

        eventCreationBottomSheetFragment.update(this::onEventCreationCreateClicked);
        settingsBottomSheetFragment.update(this::onUserProfileSaved);

        ifSubscribedToEvent(sharedPreferences,
                this::subscribeEvent,
                null
        );

        // Network change handling
        error_message = findViewById(R.id.container_404);
        ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                // network available
                runOnUiThread(() -> error_message.setVisibility(View.GONE));
            }

            @Override
            public void onLost(Network network) {
                // network unavailable
                runOnUiThread(() -> error_message.setVisibility(View.VISIBLE));
            }
        };
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        connectivityManager.registerDefaultNetworkCallback(networkCallback);

        //Account information
        btn_settings.setImageBitmap(avatarEditor.loadProfilePicture());
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

    @Override
    protected void onResume() {
        super.onResume();

        customLocationManager.startListeningForLocationUpdates();
        customLocationManager.startListeningProviderDisabled();
        customLocationManager.showEnableGpsDialogIfNecessary();

        subscribeAllEvents();
        // TODO subscribe single event again (which event id?)
    }



    /**
     *
     *
     *      Permission functionality
     *
     *
     *
     */

    // Method Duplicate in SettingsBottomSheet, despite please don't delete!
    /*   Better performance & UX with duplication: code in SettingsBottomSheet will be called from TutorialActivity in OnBoardingScreen.
     *   However, for user's that didn't create an account in Onboarding OR didn't upload a profile picture and therefore have never set the permission yet,
     *   the code part in SettingsBottomSheet would be called, but too late for the intent to show correctly after granting the permission
     *
     *   Conclusion: This code part will be called BEFORE the code in SettingsBottomSheet if the user has to grant permission from MainActivity
     *      --> Pick dialog will show in-time and user has one click less and a better UX. :-)
     */
    private final ActivityResultLauncher<String> requestStoragePermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Permission is granted
                    Intent getIntent = new Intent(Intent.ACTION_PICK);
                    getIntent.setType("image/*");
                    startActivityForResult(Intent.createChooser(getIntent, getResources().getString(R.string.settings_profile_picture_picker)), PICK_IMAGE);
                } else {
                    // Permission is denied
                    Toast.makeText(context, getResources().getString(R.string.toast_storage_permission_error), Toast.LENGTH_LONG).show();
                }
            });

    // Handling & Listening to location permission: ResultLauncher to listen to incoming results for location permission
    private final ActivityResultLauncher<String> requestLocationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Permission is granted
                    if (mMap != null) {
                        centerCamera(mMap);
                    }
                } else {
                    // Permission is denied
                    Intent intent = new Intent(MainActivity.this, PermissionDeniedActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            });

    // Handling & Listening to location permission: Method call to ask for location permission
    private void checkAndAskLocationPermission() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // You can use the API that requires the permission.
            if (mMap != null) {
                centerCamera(mMap);
            }
        } else {
            // You can directly ask for the permission. The registered ActivityResultCallback gets the result of this request.
            requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }



    /**
     *
     *
     *      Bottom sheet functionality
     *
     *
     *
     */

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
        Helpers.createEvent(customLocationManager, inputEventData, new EventListener<>() {
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

    private void onUserProfileSaved(UserProfile userProfile) {
        btn_settings.setImageBitmap(avatarEditor.loadProfilePicture());
    }



    /**
     *
     *
     *      Map features
     *
     *
     *
     */

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

        mMap.setMapStyle(getMapStyle(currentNightMode));

        // This can fail on first run, because permission is not granted. (onRequestPermissionsResult handles this case)
        centerCamera(mMap);

        markerManager = new MarkerManager();
        markerManager.setUpClusterManager(this, mMap);
        markerManager.setClusterAlgorithm();
        customMarkerRenderer = new CustomMarkerRenderer(this, sharedPreferences, mMap, markerManager.getClusterManager());
    }

    void animateZoomInCamera(LatLng latLng) {
        if (mMap != null) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10f));
        }
    }

    private void centerCamera(@NonNull GoogleMap mMap) {
        final float ZOOM_FACTOR = 13.0f;

        customLocationManager.getLastObservedLocation(latLng -> latLng.ifPresent(lng -> mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lng, ZOOM_FACTOR))));
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



    /**
     *
     *
     *      Map-Event features
     *
     *
     *
     */

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
                ifSubscribedToEvent(sharedPreferences,
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

    private void attendEvent(String eventId) {
        setStyleClicked();
        saveAttendingEvent(sharedPreferences, eventId);
        markerManager.getClusterManager().cluster();
        backendHandler.incrementEventAttendants(eventId, null);
    }

    private void leaveEvent(String eventId) {
        setStyleDefault();
        removeAttendingEvent(sharedPreferences);
        markerManager.getClusterManager().cluster();
        backendHandler.decrementEventAttendants(eventId, null);
    }



    /**
     *
     *
     *      App Styling
     *
     *
     *
     */

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
