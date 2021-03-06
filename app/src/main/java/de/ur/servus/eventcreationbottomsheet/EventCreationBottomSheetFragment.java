package de.ur.servus.eventcreationbottomsheet;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import javax.annotation.Nullable;

import de.ur.servus.R;
import de.ur.servus.core.Event;
import de.ur.servus.databinding.BottomsheetCreatorBinding;
import de.ur.servus.eventgenres.EventGenreAdapter;
import de.ur.servus.eventgenres.GenreData;

public class EventCreationBottomSheetFragment extends BottomSheetDialogFragment implements AdapterView.OnItemSelectedListener {
    @Nullable
    private View view;

    @Nullable
    private Activity activity;
    @Nullable
    BottomSheetBehavior<View> behavior;

    @Nullable
    EventGenreAdapter genreAdapter;

    @Nullable
    private OnCreateEventClickListener onCreateEventClickListener;
    @Nullable
    private OnEditEventClickListener onEditEventClickListener;

    private String selectedGenre;

    @Nullable
    private Event eventToEdit;

    @Nullable
    BottomsheetCreatorBinding binding;

    private final String[] numberPickerValues = new String[] {"∞", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20"};

    public EventCreationBottomSheetFragment() {

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
        binding = BottomsheetCreatorBinding.inflate(inflater, container, false);
        view = binding.getRoot();

        activity = (Activity) getContext();

        genreAdapter = new EventGenreAdapter(view.getContext(), GenreData.allGenres);
        binding.eventCreationGenreSpinner.setOnItemSelectedListener(this);
        binding.eventCreationGenreSpinner.setAdapter(genreAdapter);

        tryUpdateView();

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void update(@Nullable Event event, OnCreateEventClickListener onCreateEventClickListener, OnEditEventClickListener onEditEventClickListener) {
        this.onCreateEventClickListener = onCreateEventClickListener;
        this.onEditEventClickListener = onEditEventClickListener;
        this.eventToEdit = event;

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
            int maxHeight = (int) (displayMetrics.heightPixels - TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 128, displayMetrics));

            behavior.setSkipCollapsed(true);
            behavior.setMaxHeight(maxHeight);
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
    }

    private void postSetText(EditText view, String text) {
        view.post(() -> view.setText(text, TextView.BufferType.EDITABLE));
    }

    private void postSetSelection(Spinner view, int position) {
        view.post(() -> view.setSelection(position));
    }

    private void postSetNumberPicker(NumberPicker view, int position) {
        view.post(() -> view.setValue(position));
    }

    /*
     * This only work from or after OnViewCreate and after an initial update call
     */
    @SuppressLint("SetTextI18n")
    private void tryUpdateView() {
        if (binding == null || genreAdapter == null) {
            return;
        }

        // set content
        /* Initialize NumberPicker */
        binding.eventCreationAttendeeCount.setWrapSelectorWheel(false);
        binding.eventCreationAttendeeCount.setMinValue(0);
        binding.eventCreationAttendeeCount.setMaxValue(20);
        binding.eventCreationAttendeeCount.setDisplayedValues(numberPickerValues);

        if (eventToEdit != null) {
            // these post methods are necessary, because setText will not update the displayed value
            postSetText(binding.eventCreationEventname, eventToEdit.getName());
            postSetText(binding.eventCreationDescription, eventToEdit.getDescription());
            postSetSelection(binding.eventCreationGenreSpinner, genreAdapter.getPositionFromName(eventToEdit.getGenre()));
            postSetNumberPicker(binding.eventCreationAttendeeCount, Integer.parseInt(eventToEdit.getMaxAttendees()));
        } else {
            postSetText(binding.eventCreationEventname, "");
            postSetText(binding.eventCreationDescription, "");
            postSetSelection(binding.eventCreationGenreSpinner, 0);
            postSetNumberPicker(binding.eventCreationAttendeeCount, 0);
        }

        // set listeners
        binding.eventCreateButton.setOnClickListener(v -> {
            // Required field check works analog to the settings bottomsheet as an array
            // in case we decide to add further mandatory fields besides one

            boolean[] requiredFieldFilled = {false};
            boolean requirementPassed = false;

            if (binding.eventCreationEventname.getText().toString()  != null && !binding.eventCreationEventname.getText().toString().equals("")){
                requiredFieldFilled[0] = true;
            }

            for (boolean b : requiredFieldFilled) {
                if (b) {
                    requirementPassed = true;
                } else {
                    requirementPassed = false;
                    break;
                }
            }

            if (requirementPassed) {
                var name = binding.eventCreationEventname.getText().toString();
                var description = binding.eventCreationDescription.getText().toString();
                var selectedMaxAttendees = String.valueOf(binding.eventCreationAttendeeCount.getValue());
                if (eventToEdit != null) {
                    if (onEditEventClickListener != null) {
                        onEditEventClickListener.accept(eventToEdit, new EventCreationData(name, description, selectedGenre, selectedMaxAttendees));
                    }
                } else {
                    if (onCreateEventClickListener != null) {
                        onCreateEventClickListener.accept(new EventCreationData(name, description, selectedGenre, selectedMaxAttendees));
                    }
                }
            } else {
                Toast.makeText(activity, getResources().getString(R.string.toast_fill_required_fields), Toast.LENGTH_LONG).show();
            }
        });

        // style views
        binding.eventCreateButton.setText(eventToEdit == null ? R.string.event_creation_button_create : R.string.event_creation_button_edit);
    }

    @Override
    //used for the event creation spinner
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
        int selectedGenrePos = adapterView.getSelectedItemPosition();
        selectedGenre = GenreData.allGenres[selectedGenrePos].getName();
    }

    @Override
    //used for the event creation spinner
    public void onNothingSelected(AdapterView<?> adapterView) {
    }

    @Override
    public void dismiss() {
        if (this.isVisible()) {
            super.dismiss();
        }
    }
}
