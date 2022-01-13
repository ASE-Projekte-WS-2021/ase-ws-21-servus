package de.ur.servus;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.imageview.ShapeableImageView;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
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
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng uniRegensburg = new LatLng(48.996868, 12.095798);
        mMap.addMarker(new MarkerOptions().position(uniRegensburg).title("Marker at Regensburg University"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(uniRegensburg));
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