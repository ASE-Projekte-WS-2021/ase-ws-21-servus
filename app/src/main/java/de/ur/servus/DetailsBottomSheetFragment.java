package de.ur.servus;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.function.BiConsumer;

import javax.annotation.Nullable;

import de.ur.servus.core.Event;

interface OnAttendWithdrawClickListener extends BiConsumer<Event, Boolean> {}

public class DetailsBottomSheetFragment extends BottomSheetDialogFragment {
    @Nullable
    private View view;
    @Nullable
    private Button btn_attend_withdraw;
    @Nullable
    private TextView details_eventname;
    @Nullable
    private TextView details_description;
    @Nullable
    private TextView details_attendees;
    @Nullable
    private OnAttendWithdrawClickListener onClickAttendWithdrawListener;
    @Nullable
    private Event event;
    private boolean attending = false;

    public DetailsBottomSheetFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.bottomsheet_participant, container, false);

        details_eventname = view.findViewById(R.id.event_details_eventname);
        details_description = view.findViewById(R.id.event_details_description);
        details_attendees = view.findViewById(R.id.event_details_attendees);
        btn_attend_withdraw = view.findViewById(R.id.event_details_button);

        tryUpdateView();

        return view;
    }

    public void update(Event event, boolean attending, OnAttendWithdrawClickListener onClickAttendWithdrawListener) {
        this.event = event;
        this.attending = attending;
        this.onClickAttendWithdrawListener = onClickAttendWithdrawListener;

        tryUpdateView();
    }

    /*
     * This only work from or after OnViewCreate and after an initial update call
     */
    @SuppressLint("SetTextI18n")
    private void tryUpdateView() {
        if (details_eventname == null || details_description == null || details_attendees == null || event == null || btn_attend_withdraw == null || view == null) {
            return;
        }

        // set content
        details_eventname.setText(event.getName());
        details_description.setText(event.getDescription());
        details_attendees.setText(Long.toString(event.getAttendants()));

        // set listeners
        btn_attend_withdraw.setOnClickListener(v -> {
            if (onClickAttendWithdrawListener != null) {
                onClickAttendWithdrawListener.accept(event, attending);
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
    }
}
