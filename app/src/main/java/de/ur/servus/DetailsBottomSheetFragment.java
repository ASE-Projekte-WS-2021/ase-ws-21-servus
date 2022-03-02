package de.ur.servus;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import de.ur.servus.core.Event;
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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = BottomsheetParticipantBinding.inflate(inflater, container, false);
        view = binding.getRoot();

        activity = (Activity) getContext();

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
        if (view == null || binding == null || event  == null) {
            return;
        }

        // set content
        binding.eventDetailsEventname.setText(event.getName());
        binding.eventDetailsDescription.setText(event.getDescription());
        binding.eventDetailsAttendees.setText(Integer.toString(event.getAttendants().size()));
        binding.eventDetailsGenre.setText(event.getGenre());

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

        binding.eventDetailsButtonEdit.setVisibility(enableEdit ? View.VISIBLE : View.GONE);
    }
}
