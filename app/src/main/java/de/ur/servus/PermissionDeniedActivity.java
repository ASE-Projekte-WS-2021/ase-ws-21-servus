package de.ur.servus;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class PermissionDeniedActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission_denied);
    }
}