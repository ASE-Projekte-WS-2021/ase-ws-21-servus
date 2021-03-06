package de.ur.servus;

import static de.ur.servus.SettingsBottomSheetFragment.PICK_IMAGE;
import static de.ur.servus.utils.UserAccountKeys.ACCOUNT_EXISTS;
import static de.ur.servus.utils.UserAccountKeys.ACCOUNT_ITEM_ID;

import android.Manifest;
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
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.maps.android.clustering.Cluster;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import de.ur.servus.core.Attendant;
import de.ur.servus.core.BackendHandler;
import de.ur.servus.core.Event;
import de.ur.servus.core.EventListener;
import de.ur.servus.core.ListenerRegistration;
import de.ur.servus.core.UserProfile;
import de.ur.servus.core.firebase.EventUpdateData;
import de.ur.servus.core.firebase.FirestoreBackendHandler;
import de.ur.servus.eventcreationbottomsheet.EventCreationBottomSheetFragment;
import de.ur.servus.eventcreationbottomsheet.EventCreationData;
import de.ur.servus.utils.AvatarEditor;
import de.ur.servus.utils.EventHelpers;
import de.ur.servus.utils.UserAccountHelpers;


public class MainActivity extends FragmentActivity implements OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback, ClusterManagerContext {

    private static final String TUTORIAL_PREFS_ITEM = "tutorialSeen";

    private final BackendHandler backendHandler = FirestoreBackendHandler.getInstance();
    private EventHelpers eventHelpers;
    private UserAccountHelpers userAccountHelpers;
    private EventList eventList;

    @Nullable
    private String viewedEventId = null;
    Context context;
    SharedPreferences sharedPreferences;
    CustomLocationManager customLocationManager;

    @Nullable
    private Marker userMarker;

    @Nullable
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
    LinearLayout userstate_container;
    TextView userstate;


    /**
     * App state handling
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = getApplicationContext();
        sharedPreferences = this.getPreferences(Context.MODE_PRIVATE);
        customLocationManager = new CustomLocationManager(this);
        avatarEditor = new AvatarEditor(this);
        eventHelpers = new EventHelpers(this);
        eventList = new EventList();
        userAccountHelpers = new UserAccountHelpers(this);

        userAccountHelpers.saveNewUserIdIfNotExisting();

        userstate_container = findViewById(R.id.userstate_container);
        userstate = findViewById(R.id.userstate);

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
            if (onlyAllowIfAccountExists()) {
                var userId = userAccountHelpers.readStringValue(ACCOUNT_ITEM_ID, "");
                eventList.ifUserIsAttendingEvents(userId,
                        attendedEvents -> {
                            setViewedEvent(attendedEvents.get(0).getId());
                            showBottomSheet(detailsBottomSheetFragment);
                        },
                        () -> {
                            unsubscribeEvent();
                            eventCreationBottomSheetFragment.update(null, this::onEventCreationCreateClicked, this::onEventCreationEditClicked);
                            showBottomSheet(eventCreationBottomSheetFragment);
                        });
            }
        });

        btn_filter = findViewById(R.id.btn_filter);
        btn_filter.setOnClickListener(v -> showBottomSheet(filterBottomSheetFragment));

        settingsBottomSheetFragment.update(this::onUserProfileSaved);

        var userId = userAccountHelpers.readStringValue(ACCOUNT_ITEM_ID, "");
        eventList.ifUserIsAttendingEvents(userId,
                events -> this.setViewedEvent(events.get(0).getId()),
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

        customLocationManager.removeLocationListener(this::moveOwnLocationMarker);
    }

    @Override
    protected void onResume() {
        super.onResume();

        customLocationManager.startListeningForLocationUpdates();
        customLocationManager.startListeningProviderDisabled();
        customLocationManager.showEnableGpsDialogIfNecessary();

        customLocationManager.addLocationListener(this::moveOwnLocationMarker);

        subscribeAllEvents();
    }

    //Moves the user location marker to current position
    private void moveOwnLocationMarker(LatLng position){
        LatLng userLocation = new LatLng(position.latitude, position.longitude);
        if(userMarker != null) {
            userMarker.setPosition(userLocation);
        }
    }

    //Adds the user marker on the user's initial position
    private void setInitialPositionMarker(){
        customLocationManager.getLastObservedLocation(latLng -> latLng.ifPresent(lng -> {
            if (mMap != null) {
                userMarker = mMap.addMarker(new MarkerOptions()
                        .position(lng)
                        .icon(customMarkerRenderer.bitmapFromVector(getApplicationContext(), R.drawable.user_icon_marker)));
            }
        }));
    }

    /**
     * Permission functionality
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
                    Toast toast = Toast.makeText(context, getResources().getString(R.string.toast_storage_permission_error), Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
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
     * Bottom sheet functionality
     */

