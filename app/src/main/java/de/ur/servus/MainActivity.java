package de.ur.servus;

import static de.ur.servus.CustomLocationManager.REQUEST_LOCATION_PERMISSION;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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

    private static final String SUBSCRIBED_TO_EVENT = "subscribedToEvent";
    private static final String TUTORIAL_PREFS_ITEM = "tutorialSeen";

    Context context;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    CustomLocationManager customLocationManager;

    @Nullable
    private GoogleMap mMap;
    private ListenerRegistration listenerRegistration;

    MarkerManager markerManager;

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

    Button btn_attend_withdraw;
    Button btn_create_event;

    BroadcastReceiver networkReceiver;
    IntentFilter networkFilter;
    AlertDialog.Builder dialogBuilder;
    AlertDialog locationDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = getApplicationContext();
        sharedPreferences = this.getPreferences(Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        markerManager = new MarkerManager();
        customLocationManager = new CustomLocationManager(this);

        // when GPS is turned off, ask to turn it on. Starting to listen needs to be done in onCreate
        customLocationManager.addOnProviderDisabledListener(customLocationManager::showEnableGpsDialogIfNecessary);

        if (!sharedPreferences.getBoolean(TUTORIAL_PREFS_ITEM, false)) {
            editor.putBoolean(TUTORIAL_PREFS_ITEM, true).apply();

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

        /*
         * Initialize all TextViews that will get manipulated by value within the Bottom Sheets
         */
        details_eventname = findViewById(R.id.event_details_eventname);
        details_description = findViewById(R.id.event_details_description);
        details_attendees = findViewById(R.id.event_details_attendees);
        //details_creator = findViewById(R.id.event_details_creator); //TODO: Not part of MVP


        /*
         * Initialize Creator button based on an event subscription
         */
        String subscribed = sharedPreferences.getString(SUBSCRIBED_TO_EVENT, "none");
        if (!subscribed.equals("none")) {
            loadDataForEvent(subscribed);
        }

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

        BackendHandler bh = FirestoreBackendHandler.getInstance();
        this.listenerRegistration = bh.subscribeEvents(new EventListener<>() {
            @Override
            public void onEvent(List<Event> events) {
                // Log all event names to console
                Log.d("Data", events.stream().map(event -> event.getName() + ": " + event.getId()).collect(Collectors.joining(", ")));

                if (mMap == null) {
                    return;
                }

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

        if (mMap != null) {
            centerCamera(mMap);
        }
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
            loadDataForEvent(Objects.requireNonNull(marker.getTag()).toString());
            p_bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
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

                    if (subscribed.equals("none")) {
                        attendEvent(event.getId());
                    } else {
                        leaveEvent(event.getId());
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

        customLocationManager.stopListeningForLocationUpdates();
        customLocationManager.stopListeningProviderDisabled();

    }

    private void attendEvent(String eventId) {
        setStyleClicked();
        editor.putString(SUBSCRIBED_TO_EVENT, eventId);
        editor.apply();
        markerManager.showSingleMarker(eventId);
        FirestoreBackendHandler.getInstance().incrementEventAttendants(eventId, null);
    }

    private void leaveEvent(String eventId) {
        setStyleDefault();
        editor.putString(SUBSCRIBED_TO_EVENT, "none");
        editor.apply();
        markerManager.showAllMarkers();
        FirestoreBackendHandler.getInstance().decrementEventAttendants(eventId, null);
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
        int attendants = 0;

        customLocationManager.getLastObservedLocation(latLng -> {
            if (!latLng.isPresent()) {
                // TODO got no location. What should we do? Check before and disable create button?
                Log.e("EventCreation", "Got no location. Not creating event.");
                return;
            }

            Event event = new Event(event_name, event_description, latLng.get(), attendants);
            var bh = FirestoreBackendHandler.getInstance();

            bh.createNewEvent(event, new EventListener<>() {
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
        });

    }

    private void changeMapStyle(int currentNightMode) {
        if (mMap == null) {
            Log.e("mapStyle", "Map was not initialized.");
            return;
        }

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
