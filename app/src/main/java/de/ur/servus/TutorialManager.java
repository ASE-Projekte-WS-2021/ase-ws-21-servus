package de.ur.servus;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.util.Objects;

public class TutorialManager extends PagerAdapter {

    Context context;

    public TutorialManager(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return 4;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = null;

        switch (position){
            case 0:
                view = layoutInflater.inflate(context.getResources().getLayout(R.layout.tutorial_servus), container, false);
                break;

            case 1:
                view = layoutInflater.inflate(context.getResources().getLayout(R.layout.tutorial_meetups), container, false);
                break;

            case 2:
                view = layoutInflater.inflate(context.getResources().getLayout(R.layout.tutorial_accounts), container, false);
                break;

            case 3:
                view = layoutInflater.inflate(context.getResources().getLayout(R.layout.tutorial_locations), container, false);
                break;

            default:
                break;

        }
        container.addView(view);

        Button tutorial_btn_prev = Objects.requireNonNull(view).findViewById(R.id.tutorial_btn_prev);
        tutorial_btn_prev.setOnClickListener(v -> {
            if (position == 0){
                Intent intent = new Intent(context, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            } else {
                TutorialActivity.viewPager.setCurrentItem(position - 1);
            }
        });

        Button tutorial_btn_next = Objects.requireNonNull(view).findViewById(R.id.tutorial_btn_next);
        tutorial_btn_next.setOnClickListener(v -> {
            if(position == 2) {
                TutorialActivity.t_bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            } else if (position == 3) {
                // TODO: Ask for permission here?

                Intent intent = new Intent(context, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            } else {
                TutorialActivity.viewPager.setCurrentItem(position + 1);
            }
        });

        view.findViewById(R.id.tutorial_indicator_1).setOnClickListener(v -> TutorialActivity.viewPager.setCurrentItem(0));
        view.findViewById(R.id.tutorial_indicator_2).setOnClickListener(v -> TutorialActivity.viewPager.setCurrentItem(1));
        view.findViewById(R.id.tutorial_indicator_3).setOnClickListener(v -> TutorialActivity.viewPager.setCurrentItem(2));
        view.findViewById(R.id.tutorial_indicator_4).setOnClickListener(v -> TutorialActivity.viewPager.setCurrentItem(3));

        return Objects.requireNonNull(view);
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
}
