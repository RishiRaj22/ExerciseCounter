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

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marcoscg.easylicensesdialog.EasyLicensesDialogCompat;
import com.stephentuso.welcome.WelcomeHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.itsrishi.exercisecounter.R;
import me.itsrishi.exercisecounter.adapters.SessionAdapter;
import me.itsrishi.exercisecounter.listeners.RecyclerViewClickListener;
import me.itsrishi.exercisecounter.models.Session;

public class MainActivity extends AppCompatActivity implements RecyclerViewClickListener {
    ArrayList<Session> sessions;
    SessionAdapter adapter;
    @BindView(R.id.sessions_list)
    RecyclerView sessionsList;
    @BindView(R.id.session_plus_fab)
    FloatingActionButton sessionPlusFab;
    @BindView(R.id.main_activity_toolbar)
    Toolbar toolBar;

    WelcomeHelper welcomeScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        welcomeScreen = new WelcomeHelper(this, MyWelcomeActivity.class);
        welcomeScreen.show(savedInstanceState);
        setSupportActionBar(toolBar);
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchSessionList();
        adapter = new SessionAdapter(new ArrayList<>(sessions), this, true);
        sessionsList.setAdapter(adapter);
        ItemTouchHelper callback = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                                  RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                MainActivity.this.onSwipe(viewHolder.getAdapterPosition(), viewHolder.itemView);
            }
        });
        callback.attachToRecyclerView(sessionsList);
        sessionsList.refreshDrawableState();
        sessionPlusFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SessionCreateActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("index", sessions.size());
                intent.putExtra("sessions", sessions);
                startActivity(intent);
            }
        });
    }

    private void fetchSessionList() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            FileInputStream fileInputStream = this.openFileInput("sessions.json");
            sessions = mapper.readValue(fileInputStream, mapper.getTypeFactory().constructCollectionType(ArrayList.class, Session.class));
            fileInputStream.close();
        } catch (IOException ex) {
            ex.printStackTrace();
            sessions = new ArrayList<>();
        }
    }

    private void play(Session session) {
        Intent intent = new Intent(MainActivity.this, ExerciseActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("session_exercise", session);
        startActivity(intent);
    }

    private void saveSessions() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            FileOutputStream outputStream = this.openFileOutput("sessions.json", MODE_PRIVATE);
            mapper.writeValue(outputStream, sessions);
            outputStream.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onClick(int position, View view) {
        if (view instanceof ImageView) {
            Intent intent = new Intent(MainActivity.this, SessionCreateActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("index", position);
            intent.putParcelableArrayListExtra("sessions", sessions);
            startActivity(intent);
        }
        if (view instanceof LinearLayout)
            play(sessions.get(position));
    }

    public void onSwipe(final int position, View v) {
        final String sessionName = sessions.get(position).getName();
        new AlertDialog.Builder(this)
                .setCancelable(false)
                .setIcon(R.drawable.ic_delete_white_24dp)
                .setTitle("Delete " + sessionName)
                .setMessage("Are you sure you want to delete " + sessionName + "?")
                .setPositiveButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        adapter.setSessions(sessions);
                        MainActivity.this.sessionsList.refreshDrawableState();
                    }
                })
                .setNegativeButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        File file = new File(getFilesDir(), "session_" + sessions.get(position).getName());
                        Log.d("DEL_STATUS", "" + file.delete());
                        sessions.remove(position);
                        long time = System.nanoTime();
                        saveSessions();
                        long delta = System.nanoTime() - time;
                        adapter.setSessions(sessions);
                        double d = delta / Math.pow(10, 9);
                        adapter.notifyItemRemoved(position);
                        Log.d("TIME_DUR", String.format("Deletion of %d took %f time", position, d));
                    }
                })
                .show();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        welcomeScreen.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        menu.add("Tutorial").setIcon(R.drawable.ic_help_outline_white_24dp).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                welcomeScreen.forceShow();
                return false;
            }
        }).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case (R.id.action_favorite):
                intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=" + "me.itsrishi.exercisecounter"));
                startActivity(intent);
                break;
            case R.id.action_settings:
                intent = new Intent(MainActivity.this, SettingsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                break;
            case R.id.action_stat:
                intent = new Intent(MainActivity.this, StatsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                break;
            case R.id.action_about:
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://rishiraj22.github.io"));
                startActivity(intent);
                break;
            case R.id.action_license:
                new EasyLicensesDialogCompat(this)
                        .setTitle("Open source licenses")
                        .setPositiveButton(android.R.string.ok, null)
                        .show();
                break;
        }
        return true;
    }
}
