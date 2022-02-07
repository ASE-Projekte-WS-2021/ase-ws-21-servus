package de.ur.servus;

import static de.ur.servus.MainActivity.SUBSCRIBED_TO_EVENT;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.function.Consumer;

import javax.annotation.Nullable;

import de.ur.servus.core.BackendHandler;
import de.ur.servus.core.Event;
import de.ur.servus.core.EventListener;
import de.ur.servus.core.ListenerRegistration;
import de.ur.servus.core.firebase.FirestoreBackendHandler;

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

    private final SharedPreferences sharedPreferences;
    private final Context context;
    @Nullable
    private ListenerRegistration listenerRegistration;
    @Nullable
    private Consumer<Event> onClickListener;
    @Nullable
    private Consumer<String> onEventNotFoundListener;
    @Nullable
    private Event event;
    private boolean attending = false;

    public DetailsBottomSheetFragment(SharedPreferences sharedPreferences, Context context) {
        this.sharedPreferences = sharedPreferences;
        this.context = context;
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

        tryFillViews();
        tryStyleButton();
        trySetOnClickListener();

        return view;
    }

    public void setOnClickListener(Consumer<Event> onClickListener) {
        this.onClickListener = onClickListener;
    }

    public void setOnEventNotFoundListener(Consumer<String> onEventNotFoundListener) {
        this.onEventNotFoundListener = onEventNotFoundListener;
    }

    public void subscribeEvent(String eventId, Runnable onFirstFetch) {
        if (listenerRegistration != null) {
            listenerRegistration.unsubscribe();
        }

        BackendHandler bh_marker = FirestoreBackendHandler.getInstance();
        listenerRegistration = bh_marker.subscribeEvent(eventId, new EventListener<>() {
            boolean firstFetch = true;

            @SuppressLint("SetTextI18n")
            @Override
            public void onEvent(Event e) {
                attending = sharedPreferences.getString(SUBSCRIBED_TO_EVENT, "none").equals(e.getId());
                event = e;

                tryFillViews();
                tryStyleButton();
                trySetOnClickListener();

                if (firstFetch) {
                    firstFetch = false;
                    onFirstFetch.run();
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e("Data", e.getMessage());

                var editor = sharedPreferences.edit();
                editor.putString(SUBSCRIBED_TO_EVENT, "none");
                editor.apply();

                if (onEventNotFoundListener != null) {
                    onEventNotFoundListener.accept(eventId);
                }

                dismiss();
            }
        });
    }

    /*
     * These only work from OnViewCreate or later and after event is set
     */
    @SuppressLint("SetTextI18n")
    private void tryFillViews() {
        if (details_eventname == null || details_description == null || details_attendees == null || event == null) {
            return;
        }

        details_eventname.setText(event.getName());
        details_description.setText(event.getDescription());
        details_attendees.setText(Long.toString(event.getAttendants()));
    }

    private void tryStyleButton() {
        if (view == null || btn_attend_withdraw == null) {
            return;
        }

        if (attending) {
            btn_attend_withdraw.setText(R.string.event_details_button_withdraw);
            btn_attend_withdraw.setBackgroundResource(R.drawable.style_btn_roundedcorners_clicked);
            btn_attend_withdraw.setTextColor(context.getResources().getColor(R.color.servus_pink, view.getContext().getTheme()));
        } else {
            btn_attend_withdraw.setText(R.string.event_details_button_attend);
            btn_attend_withdraw.setBackgroundResource(R.drawable.style_btn_roundedcorners);
            btn_attend_withdraw.setTextColor(context.getResources().getColor(R.color.servus_white, view.getContext().getTheme()));
        }
    }

    private void trySetOnClickListener() {
        if (btn_attend_withdraw == null) {
            return;
        }

        btn_attend_withdraw.setOnClickListener(v -> {
            if (onClickListener != null) {
                onClickListener.accept(event);
            }
        });
    }
}
