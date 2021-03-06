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
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.marcoscg.easylicensesdialog.EasyLicensesDialogCompat;

import java.util.ArrayList;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.itsrishi.exercisecounter.R;
import me.itsrishi.exercisecounter.models.Exercise;
import me.itsrishi.exercisecounter.models.Session;

public class ExerciseCreateActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {

    @BindView(R.id.num_turns)
    AppCompatEditText numTurns;
    @BindView(R.id.time_per_turn)
    AppCompatEditText timePerTurn;
    @BindView(R.id.gap_between_turns)
    AppCompatEditText gapBetweenTurns;
    @BindView(R.id.exercise_submit)
    AppCompatButton exerciseSubmit;
    @BindView(R.id.exercise_name)
    AppCompatEditText exerciseName;
    @BindView(R.id.autoplay)
    SwitchCompat autoplay;
    @BindView(R.id.ex_create_activity_toolbar)
    Toolbar toolBar;
    private Exercise exercise;
    private Session session;
    private ArrayList<Session> sessions;
    private int index;
    private int position;
    private boolean shouldAutoplay = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_create);
        ButterKnife.bind(this);
        setSupportActionBar(toolBar);
        setTitle(R.string.title_activity_exercise_create);

        if (savedInstanceState != null) {
            sessions = savedInstanceState.getParcelableArrayList("sessions");
            index = savedInstanceState.getInt("index");
            position = savedInstanceState.getInt("position");
        } else {
            sessions = getIntent().getParcelableArrayListExtra("sessions");
            index = getIntent().getIntExtra("index", 0);
            position = getIntent().getIntExtra("position", 0);
        }
        session = sessions.get(index);

        try {
            exercise = session.getExercises().get(position);
        } catch (IndexOutOfBoundsException ex) {
            ex.printStackTrace();
        } catch (NullPointerException ex) {
            session.setExercises(new ArrayList<Exercise>());
        }

        if (exercise != null) {
            setTitle(R.string.title_activity_session_edit);
            exerciseName.setText(exercise.getName());
            numTurns.setText(String.format(Locale.ENGLISH, "%d", exercise.getTurns()));
            timePerTurn.setText(String.format(Locale.ENGLISH, "%f", exercise.getTimePerTurn()));
            gapBetweenTurns.setText(String.format(Locale.ENGLISH, "%f", exercise.getGapBetweenTurns()));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                shouldAutoplay = exercise.getAutoplay();
                autoplay.setChecked(exercise.getAutoplay());
            }
        }
        autoplay.setOnCheckedChangeListener(this);
        exerciseSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (updateSessions()) return;
                Intent intent = new Intent(ExerciseCreateActivity.this, SessionCreateActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("index", index);
                intent.putExtra("sessions", sessions);
                startActivity(intent);
            }
        });

        ButterKnife.bind(this);
    }

    /**
     * @return If an error occurs, this flag is raised
     */
    private boolean updateSessions() {
        String exName = exerciseName.getText().toString();
        int turnCount;
        float gapCount;
        float timeCount;
        try {
            turnCount = Integer.valueOf(numTurns.getText().toString());
            gapCount = Float.valueOf(gapBetweenTurns.getText().toString());
            timeCount = Float.valueOf(timePerTurn.getText().toString());
        } catch (NumberFormatException ex) {
            Toast.makeText(ExerciseCreateActivity.this, "Enter valid values", Toast.LENGTH_SHORT).show();
            return true;
        }
        if (exName.equals("")) {
            Toast.makeText(ExerciseCreateActivity.this, "Enter valid name", Toast.LENGTH_SHORT).show();
            return true;
        }
        exercise = new Exercise(exName, turnCount, timeCount, gapCount, -1, shouldAutoplay);
        ArrayList<Exercise> exercises = session.getExercises();
        if (position < exercises.size())
            exercises.set(position, exercise);
        else exercises.add(exercise);
        session.setExercises(exercises);
        sessions.set(index, session);
        return false;
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setMessage("Do you want to save the changes?");
        alertBuilder.setTitle("Save");
        alertBuilder.setNegativeButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                exerciseSubmit.performClick();
            }
        });
        alertBuilder.setPositiveButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ExerciseCreateActivity.super.onBackPressed();
            }
        });
        alertBuilder.show();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        this.shouldAutoplay = isChecked;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        updateSessions();
        outState.putParcelableArrayList("sessions", sessions);
        outState.putInt("index", index);
        outState.putInt("position", position);
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
            case (R.id.action_favorite):
                intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=" + "me.itsrishi.exercisecounter"));
                startActivity(intent);
                break;
            case R.id.action_settings:
                intent = new Intent(ExerciseCreateActivity.this, SettingsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                break;
            case R.id.action_stat:
                intent = new Intent(ExerciseCreateActivity.this, StatsActivity.class);
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
