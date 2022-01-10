package de.ur.servus;

import android.graphics.Typeface;
import android.os.Bundle;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import de.ur.servus.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);





        View c_bottomSheet = findViewById(R.id.creator_bottomSheet);
        View p_bottomSheet = findViewById(R.id.participant_bottomSheet);
        View s_bottomSheet = findViewById(R.id.settings_bottomSheet);
        BottomSheetBehavior c_bottomSheetBehavior = BottomSheetBehavior.from(c_bottomSheet);
        BottomSheetBehavior p_bottomSheetBehavior = BottomSheetBehavior.from(p_bottomSheet);
        BottomSheetBehavior s_bottomSheetBehavior = BottomSheetBehavior.from(s_bottomSheet);
        addBottomSheetCallbacks(c_bottomSheetBehavior, p_bottomSheetBehavior, s_bottomSheetBehavior);

        Button tmp_Button4BS_1 = findViewById(R.id.tmp_btn_1);
        tmp_Button4BS_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                s_bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });
        Button tmp_Button4BS_2 = findViewById(R.id.tmp_btn_2);
        tmp_Button4BS_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                c_bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });
        Button tmp_Button4BS_3 = findViewById(R.id.tmp_btn_3);
        tmp_Button4BS_3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                p_bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });






        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action BLAH2", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    private void addBottomSheetCallbacks(BottomSheetBehavior c, BottomSheetBehavior p, BottomSheetBehavior s) {
        c.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        //TODO
                        break;

                    case BottomSheetBehavior.STATE_EXPANDED:
                        //TODO
                        break;

                    case BottomSheetBehavior.STATE_DRAGGING:
                        //TODO
                        break;

                    case BottomSheetBehavior.STATE_HIDDEN:
                        //TODO
                        break;

                    case BottomSheetBehavior.STATE_SETTLING:
                        //TODO
                        break;

                    case BottomSheetBehavior.STATE_HALF_EXPANDED:
                        //TODO
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                //TODO
                // potentially empty (?)
            }
        });

        p.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        //TODO
                        break;

                    case BottomSheetBehavior.STATE_EXPANDED:
                        //TODO
                        break;

                    case BottomSheetBehavior.STATE_DRAGGING:
                        //TODO
                        break;

                    case BottomSheetBehavior.STATE_HIDDEN:
                        //TODO
                        break;

                    case BottomSheetBehavior.STATE_SETTLING:
                        //TODO
                        break;

                    case BottomSheetBehavior.STATE_HALF_EXPANDED:
                        //TODO
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                //TODO
                // potentially empty (?)
            }
        });

        s.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        //TODO
                        break;

                    case BottomSheetBehavior.STATE_EXPANDED:
                        //TODO
                        break;

                    case BottomSheetBehavior.STATE_DRAGGING:
                        //TODO
                        break;

                    case BottomSheetBehavior.STATE_HIDDEN:
                        //TODO
                        break;

                    case BottomSheetBehavior.STATE_SETTLING:
                        //TODO
                        break;

                    case BottomSheetBehavior.STATE_HALF_EXPANDED:
                        //TODO
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                //TODO
                // potentially empty (?)
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
}