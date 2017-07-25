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

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.itsrishi.exercisecounter.R;
import me.itsrishi.exercisecounter.models.Session;
import me.itsrishi.exercisecounter.views.StatsView;

public class StatsActivity extends AppCompatActivity {
    //
//    @BindView(R.id.debugTxt)
//    TextView debugTxt;
    @BindView(R.id.stats_view)
    StatsView statsView;
    ArrayList<Session> sessions;
    @BindView(R.id.stat_session_chooser)
    Spinner statSessionChooser;
    @BindView(R.id.stat_view_chooser)
    Spinner statViewChooser;
    @BindView(R.id.stats_activity_toolbar)
    Toolbar toolBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);
        ButterKnife.bind(this);
        setSupportActionBar(toolBar);
        setTitle(R.string.title_activity_stats);

        ObjectMapper mapper = new ObjectMapper();
        try {
            FileInputStream fileInputStream = this.openFileInput("sessions.json");
            sessions = mapper.readValue(fileInputStream, mapper.getTypeFactory().constructCollectionType(ArrayList.class, Session.class));
            fileInputStream.close();
        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }

        if (sessions == null || sessions.size() <= 0 || sessions.get(0) == null) {
            Toast.makeText(this, "No stats available", Toast.LENGTH_LONG).show();
            return;
        }
        String name = sessions.get(0).getName();
        final String[] sessionNameList = new String[sessions.size()];
        for (int i = 0; i < sessions.size(); i++) {
            sessionNameList[i] = sessions.get(i).getName();
        }
        statSessionChooser.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, sessionNameList));
        statSessionChooser.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setGraphForSession(sessionNameList[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        statViewChooser.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        statsView.setCurrentView(StatsView.DAY_VIEW);
                        break;
                    case 1:
                        statsView.setCurrentView(StatsView.WEEK_VIEW);
                        break;
                    default:
                        Log.e("StatsActivity", "View chooser spinner given bad value");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        statSessionChooser.setSelection(0);
        statViewChooser.setSelection(0);
        statSessionChooser.refreshDrawableState();
        statViewChooser.refreshDrawableState();
        setGraphForSession(name);
    }

    private void setGraphForSession(String name) {
        try {
            int[] values;
            File file = new File(this.getFilesDir(), "session_" + name);
            long len = file.length();
            FileInputStream fileInputStream = new FileInputStream(file);
            int day = fileInputStream.read();
            int month = fileInputStream.read();
            int year = 2000 + fileInputStream.read();
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, day);
            SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
//            debugTxt.setText(format.format(calendar.getTime()));
            values = new int[(int) len - 3];
            for (int i = 3; i < len; i++) {
                int val = fileInputStream.read();
                values[i - 3] = val;
//                debugTxt.append(" " + val);
            }
            statsView.addValues(values, calendar, statsView.getCurrentView());
        } catch (IOException e) {
            statsView.addValues(null, null, StatsView.DAY_VIEW);
            e.printStackTrace();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        MenuItem currItem = menu.findItem(R.id.action_stat);
        currItem.setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch(item.getItemId()) {
            case R.id.action_favorite:
                Toast.makeText(this,"Fav pressed",Toast.LENGTH_LONG).show();
                break;
            case R.id.action_settings:
                intent = new Intent(StatsActivity.this, SettingsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                break;
        }
        return true;
    }
}
