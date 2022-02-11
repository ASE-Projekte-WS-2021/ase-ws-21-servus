package de.ur.servus;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import javax.annotation.Nullable;

public class SettingsBottomSheetFragment extends BottomSheetDialogFragment {
    @Nullable
    private View view;

    public SettingsBottomSheetFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.bottomsheet_settings, container, false);

        tryUpdateView();

        return view;
    }

    public void update() {

        tryUpdateView();
    }

    /*
     * This only work from or after OnViewCreate and after an initial update call
     */
    @SuppressLint("SetTextI18n")
    private void tryUpdateView() {

        // set content

        // set listeners

        // style views
    }
}
