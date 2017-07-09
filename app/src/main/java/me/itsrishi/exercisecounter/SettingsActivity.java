package me.itsrishi.exercisecounter;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.rtp.AudioGroup;
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

public class SettingsActivity extends AppCompatActivity {

    private static final int EXPORT_CODE = 10;
    private static final int IMPORT_CODE = 20;
    private static final String TAG = "SETTINGS";
    public static final String PREFS = "PREFERENCES";
    private static boolean mute,tts,countdown,log,notifs;
    private static final String MUTE_TAG = "mute";
    private static final String TTS_TAG = "mute";
    private static final String COUNTDOWN_TAG = "mute";
    private static final String LOG_TAG = "mute";
    private static final String NOTIFS_TAG = "notif";
    private static final String TIME_BEF_NOT_TAG = "timeBefNotif";

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
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        Log.d(TAG, "Activity result called");
        if (requestCode == EXPORT_CODE && resultCode == Activity.RESULT_OK) {
            // Use the provided utility method to parse the result
            List<Uri> files = Utils.getSelectedFilesFromResult(intent);
            for (Uri uri : files) {
                try {
                    File file = Utils.getFileForUri(uri);
                    // TODO: 09-07-2017 Get this name as a user input
                    file = new File(file, "sessions.excount");
                    FileInputStream inputStream = this.openFileInput("sessions.json");
                    FileChannel source = inputStream.getChannel();
                    FileOutputStream outputStream = new FileOutputStream(file);
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
                intent.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_DIR);
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
                                File file = new File(dir, "sessions.json");
                                file.delete();
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
        return 10;
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
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_favorite:
                Toast.makeText(this,"Fav pressed",Toast.LENGTH_LONG).show();
                break;
            case R.id.action_stat:
                Toast.makeText(this,"Stats pressed",Toast.LENGTH_LONG).show();
                break;
        }
        return true;
    }
}
