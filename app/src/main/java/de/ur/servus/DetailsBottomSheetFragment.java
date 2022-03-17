package de.ur.servus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.io.IOException;
import java.net.URL;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import de.ur.servus.core.Attendant;
import de.ur.servus.core.Event;
import de.ur.servus.core.UserProfile;
import de.ur.servus.databinding.BottomsheetParticipantAttendeeBinding;
import de.ur.servus.databinding.BottomsheetParticipantBinding;
import de.ur.servus.utils.AvatarEditor;
import de.ur.servus.utils.TriConsumer;

interface OnAttendWithdrawClickListener extends TriConsumer<Event, Boolean, Boolean> {}

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
    private BiConsumer<Event, UserProfile> onClickRemoveUserListener;
    @Nullable
    private Event event;

    AvatarEditor avatarEditor;

    private boolean attendingThisEvent = false;
    private boolean attendingAnyEvent = false;
    private boolean isLoading = false;
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
        avatarEditor = new AvatarEditor(activity);

        tryUpdateView();

        return view;
    }

    public void update(Event event, boolean attendingThisEvent, boolean attendingAnyEvent, boolean isCreator, OnAttendWithdrawClickListener onClickAttendWithdrawListener, Consumer<Event> onClickEditEventListener, BiConsumer<Event, UserProfile> onClickRemoveUserListener) {
        this.event = event;
        this.attendingThisEvent = attendingThisEvent;
        this.attendingAnyEvent = attendingAnyEvent;
        this.isLoading = false;
        this.isCreator = isCreator;
        this.onClickAttendWithdrawListener = onClickAttendWithdrawListener;
        this.onClickEditEventListener = onClickEditEventListener;
        this.onClickRemoveUserListener = onClickRemoveUserListener;

        if (this.isAdded()) {
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
            int maxHeight = (int) (displayMetrics.heightPixels - TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 128, displayMetrics));

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

        if (event.getDescription() == null || event.getDescription().equals("")){
            binding.eventDetailsDescriptionContainer.setVisibility(View.GONE);
        } else {
            binding.eventDetailsDescriptionContainer.setVisibility(View.VISIBLE);
            binding.eventDetailsDescription.setText(event.getDescription());
        }

        binding.eventDetailsGenre.setText(event.getGenre());


        String totalAttendeeCount;
        if (event.getMaxAttendees() != null ){
            if (event.getMaxAttendees().equals("0")){
                totalAttendeeCount = "∞";
            } else{
                totalAttendeeCount = event.getMaxAttendees();
            }
        } else {
            totalAttendeeCount = "N/A";
        }
        binding.eventDetailsTotalAttendeeCount.setText(event.getAttendants().size() + " " + view.getContext().getResources().getString(R.string.event_details_total_attendees_count) + " " + totalAttendeeCount);
        binding.eventDetailsAttendeesContainer.setVisibility(View.VISIBLE);

        if (binding.eventDetailsAttendees.getChildCount() >= 0) {
            binding.eventDetailsAttendees.removeAllViews();
            for (int i = 0; i < event.getAttendants().size(); i++) {
                Attendant attendant = event.getAttendants().get(i);
                var listItem = createAttendantDetailsItem(attendant);
                binding.eventDetailsAttendees.addView(listItem, i);
            }
        }

        // set listeners
        binding.eventDetailsButton.setOnClickListener(v -> {
            if (onClickAttendWithdrawListener != null) {
                this.isLoading = true;
                tryUpdateView();
                onClickAttendWithdrawListener.accept(event, attendingThisEvent, isCreator);
            }
        });

        binding.eventDetailsButtonEdit.setOnClickListener(v -> {
            if (onClickEditEventListener != null) {
                onClickEditEventListener.accept(event);
            }
        });

        // style views
        // not attending an event => show attend button
        // attending an event AND attending this event AND is the creator => show close button
        // attending an event AND attending this event AND is NOT the creator => show withdraw button
        // attending an event AND NOT attending this event => show no button (for now)
        if (!attendingAnyEvent) {
            binding.eventDetailsButton.setVisibility(View.VISIBLE);
            binding.eventDetailsButton.setText(R.string.event_details_button_attend);
            binding.eventDetailsButton.setBackgroundResource(R.drawable.style_btn_roundedcorners);
            binding.eventDetailsButton.setTextColor(view.getContext().getResources().getColor(R.color.servus_white, view.getContext().getTheme()));
        } else if (attendingThisEvent) {
            binding.eventDetailsButton.setVisibility(View.VISIBLE);
            binding.eventDetailsButton.setBackgroundResource(R.drawable.style_btn_roundedcorners_clicked);
            binding.eventDetailsButton.setTextColor(view.getContext().getResources().getColor(R.color.servus_pink, view.getContext().getTheme()));
            if (isCreator) {
                binding.eventDetailsButton.setText(R.string.event_details_button_close_event);
            } else {
                binding.eventDetailsButton.setText(R.string.event_details_button_withdraw);
            }
        } else {
            binding.eventDetailsButton.setVisibility(View.GONE);
        }

        if(isLoading){
            // change button style to loading inidcator
            binding.eventDetailsButton.setBackgroundResource(R.drawable.style_btn_roundedcorners_loading);
            binding.eventDetailsButton.setTextColor(view.getContext().getResources().getColor(R.color.servus_pink, view.getContext().getTheme()));
            binding.eventDetailsButton.setText(R.string.event_details_button_attend_loading);
            binding.eventDetailsButton.setOnClickListener(null);
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
        if (isCreator && !attendant.isCreator()) {
            attendeeBinding.eventDetailsAttendeeDismiss.setVisibility(View.VISIBLE);
        }

        // Set name of attendee
        attendeeBinding.eventDetailsAttendeeName.setText(attendant.getUserName());

        // Fetch profile picture(s) separately and decode to a bitmap
        Bitmap currentPicture = BitmapFactory.decodeResource(view.getContext().getResources(), R.drawable.img_placeholder_avatar);

        // Create a UserProfile out of all attendee data
        UserProfile attendeeProfile = new UserProfile(attendant.getUserId(), attendant.getUserName(), attendant.getUserGender(), attendant.getUserBirthdate(), attendant.getUserCourse(), currentPicture);

        // Add functionality for the on click
        attendeeBinding.eventDetailsAttendeeDataContainer.setTag(attendeeProfile);
        attendeeBinding.eventDetailsAttendeeDismiss.setTag(attendeeProfile);

        attendeeBinding.eventDetailsAttendeeDataContainer.setOnClickListener(v -> onAttendantClicked(attendeeProfile));

        if (attendant.getUserPicturePath() != null && !attendant.getUserPicturePath().equals("")) {
            try {
                URL url = new URL(attendant.getUserPicturePath());
                Glide.with(this)
                        .load(url)
                        .addListener(new RequestListener<>() {
                            @Override
                            public boolean onLoadFailed(@androidx.annotation.Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                BitmapDrawable drawable = (BitmapDrawable) resource;
                                Bitmap bitmap = drawable.getBitmap();
                                UserProfile attendeeProfile = new UserProfile(attendant.getUserId(), attendant.getUserName(), attendant.getUserGender(), attendant.getUserBirthdate(), attendant.getUserCourse(), bitmap);
                                attendeeBinding.eventDetailsAttendeeDataContainer.setOnClickListener(v -> onAttendantClicked(attendeeProfile));
                                return false;
                            }
                        })
                        .centerCrop()
                        .placeholder(R.drawable.img_placeholder_avatar)
                        .into(attendeeBinding.eventDetailsAttendeeImage);

            }catch (IOException e) {
                Log.e("GLIDE", e.getMessage());
            }


        }

        attendeeBinding.eventDetailsAttendeeDismiss.setOnClickListener(v -> {
            UserProfile user = (UserProfile) v.getTag();

            if (onClickRemoveUserListener != null) {
                onClickRemoveUserListener.accept(event, user);
            }
        });

        return attendeeItem;
    }

    private void onAttendantClicked(UserProfile profile){
        // When clicked on the data, show servus card
        ProfileCardFragment servusCard = ProfileCardFragment.newInstance(profile);

        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.add(servusCard, servusCard.getTag());
        transaction.addToBackStack(null);
        transaction.commit();
    }

}

