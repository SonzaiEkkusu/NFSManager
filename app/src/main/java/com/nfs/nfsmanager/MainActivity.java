package com.nfs.nfsmanager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.Menu;
import android.view.SubMenu;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;
import com.nfs.nfsmanager.activities.CPUTimesActivity;
import com.nfs.nfsmanager.activities.DeviceInfoActivity;
import com.nfs.nfsmanager.activities.LogsActivity;
import com.nfs.nfsmanager.fragments.AboutFragment;
import com.nfs.nfsmanager.fragments.DashBoardFragment;
import com.nfs.nfsmanager.fragments.NFSFragment;
import com.nfs.nfsmanager.utils.CPUTimes;
import com.nfs.nfsmanager.utils.Flasher;
import com.nfs.nfsmanager.utils.NFS;
import com.nfs.nfsmanager.utils.UpdateCheck;
import com.nfs.nfsmanager.utils.Utils;

import java.io.File;

import in.sunilpaulmathew.rootfilepicker.utils.FilePicker;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on January 07, 2020
 */

public class MainActivity extends AppCompatActivity {

    private AppCompatImageButton mSettings;
    private MaterialTextView mProgressMessage;
    private boolean mExit, mWarning = true;
    private FrameLayout mBottomMenu;
    private Intent mIntent;
    private LinearLayout mProgressLayout;
    private final Handler mHandler = new Handler();
    private int doze, shield, dns, ads, ow, selinux, sync, tt, sf, zygot, lmk;
    private String gov, sched, tcp;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Initialize App Theme
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AppCompatImageButton mModuleImage = findViewById(R.id.module_image);
        mSettings =  findViewById(R.id.settings_menu);
        MaterialTextView mModuleStatus = findViewById(R.id.status_message);
        MaterialTextView mModuleTitle = findViewById(R.id.module_version);
        mProgressMessage = findViewById(R.id.progress_text);
        FrameLayout mStatusLayout = findViewById(R.id.support_statue);
        LinearLayout mOffLineAd = findViewById(R.id.offline_ad);
        mBottomMenu = findViewById(R.id.bottom_menu);
        mProgressLayout = findViewById(R.id.progress_layout);

        mSettings.setOnClickListener(v -> settingsMenu());

        mOffLineAd.setOnClickListener(v -> Utils.launchUrl("https://t.me/nfsreleases/424", this));

        mModuleImage.setImageDrawable(ContextCompat.getDrawable(this, R.mipmap.ic_launcher));
        mModuleTitle.setText(getString(R.string.module_version, NFS.getModVersion() +
                (NFS.getReleaseStatus().equals("") ? "" : " (" + NFS.getReleaseStatus() + ")")));

        if (NFS.isModuleParent()) {
            if (NFS.isModuleRemoved() || NFS.isModuleDisabled()) {
                mModuleStatus.setText(getString(R.string.module_status_message, NFS.isModuleRemoved() ?
                        getString(R.string.removed) : getString(R.string.disabled)));
                mModuleStatus.setVisibility(View.VISIBLE);
                mStatusLayout.setVisibility(View.VISIBLE);
                return;
            } else if (!NFS.isNFSRunning()) {
                mModuleStatus.setText(getString(R.string.module_status_execution_failed));
                mModuleStatus.setVisibility(View.VISIBLE);
                mStatusLayout.setVisibility(View.VISIBLE);
                return;
            } else if (!NFS.isNFSParent()) {
                mModuleStatus.setText(getString(R.string.data_removed));
                mModuleStatus.setVisibility(View.VISIBLE);
                mStatusLayout.setVisibility(View.VISIBLE);
                return;
            }
        } else {
            mModuleStatus.setText(getString(R.string.no_support));
            mModuleStatus.setVisibility(View.VISIBLE);
            mStatusLayout.setVisibility(View.VISIBLE);
            return;
        }

