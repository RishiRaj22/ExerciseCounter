/*
 * MIT License
 *
 * Copyright (c) 2017 Rishi Raj
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package me.itsrishi.exercisecounter.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.nononsenseapps.filepicker.FilePickerActivity;
import com.nononsenseapps.filepicker.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import me.itsrishi.exercisecounter.R;

public class SettingsActivity extends AppCompatActivity {

    private static final int EXPORT_CODE = 10;
    private static final int IMPORT_CODE = 20;
    private static final String TAG = "SETTINGS";
    public static final String PREFS = "PREFERENCES";
    private static boolean mute,tts,countdown,log,notifs;
    public static final String MUTE_TAG = "mute";
    public static final String TTS_TAG = "tts";
    public static final String COUNTDOWN_TAG = "countdown";
    public static final String LOG_TAG = "log";
    public static final String NOTIFS_TAG = "notif";
    public static final String TIME_BEF_NOT_TAG = "timeBefNotif";

    private static boolean init = false;
    private static int timeBefNotif;
    private SharedPreferences.Editor editor;
    @BindView(R.id.settings_switch_mute)
    SwitchCompat settingsSwitchMute;
    @BindView(R.id.settings_switch_tts)
    SwitchCompat settingsSwitchTts;
    @BindView(R.id.settings_switch_countdown)
    SwitchCompat settingsSwitchCountdown;
    @BindView(R.id.settings_switch_notif)
    SwitchCompat settingsSwitchNotif;
    @BindView(R.id.settings_text_notif_time)
    AppCompatEditText settingsSwitchNotifTime;
    @BindView(R.id.settings_switch_log)
    SwitchCompat settingsSwitchLog;
    @BindView(R.id.settings_entry_import)
    LinearLayout settingsEntryImport;
    @BindView(R.id.settings_entry_export)
    LinearLayout settingsEntryExport;
    @BindView(R.id.settings_entry_remove_all)
    LinearLayout settingsEntryRemoveAll;
    @BindView(R.id.settings_activity_toolbar)
    Toolbar toolBar;
    private int clicked = 0;

    public static void init(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS,MODE_PRIVATE);
        mute = preferences.getBoolean(MUTE_TAG,false);
        tts = preferences.getBoolean(TTS_TAG,true);
        countdown = preferences.getBoolean(COUNTDOWN_TAG,true);
        log = preferences.getBoolean(LOG_TAG,true);
        notifs = preferences.getBoolean(NOTIFS_TAG,true);
        timeBefNotif = preferences.getInt(TIME_BEF_NOT_TAG,5);
        init = true;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);
        setSupportActionBar(toolBar);
        setTitle(R.string.title_activity_settings);
        editor = getSharedPreferences(PREFS,MODE_PRIVATE).edit();
        if(mute) {
            tts = false;
            countdown = false;
            settingsSwitchTts.setEnabled(false);
            settingsSwitchCountdown.setEnabled(false);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            settingsSwitchMute.setChecked(mute);
            settingsSwitchTts.setChecked(tts);
            settingsSwitchCountdown.setChecked(countdown);
            settingsSwitchNotif.setChecked(notifs);
            settingsSwitchLog.setChecked(log);
        }
        settingsSwitchNotifTime.setText(""+timeBefNotif);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        Log.d(TAG, "Activity result called");
        if (requestCode == EXPORT_CODE && resultCode == Activity.RESULT_OK) {
            // Use the provided utility method to parse the result
            List<Uri> files = Utils.getSelectedFilesFromResult(intent);
            for (Uri uri : files) {
                try {
                    File given = Utils.getFileForUri(uri);
                    File file = new File(given.getPath()+".excount");
                    given.delete();
                    FileInputStream inputStream = this.openFileInput("sessions.json");
                    FileChannel source = inputStream.getChannel();
                    // To prevent destination FileChannel from being empty
                    FileOutputStream outputStream = new FileOutputStream(file);
                    outputStream.write(255);
                    outputStream.close();
                    outputStream = new FileOutputStream(file);
                    FileChannel dest = outputStream.getChannel();
                    dest.transferFrom(source, 0, source.size());
                    source.close();
                    dest.close();
                    Log.d(TAG, "File written successfully");
                    Toast.makeText(this, "Sessions exported successfully!", Toast.LENGTH_LONG).show();
                } catch (IOException e) {
                    Toast.makeText(this, "Sessions could not be exported", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        }
        if (requestCode == IMPORT_CODE && resultCode == Activity.RESULT_OK) {
            List<Uri> files = Utils.getSelectedFilesFromResult(intent);
            for (Uri uri : files) {
                File file = Utils.getFileForUri(uri);
                String filePath = file.getAbsolutePath();
                Log.d(TAG, filePath);
                Intent i = new Intent(SettingsActivity.this, ImportSessionActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                i.putExtra("file", filePath);
                startActivity(i);
            }
        }
    }

    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Permission is granted");
                return true;
            } else {
                Log.v(TAG, "Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else {
            Log.v(TAG, "Permission is granted");
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.v(TAG, "Permission: " + permissions[0] + "was " + grantResults[0]);
        }
        if (clicked == IMPORT_CODE)
            settingsEntryImport.performClick();
        if (clicked == EXPORT_CODE)
            settingsEntryExport.performClick();

    }

    @OnCheckedChanged({R.id.settings_switch_countdown,R.id.settings_switch_log,R.id.settings_switch_mute,R.id.settings_switch_notif,R.id.settings_switch_tts})
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch(buttonView.getId()) {
            case R.id.settings_switch_mute:
                mute = isChecked;
                if(mute) {
                    tts = false;
                    countdown = false;
                    settingsSwitchTts.setEnabled(false);
                    settingsSwitchCountdown.setEnabled(false);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                        settingsSwitchTts.setChecked(false);
                        settingsSwitchCountdown.setChecked(false);
                    }
                }
                else {
                    settingsSwitchTts.setEnabled(true);
                    settingsSwitchCountdown.setEnabled(true);
                }
                editor.putBoolean(MUTE_TAG,isChecked);
                break;
            case R.id.settings_switch_tts:
                tts = isChecked;
                editor.putBoolean(TTS_TAG,isChecked);
                break;
            case R.id.settings_switch_countdown:
                countdown = isChecked;
                editor.putBoolean(COUNTDOWN_TAG,isChecked);
                break;
            case R.id.settings_switch_notif:
                notifs = isChecked;
                editor.putBoolean(NOTIFS_TAG,isChecked);
                break;
            case R.id.settings_switch_log:
                log = isChecked;
                editor.putBoolean(LOG_TAG,isChecked);
                break;
            default:
                Log.e(TAG,"Implement on checked changed for the view");
        }
    }
    @OnClick({R.id.settings_entry_import, R.id.settings_entry_export, R.id.settings_entry_remove_all})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.settings_entry_import:
                if (!isStoragePermissionGranted()) {
                    clicked = IMPORT_CODE;
                    return;
                }
                Intent i = new Intent(SettingsActivity.this, FilePickerActivity.class);
                i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
                i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, false);
                i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_FILE);
                i.putExtra(FilePickerActivity.EXTRA_START_PATH, Environment.getExternalStorageDirectory().getPath());

                startActivityForResult(i, IMPORT_CODE);
                break;
            case R.id.settings_entry_export:
                if (!isStoragePermissionGranted()) {
                    clicked = EXPORT_CODE;
                    return;
                }
                Intent intent = new Intent(SettingsActivity.this, FilePickerActivity.class);
                intent.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
                intent.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, true);
                intent.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_NEW_FILE);
                intent.putExtra(FilePickerActivity.EXTRA_START_PATH, Environment.getExternalStorageDirectory().getPath());
                startActivityForResult(intent, EXPORT_CODE);
                break;
            case R.id.settings_entry_remove_all:
                new AlertDialog.Builder(SettingsActivity.this)
                        .setTitle("Remove all sessions")
                        .setMessage("WARNING: All your sessions will be removed")
                        .setNegativeButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                File dir = getFilesDir();
                                for(File file: dir.listFiles()) {
                                    file.delete();
                                }
                            }
                        })
                        .setPositiveButton(R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).show();
                break;
        }
    }

    @Override
    protected void onPause() {
        timeBefNotif = Integer.parseInt(settingsSwitchNotifTime.getText().toString());
        editor.putInt(TIME_BEF_NOT_TAG,timeBefNotif);
        editor.commit();
        Log.d(TAG,"Preferences commited");
        super.onPause();
    }

    public static boolean isMute() {
        if(init)
            return mute;
        Log.e(TAG,"SETTINGS NOT INITIALISED");
        return false;

    }

    public static boolean isTts() {
        if(init)
            return tts;
        Log.e(TAG,"SETTINGS NOT INITIALISED");
        return true;
    }

    public static boolean isCountdown() {
        if(init)
            return countdown;
        Log.e(TAG,"SETTINGS NOT INITIALISED");
        return true;

    }

    public static boolean isLog() {
        if(init)
            return log;
        Log.e(TAG,"SETTINGS NOT INITIALISED");
        return true;
    }

    public static int getTimeBefNotif() {
        if(init)
            return timeBefNotif;
        Log.e(TAG,"SETTINGS NOT INITIALISED");
        return 5;
    }

    public static boolean isNotifs() {
        if(init)
            return notifs;
        Log.e(TAG,"SETTINGS NOT INITIALISED");
        return true;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        MenuItem currItem = menu.findItem(R.id.action_settings);
        currItem.setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent();
        switch(item.getItemId()) {
            case R.id.action_favorite:
                Toast.makeText(this,"Fav pressed",Toast.LENGTH_LONG).show();
                break;
            case R.id.action_stat:
                intent = new Intent(SettingsActivity.this, StatsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                break;
        }
        return true;
    }
}
