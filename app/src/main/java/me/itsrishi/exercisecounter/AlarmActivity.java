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

import android.app.AlarmManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.itsrishi.exercisecounter.adapters.AlarmAdapter;
import me.itsrishi.exercisecounter.listeners.AlarmDialogStateListener;
import me.itsrishi.exercisecounter.listeners.RecyclerViewClickListener;
import me.itsrishi.exercisecounter.models.AlarmTime;
import me.itsrishi.exercisecounter.models.Session;
import me.itsrishi.exercisecounter.views.AlarmDialogFragment;

public class AlarmActivity extends AppCompatActivity implements RecyclerViewClickListener, AlarmDialogStateListener {

    @BindView(R.id.alarm_edit_list)
    RecyclerView alarmEditList;
    @BindView(R.id.alarm_plus_fab)
    FloatingActionButton alarmPlusFab;
    @BindView(R.id.alarm_activity_toolbar)
    Toolbar toolBar;
    @BindView(R.id.alarms_submit)
    AppCompatButton alarmsSubmit;
    ArrayList<Session> sessions;
    ArrayList<AlarmTime> alarmTimes;
    int index;
    AlarmAdapter alarmAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);
        ButterKnife.bind(this);
        setSupportActionBar(toolBar);
        setTitle("Alarms Screen");
        if (savedInstanceState != null) {
            sessions = savedInstanceState.getParcelableArrayList("sessions");
            index = savedInstanceState.getInt("index");
            AlarmTime alarmTime = savedInstanceState.getParcelable("alarm");
        }
        if (sessions == null) {
            sessions = getIntent().getParcelableArrayListExtra("sessions");
            index = getIntent().getIntExtra("index", 0);
        }
        alarmTimes = sessions.get(index).getAlarmTimes();
        if (alarmTimes == null) {
            alarmTimes = new ArrayList<>();
        }
        alarmAdapter = new AlarmAdapter(alarmTimes, this);
        alarmEditList.setAdapter(alarmAdapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int pos = viewHolder.getAdapterPosition();
                alarmTimes.remove(pos);
                alarmAdapter.notifyItemRemoved(pos);
                alarmEditList.refreshDrawableState();
            }
        });
        touchHelper.attachToRecyclerView(alarmEditList);
        alarmPlusFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                byte val = 0;
                int size = 0;
                if (alarmTimes != null)
                    size = alarmTimes.size();
                AlarmDialogFragment fragment = AlarmDialogFragment.getInstance(AlarmActivity.this, new AlarmTime(val, val, val, true), size);
                fragment.setCancelable(false);
                fragment.show(AlarmActivity.this.getFragmentManager(), "tag");
            }
        });
    }

    @Override
    public void onClick(int position, View view) {
        byte val = 0;
        AlarmTime time = alarmTimes.get(position);
        AlarmDialogFragment fragment = AlarmDialogFragment.getInstance(this, new AlarmTime(time.getMins(), time.getHours(), time.getRepeatDays(), true), position);
        fragment.setCancelable(false);
        fragment.show(AlarmActivity.this.getFragmentManager(), "tag");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.action_favorite:
                Toast.makeText(this, "Fav pressed", Toast.LENGTH_LONG).show();
                break;
            case R.id.action_settings:
                intent = new Intent(AlarmActivity.this, SettingsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                break;
            case R.id.action_stat:
                intent = new Intent(AlarmActivity.this, StatsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                break;
        }
        return true;
    }

    @Override
    public void onSubmit(int pos, AlarmTime alarmTime) {
        if (alarmTimes != null && alarmTimes.size() > pos)
            alarmTimes.set(pos, alarmTime);
        else alarmTimes.add(alarmTime);
        sessions.get(index).setAlarmTimes(alarmTimes);
        alarmAdapter.setAlarmTimes(alarmTimes);
        alarmAdapter.notifyDataSetChanged();
        alarmEditList.refreshDrawableState();
    }

    @Override
    public void onCancel() {

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("sessions", sessions);
        outState.putInt("index", index);
    }

    @OnClick(R.id.alarms_submit)
    public void onViewClicked() {
        Intent intent = new Intent(AlarmActivity.this,SessionCreateActivity.class);
        intent.putParcelableArrayListExtra("sessions",sessions);
        intent.putExtra("index",index);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setMessage("Do you want to add the alarms?");
        alertBuilder.setTitle("Add alarms");
        alertBuilder.setNegativeButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alarmsSubmit.performClick();
            }
        });
        alertBuilder.setPositiveButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                AlarmActivity.super.onBackPressed();
            }
        });
        alertBuilder.show();
    }
}
