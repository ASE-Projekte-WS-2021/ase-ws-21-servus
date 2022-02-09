package de.ur.servus;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

public class TutorialActivity extends AppCompatActivity {

    public static ViewPager viewPager;
    TutorialManager tutorialManager;

    View t_bottomSheet;
    public static BottomSheetBehavior<View> t_bottomSheetBehavior;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);

        viewPager = findViewById(R.id.tutorial_viewpager);
        tutorialManager = new TutorialManager(this);

        viewPager.setAdapter(tutorialManager);

        t_bottomSheet = findViewById(R.id.tutorial_bottomSheet);
        t_bottomSheetBehavior = BottomSheetBehavior.from(t_bottomSheet);

        Button tutorial_finalize_account = findViewById(R.id.tutorial_finalize_account_creation);
        tutorial_finalize_account.setOnClickListener(v -> {
            t_bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            viewPager.setCurrentItem(3);
        });
    }
}