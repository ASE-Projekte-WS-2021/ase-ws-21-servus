package de.ur.servus;

import static android.content.Context.MODE_PRIVATE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.Calendar;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import de.ur.servus.core.UserProfile;
import de.ur.servus.utils.AvatarEditor;

public class SettingsBottomSheetFragment extends BottomSheetDialogFragment implements ActivityCompat.OnRequestPermissionsResultCallback {

    @Nullable
    private View view;

    @Nullable
    private Activity activity;
    @Nullable
    private Context context;
    @Nullable
    private AvatarEditor avatarEditor;

    @Nullable
    BottomSheetBehavior<View> behavior;

    @Nullable
    private LinearLayout ll_select_picture;
    @Nullable
    private ShapeableImageView settingsProfilePicture;
    @Nullable
    private EditText name;
    @Nullable
    private RadioGroup gender;
    @Nullable
    private RadioButton genderSelection;
    @Nullable
    private DatePicker birthday;
    @Nullable
    private EditText course;
    @Nullable
    private Button btn_preview;

    @Nullable
    private Bitmap selectedImage;

    @Nullable
    private Consumer<UserProfile> onProfileSavedListener;

    @Nullable
    private SharedPreferences sharedPreferences;

    public static final String ACCOUNT = "userAccount";
    public static final String ACCOUNT_ITEM_NAME = "Username";
    public static final String ACCOUNT_ITEM_AGE = "Birthday";
    public static final String ACCOUNT_ITEM_GENDER = "Gender";
    public static final String ACCOUNT_ITEM_COURSE = "CourseOfStudy";
    public static final String ACCOUNT_ITEM_ID = "UserId";

    public static final int PICK_IMAGE = 3;

