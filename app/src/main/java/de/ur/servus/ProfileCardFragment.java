package de.ur.servus;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import de.ur.servus.core.UserProfile;
import de.ur.servus.utils.RoundishImageView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileCardFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileCardFragment extends DialogFragment implements View.OnClickListener {

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

    private View profileCardBackground;

    public ProfileCardFragment() {
        // Required empty public constructor
    }

    public static ProfileCardFragment newInstance(UserProfile user) {
        ProfileCardFragment fragment = new ProfileCardFragment();

        Bundle args = new Bundle();
        args.putString(PROFILE_NAME, user.getName());
        args.putString(PROFILE_GENDER, user.getGender());
        args.putString(PROFILE_BIRTHDATE, user.getBirthdate());
        args.putString(PROFILE_COURSE, user.getCourse());
        args.putParcelable(PROFILE_PICTURE, user.getPicture());
        fragment.setArguments(args);

        return fragment;
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

        profileCardBackground = inflatedView.findViewById(R.id.profilecard_background);

        TextView tv_name = inflatedView.findViewById(R.id.profilecard_name);
        ImageView iv_gender = inflatedView.findViewById(R.id.profilecard_gender_indicator);
        TextView tv_date = inflatedView.findViewById(R.id.profilecard_birthdate);
        TextView tv_course = inflatedView.findViewById(R.id.profilecard_course);
        RoundishImageView iv_picture = inflatedView.findViewById(R.id.profilecard_picture);

        tv_name.setText(pName);
        tv_date.setText(pBirthdate);
        iv_gender.setImageDrawable(getGenderDrawable(pGender));
        tv_course.setText(pCourse);
        iv_picture.setImageBitmap(pPicture);

        return inflatedView;
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private Drawable getGenderDrawable(String gender) {
        if (gender.equals(getResources().getString(R.string.settings_gender_male))) {
            Log.i("ProfileCard: ", "m");
            return getResources().getDrawable(R.drawable.ic_gender_male_checked);
        }
        else if (gender.equals(getResources().getString(R.string.settings_gender_female))){
            Log.i("ProfileCard: ", "f");
            return getResources().getDrawable(R.drawable.ic_gender_female_checked);
        }
        else {
            Log.i("ProfileCard: ", "d");
            return getResources().getDrawable(R.drawable.ic_gender_divers_checked);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        /*
        if (pName != null) Log.i("ProfileCard: ", pName);
        if (pBirthdate != null) Log.i("ProfileCard: ", pBirthdate);
        if (pGender != null) Log.i("ProfileCard: ", pGender);
        if (pCourse != null) Log.i("ProfileCard: ", pCourse);
        if (pPicture != null) Log.i("ProfileCard: ", pPicture.toString());
        */

        profileCardBackground.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() != R.id.profilecard_card) this.dismiss();
    }
}