    private void showBottomSheet(@Nullable BottomSheetDialogFragment bottomSheet) {
        if (bottomSheet != null) {
            bottomSheet.show(getSupportFragmentManager(), bottomSheet.getTag());
        }
    }

    private void onDetailsAttendWithdrawClicked(Event event, boolean attending, boolean isCreator) {
        if (context == null || !onlyAllowIfAccountExists()) {
            return;
        }

        if (attending && isCreator) {
            backendHandler.deleteEvent(event.getId())
                    .addOnSuccessListener(runnable -> leaveEvent(event.getId()));
            detailsBottomSheetFragment.dismiss();
        } else if (attending) {
            leaveEvent(event.getId());
        } else {
            attendEvent(event.getId(), false);
        }
    }

    private void onEventCreationCreateClicked(EventCreationData inputEventData) {
        if(mMap != null){
            centerCamera(mMap);
        }
        eventHelpers.createEvent(customLocationManager, inputEventData, new EventListener<>() {
            @Override
            public void onEvent(String id) {
                attendEvent(id, true);
            }

            @Override
            public void onError(Exception e) {
                Log.e("EventCreation", e.getMessage());
            }
        });

        //close bottomsheet
        if (eventCreationBottomSheetFragment != null) {
            eventCreationBottomSheetFragment.dismiss();
        }
    }


    private void onEventCreationEditClicked(Event event, EventCreationData inputEventData) {
        var eventUpdate = new EventUpdateData(inputEventData.name, inputEventData.description, inputEventData.genre, inputEventData.maxAttendees);
        backendHandler.updateEvent(event.getId(), eventUpdate.toUpdateMap(), null);

        //close bottomsheet
        if (eventCreationBottomSheetFragment != null) {
            eventCreationBottomSheetFragment.dismiss();
        }
    }

    private void onDetailsEditEventClicked(Event event) {
        if (onlyAllowIfAccountExists()) {
            detailsBottomSheetFragment.dismiss();
            showBottomSheet(eventCreationBottomSheetFragment);
        }
    }

    private void onDetailsRemoveUserClicked(Event event, UserProfile user){
        backendHandler.removeEventAttendantById(event.getId(), user.getUserID());
    }

    private void onUserProfileSaved(UserProfile userProfile) {
        btn_settings.setImageBitmap(avatarEditor.loadProfilePicture());
    }

    private void onRequireAccount() {
        showBottomSheet(settingsBottomSheetFragment);
    }


    /**
     * Map features
     */

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        int currentNightMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;

        mMap.setMapStyle(getMapStyle(currentNightMode));

        // This can fail on first run, because permission is not granted. (onRequestPermissionsResult handles this case)
        centerCamera(mMap);

