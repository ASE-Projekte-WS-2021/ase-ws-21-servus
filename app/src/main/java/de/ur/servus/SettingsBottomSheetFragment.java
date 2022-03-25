package de.ur.servus;

import static android.content.Context.MODE_PRIVATE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import de.ur.servus.utils.UserAccountHelpers;
import de.ur.servus.utils.UserAccountKeys;

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
    private UserAccountHelpers userAccountHelpers;

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
            userAccountHelpers = new UserAccountHelpers(activity);

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
            int maxHeight = (int) (displayMetrics.heightPixels - TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 128, displayMetrics));

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
                userAccountHelpers == null ||
                avatarEditor == null ||
                birthday == null) {
            return;
        }

        // set content
        Calendar calendar = Calendar.getInstance();
        int cYear = calendar.get(Calendar.YEAR);
        int cMonth = calendar.get(Calendar.MONTH);
        int cDay = calendar.get(Calendar.DAY_OF_MONTH);

        //Init to TODAY 18 years ago - uncommon to have students below 18y' old
        //  --> Will be always set to saved date of birth, if any is set
        birthday.init(cYear - 18, cMonth, cDay, (view, year, monthOfYear, dayOfMonth) -> {
        });
        calendar.set(cYear, cMonth, cDay);
        birthday.setMaxDate(calendar.getTimeInMillis());

        loadAccountDetails();

        // set listeners
        ll_select_picture.setOnClickListener(v -> checkAndAskStoragePermission());

        btn_preview.setOnClickListener(v -> {
            if (saveInputs()){
                UserProfile ownProfile = userAccountHelpers.getOwnProfile(avatarEditor);
                ProfileCardFragment servusCard = ProfileCardFragment.newInstance(ownProfile);

                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.add(servusCard, servusCard.getTag());
                transaction.addToBackStack(null);
                transaction.commit();

                if (onProfileSavedListener != null) {
                    onProfileSavedListener.accept(ownProfile);
                }

                this.dismiss();
            }
        });

        // style views
        if (activity != null && activity.getPreferences(MODE_PRIVATE).getBoolean(TutorialActivity.TUTORIAL_SHOWING, false)) {
            if (view != null) {
                btn_preview.setVisibility(View.GONE);
                view.findViewById(R.id.settings_tutorial_btn_next).setVisibility(View.VISIBLE);

                view.findViewById(R.id.settings_tutorial_btn_next).setOnClickListener(v -> {
                    if (saveInputs()) {
                        TutorialActivity.viewPager.setCurrentItem(3);
                        this.dismiss();
                    }
                });
            }
        }
    }


    /**
     * Permissions functionality
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

    private void checkAndAskStoragePermission() {
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
     * Profile input functionality
     */

    public boolean saveInputs() {
        if (userAccountHelpers == null || avatarEditor == null) return false;

        boolean[] requiredFieldFilled = {false, false, false};

        if (name != null) {
            if (name.getText() == null || String.valueOf(name.getText()).equals("")) {
                requiredFieldFilled[0] = false; // Necessary for possible secondary changes after initial account creation
            } else {
                userAccountHelpers.saveStringValue(UserAccountKeys.ACCOUNT_ITEM_NAME, String.valueOf(name.getText()));
                requiredFieldFilled[0] = true;
            }
        }

        if (gender != null) {
            int selectedId = gender.getCheckedRadioButtonId();
            genderSelection = gender.findViewById(selectedId);

            if (genderSelection != null) {
                if (genderSelection.getText() != null) {
                    userAccountHelpers.saveStringValue(UserAccountKeys.ACCOUNT_ITEM_GENDER, String.valueOf(genderSelection.getText()));
                    requiredFieldFilled[1] = true;
                }
            }
        }

        if (birthday != null) {
            int day = birthday.getDayOfMonth();
            int month = birthday.getMonth() + 1;
            int year = birthday.getYear();
            String birthdate = day + "." + month + "." + year;

            userAccountHelpers.saveStringValue(UserAccountKeys.ACCOUNT_ITEM_AGE, birthdate);
            requiredFieldFilled[2] = true; // No further if needed -> Always takes currently initialized date as default
        }

        if (course != null) {
            userAccountHelpers.saveStringValue(UserAccountKeys.ACCOUNT_ITEM_COURSE, String.valueOf(course.getText()));
        }

        if (selectedImage != null) {
            avatarEditor.saveProfilePicture(selectedImage);
        }


        // Check requirements before applying to Shared Preferences
        // Handles error toast
        boolean requirementPassed = false;
        for (boolean b : requiredFieldFilled) {
            if (b) {
                requirementPassed = true;
            } else {
                requirementPassed = false;
                break;
            }
        }

        if (requirementPassed) {
            userAccountHelpers.saveBooleanValue(UserAccountKeys.ACCOUNT_EXISTS, true);
            return true;
        } else {
            Toast.makeText(activity, getResources().getString(R.string.toast_fill_required_fields), Toast.LENGTH_LONG).show();
            return false;
        }
    }

    private void loadAccountDetails() {
        if (userAccountHelpers == null ||
                name == null ||
                gender == null ||
                birthday == null ||
                course == null ||
                settingsProfilePicture == null ||
                avatarEditor == null) {
            return;
        }

        name.setText(userAccountHelpers.readStringValue(UserAccountKeys.ACCOUNT_ITEM_NAME, ""));
        String genderCase = userAccountHelpers.readStringValue(UserAccountKeys.ACCOUNT_ITEM_GENDER, getResources().getString(R.string.settings_gender));
        if (genderCase.equals(getResources().getString(R.string.settings_gender_male)))
            genderSelection = gender.findViewById(R.id.settings_gender_male);
        else if (genderCase.equals(getResources().getString(R.string.settings_gender_female)))
            genderSelection = gender.findViewById(R.id.settings_gender_female);
        else genderSelection = gender.findViewById(R.id.settings_gender_divers);

        if (genderSelection != null) genderSelection.setChecked(true);
        String savedBirthdate = userAccountHelpers.readStringValue(UserAccountKeys.ACCOUNT_ITEM_AGE, null);
        if (savedBirthdate != null) {
            String[] separatedDate = savedBirthdate.split("\\.");
            int bdDay = Integer.parseInt(separatedDate[0]);
            int bdMonth = Integer.parseInt(separatedDate[1]) - 1;
            int bdYear = Integer.parseInt(separatedDate[2]);

            birthday.init(bdYear, bdMonth, bdDay, (view, year, monthOfYear, dayOfMonth) -> {
            });
        }
        course.setText(userAccountHelpers.readStringValue(UserAccountKeys.ACCOUNT_ITEM_COURSE, ""));

        settingsProfilePicture.setImageBitmap(avatarEditor.loadProfilePicture());
    }

    @Override
    public void dismiss() {
        if (this.isVisible()) {
            super.dismiss();
        }
    }
}
