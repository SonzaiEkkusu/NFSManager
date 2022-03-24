package com.nfs.nfsmanager.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;

import com.google.android.material.textview.MaterialTextView;
import com.nfs.nfsmanager.BuildConfig;
import com.nfs.nfsmanager.R;
import com.nfs.nfsmanager.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on October 11, 2020
 */

public class ChangeLogActivity extends AppCompatActivity {

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_changelog);

        AppCompatImageButton mBack = findViewById(R.id.back);
        MaterialTextView mChangeLog = findViewById(R.id.change_log);
        MaterialTextView mTitle = findViewById(R.id.app_title);
        MaterialTextView mCancel = findViewById(R.id.cancel_button);
        mTitle.setText(getString(R.string.app_name) + " " + BuildConfig.VERSION_NAME);
        String change_log = null;
        try {
            change_log = new JSONObject(Objects.requireNonNull(Utils.readAssetFile(
                    this, "release.json"))).getString("fullReleaseNotes");
        } catch (JSONException ignored) {
        }
        mChangeLog.setText(change_log);
        mCancel.setOnClickListener(v -> onBackPressed());
        mBack.setOnClickListener(v -> onBackPressed());
    }

}