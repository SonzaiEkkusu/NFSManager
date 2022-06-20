package com.nfs.nfsmanager.utils;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.content.FileProvider;
import androidx.preference.PreferenceManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.nfs.nfsmanager.BuildConfig;
import com.nfs.nfsmanager.MainActivity;
import com.nfs.nfsmanager.R;
import com.nfs.nfsmanager.receivers.UpdateReceiver;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on March 07, 2020
 */

public class UpdateCheck {

    private static int mVersionCode;
    private static JSONObject mJSONObject = null;
    private static final String mUPDATE_STATUS = "unavailable";
    private static String mDownloadURL = null, mReleaseNotes = null, mSHA1 = null, mVersionName = null;

    /*
     * Based on the ApkSignatureVerifier.java in https://github.com/f-droid/fdroidclient
     * Ref: https://raw.githubusercontent.com/f-droid/fdroidclient/master/app/src/main/java/org/fdroid/fdroid/installer/ApkSignatureVerifier.java
     */
    public static boolean isSignatureMatched(Context context) {
        String mKey = "[48, -126, 3, -69, 48, -126, 2, -93, -96, 3, 2, 1, 2, 2, 4, 83, 28, 18, 69, 48, 13, 6, 9, 42, -122, 72, -122, -9, 13, 1, 1, 11, 5, 0, 48, -127, -115, 49, 14, 48, 12, 6, 3, 85, 4, 6, 19, 5, 73, 110, 100, 105, 97, 49, 15, 48, 13, 6, 3, 85, 4, 8, 19, 6, 75, 101, 114, 97, 108, 97, 49, 14, 48, 12, 6, 3, 85, 4, 7, 19, 5, 75, 111, 99, 104, 105, 49, 18, 48, 16, 6, 3, 85, 4, 10, 19, 9, 83, 109, 97, 114, 116, 80, 97, 99, 107, 49, 33, 48, 31, 6, 3, 85, 4, 11, 19, 24, 83, 109, 97, 114, 116, 80, 97, 99, 107, 45, 75, 101, 114, 110, 101, 108, 32, 109, 97, 110, 97, 103, 101, 114, 49, 35, 48, 33, 6, 3, 85, 4, 3, 19, 26, 83, 117, 110, 105, 32, 80, 97, 117, 108, 32, 77, 97, 116, 104, 101, 119, 32, 77, 101, 110, 97, 99, 104, 101, 114, 121, 48, 30, 23, 13, 49, 55, 49, 48, 50, 54, 49, 49, 49, 57, 50, 48, 90, 23, 13, 52, 50, 49, 48, 50, 48, 49, 49, 49, 57, 50, 48, 90, 48, -127, -115, 49, 14, 48, 12, 6, 3, 85, 4, 6, 19, 5, 73, 110, 100, 105, 97, 49, 15, 48, 13, 6, 3, 85, 4, 8, 19, 6, 75, 101, 114, 97, 108, 97, 49, 14, 48, 12, 6, 3, 85, 4, 7, 19, 5, 75, 111, 99, 104, 105, 49, 18, 48, 16, 6, 3, 85, 4, 10, 19, 9, 83, 109, 97, 114, 116, 80, 97, 99, 107, 49, 33, 48, 31, 6, 3, 85, 4, 11, 19, 24, 83, 109, 97, 114, 116, 80, 97, 99, 107, 45, 75, 101, 114, 110, 101, 108, 32, 109, 97, 110, 97, 103, 101, 114, 49, 35, 48, 33, 6, 3, 85, 4, 3, 19, 26, 83, 117, 110, 105, 32, 80, 97, 117, 108, 32, 77, 97, 116, 104, 101, 119, 32, 77, 101, 110, 97, 99, 104, 101, 114, 121, 48, -126, 1, 34, 48, 13, 6, 9, 42, -122, 72, -122, -9, 13, 1, 1, 1, 5, 0, 3, -126, 1, 15, 0, 48, -126, 1, 10, 2, -126, 1, 1, 0, -93, -67, -12, 34, 23, -76, 100, -49, 117, 10, 42, 53, -19, 54, 110, -24, -109, 107, -128, -75, 80, 58, 56, 97, -65, 3, -98, -69, 111, 104, 23, 13, -40, 1, -42, 54, -1, 77, 125, 93, 85, 14, -118, -35, -71, 5, 123, -69, 23, -102, 9, -40, 52, -38, -24, -5, 85, 101, -112, -98, -71, 97, -84, -66, 76, 52, 86, 78, -55, -113, -40, 108, 110, 32, 106, -69, -107, 91, -18, -7, 59, -94, -37, -68, 97, 70, -5, 48, -22, -8, 113, 107, 96, -124, 127, 13, -61, -122, -45, -89, 3, -55, 41, -7, -89, -61, 11, -36, 9, -11, -111, -105, -5, -7, -115, 41, -67, 68, 55, 107, -19, 115, -92, -74, -116, -64, 11, -112, -75, -104, 95, 79, -106, -105, 16, 2, -79, 87, 70, 115, 73, -126, -15, 127, -92, 123, -83, -23, -107, -24, -36, -68, 6, 99, 107, -105, -1, 16, 99, 113, -82, 95, -47, 6, -95, -8, -18, -40, -104, 22, 21, 104, -26, -103, 97, 97, -19, -93, -103, -63, 61, 71, -103, -92, 95, -42, -118, 2, -99, 37, -15, -120, -84, 1, 69, -65, 6, -82, 70, -62, 86, 34, 19, -127, -109, -49, 89, 7, 46, 3, 123, -116, 127, -73, -77, 89, -22, -76, -63, 40, 123, -48, -124, -87, -93, -127, -68, 16, 43, 2, 59, 52, -63, 36, 111, 68, 119, 96, -10, 86, 7, 80, 35, 29, 28, -125, 95, 112, -101, 82, -117, 56, -45, -75, -97, 95, 2, 3, 1, 0, 1, -93, 33, 48, 31, 48, 29, 6, 3, 85, 29, 14, 4, 22, 4, 20, 57, 125, -58, -16, 30, 48, -4, -79, -11, 127, -18, -102, 77, 126, 122, 56, 118, 83, -57, 70, 48, 13, 6, 9, 42, -122, 72, -122, -9, 13, 1, 1, 11, 5, 0, 3, -126, 1, 1, 0, -123, 24, -8, -90, 49, 117, 25, 86, -40, 42, 93, 126, -104, 89, -117, -76, -118, 92, -56, 111, 105, 65, -106, 102, -9, 110, -46, -67, 15, 0, 99, -10, 127, -113, 82, -65, 21, -71, -89, 104, 126, -113, 95, 25, -97, -12, -9, -64, -2, 83, -84, 100, -21, -84, -92, -97, 61, -17, 67, -98, -66, 79, -91, 12, -15, 106, 9, 11, -22, -112, -28, -60, -75, -73, -72, 69, -118, -5, -45, 10, -25, 86, -65, 113, 76, 112, 59, 39, -87, 5, 57, 117, 102, 82, -43, 72, 83, 50, 52, -26, 49, -120, 15, -58, 13, -91, 76, 114, 93, -36, -46, -64, 23, 85, -70, -97, 124, -75, 12, 71, -5, 44, -81, 80, 40, 50, 126, -21, -127, 127, 116, -120, 97, -55, -121, 37, -111, 102, 83, -17, 9, -108, -37, -99, 20, -3, -39, -74, -15, -91, 25, 98, 36, -116, 118, 22, 116, -114, 60, 15, 49, 105, 123, 94, 29, 114, 12, -20, -61, -62, -71, 104, 48, 91, 63, -110, -54, -18, 94, -45, -11, -51, -111, -123, -75, -8, 36, -58, 88, -15, -116, 12, 4, 95, 16, 92, 61, -22, 12, 56, 5, 37, 44, -47, 123, 104, -98, -111, 21, -114, -121, 127, -64, -58, -9, -93, 63, -116, 83, -31, 61, -99, 34, 6, 19, -94, 69, 47, -106, -61, -115, -47, -50, -97, -28, -39, -54, 41, -48, -125, 120, 93, 84, 109, -84, 9, -83, 110, 119, 68, 72, -76, -53, 63, -52, 75]";
        String mAppKey = Arrays.toString(Objects.requireNonNull(getSignature(context.getPackageName(), context)));
        return mKey.equals(mAppKey);
    }

