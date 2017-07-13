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

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.itsrishi.exercisecounter.adapters.ExerciseAdapter;
import me.itsrishi.exercisecounter.listeners.ExerciseModificationListener;
import me.itsrishi.exercisecounter.listeners.RecyclerViewClickListener;
import me.itsrishi.exercisecounter.misc.ExerciseTouchHelperCallback;
import me.itsrishi.exercisecounter.models.Exercise;
import me.itsrishi.exercisecounter.models.Session;

public class SessionCreateActivity extends Activity implements View.OnClickListener,
        ExerciseModificationListener, RecyclerViewClickListener {

    @BindView(R.id.session_name_set)
    AppCompatEditText sessionNameSet;
    @BindView(R.id.session_gap_set)
    AppCompatEditText sessionGapSet;
    @BindView(R.id.exercise_edit_list)
    RecyclerView exerciseEditList;
    @BindView(R.id.session_submit)
    AppCompatButton sessionSubmit;
    ExerciseAdapter exerciseAdapter;
    ArrayList<Session> sessions;
    Session session;
    ItemTouchHelper touchHelper;
    ArrayList<Exercise> prev, temp;
    ArrayList<Exercise> exercises;
    int index;
    @BindView(R.id.exercise_plus_fab)
    FloatingActionButton exercisePlusFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_create);
        ButterKnife.bind(this);
        if (savedInstanceState != null) {
            index = savedInstanceState.getInt("index");
            sessions = savedInstanceState.getParcelableArrayList("sessions");
        } else {
            sessions = getIntent().getParcelableArrayListExtra("sessions");
            index = getIntent().getIntExtra("index", 0);
        }

        if (sessions == null) {
            sessions = new ArrayList<>();
            session = new Session();
        } else {

            if (index < sessions.size())
                session = sessions.get(index);

            if (session != null) {
                sessionNameSet.setText(session.getName());
                if (session.getGapBetweenExercises() != -1)
                    sessionGapSet.setText(String.format(Locale.ENGLISH, "%d", session.getGapBetweenExercises()));
                exercises = session.getExercises();
            } else session = new Session();
        }

        if (exercises != null) {
            prev = new ArrayList<>(exercises);
            temp = new ArrayList<>(exercises);
        }

        ArrayList<ExerciseModificationListener> listeners = new ArrayList<>(1);
        listeners.add(this);
        exerciseAdapter = new ExerciseAdapter(exercises
                , listeners, this);
        exerciseEditList.setAdapter(exerciseAdapter);


        touchHelper = new ItemTouchHelper(new ExerciseTouchHelperCallback(exerciseAdapter));
        touchHelper.attachToRecyclerView(exerciseEditList);

        exerciseAdapter.notifyDataSetChanged();
        exerciseEditList.refreshDrawableState();
        exercisePlusFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                session.setName(sessionNameSet.getText().toString());
                try {
                    session.setGapBetweenExercises(Integer.valueOf(sessionGapSet.getText().toString()));
                } catch (NumberFormatException ex) {
                    ex.printStackTrace();
                }
                if (index < sessions.size())
                    sessions.set(index, session);
                else sessions.add(session);
                int position = 0;
                if (exercises != null)
                    position = exercises.size();

                Intent intent = new Intent(SessionCreateActivity.this, ExerciseCreateActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("index", index);
                intent.putExtra("position", position);
                intent.putParcelableArrayListExtra("sessions", sessions);
                startActivity(intent);
            }
        });
        sessionSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (saveSessionToFile()) {
                    Intent intent = new Intent(SessionCreateActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
            }
        });

    }


    @Override
    public void onChange(boolean wasRemoval) {
        if (exercises.size() < prev.size() - 1)
            prev = new ArrayList<>(temp);
        if (wasRemoval)
            Snackbar.make(exerciseEditList, R.string.undo_message, Snackbar.LENGTH_LONG).setAction(R.string.undo_text, SessionCreateActivity.this).setActionTextColor(Color.RED).show();
        else prev = new ArrayList<>(exercises);
        temp = new ArrayList<>(exercises);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        saveSession();
        outState.putParcelableArrayList("sessions", sessions);
        outState.putInt("index", index);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setMessage("Do you want to save the changes?");
        alertBuilder.setTitle("Save");
        alertBuilder.setNegativeButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (saveSessionToFile()) {
                    SessionCreateActivity.super.onBackPressed();
                }
            }
        });
        alertBuilder.setPositiveButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SessionCreateActivity.super.onBackPressed();
            }
        });
        alertBuilder.show();
    }

    private void saveSession() {
        session.setName(sessionNameSet.getText().toString());
        try {
            session.setGapBetweenExercises(Integer.valueOf(sessionGapSet.getText().toString()));
        } catch (NumberFormatException ex) {
            session.setGapBetweenExercises(-1);
        }
        session.setExercises(exercises);
        if (index < sessions.size())
            sessions.set(index, session);
        else sessions.add(session);
    }

    private boolean saveSessionToFile() {
        saveSession();
        if (session.getExercises() == null) {
            Toast.makeText(this, "Add exercises by tapping plus button", Toast.LENGTH_LONG).show();
            return false;
        }
        if (session.getName().equals("")) {
            Toast.makeText(this, "Enter valid name for session", Toast.LENGTH_LONG).show();
            return false;
        }
        if (session.getGapBetweenExercises() == -1) {
            Toast.makeText(this, "Enter valid gap between exercises", Toast.LENGTH_LONG).show();
            return false;
        }
        ObjectMapper mapper = new ObjectMapper();
        try {
            FileOutputStream outputStream = SessionCreateActivity.this.openFileOutput("sessions.json", MODE_PRIVATE);
            mapper.writeValue(outputStream, sessions);
            outputStream.close();
            Log.d("JSON-VAL", "sessions.json:\n" + mapper.writeValueAsString(sessions));
            return true;
        } catch (IOException ex) {
            Toast.makeText(SessionCreateActivity.this, "Session couldn't be saved",
                    Toast.LENGTH_LONG).show();
            ex.printStackTrace();
            return false;
        }
    }

    @Override
    public void onClick(View v) {
        exercises.add(new Exercise());
        Collections.copy(exercises, prev);
        exerciseAdapter.notifyDataSetChanged();
        exerciseEditList.refreshDrawableState();
    }

    @Override
    public void onClick(int position, View view) {
        saveSession();
        Intent intent = new Intent(SessionCreateActivity.this, ExerciseCreateActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("index", index);
        intent.putExtra("position", position);
        intent.putParcelableArrayListExtra("sessions", sessions);
        startActivity(intent);
    }
}
