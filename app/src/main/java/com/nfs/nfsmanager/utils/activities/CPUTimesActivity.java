package com.nfs.nfsmanager.utils.activities;

import android.os.Bundle;
import android.view.Menu;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.PopupMenu;

import com.google.android.material.textview.MaterialTextView;
import com.nfs.nfsmanager.R;
import com.nfs.nfsmanager.utils.CPUTimes;
import com.nfs.nfsmanager.utils.Utils;
import com.nfs.nfsmanager.utils.fragments.CPUTimesFragment;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on December 10, 2020
 */

public class CPUTimesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cputimes);

        AppCompatImageButton mBack = findViewById(R.id.back);
        AppCompatImageButton mCoreSelect = findViewById(R.id.core_pick);
        MaterialTextView mCoreTitle = findViewById(R.id.core_title);

        CPUTimes.mRecycleViewAdapter = new CPUTimesFragment.RecycleViewAdapter(CPUTimes.getData(
                "/sys/devices/system/cpu/cpu0/cpufreq/stats/time_in_state"));
        mCoreTitle.setText(getString(R.string.cpu_core, "0"));
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                new CPUTimesFragment()).commit();

        mBack.setOnClickListener(v -> super.onBackPressed());
        mCoreSelect.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(this, mCoreSelect);
            Menu menu = popupMenu.getMenu();
            for (int i = 0; i < 10; i++) {
                if (Utils.exist("/sys/devices/system/cpu/cpu" + i + "/cpufreq/stats/time_in_state")) {
                    menu.add(Menu.NONE, i, Menu.NONE, getString(R.string.cpu_core, String.valueOf(i)));
                }
            }
            popupMenu.setOnMenuItemClickListener(item -> {
                for (int i = 0; i < 10; i++) {
                    if (item.getItemId() == i) {
                        mCoreTitle.setText(getString(R.string.cpu_core, String.valueOf(i)));
                        CPUTimes.mRecycleViewAdapter = new CPUTimesFragment.RecycleViewAdapter(CPUTimes.getData(
                                "/sys/devices/system/cpu/cpu" + i + "/cpufreq/stats/time_in_state"));
                        CPUTimes.mRecyclerView.setAdapter(CPUTimes.mRecycleViewAdapter);
                    }
                }
                return false;
            });
            popupMenu.show();
        });
    }

}