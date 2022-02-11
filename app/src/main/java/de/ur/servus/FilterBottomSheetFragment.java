package de.ur.servus;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import javax.annotation.Nullable;

public class FilterBottomSheetFragment extends BottomSheetDialogFragment {
    @Nullable
    private View view;

    public FilterBottomSheetFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.bottomsheet_filter, container, false);

        tryUpdateView();

        return view;
    }

    public void update(OnCreateEventClickListener onCreateEventClickListener) {

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
