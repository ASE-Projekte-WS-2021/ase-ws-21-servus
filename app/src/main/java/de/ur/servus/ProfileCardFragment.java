package de.ur.servus;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import de.ur.servus.core.UserProfile;
import de.ur.servus.utils.RoundishImageView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileCardFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileCardFragment extends DialogFragment {

    private static final String PROFILE_NAME = "profileName";
    private static final String PROFILE_GENDER = "profileGender";
    private static final String PROFILE_BIRTHDATE = "profileBirthdate";
    private static final String PROFILE_COURSE = "profileCourse";
    private static final String PROFILE_PICTURE = "profilePicture";

    private String pName;
    private String pGender;
    private String pBirthdate;
    private String pCourse;
    private Bitmap pPicture;

    public ProfileCardFragment() {
        // Required empty public constructor
    }

    public static ProfileCardFragment newInstance(UserProfile user) {
        ProfileCardFragment fragment = new ProfileCardFragment();

        Bundle args = new Bundle();
        args.putString(PROFILE_NAME, user.getUserName());
        args.putString(PROFILE_GENDER, user.getUserGender());
        args.putString(PROFILE_BIRTHDATE, user.getUserBirthdate());
        args.putString(PROFILE_COURSE, user.getUserCourse());
        args.putParcelable(PROFILE_PICTURE, user.getUserPicture());
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onStart() {
        super.onStart();

        Window window = getDialog().getWindow();

        // Set the displayed position where the dialog should be displayed on screen
        window.setGravity(Gravity.TOP);
        WindowManager.LayoutParams params = window.getAttributes();
        params.y = 128;
        window.setAttributes(params);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            pName = getArguments().getString(PROFILE_NAME);
            pGender = getArguments().getString(PROFILE_GENDER);
            pBirthdate = getArguments().getString(PROFILE_BIRTHDATE);
            pCourse = getArguments().getString(PROFILE_COURSE);
            pPicture = getArguments().getParcelable(PROFILE_PICTURE);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View inflatedView = inflater.inflate(R.layout.fragment_profile_card, container, false);
        //inflatedView.bringToFront();

        // Set transparent background and no title
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }

        TextView tv_name = inflatedView.findViewById(R.id.profilecard_name);
        ImageView iv_gender = inflatedView.findViewById(R.id.profilecard_gender_indicator);
        TextView tv_date = inflatedView.findViewById(R.id.profilecard_birthdate);
        TextView tv_course = inflatedView.findViewById(R.id.profilecard_course);
        LinearLayout ll_course = inflatedView.findViewById(R.id.profilecard_course_container);
        RoundishImageView iv_picture = inflatedView.findViewById(R.id.profilecard_picture);

        tv_name.setText(pName);
        tv_date.setText(pBirthdate);
        iv_gender.setImageDrawable(getGenderDrawable(pGender));
        if (pCourse == null || pCourse.equals("")) tv_course.setText(getResources().getString(R.string.profilecard_course_404));
        else tv_course.setText(pCourse);
        iv_picture.setImageBitmap(pPicture);

        return inflatedView;
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private Drawable getGenderDrawable(String gender) {
        if (gender.equals(getResources().getString(R.string.settings_gender_male))) {
            return getResources().getDrawable(R.drawable.ic_gender_male_checked);
        }
        else if (gender.equals(getResources().getString(R.string.settings_gender_female))){
            return getResources().getDrawable(R.drawable.ic_gender_female_checked);
        }
        else {
            return getResources().getDrawable(R.drawable.ic_gender_divers_checked);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void dismiss() {
        if (this.isVisible()) {
            super.dismiss();
        }
    }
}