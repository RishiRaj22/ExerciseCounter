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

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.itsrishi.exercisecounter.adapters.AlarmAdapter;
import me.itsrishi.exercisecounter.listeners.RecyclerViewClickListener;
import me.itsrishi.exercisecounter.models.AlarmTime;

public class AlarmActivity extends AppCompatActivity implements RecyclerViewClickListener {

    @BindView(R.id.alarm_edit_list)
    RecyclerView alarmEditList;
    @BindView(R.id.alarm_plus_fab)
    FloatingActionButton alarmPlusFab;
    ArrayList<AlarmTime> alarmTimes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);
        ButterKnife.bind(this);
        alarmTimes = new ArrayList<>();// TODO: 05-07-2017 Get data from intent and bundle 
        alarmEditList.setAdapter(new AlarmAdapter(alarmTimes,this));
        ItemTouchHelper touchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                // TODO: 05-07-2017 Implement this
            }
        });
        touchHelper.attachToRecyclerView(alarmEditList);
    }

    @Override
    public void onClick(int position, View view) {
        // TODO: 05-07-2017 Implement alertdialog containing timer and days of week
    }
}
