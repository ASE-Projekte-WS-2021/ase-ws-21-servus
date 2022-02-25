package de.ur.servus;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class TutorialActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener {

    public static ViewPager viewPager;
    TutorialManager tutorialManager;

    private Button btn_prev;
    private Button btn_next;

    private LinearLayout indicators;

    SettingsBottomSheetFragment settingsBottomSheetFragment = new SettingsBottomSheetFragment();

    public static String TUTORIAL_SHOWING = "currentlyShowing";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);

        // Flag to discover if Tutorial is currently showing
        getPreferences(MODE_PRIVATE).edit().putBoolean(TUTORIAL_SHOWING, true).apply();

        viewPager = findViewById(R.id.tutorial_viewpager);
        tutorialManager = new TutorialManager(this, TutorialActivity.this);

        viewPager.setAdapter(tutorialManager);

        // Set content
        btn_prev = findViewById(R.id.tutorial_btn_prev);
        btn_next = findViewById(R.id.tutorial_btn_next);
        indicators = findViewById(R.id.tutorial_indicator_container);

        // Set Listeners
        btn_next.setOnClickListener(v -> {
            switch (viewPager.getCurrentItem()){
                case 2:
                    btn_next.setText(getResources().getString(R.string.tutorial_create_account));
                    showBottomSheet(settingsBottomSheetFragment);
                    break;

                case 3:
                    endOnboarding();
                    break;

                default:
                    viewPager.setCurrentItem(viewPager.getCurrentItem()+1);
                    break;
            }
        });

        btn_prev.setOnClickListener(v -> {
            if (viewPager.getCurrentItem() == 0) endOnboarding();
            else viewPager.setCurrentItem(viewPager.getCurrentItem()-1);
        });

        viewPager.addOnPageChangeListener(this);
        findViewById(R.id.tutorial_indicator_0).setOnClickListener(v -> viewPager.setCurrentItem(0));
        findViewById(R.id.tutorial_indicator_1).setOnClickListener(v -> viewPager.setCurrentItem(1));
        findViewById(R.id.tutorial_indicator_2).setOnClickListener(v -> viewPager.setCurrentItem(2));
        findViewById(R.id.tutorial_indicator_3).setOnClickListener(v -> viewPager.setCurrentItem(3));
    }

    @Override
    protected void onDestroy() {
        getPreferences(MODE_PRIVATE).edit().putBoolean(TUTORIAL_SHOWING, false).apply();
        super.onDestroy();
    }

    private void endOnboarding() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }



    /**
     *
     *
     *      Viewpager functionality
     *
     *
     *
     */

    @Override // Forced to be overwritten; no further functionality necessary
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

    @Override // Allows handling of UI for selected pages
    public void onPageSelected(int position) {

        if (position == 0) {
            btn_prev.setText(getResources().getString(R.string.tutorial_skip));
        } else btn_prev.setText(getResources().getString(R.string.tutorial_prev));

        if (position == 2){
            btn_next.setText(getResources().getString(R.string.tutorial_create_account));
        }

        if (position == 3) {
            btn_next.setText(getResources().getString(R.string.tutorial_finalize));
        }

        updateIndicators(position);
    }

    @Override // Forced to be overwritten; no further functionality necessary
    public void onPageScrollStateChanged(int state) {}

    private void updateIndicators(int position){
        for (int i = 0; i < indicators.getChildCount(); i++) {
            if (indicators.getChildAt(i) instanceof ImageView) ((ImageView) indicators.getChildAt(i)).setImageResource(R.drawable.tutorial_slideindicator_unselected);
        }
        ((ImageView) indicators.getChildAt(position)).setImageResource(R.drawable.tutorial_slideindicator_selected);
    }



    /**
     *
     *
     *      Bottomsheet functionality
     *
     *
     *
     */

    private void showBottomSheet(@Nullable BottomSheetDialogFragment bottomSheet) {
        if (bottomSheet != null) {
            bottomSheet.show(getSupportFragmentManager(), bottomSheet.getTag());
        }
    }
}