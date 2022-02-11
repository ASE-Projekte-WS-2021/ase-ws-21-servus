package de.ur.servus;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.function.Consumer;

import javax.annotation.Nullable;

/*
 * Types for callbacks
 */
class EventCreationData{
    public final String name;
    public final String description;

    EventCreationData(String name, String description) {
        this.name = name;
        this.description = description;
    }
}

interface OnCreateEventClickListener extends Consumer<EventCreationData> {}

public class EventCreationBottomSheetFragment extends BottomSheetDialogFragment {
    @Nullable
    private View view;
    @Nullable
    private EditText et_event_name;
    @Nullable
    private EditText et_event_description;
    @Nullable
    private Button event_create_button;
    @Nullable
    private OnCreateEventClickListener onCreateEventClickListener;

    public EventCreationBottomSheetFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.bottomsheet_creator, container, false);

        et_event_name = view.findViewById(R.id.event_creation_eventname);
        et_event_description = view.findViewById(R.id.event_creation_description);
        event_create_button = view.findViewById(R.id.event_create_button);

        tryUpdateView();

        return view;
    }

    public void update(OnCreateEventClickListener onCreateEventClickListener) {
        this.onCreateEventClickListener = onCreateEventClickListener;

        tryUpdateView();
    }

    /*
     * This only work from or after OnViewCreate and after an initial update call
     */
    @SuppressLint("SetTextI18n")
    private void tryUpdateView() {
        if (event_create_button == null || et_event_name == null || et_event_description == null) {
            return;
        }

        // set content

        // set listeners
        event_create_button.setOnClickListener(v -> {
            if (onCreateEventClickListener != null) {
                var name = et_event_name.getText().toString();
                var description = et_event_description.getText().toString();
                onCreateEventClickListener.accept(new EventCreationData(name, description));
            }
        });

        // style views
    }
}