    @SuppressLint("PackageManagerGetSignatures")
    private static byte[] getSignature(String packageid, Context context) {
        try {
            PackageInfo pkgInfo = context.getPackageManager().getPackageInfo(packageid, PackageManager.GET_SIGNATURES);
            return signatureToBytes(pkgInfo.signatures);
        } catch (PackageManager.NameNotFoundException ignored) {}
        return null;
    }

    private static byte[] signatureToBytes(Signature[] signatures) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        for (Signature sig : signatures) {
            try {
                outputStream.write(sig.toByteArray());
            } catch (IOException ignored) {}
        }
        return outputStream.toByteArray();
    }

    public static boolean isUpdateAvailable() {
        return mJSONObject != null && BuildConfig.VERSION_CODE < versionCode();
    }

    private static int versionCode() {
        if (mJSONObject == null) return 0;
        return mVersionCode;
    }

    public static MaterialAlertDialogBuilder updateAvailableDialog(Context context) {
        return new MaterialAlertDialogBuilder(context)
                .setIcon(R.mipmap.ic_launcher)
                .setTitle(context.getString(R.string.update_available, UpdateCheck.versionName()))
                .setMessage(UpdateCheck.getChangelogs())
                .setCancelable(false)
                .setNegativeButton(context.getString(R.string.cancel), (dialog, id) -> {
                })
                .setPositiveButton(context.getString(R.string.get_it), (dialog, id) -> updaterTask(context));
    }

    private static String getChangelogs() {
        if (mJSONObject == null) return null;
        return mReleaseNotes;
    }

    private static String getChecksum() {
        if (mJSONObject == null) return null;
        return mSHA1;
    }

    private static String getDownloadUrl() {
        if (mJSONObject == null) return null;
        return mDownloadURL;
    }

    public static String getUpdateStatus() {
        return mUPDATE_STATUS;
    }

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    private static String versionName() {
        if (mJSONObject == null) return null;
        return mVersionName;
    }

    private static void getLatestApp(Context context) {
        Utils.download(new File(context.getExternalFilesDir(""), "com.nfs.nfsmanager.apk").getAbsolutePath(), getDownloadUrl());
    }

    public static void initialize(int updateCheckInterval, boolean notificationService, Context context) {
        new AsyncTasks() {

            private long ucTimeStamp;
            private int interval;
            @Override
            public void onPreExecute() {
                ucTimeStamp = PreferenceManager.getDefaultSharedPreferences(context).getLong("ucTimeStamp", 0);
                interval = updateCheckInterval * 60 * 60 * 1000;
            }

            @Override
            public void doInBackground() {
                if (System.currentTimeMillis() > ucTimeStamp + interval) {
                    try (InputStream is = new URL("https://raw.githubusercontent.com/sunilpaulmathew/NFSManager/master/app/src/main/assets/release.json").openStream()) {
                        BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                        String jsonText = readAll(rd);
                        mJSONObject = new JSONObject(jsonText);
                        mReleaseNotes = mJSONObject.getString("releaseNotes");
                        mDownloadURL = mJSONObject.getString("downloadUrl");
                        mSHA1 = mJSONObject.getString("sha1");
                        mVersionCode = mJSONObject.getInt("versionCode");
                        mVersionName = mJSONObject.getString("versionName");
                        if (mJSONObject != null) {
                            PreferenceManager.getDefaultSharedPreferences(context).edit().putLong("ucTimeStamp", System.currentTimeMillis()).apply();
                        }
                        AlarmManager mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                        Intent mIntent = new Intent(context, UpdateReceiver.class);
                        @SuppressLint("UnspecifiedImmutableFlag")
                        PendingIntent mPendingIntent = PendingIntent.getBroadcast(context, 0, mIntent, PendingIntent.FLAG_IMMUTABLE);
                        mAlarmManager.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 60 * 60 * 1000, mPendingIntent);
                    } catch (JSONException | IOException ignored) {
                    }
                }
            }

            @Override
            public void onPostExecute() {
                if (notificationService) {
                    Uri mAlarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
                    Intent mIntent = new Intent(context, MainActivity.class);
                    mIntent.putExtra(getUpdateStatus(), "UPDATE_AVAILABLE");
                    mIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    PendingIntent mPendingIntent = PendingIntent.getActivity(context, 0, mIntent, PendingIntent.FLAG_IMMUTABLE);
                    NotificationChannel mNotificationChannel = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        mNotificationChannel = new NotificationChannel("channel", context.getString(R.string.app_name), NotificationManager.IMPORTANCE_HIGH);
                    }
                    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, "channel");
                    Notification mNotification = mBuilder.setContentTitle(context.getString(R.string.update_available, UpdateCheck.versionName()))
                            .setContentText(context.getString(R.string.update_notification_summary))
                            .setStyle(new NotificationCompat.BigTextStyle())
                            .setPriority(Notification.PRIORITY_HIGH)
                            .setSmallIcon(R.drawable.ic_update)
                            .setContentIntent(mPendingIntent)
                            .setOnlyAlertOnce(true)
                            .setSound(mAlarmSound)
                            .setAutoCancel(true)
                            .build();

                    NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        notificationManager.createNotificationChannel(mNotificationChannel);
                    }
                    try {
                        notificationManager.notify(0, mNotification);
                    } catch (NullPointerException ignored) {}
                } else if (updateCheckInterval == 0) {
                    if (mJSONObject == null) {
                        new MaterialAlertDialogBuilder(context)
                                .setIcon(R.mipmap.ic_launcher)
                                .setTitle(R.string.app_name)
                                .setMessage(R.string.no_internet)
                                .setPositiveButton(context.getString(R.string.cancel), (dialog, id) -> {
                                })
                                .show();
                        return;
                    }
                    if (isUpdateAvailable()) {
                        updateAvailableDialog(context).show();
                    } else {
                        new MaterialAlertDialogBuilder(context)
                                .setIcon(R.mipmap.ic_launcher)
                                .setTitle(R.string.app_name)
                                .setMessage(R.string.update_unavailable)
                                .setPositiveButton(context.getString(R.string.cancel), (dialog, id) -> {
                                })
                                .show();
                    }
                } else {
                    if (mJSONObject == null) {
                        return;
                    }
                    if (isUpdateAvailable()) {
                        updateAvailableDialog(context).show();
                    }
                }
            }
        }.execute();
    }

    private static void installUpdate(Context context) {
        Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Uri uriFile;
        uriFile = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider",
                new File(context.getExternalFilesDir(""), "com.nfs.nfsmanager.apk"));
        intent.setDataAndType(uriFile, "application/vnd.android.package-archive");
        context.startActivity(Intent.createChooser(intent, ""));
    }

    private static void updaterTask(Context context) {
        new AsyncTasks() {
            private ProgressDialog mProgressDialog;

            @Override
            public void onPreExecute() {
                mProgressDialog = new ProgressDialog(context);
                mProgressDialog.setMessage(context.getString(R.string.downloading, context.getString(R.string.app_name) + "..."));
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
                if (Utils.exist(new File(context.getExternalFilesDir(""), "com.nfs.nfsmanager.apk").getAbsolutePath())) {
                    Utils.delete(new File(context.getExternalFilesDir(""), "com.nfs.nfsmanager.apk").getAbsolutePath());
                }
            }

            @Override
            public void doInBackground() {
                getLatestApp(context);
            }

            @Override
            public void onPostExecute() {
                try {
                    mProgressDialog.dismiss();
                } catch (IllegalArgumentException ignored) {
                }
                if (Utils.exist(new File(context.getExternalFilesDir(""), "com.nfs.nfsmanager.apk").getAbsolutePath()) &&
                        Utils.getChecksum(new File(context.getExternalFilesDir(""), "com.nfs.nfsmanager.apk").getAbsolutePath())
                                .contains(Objects.requireNonNull(getChecksum()))) {
                    installUpdate(context);
                } else {
                    new MaterialAlertDialogBuilder(context)
                            .setIcon(R.mipmap.ic_launcher)
                            .setTitle(R.string.app_name)
                            .setMessage(R.string.download_failed)
                            .setPositiveButton(R.string.cancel, (dialog, which) -> {
                            }).show();
                }
            }
        }.execute();
    }

}