        markerManager = new MarkerManager(this, googleMap);
        markerManager.setClusterAlgorithm();
        customMarkerRenderer = new CustomMarkerRenderer(this, mMap, markerManager.getClusterManager(), eventList);
        setInitialPositionMarker();
    }

    void animateZoomInCamera(LatLng latLng) {
        if (mMap != null) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, mMap.getCameraPosition().zoom));
        }
    }

    private void centerCamera(@NonNull GoogleMap mMap) {
        final float ZOOM_FACTOR = 13.0f;

        customLocationManager.getLastObservedLocation(latLng -> latLng.ifPresent(lng -> mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lng, ZOOM_FACTOR))));
    }

    private MapStyleOptions getMapStyle(int currentNightMode) {
        try {
            if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
                return new MapStyleOptions(getResources().getString(R.string.map_dark_mode));
            }
            return new MapStyleOptions(getResources().getString(R.string.map_light_mode));
        } catch (Resources.NotFoundException e) {
            return new MapStyleOptions(getResources().getString(R.string.map_light_mode));
        }
    }

    @Override
    public boolean onClusterClick(Cluster cluster) {
        animateZoomInCamera(cluster.getPosition());
        return true;
    }

    @Override
    public boolean onClusterItemClick(MarkerClusterItem markerClusterItem) {
        animateZoomInCamera(markerClusterItem.getPosition());
        var eventId = markerClusterItem.getEvent().getId();
        setViewedEvent(eventId);
        showBottomSheet(detailsBottomSheetFragment);
        return true;
    }


    /**
     * Map-Event features
     */

    public void subscribeAllEvents() {
        if (allEventsListenerRegistration != null) {
            allEventsListenerRegistration.unsubscribe();
        }

        this.allEventsListenerRegistration = backendHandler.subscribeEvents(new EventListener<>() {
            @Override
            public void onEvent(List<Event> events) {
                // Log all event names to console
                Log.d("LoadAllEvents", events.stream().map(event -> event.getName() + ": " + event.getId()).collect(Collectors.joining(", ")));
                eventList.setEvents(events);

                if (markerManager != null) {
                    var clusterManager = markerManager.getClusterManager();
                    clusterManager.clearItems();

                    // create markers
                    events.forEach(event -> {
                        MarkerClusterItem marker = new MarkerClusterItem(event);
                        clusterManager.addItem(marker);
                    });

                    handleCurrentlyViewedEvent();
                    redrawClusters();
                }

                // style bottom button
                var userId = userAccountHelpers.readStringValue(ACCOUNT_ITEM_ID, "");
                eventList.ifUserIsAttendingEvents(userId,
                        attendedEvents -> {
                            var event = attendedEvents.stream().filter(e -> Objects.equals(e.getId(), attendedEvents.get(0).getId())).findFirst();
                            if (event.isPresent()) {
                                var ownUserId = userAccountHelpers.readStringValue(ACCOUNT_ITEM_ID, "");
                                var isCreator = event.get().isUserOwner(ownUserId);
                                setBottomButtonStyle(isCreator, true);
                            }
                        },
                        () -> setBottomButtonStyle(false, false)
                );
            }

            @Override
            public void onError(Exception e) {
                Log.e("LoadAllEvents", e.getMessage());
            }

        });
    }

    /**
     * Set which event is currently being viewed.
     * @param eventId The events id.
     */
    public void setViewedEvent(String eventId) {
        this.viewedEventId = eventId;
        handleCurrentlyViewedEvent();
    }

    private void handleCurrentlyViewedEvent() {
        var viewedEventOptional = eventList.getEvents().stream().filter(event -> Objects.equals(event.getId(), viewedEventId)).findFirst();

        if (viewedEventOptional.isPresent()) {
            var event = viewedEventOptional.get();
            var userId = userAccountHelpers.readStringValue(ACCOUNT_ITEM_ID, "");

            var attending = event.isUserAttending(userId);
            var subscribedToAnyEvent = eventList.isUserAttendingAnyEvent(userId);
            boolean isOwner = event.isUserOwner(userAccountHelpers.readStringValue(ACCOUNT_ITEM_ID, null));

            // update details sheet
            if (detailsBottomSheetFragment != null) {
                detailsBottomSheetFragment.update(
                        event,
                        attending,
                        subscribedToAnyEvent,
                        isOwner,
                        this::onDetailsAttendWithdrawClicked,
                        this::onDetailsEditEventClicked,
                        this::onDetailsRemoveUserClicked
                );
            }

            // update edit view
            if (eventCreationBottomSheetFragment != null) {
                eventCreationBottomSheetFragment.update(
                        event,
                        this::onEventCreationCreateClicked,
                        this::onEventCreationEditClicked
                );
            }
        } else {
            detailsBottomSheetFragment.dismiss();
        }
    }

    private void unsubscribeEvent() {
        this.viewedEventId = null;
    }

    private void attendEvent(String eventId, boolean isCreator) {
        var localProfile = userAccountHelpers.getOwnProfile(avatarEditor);

        if (localProfile.getUserID() != null) {

            var attendant = new Attendant(localProfile.getUserID(), isCreator, localProfile.getUserName(), localProfile.getUserGender(), localProfile.getUserBirthdate(), localProfile.getUserCourse(), "tbd");
            backendHandler.addEventAttendant(eventId, attendant,localProfile.getUserPicture())
                    .addOnSuccessListener(unused -> redrawClusters());
        } else {
            Log.e("eventAttend", "No own user id found.");
        }
    }

    private void leaveEvent(String eventId) {
        var userId = userAccountHelpers.readStringValue(ACCOUNT_ITEM_ID, null);
        if (userId != null) {
            backendHandler.removeEventAttendantById(eventId, userId)
                    .addOnSuccessListener(unused -> redrawClusters());

            if (eventCreationBottomSheetFragment != null) {
                eventCreationBottomSheetFragment.update(null,
                        this::onEventCreationCreateClicked,
                        this::onEventCreationEditClicked
                );
            }
        } else {
            Log.e("eventAttend", "No own user if found.");
        }
    }

    private void redrawClusters() {
        if (markerManager != null) {
            markerManager.getClusterManager().cluster();
        } else {
            Log.e("DrawClusters", "Marker Manager was null.");
        }
    }


    /**
     * App Styling
     */

    private void setBottomButtonStyle(boolean isCreator, boolean isAttending) {
        if (btn_creator == null) {
            return;
        }

        if (isCreator) {
            btn_creator.setText(R.string.main_bottom_button_view_own_event);
            btn_creator.setBackgroundResource(R.drawable.style_btn_roundedcorners_clicked);
            btn_creator.setTextColor(getResources().getColor(R.color.servus_pink, getTheme()));

            userstate_container.setVisibility(View.VISIBLE);
            userstate.setText(getResources().getString(R.string.userstate_creator));
        } else if (isAttending) {
            btn_creator.setText(R.string.main_bottom_button_view_event);
            btn_creator.setBackgroundResource(R.drawable.style_btn_roundedcorners_clicked);
            btn_creator.setTextColor(getResources().getColor(R.color.servus_pink, getTheme()));

            userstate_container.setVisibility(View.VISIBLE);
            userstate.setText(getResources().getString(R.string.userstate_attendee));
        } else {
            btn_creator.setText(R.string.content_create_meetup);
            btn_creator.setBackgroundResource(R.drawable.style_btn_roundedcorners);
            btn_creator.setTextColor(getResources().getColor(R.color.servus_white, getTheme()));

            userstate_container.setVisibility(View.GONE);
        }
    }


    /**
     * All other functionalities
     */

    private boolean onlyAllowIfAccountExists() {
        boolean accountExists = this.userAccountHelpers.readBooleanValue(ACCOUNT_EXISTS, false);

        if (accountExists) {
            return true;
        } else {
            Toast toast = Toast.makeText(this, getResources().getString(R.string.toast_require_local_account), Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();

            showBottomSheet(settingsBottomSheetFragment);
            return false;
        }
    }
}
