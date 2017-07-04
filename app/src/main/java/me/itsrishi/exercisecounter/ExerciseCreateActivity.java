/*
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
 */

package me.itsrishi.exercisecounter;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
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
    private Exercise exercise;
    private Session session;
    private int index;
    private int position;
    private boolean shouldAutoplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_create);
        ButterKnife.bind(this);

        if (savedInstanceState != null) {
            session = savedInstanceState.getParcelable("session");
            index = savedInstanceState.getInt("index");
            position = savedInstanceState.getInt("position");
        } else {
            session = getIntent().getParcelableExtra("session");
            index = getIntent().getIntExtra("index", 0);
            position = getIntent().getIntExtra("position", 0);
        }

        try {
            exercise = session.getExercises().get(position);
        } catch (IndexOutOfBoundsException ex) {
            ex.printStackTrace();
        } catch (NullPointerException ex) {
            session.setExercises(new ArrayList<Exercise>());
        }

        if (exercise != null) {
            exerciseName.setText(exercise.getName());
            numTurns.setText("" + exercise.getTurns());
            timePerTurn.setText("" + exercise.getTimePerTurn());
            gapBetweenTurns.setText("" + exercise.getGapBetweenTurns());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                autoplay.setChecked(exercise.getAutoplay());
            }
        }
        autoplay.setOnCheckedChangeListener(this);
        exerciseSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                    return;
                }
                if(exName == "") {
                    Toast.makeText(ExerciseCreateActivity.this, "Enter valid na", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (exercise == null) {
                    exercise = new Exercise(exName, turnCount, timeCount, gapCount, -1, shouldAutoplay);
                    session.getExercises().add(exercise);
                } else {
                    exercise = new Exercise(exName, turnCount, timeCount, gapCount, -1, shouldAutoplay);
                    session.getExercises().add(position, exercise);
                    session.getExercises().remove(position + 1);
                }
                Intent intent = new Intent(ExerciseCreateActivity.this, SessionCreateActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("index", index);
                intent.putExtra("session", session);
                startActivity(intent);
            }
        });

        ButterKnife.bind(this);
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setMessage("Do you want to save the changes?");
        alertBuilder.setTitle("Save");
        alertBuilder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                exerciseSubmit.performClick();
            }
        });
        alertBuilder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
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
        outState.putParcelable("session", session);
        outState.putInt("index", index);
        outState.putInt("position", position);
    }
}