    public SettingsBottomSheetFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.mBottomSheetFragments);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.bottomsheet_settings, container, false);

        context = getContext();
        activity = (Activity) context;

        if (activity != null) {
            avatarEditor = new AvatarEditor(activity);
            if (context != null) {
                sharedPreferences = activity.getSharedPreferences(ACCOUNT, MODE_PRIVATE);
            }

            if (view != null) {
                name = view.findViewById(R.id.settings_profile_name);
                gender = view.findViewById(R.id.settings_gender_selection);
                birthday = view.findViewById(R.id.settings_age);
                course = view.findViewById(R.id.settings_study_course);
                ll_select_picture = view.findViewById(R.id.settings_profile_picture_container);
                settingsProfilePicture = view.findViewById(R.id.settings_profile_picture);
                btn_preview = view.findViewById(R.id.settings_btn_vita);
            }

            tryUpdateView();
        }

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        if (view != null) {
            View root = (View) view.getParent();
            behavior = BottomSheetBehavior.from(root);

            DisplayMetrics displayMetrics = new DisplayMetrics();
            if (activity != null) {
                activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            }
            int maxHeight = (int) (displayMetrics.heightPixels - TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,128, displayMetrics));

            behavior.setSkipCollapsed(true);
            behavior.setMaxHeight(maxHeight);
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
    }

    public void update(Consumer<UserProfile> onProfileSavedListener) {
        this.onProfileSavedListener = onProfileSavedListener;
        tryUpdateView();
    }

    /*
     * This only work from or after OnViewCreate and after an initial update call
     */
    @SuppressLint("SetTextI18n")
    private void tryUpdateView() {

        if (ll_select_picture == null ||
                activity == null ||
                context == null ||
                btn_preview == null ||
                sharedPreferences == null ||
                avatarEditor == null ||
                birthday == null){
            return;
        }

        // set content
        Calendar calendar = Calendar.getInstance();
        int cYear = calendar.get(Calendar.YEAR);
        int cMonth = calendar.get(Calendar.MONTH);
        int cDay = calendar.get(Calendar.DAY_OF_MONTH);

        //Init to TODAY 18 years ago - uncommon to have students below 18y' old
        //  --> Will be always set to saved date of birth, if any is set
        birthday.init(cYear - 18, cMonth, cDay, (view, year, monthOfYear, dayOfMonth) -> {});
        calendar.set(cYear, cMonth, cDay);
        birthday.setMaxDate(calendar.getTimeInMillis());

        loadAccountDetails();

        // set listeners
        ll_select_picture.setOnClickListener(v -> checkAndAskStoragePermission());

        btn_preview.setOnClickListener(v -> {
            saveInputs();
            this.dismiss();

            UserProfile ownProfile = new UserProfile(
                    sharedPreferences.getString(ACCOUNT_ITEM_NAME, "DEF_NAME"),
                    sharedPreferences.getString(ACCOUNT_ITEM_GENDER, "DEF_GENDER"),
                    sharedPreferences.getString(ACCOUNT_ITEM_AGE, "DEF_BIRTHDATE"),
                    sharedPreferences.getString(ACCOUNT_ITEM_COURSE, "DEF_COURSE"),
                    avatarEditor.loadProfilePicture()
            );

            FragmentTransaction transaction;

            transaction = getParentFragmentManager().beginTransaction();
            ProfileCardFragment servusCard = ProfileCardFragment.newInstance(ownProfile);
            transaction.add(R.id.root_layout, servusCard, servusCard.getTag());
            transaction.addToBackStack(null);
            transaction.commit();

            if (onProfileSavedListener != null) {
                onProfileSavedListener.accept(ownProfile);
            }
        });

        // style views
        if (activity != null && activity.getPreferences(MODE_PRIVATE).getBoolean(TutorialActivity.TUTORIAL_SHOWING, false)) {
            if (view != null) {
                btn_preview.setVisibility(View.GONE);
                view.findViewById(R.id.settings_tutorial_btn_next).setVisibility(View.VISIBLE);

                view.findViewById(R.id.settings_tutorial_btn_next).setOnClickListener(v -> {
                    TutorialActivity.viewPager.setCurrentItem(3);
                    saveInputs();
                    this.dismiss();
                });
            }
        }
    }



    /**
     *
     *
     *      Permissions functionality
     *
     *
     *
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
                    Toast.makeText(context, getResources().getString(R.string.toast_storage_permission_error), Toast.LENGTH_LONG).show();
                }
            });

    private void checkAndAskStoragePermission(){
        if (context != null) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // You can use the API that requires the permission.
                Intent getIntent = new Intent(Intent.ACTION_PICK);
                getIntent.setType("image/*");
                startActivityForResult(Intent.createChooser(getIntent, getResources().getString(R.string.settings_profile_picture_picker)), PICK_IMAGE);
            } else {
                // You can directly ask for the permission. The registered ActivityResultCallback gets the result of this request.
                requestStoragePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @androidx.annotation.Nullable Intent data) {
        if ((data != null) && requestCode == PICK_IMAGE) {
            if (avatarEditor != null) {
                selectedImage = avatarEditor.fetchImageFromStorage(data.getData());
            }
            if (settingsProfilePicture != null) {
                settingsProfilePicture.setImageBitmap(selectedImage);
            }
        }
    }



    /**
     *
     *
     *      Profile input functionality
     *
     *
     *
     */

    public void saveInputs() {
        if (sharedPreferences == null || avatarEditor == null) return;

        SharedPreferences.Editor editor = sharedPreferences.edit();

        if (name != null) {
            editor.putString(ACCOUNT_ITEM_NAME, String.valueOf(name.getText()));
        }

        if (gender != null) {
            int selectedId = gender.getCheckedRadioButtonId();
            genderSelection = gender.findViewById(selectedId);
            if (genderSelection != null)
                editor.putString(ACCOUNT_ITEM_GENDER, String.valueOf(genderSelection.getText()));
        }

        if (birthday != null) {
            int day = birthday.getDayOfMonth();
            int month = birthday.getMonth() + 1;
            int year = birthday.getYear();
            String birthdate = day + "." + month + "." + year;

            editor.putString(ACCOUNT_ITEM_AGE, birthdate);
        }

        if (course != null) {
            editor.putString(ACCOUNT_ITEM_COURSE, String.valueOf(course.getText()));
        }

        if (selectedImage != null) {
            avatarEditor.saveProfilePicture(selectedImage);
        }

        editor.apply();
    }

    private void loadAccountDetails() {
        if (sharedPreferences == null ||
                name == null ||
                gender == null ||
                birthday == null ||
                course == null ||
                settingsProfilePicture == null ||
                avatarEditor == null){
            return;
        }

        if (sharedPreferences.contains(ACCOUNT_ITEM_NAME)) {
            name.setText(sharedPreferences.getString(ACCOUNT_ITEM_NAME, getResources().getString(R.string.settings_profile_name)));
        }
        if (sharedPreferences.contains(ACCOUNT_ITEM_GENDER)) {
            String genderCase = sharedPreferences.getString(ACCOUNT_ITEM_GENDER, getResources().getString(R.string.settings_gender));
            if (genderCase.equals(getResources().getString(R.string.settings_gender_male)))
                genderSelection = gender.findViewById(R.id.settings_gender_male);
            else if (genderCase.equals(getResources().getString(R.string.settings_gender_female)))
                genderSelection = gender.findViewById(R.id.settings_gender_female);
            else genderSelection = gender.findViewById(R.id.settings_gender_divers);

            if (genderSelection != null) genderSelection.setChecked(true);
        }
        if (sharedPreferences.contains(ACCOUNT_ITEM_AGE)) {
            String savedBirthdate = sharedPreferences.getString(ACCOUNT_ITEM_AGE, getResources().getString(R.string.settings_age));
            String[] separatedDate = savedBirthdate.split("\\.");
            int bdDay = Integer.parseInt(separatedDate[0]);
            int bdMonth = Integer.parseInt(separatedDate[1]) - 1;
            int bdYear = Integer.parseInt(separatedDate[2]);

            birthday.init(bdYear, bdMonth, bdDay, (view, year, monthOfYear, dayOfMonth) -> {});
        }
        if (sharedPreferences.contains(ACCOUNT_ITEM_COURSE)) {
            course.setText(sharedPreferences.getString(ACCOUNT_ITEM_COURSE, getResources().getString(R.string.settings_study_course)));
        }

        settingsProfilePicture.setImageBitmap(avatarEditor.loadProfilePicture());
    }
}
