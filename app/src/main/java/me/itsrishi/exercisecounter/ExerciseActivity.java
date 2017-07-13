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

import android.app.ActivityManager;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedList;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.itsrishi.exercisecounter.adapters.UpcomingExercisesAdapter;
import me.itsrishi.exercisecounter.listeners.ExerciseModificationListener;
import me.itsrishi.exercisecounter.listeners.IntegerChangeListener;
import me.itsrishi.exercisecounter.misc.ExerciseTouchHelperCallback;
import me.itsrishi.exercisecounter.models.Exercise;
import me.itsrishi.exercisecounter.models.Session;
import me.itsrishi.exercisecounter.views.PlayingView;

public class ExerciseActivity extends AppCompatActivity implements IntegerChangeListener, ExerciseModificationListener, View.OnClickListener {

    private static final String TAG = "EXACTIVITY";
    public static Session session;
    @BindView(R.id.playing_view)
    PlayingView playingView;
    @BindView(R.id.upcoming_exercises)
    RecyclerView recyclerView;
    UpcomingExercisesAdapter adapter;
    ItemTouchHelper touchHelper;
    ArrayList<Exercise> prev, temp;
    ArrayList<Exercise> exercises;
    private int index;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            session = savedInstanceState.getParcelable("session_exercise");
            exercises = savedInstanceState.getParcelableArrayList("exercises");
        } else session = getIntent().getParcelableExtra("session_exercise");
        if (exercises == null)
            exercises = new ArrayList<>(session.getExercises());

        setContentView(R.layout.activity_exercise);
        ButterKnife.bind(this);

        playingView.addIntegerChangeListener(this);

        LinkedList<ExerciseModificationListener> exerciseModificationListeners = new LinkedList<>();
        exerciseModificationListeners.add(this);
        exerciseModificationListeners.add(playingView);
        adapter = new UpcomingExercisesAdapter(exercises, 0, exerciseModificationListeners);
        touchHelper = new ItemTouchHelper(new ExerciseTouchHelperCallback(adapter));
        touchHelper.attachToRecyclerView(recyclerView);
        recyclerView.setAdapter(adapter);

        playingView.setGapBetweenExercises(session.getGapBetweenExercises());
        playingView.setExercises(exercises);

        adapter.notifyDataSetChanged();
        recyclerView.refreshDrawableState();

        prev = new ArrayList<>(exercises);
        temp = new ArrayList<>(exercises);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setTaskDescription(new ActivityManager.TaskDescription("Continue " + session.getName()));
        }

    }

    @Override
    public void onChange(int index) {
        this.index = index;
        adapter.setBeg(index);
        adapter.notifyDataSetChanged();
        recyclerView.refreshDrawableState();
        if (index >= exercises.size() && SettingsActivity.isLog()) {
            logSession();
        }
    }

    @Override
    public void onChange(boolean wasRemoval) {
        if (exercises.size() < prev.size() - 1)
            prev = new ArrayList<>(temp);
        if (wasRemoval)
            Snackbar.make(recyclerView, R.string.undo_message, Snackbar.LENGTH_LONG).setAction(R.string.undo_text, ExerciseActivity.this).setActionTextColor(Color.RED).show();
        else prev = new ArrayList<>(exercises);
        temp = new ArrayList<>(exercises);
    }

    @Override
    public void onClick(View v) {
        exercises.add(new Exercise());
        Collections.copy(exercises, prev);

        adapter.notifyDataSetChanged();
        recyclerView.refreshDrawableState();
        playingView.onChange(false);
    }

    @Override
    public void onBackPressed() {
        if (index >= exercises.size()) {
            super.onBackPressed();
            return;
        }
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setMessage(R.string.abort_message);
        alertBuilder.setTitle("Abort");
        alertBuilder.setNegativeButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ExerciseActivity.super.onBackPressed();
            }
        });
        alertBuilder.setPositiveButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        alertBuilder.show();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList("exercises", exercises);
        outState.putParcelable("session_exercise", session);
    }

    private void logSession() {
        String fileName = "session_" + session.getName();
        Calendar lastUpdated = Calendar.getInstance();
        Calendar currentDate = Calendar.getInstance();
        RandomAccessFile file = null;
        try {
            file = new RandomAccessFile(new File(getFilesDir(), fileName), "rw");
        } catch (FileNotFoundException e) {
            Log.d(TAG, "File not found");
            e.printStackTrace();
        }
        byte day = 0;
        byte month = 0;
        byte bYear = 0;
        int year, days;
        try {
            day = file.readByte();
            month = file.readByte();
            bYear = file.readByte();
            year = 2000 + bYear;
            lastUpdated.set(year, month, day);
        } catch (IOException e) {
            Log.d(TAG, "File did not have day, month or year attribute");
            e.printStackTrace();
        }
        try {
            file.seek(0);
            file.writeByte(currentDate.get(Calendar.DAY_OF_MONTH));
            file.writeByte(currentDate.get(Calendar.MONTH));
            file.writeByte(currentDate.get(Calendar.YEAR) - 2000);
            days = daysBetween(currentDate, lastUpdated);
            long len = file.length();
            file.seek(len);
            while (days > 1) {
                file.writeByte(0);
                days--;
            }
            if (days == 1)
                file.writeByte(1);
            if (days == 0) {
                if (len < 4) { // If file is just made
                    file.seek(len);
                    file.writeByte(1);
                } else {
                    file.seek(len - 1);
                    byte turns = file.readByte();
                    file.seek(len - 1);
                    file.writeByte(turns + 1);

                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                file.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Get days between two dates if day1 > day2 else false
     *
     * @param day1 The bigger date
     * @param day2 The smaller date
     * @return -1 if day1 < day2 else days between them
     */
    private int daysBetween(Calendar day1, Calendar day2) {
        Calendar dayOne = (Calendar) day1.clone(),
                dayTwo = (Calendar) day2.clone();

        if (dayOne.get(Calendar.YEAR) == dayTwo.get(Calendar.YEAR)) {
            int ret = dayOne.get(Calendar.DAY_OF_YEAR) - dayTwo.get(Calendar.DAY_OF_YEAR);
            if (ret < 0)
                return -1;
            else return ret;
        } else {
            if (dayTwo.get(Calendar.YEAR) > dayOne.get(Calendar.YEAR)) {
                return -1;
            }
            int extraDays = 0;

            int dayOneOriginalYearDays = dayOne.get(Calendar.DAY_OF_YEAR);

            while (dayOne.get(Calendar.YEAR) > dayTwo.get(Calendar.YEAR)) {
                dayOne.add(Calendar.YEAR, -1);
                // getActualMaximum() important for leap years
                extraDays += dayOne.getActualMaximum(Calendar.DAY_OF_YEAR);
            }

            return extraDays - dayTwo.get(Calendar.DAY_OF_YEAR) + dayOneOriginalYearDays;
        }
    }

}
