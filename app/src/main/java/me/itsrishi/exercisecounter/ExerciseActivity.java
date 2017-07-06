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
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.itsrishi.exercisecounter.adapters.UpcomingExercisesAdapter;
import me.itsrishi.exercisecounter.listeners.ExerciseModificationListener;
import me.itsrishi.exercisecounter.models.Exercise;
import me.itsrishi.exercisecounter.listeners.IntegerChangeListener;
import me.itsrishi.exercisecounter.models.Session;
import me.itsrishi.exercisecounter.views.PlayingView;

public class ExerciseActivity extends AppCompatActivity implements IntegerChangeListener, ExerciseModificationListener, View.OnClickListener {

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
        if (index >= exercises.size()) {
            // TODO: 02-07-2017 Log time of session
        }
    }

    public PlayingView getPlayingView() {
        return playingView;
    }

    public void setPlayingView(PlayingView playingView) {
        this.playingView = playingView;
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
        alertBuilder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ExerciseActivity.super.onBackPressed();
            }
        });
        alertBuilder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
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

}
