package de.ur.servus.utils;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import java.util.Objects;

import de.ur.servus.R;
import de.ur.servus.SettingsBottomSheetFragment;

public class TutorialManager extends PagerAdapter {

    private final Activity activity;
    private final Context context;
    SettingsBottomSheetFragment settingsBottomSheetFragment = new SettingsBottomSheetFragment();

    public TutorialManager(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;
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

        return Objects.requireNonNull(view);
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return super.getItemPosition(object);
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
}
