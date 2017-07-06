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

package me.itsrishi.exercisecounter;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.itsrishi.exercisecounter.adapters.SessionAdapter;
import me.itsrishi.exercisecounter.models.Session;

public class ImportSessionActivity extends AppCompatActivity {
    private static final String TAG = "IMPORT_STATUS";
    ArrayList<Session> sessions;
    ArrayList<Session> newSessions;
    SessionAdapter adapter;
    @BindView(R.id.new_sesions_list)
    RecyclerView sessionsList;
    @BindView(R.id.session_tick_fab)
    FloatingActionButton sessionTickFab;
    String fileLoc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.import_session);
        ButterKnife.bind(this);
        fileLoc = getIntent().getStringExtra("file");
        Log.d(TAG, (fileLoc == null) ? "null" : fileLoc);
        fetchSessions();
        fetchNewSessions();
        sessionTickFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (newSessions != null) {
                    if (sessions == null)
                        sessions = newSessions;
                    else sessions.addAll(newSessions);
                    backToMainActivity();
                } else {
                    Toast.makeText(ImportSessionActivity.this, "Invalid sessions File. Can't be added", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void backToMainActivity() {
        Intent intent = new Intent(ImportSessionActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (sessions != null) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                mapper.writeValue(this.openFileOutput("sessions.json", MODE_PRIVATE), sessions);
            } catch (IOException ex) {
                Toast.makeText(this, "Error writing file", Toast.LENGTH_LONG).show();
            }
        }
        startActivity(intent);
    }

    private void fetchNewSessions() {
        if (isStoragePermissionGranted()) {
            Uri uri;
            InputStream inStream = null;
            if (fileLoc != null) {
                try {
                    inStream = new FileInputStream(fileLoc);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            } else if ((uri = getIntent().getData()) != null) {
                Log.d("IMPORT_STATUS", "URI is not null.\n Path: " + uri.getPath()
                        + "Encoded path: " + uri.getEncodedPath());
                String scheme = uri.getScheme();
                try {
                    if (scheme.equals("file")) {
                        inStream = openFileInput(uri.getEncodedPath());
                    }
                    if (scheme.equals("content")) {
                        ContentResolver cr = getContentResolver();
                        inStream = cr.openInputStream(uri);
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
            try {
                if (inStream == null) return;
                Log.d("IMPORT_STATUS", "Got file inputstream");
                ObjectMapper mapper = new ObjectMapper();
                newSessions = mapper.readValue(inStream,
                        mapper.getTypeFactory()
                                .constructCollectionType(ArrayList.class, Session.class));
                Log.d("IMPORT_STATUS", "Got value from map");
                adapter = new SessionAdapter(newSessions);
                sessionsList.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                sessionsList.refreshDrawableState();
            } catch (Exception ex) {
                Toast.makeText(this, "Error reading file", Toast.LENGTH_LONG).show();
                ex.printStackTrace();
            }
        }
    }

    private void fetchSessions() {
        ObjectMapper mapper = new ObjectMapper();
        try {

            FileInputStream fileInputStream = this.openFileInput("sessions.json");
            sessions = mapper.readValue(fileInputStream, mapper.getTypeFactory()
                    .constructCollectionType(ArrayList.class, Session.class));
            fileInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
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
            fetchNewSessions();
        }
    }
}
