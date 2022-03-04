package de.ur.servus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import de.ur.servus.core.Attendant;
import de.ur.servus.core.Event;
import de.ur.servus.core.UserProfile;

interface OnAttendWithdrawClickListener extends BiConsumer<Event, Boolean> {}

public class DetailsBottomSheetFragment extends BottomSheetDialogFragment {
    @Nullable
    private View view;

    @Nullable
    private Activity activity;
    @Nullable
    private Context context;
    @Nullable
    BottomSheetBehavior<View> behavior;

    @Nullable
    private Button btn_attend_withdraw;
    @Nullable
    private Button btn_edit_event;
    @Nullable
    private TextView details_eventname;
    @Nullable
    private TextView details_description;
    @Nullable
    private LinearLayout details_attendees;
    @Nullable
    private TextView details_total_attendees;
    @Nullable
    private TextView details_genre;

    @Nullable
    private OnAttendWithdrawClickListener onClickAttendWithdrawListener;
    @Nullable
    private Consumer<Event> onClickEditEventListener;
    @Nullable
    private Event event;

    private boolean attending = false;
    private boolean enableEdit = false;

    public DetailsBottomSheetFragment() {
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
        view = inflater.inflate(R.layout.bottomsheet_participant, container, false);

        context = getContext();
        activity = (Activity) context;

        if (view != null) {
            details_eventname = view.findViewById(R.id.event_details_eventname);
            details_description = view.findViewById(R.id.event_details_description);
            details_attendees = view.findViewById(R.id.event_details_attendees);
            details_total_attendees = view.findViewById(R.id.event_details_total_attendee_count);
            btn_attend_withdraw = view.findViewById(R.id.event_details_button);
            details_genre = view.findViewById(R.id.event_details_genre);
            btn_edit_event = view.findViewById(R.id.event_details_button_edit);
        }

        tryUpdateView();

        return view;
    }

    public void update(Event event, boolean attending, boolean enableEdit, OnAttendWithdrawClickListener onClickAttendWithdrawListener, Consumer<Event> onClickEditEventListener) {
        this.event = event;
        this.attending = attending;
        this.enableEdit = enableEdit;
        this.onClickAttendWithdrawListener = onClickAttendWithdrawListener;
        this.onClickEditEventListener = onClickEditEventListener;

        tryUpdateView();
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
        if (details_eventname == null || details_description == null || details_attendees == null || event == null || btn_attend_withdraw == null || details_genre == null || view == null || btn_edit_event == null) {
            return;
        }

        // set content
        details_eventname.setText(event.getName());
        details_description.setText(event.getDescription());
        details_genre.setText(event.getGenre());

        String totalAttendeeCount = "X";
        details_total_attendees.setText(event.getAttendants().size() + " " + getResources().getString(R.string.event_details_total_attendees_count) + " " + totalAttendeeCount);

        if (details_attendees.getChildCount() >= 0){
            details_attendees.removeAllViews();
            for (int i = 0; i < event.getAttendants().size(); i++){
                Attendant current = event.getAttendants().get(i);

                @SuppressLint("InflateParams")
                View ll_attendee = getLayoutInflater().inflate(R.layout.bottomsheet_participant_attendee, null);
                LinearLayout ll_data = ll_attendee.findViewById(R.id.event_details_attendee_data_container);
                TextView tv_role = ll_attendee.findViewById(R.id.event_details_attendee_role);
                TextView tv_name = ll_attendee.findViewById(R.id.event_details_attendee_name);
                ImageView iv_dismiss_user = ll_attendee.findViewById(R.id.event_details_attendee_dismiss);

                // Set role of attendee
                if (current.isCreator()) tv_role.setText(getResources().getString(R.string.event_details_label_role_creator));
                else tv_role.setText(getResources().getString(R.string.event_details_label_role_attendee));

                // Set name of attendee
                tv_name.setText(current.getUserName());

                // Fetch profile picture(s) separately and decode to a bitmap
                Bitmap currentPicture = BitmapFactory.decodeResource(context.getResources(), R.drawable.img_placeholder_avatar);
                if (current.getUserPicturePath() != null && !current.getUserPicturePath().equals("")){
                    //TODO: Load Image from Firebase
                }

                // Create a UserProfile out of all attendee data
                UserProfile attendeeProfile = new UserProfile(current.getUserId(), current.getUserName(), current.getUserGender(), current.getUserBirthdate(), current.getUserCourse(), currentPicture);

                // Add functionality for the on click
                ll_data.setTag(attendeeProfile);
                iv_dismiss_user.setTag(attendeeProfile);

                ll_data.setOnClickListener(v -> {
                    // When clicked on the data, show servus card
                    ProfileCardFragment servusCard = ProfileCardFragment.newInstance((UserProfile) v.getTag());

                    FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                    transaction.add(servusCard, servusCard.getTag());
                    transaction.addToBackStack(null);
                    transaction.commit();
                });

                iv_dismiss_user.setOnClickListener(v -> {
                    UserProfile user = (UserProfile) v.getTag();

                    // TODO: Replace Toast with actual removement
                    Toast.makeText(activity, "User " + user.getUserID() + " is not yet able to be removed", Toast.LENGTH_SHORT).show();
                });

                details_attendees.addView(ll_attendee, i);
            }
        }

        // set listeners
        btn_attend_withdraw.setOnClickListener(v -> {
            if (onClickAttendWithdrawListener != null) {
                onClickAttendWithdrawListener.accept(event, attending);
            }
        });

        btn_edit_event.setOnClickListener(v -> {
            if (onClickEditEventListener != null) {
                onClickEditEventListener.accept(event);
            }
        });

        // style views
        if (attending) {
            btn_attend_withdraw.setText(R.string.event_details_button_withdraw);
            btn_attend_withdraw.setBackgroundResource(R.drawable.style_btn_roundedcorners_clicked);
            btn_attend_withdraw.setTextColor(view.getContext().getResources().getColor(R.color.servus_pink, view.getContext().getTheme()));
        } else {
            btn_attend_withdraw.setText(R.string.event_details_button_attend);
            btn_attend_withdraw.setBackgroundResource(R.drawable.style_btn_roundedcorners);
            btn_attend_withdraw.setTextColor(view.getContext().getResources().getColor(R.color.servus_white, view.getContext().getTheme()));
        }

        btn_edit_event.setVisibility(enableEdit ? View.VISIBLE : View.GONE);
    }
}
