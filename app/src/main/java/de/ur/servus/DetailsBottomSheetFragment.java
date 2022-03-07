package de.ur.servus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import de.ur.servus.core.Attendant;
import de.ur.servus.core.Event;
import de.ur.servus.core.UserProfile;
import de.ur.servus.databinding.BottomsheetParticipantAttendeeBinding;
import de.ur.servus.databinding.BottomsheetParticipantBinding;

interface OnAttendWithdrawClickListener extends BiConsumer<Event, Boolean> {}

public class DetailsBottomSheetFragment extends BottomSheetDialogFragment {
    @Nullable
    private View view;

    @Nullable
    private Activity activity;
    @Nullable
    BottomSheetBehavior<View> behavior;

    @Nullable
    private BottomsheetParticipantBinding binding;

    @Nullable
    private OnAttendWithdrawClickListener onClickAttendWithdrawListener;
    @Nullable
    private Consumer<Event> onClickEditEventListener;
    @Nullable
    private Event event;

    private boolean attending = false;
    private boolean isCreator = false;

    public DetailsBottomSheetFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.mBottomSheetFragments);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = BottomsheetParticipantBinding.inflate(inflater, container, false);
        view = binding.getRoot();

        activity = (Activity) getContext();

        tryUpdateView();

        return view;
    }

    public void update(Event event, boolean attending, boolean isCreator, OnAttendWithdrawClickListener onClickAttendWithdrawListener, Consumer<Event> onClickEditEventListener) {
        this.event = event;
        this.attending = attending;
        this.isCreator = isCreator;
        this.onClickAttendWithdrawListener = onClickAttendWithdrawListener;
        this.onClickEditEventListener = onClickEditEventListener;

        if(this.isAdded()){
            tryUpdateView();
        }
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

    /*
     * This only work from or after OnViewCreate and after an initial update call
     */
    @SuppressLint("SetTextI18n")
    private void tryUpdateView() {
        if (view == null || binding == null || event == null || activity == null) {
            return;
        }

        // set content
        binding.eventDetailsEventname.setText(event.getName());
        binding.eventDetailsDescription.setText(event.getDescription());
        binding.eventDetailsGenre.setText(event.getGenre());

        String totalAttendeeCount = "X";
        binding.eventDetailsTotalAttendeeCount.setText(event.getAttendants().size() + " " + view.getContext().getResources().getString(R.string.event_details_total_attendees_count) + " " + totalAttendeeCount);

        if (binding.eventDetailsAttendeesContainer.getChildCount() >= 0) {
            binding.eventDetailsAttendeesContainer.removeAllViews();
            for (int i = 0; i < event.getAttendants().size(); i++) {
                Attendant attendant = event.getAttendants().get(i);
                var listItem = createAttendantDetailsItem(attendant);
                binding.eventDetailsAttendeesContainer.addView(listItem, i);
            }
        }

        // set listeners
        binding.eventDetailsButton.setOnClickListener(v -> {
            if (onClickAttendWithdrawListener != null) {
                onClickAttendWithdrawListener.accept(event, attending);
            }
        });

        binding.eventDetailsButtonEdit.setOnClickListener(v -> {
            if (onClickEditEventListener != null) {
                onClickEditEventListener.accept(event);
            }
        });

        // style views
        if (attending) {
            binding.eventDetailsButton.setText(R.string.event_details_button_withdraw);
            binding.eventDetailsButton.setBackgroundResource(R.drawable.style_btn_roundedcorners_clicked);
            binding.eventDetailsButton.setTextColor(view.getContext().getResources().getColor(R.color.servus_pink, view.getContext().getTheme()));
        } else {
            binding.eventDetailsButton.setText(R.string.event_details_button_attend);
            binding.eventDetailsButton.setBackgroundResource(R.drawable.style_btn_roundedcorners);
            binding.eventDetailsButton.setTextColor(view.getContext().getResources().getColor(R.color.servus_white, view.getContext().getTheme()));
        }

        binding.eventDetailsButtonEdit.setVisibility(isCreator ? View.VISIBLE : View.GONE);
    }

    private View createAttendantDetailsItem(Attendant attendant) {
        assert activity != null;
        assert view != null;

        BottomsheetParticipantAttendeeBinding attendeeBinding = BottomsheetParticipantAttendeeBinding.inflate(getLayoutInflater());
        View attendeeItem = attendeeBinding.getRoot();

        // Set role of attendee
        if (attendant.isCreator()) {
            attendeeBinding.eventDetailsAttendeeRole.setText(view.getContext().getResources().getString(R.string.event_details_label_role_creator));
        } else {
            attendeeBinding.eventDetailsAttendeeRole.setText(view.getContext().getResources().getString(R.string.event_details_label_role_attendee));
        }

        // dismiss button is visible, if user is creator and attendant is not creator
        if(isCreator && !attendant.isCreator()) {
            attendeeBinding.eventDetailsAttendeeDismiss.setVisibility(View.VISIBLE);
        }

        // Set name of attendee
        attendeeBinding.eventDetailsAttendeeName.setText(attendant.getUserName());

        // Fetch profile picture(s) separately and decode to a bitmap
        Bitmap currentPicture = BitmapFactory.decodeResource(view.getContext().getResources(), R.drawable.img_placeholder_avatar);
        if (attendant.getUserPicturePath() != null && !attendant.getUserPicturePath().equals("")) {
            //TODO: Load Image from Firebase
        }

        // Create a UserProfile out of all attendee data
        UserProfile attendeeProfile = new UserProfile(attendant.getUserId(), attendant.getUserName(), attendant.getUserGender(), attendant.getUserBirthdate(), attendant.getUserCourse(), currentPicture);

        // Add functionality for the on click
        attendeeBinding.eventDetailsAttendeeDataContainer.setTag(attendeeProfile);
        attendeeBinding.eventDetailsAttendeeDismiss.setTag(attendeeProfile);

        attendeeBinding.eventDetailsAttendeeDataContainer.setOnClickListener(v -> {
            // When clicked on the data, show servus card
            ProfileCardFragment servusCard = ProfileCardFragment.newInstance((UserProfile) v.getTag());

            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.add(servusCard, servusCard.getTag());
            transaction.addToBackStack(null);
            transaction.commit();
        });

        attendeeBinding.eventDetailsAttendeeDismiss.setOnClickListener(v -> {
            UserProfile user = (UserProfile) v.getTag();

            // TODO: Replace Toast with actual removement
            Toast.makeText(activity, "User " + user.getUserID() + " is not yet able to be removed", Toast.LENGTH_SHORT).show();
        });

        return attendeeItem;
    }
}
