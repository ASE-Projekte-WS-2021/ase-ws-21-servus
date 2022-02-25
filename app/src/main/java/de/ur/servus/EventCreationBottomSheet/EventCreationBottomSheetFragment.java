package de.ur.servus.EventCreationBottomSheet;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.Objects;

import javax.annotation.Nullable;

import de.ur.servus.R;
import de.ur.servus.core.Event;
import de.ur.servus.eventGenres.EventGenreAdapter;
import de.ur.servus.eventGenres.GenreData;

public class EventCreationBottomSheetFragment extends BottomSheetDialogFragment implements AdapterView.OnItemSelectedListener {
    @Nullable
    private View view;

    @Nullable
    private Activity activity;
    @Nullable
    private Context context;
    @Nullable
    BottomSheetBehavior<View> behavior;

    @Nullable
    private EditText et_event_name;
    @Nullable
    private EditText et_event_description;
    @Nullable
    private Button event_create_button;
    @Nullable
    Spinner spinner_genre;
    @Nullable
    EventGenreAdapter genreAdapter;

    @Nullable
    private OnCreateEventClickListener onCreateEventClickListener;
    @Nullable
    private OnEditEventClickListener onEditEventClickListener;

    /* Inputs */
    private String name;
    private String description;
    private String selectedGenre;

    @Nullable
    private Event eventToEdit;

    public EventCreationBottomSheetFragment() {

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
        view = inflater.inflate(R.layout.bottomsheet_creator, container, false);

        context = getContext();
        activity = (Activity) context;

        if (view != null) {
            et_event_name = view.findViewById(R.id.event_creation_eventname);
            et_event_description = view.findViewById(R.id.event_creation_description);
            event_create_button = view.findViewById(R.id.event_create_button);
            spinner_genre = view.findViewById(R.id.event_creation_genre_spinner);

            genreAdapter = new EventGenreAdapter(view.getContext(), GenreData.getGenreList());
            if (spinner_genre != null) {
                spinner_genre.setOnItemSelectedListener(this);
                spinner_genre.setAdapter(genreAdapter);
            }
        }

        tryUpdateView();

        return view;
    }

    public void update(@Nullable Event event, OnCreateEventClickListener onCreateEventClickListener, OnEditEventClickListener onEditEventClickListener) {
        this.onCreateEventClickListener = onCreateEventClickListener;
        this.onEditEventClickListener = onEditEventClickListener;

        // update input data
        if (event == null) {
            // no event
            clearInputData();
        } else if (eventToEdit == null || !Objects.equals(event.getId(), eventToEdit.getId())) {
            // other event
            clearInputData();
            updateInputDataFromEvent(event);
        } else if (Objects.equals(event.getId(), eventToEdit.getId())) {
            // same event
            updateInputDataFromEvent(event);
            Log.d("dbg", "same event changed");
        }

        this.eventToEdit = event;

        tryUpdateView();
    }

    private void updateInputDataFromEvent(@NonNull Event event) {
        this.name = event.getName();
        this.description = event.getDescription();
        this.selectedGenre = event.getGenre();
    }

    private void clearInputData() {
        this.name = "";
        this.description = "";
        this.selectedGenre = "Activity";
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
        if (event_create_button == null || et_event_name == null || et_event_description == null) {
            return;
        }

        // set content
        et_event_name.setText(name);
        et_event_description.setText(description);
        spinner_genre.setSelection(genreAdapter.getPosition(selectedGenre));


        // set listeners
        event_create_button.setOnClickListener(v -> {
            var name = et_event_name.getText().toString();
            var description = et_event_description.getText().toString();
            if (eventToEdit != null) {
                if (onEditEventClickListener != null) {
                    onEditEventClickListener.accept(eventToEdit, new EventCreationData(name, description, selectedGenre));
                }
            } else {
                if (onCreateEventClickListener != null) {
                    onCreateEventClickListener.accept(new EventCreationData(name, description, selectedGenre));
                }
            }
        });

        // style views
        event_create_button.setText(eventToEdit == null ? R.string.event_creation_button_create : R.string.event_creation_button_edit);
    }

    @Override
    //used for the event creation spinner
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
        int selectedGenrePos = adapterView.getSelectedItemPosition();
        switch (selectedGenrePos) {
            case 0:
                selectedGenre = "Activity";
                break;
            case 1:
                selectedGenre = "Food";
                break;
            case 2:
                selectedGenre = "Hang-out";
                break;
            case 3:
                selectedGenre = "Party";
                break;
            case 4:
                selectedGenre = "Sport";
                break;
            default:
                break;
        }

    }

    @Override
    //used for the event creation spinner
    public void onNothingSelected(AdapterView<?> adapterView) {
    }
}