        BottomNavigationView mBottomNav = findViewById(R.id.bottom_navigation);
        mBottomNav.setVisibility(View.VISIBLE);
        mBottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            switch (item.getItemId()) {
                case R.id.nav_dashboard:
                    selectedFragment = new DashBoardFragment();
                    break;
                case R.id.nav_nfs:
                    selectedFragment = new NFSFragment();
                    break;
                case R.id.nav_about:
                    selectedFragment = new AboutFragment();
                    break;
            }
            assert selectedFragment != null;
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    selectedFragment).commit();
            return true;
        });

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new DashBoardFragment()).commit();
        }

        if (!NFS.isProUser() && Utils.getOrientation(this) == Configuration.ORIENTATION_PORTRAIT) {
            mOffLineAd.setVisibility(View.VISIBLE);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && Utils.getBoolean("update_check_auto", true, this)
                && Utils.isNotificationAccessDenied(this)) {
            notificationPermissionRequest.launch(
                    Manifest.permission.POST_NOTIFICATIONS
            );
        }

    }

    @SuppressLint("SetTextI18n")
    private void settingsMenu() {
        PopupMenu popupMenu = new PopupMenu(this, mSettings);
        Menu menu = popupMenu.getMenu();
        if (NFS.isNFSRunning()) {
            menu.add(Menu.NONE, 1, Menu.NONE, getString(R.string.view_log));
        }
        menu.add(Menu.NONE, 10, Menu.NONE, R.string.device_info);
        if (CPUTimes.supported("/sys/devices/system/cpu/cpu0/cpufreq/stats/time_in_state")) {
            menu.add(Menu.NONE, 11, Menu.NONE, R.string.cpu_stats);
        }
        if (NFS.isModuleParent()) {
            SubMenu module_options = menu.addSubMenu(Menu.NONE, 0, Menu.NONE, getString(R.string.module_settings));
            module_options.add(Menu.NONE, 2, Menu.NONE, getString(R.string.nfs_disable)).setCheckable(true)
                    .setChecked(NFS.isModuleDisabled());
            module_options.add(Menu.NONE, 3, Menu.NONE, getString(R.string.nfs_remove)).setCheckable(true)
                    .setChecked(NFS.isModuleRemoved());
            if (NFS.supported()) {
                module_options.add(Menu.NONE, 4, Menu.NONE, getString(R.string.nfs_delete));
            }
        }
        if (UpdateCheck.isSignatureMatched(this)) {
            SubMenu appSettings = menu.addSubMenu(Menu.NONE, 0, Menu.NONE, getString(R.string.app_settings));
            appSettings.add(Menu.NONE, 9, Menu.NONE, getString(R.string.update_check_auto)).setCheckable(true)
                    .setChecked(Utils.getBoolean("update_check_auto", true, this));
        }
        menu.add(Menu.NONE, 5, Menu.NONE, getString(R.string.flash_nfs));
        SubMenu reboot = menu.addSubMenu(Menu.NONE, 0, Menu.NONE, getString(R.string.reboot));
        reboot.add(Menu.NONE, 6, Menu.NONE, getString(R.string.normal));
        reboot.add(Menu.NONE, 7, Menu.NONE, getString(R.string.recovery));
        reboot.add(Menu.NONE, 8, Menu.NONE, getString(R.string.bootloader));
        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case 0:
                    break;
                case 1:
                    mIntent = new Intent(this, LogsActivity.class);
                    startActivity(mIntent);
                    break;
                case 2:
                    if (NFS.isModuleDisabled()) {
                        Utils.delete("/data/adb/modules/busybox-system-android/disable");
                    } else {
                        Utils.create("", "/data/adb/modules/busybox-system-android/disable");
                    }
                    restartApp();
                    break;
                case 3:
                    if (NFS.isModuleRemoved()) {
                        Utils.delete("/data/adb/modules/busybox-system-android/remove");
                    } else {
                        Utils.create("", "/data/adb/modules/busybox-system-android/remove");
                    }
                    restartApp();
                    break;
                case 4:
                    new MaterialAlertDialogBuilder(this)
                            .setIcon(R.mipmap.ic_launcher)
                            .setTitle(getString(R.string.delete_title))
                            .setMessage(getText(R.string.delete_message))
                            .setNeutralButton(getString(R.string.cancel), (dialogInterface, ii) -> {
                            })
                            .setPositiveButton(getString(R.string.delete), (dialogInterface, ii) -> {
                                Utils.delete("/data/NFS/");
                                restartApp();
                            })
                            .show();
                    break;
                case 5:
                    FilePicker filePicker = new FilePicker(
                            activityResultLauncher,
                            this
                    );
                    filePicker.setExtension("zip");
                    filePicker.setPath(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString());
                    filePicker.launch();
                    break;
                case 6:
                    Utils.reboot("", mProgressLayout, mProgressMessage, this);
                    break;
                case 7:
                    Utils.reboot(" recovery", mProgressLayout, mProgressMessage, this);
                    break;
                case 8:
                    Utils.reboot(" bootloader", mProgressLayout, mProgressMessage, this);
                    break;
                case 9:
                    if (Utils.getBoolean("update_check_auto", true, this)) {
                        Utils.saveBoolean("update_check_auto", false, this);
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && Utils.isNotificationAccessDenied(this)) {
                            notificationPermissionRequest.launch(
                                    Manifest.permission.POST_NOTIFICATIONS
                            );
                        } else {
                            Utils.saveBoolean("update_check_auto", true, this);
                        }
                    }
                    break;
                case 10:
                    mIntent = new Intent(this, DeviceInfoActivity.class);
                    startActivity(mIntent);
                    break;
                case 11:
                    mIntent = new Intent(this, CPUTimesActivity.class);
                    startActivity(mIntent);
            }
            return false;
        });
        popupMenu.show();
    }

    private void showWarning() {
        View checkBoxView = View.inflate(this, R.layout.checkbox_layout, null);
        MaterialCheckBox checkBox = checkBoxView.findViewById(R.id.checkbox);
        checkBox.setChecked(true);
        checkBox.setText(getString(R.string.always_show));
        checkBox.setOnCheckedChangeListener((buttonView, isChecked)
                -> mWarning = isChecked);
        MaterialAlertDialogBuilder warning = new MaterialAlertDialogBuilder(this);
        warning.setIcon(R.mipmap.ic_launcher);
        warning.setTitle(getString(R.string.nfs_conflicts));
        warning.setMessage(getString(R.string.nfs_conflicts_summary) +
                NFS.conflictsList(this));
        warning.setCancelable(false);
        warning.setView(checkBoxView);
        warning.setPositiveButton(getString(R.string.got_it), (dialog, id) ->
                Utils.saveBoolean("warningMessage", mWarning, this)
        );
        warning.show();
    }

    private void restartApp() {
        mIntent = new Intent(this, MainActivity.class);
        mIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(mIntent);
    }

    private final ActivityResultLauncher<String> notificationPermissionRequest =
            registerForActivityResult(new ActivityResultContracts
                    .RequestPermission(), isGranted -> Utils.saveBoolean("update_check_auto", isGranted, this)
            );

    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null && FilePicker.getSelectedFile().exists()) {
                    File mSelectedFile = FilePicker.getSelectedFile();
                    if (!mSelectedFile.getName().endsWith("zip")) {
                        Utils.longSnackbar(mBottomMenu, getString(R.string.invalid_zip));
                        return;
                    }
                    new MaterialAlertDialogBuilder(this)
                            .setIcon(R.mipmap.ic_launcher)
                            .setTitle(R.string.app_name)
                            .setMessage(getString(R.string.sure_message, mSelectedFile.getName()))
                            .setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> {
                            })
                            .setPositiveButton(getString(R.string.flash), (dialogInterface, i) -> Flasher.flashModule(mSelectedFile, this)).show();
                }
            }
    );

    @Override
    public void onStart() {
        super.onStart();

        if (NFS.isModuleParent() && Utils.getBoolean("warningMessage", true, this)
                && !NFS.conflictsList(this).isEmpty()) {
            showWarning();
        }
        if (NFS.isModuleRemoved() || NFS.isModuleDisabled()) {
            Utils.indefiniteSnackbar(findViewById(android.R.id.content), getString(R.string.module_status_message, NFS.isModuleRemoved() ?
                    getString(R.string.removed) : getString(R.string.disabled)));
        }

        doze = NFS.getDozeMode();
        shield = NFS.getShield();
        dns = NFS.getDNSMode();
        ads = NFS.getAds();
        ow = NFS.getOW();
        selinux = NFS.getSELinuxMode();
        sync = NFS.getSync();
        tt = NFS.getTT();
        sf = NFS.getSF();
        zygot = NFS.getZygote();
        lmk = NFS.getLMK();
        gov = NFS.getGOV();
        sched = NFS.getSched();
        tcp = NFS.getTCP();

        if (NFS.getGOV().equals("performance")) {
            new MaterialAlertDialogBuilder(this)
                    .setIcon(R.mipmap.ic_launcher)
                    .setTitle(R.string.warning)
                    .setMessage(getString(R.string.performance_warning))
                    .setCancelable(false)
                    .setPositiveButton(getString(R.string.got_it), (dialog, id) -> {
                    })
                    .show();
        }

        if (Utils.getBoolean("update_check_auto", true, this) && UpdateCheck.isSignatureMatched(this)) {
            String mUpdateStatus = getIntent().getStringExtra(UpdateCheck.getUpdateStatus());
            if (mUpdateStatus != null && mUpdateStatus.equals("UPDATE_AVAILABLE")) {
                UpdateCheck.updateAvailableDialog(this).show();
            } else {
                UpdateCheck.initialize(1, false, this);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (doze != NFS.getDozeMode() || shield != NFS.getShield()
                || dns != NFS.getDNSMode() || ads != NFS.getAds() || ow != NFS.getOW()
                || selinux != NFS.getSELinuxMode() || sync != NFS.getSync() || tt != NFS.getTT()
                || sf != NFS.getSF() || zygot != NFS.getZygote() || lmk != NFS.getLMK()
                || !gov.equals(NFS.getGOV()) || !sched.equals(NFS.getSched())
                || !tcp.equals(NFS.getTCP())) {
            new MaterialAlertDialogBuilder(this)
                    .setMessage(getString(R.string.reboot_dialog))
                    .setCancelable(false)
                    .setIcon(R.mipmap.ic_launcher)
                    .setTitle(getString(R.string.reboot_required))
                    .setNegativeButton(getString(R.string.cancel), (dialog1, id1) -> super.onBackPressed())
                    .setPositiveButton(getString(R.string.reboot), (dialog1, id1) -> Utils.reboot("", mProgressLayout, mProgressMessage, this))
                    .show();
        } else {
            if (mExit) {
                mExit = false;
                super.onBackPressed();
            } else {
                Utils.longSnackbar(mBottomMenu, getString(R.string.press_back));
                mExit = true;
                mHandler.postDelayed(() -> mExit = false, 2000);
            }
        }
    }

}