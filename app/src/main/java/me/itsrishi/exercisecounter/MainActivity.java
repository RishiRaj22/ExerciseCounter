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

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.itsrishi.exercisecounter.adapters.SessionAdapter;
import me.itsrishi.exercisecounter.listeners.RecyclerViewClickListener;
import me.itsrishi.exercisecounter.models.BodyPart;
import me.itsrishi.exercisecounter.models.Exercise;
import me.itsrishi.exercisecounter.models.Session;

public class MainActivity extends AppCompatActivity implements RecyclerViewClickListener {
    ArrayList<Session> sessions;
    SessionAdapter adapter;
    @BindView(R.id.sessions_list)
    RecyclerView sessionsList;
    @BindView(R.id.session_plus_fab)
    FloatingActionButton sessionPlusFab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        sessions = new ArrayList<>();

        ObjectMapper mapper = new ObjectMapper();
        int i = 0;
        while (true) {
            try {
                FileInputStream inputStream = this.openFileInput(String.format("sessions_%d.json",i));
                sessions.add(mapper.readValue(inputStream, Session.class));
                inputStream.close();
            } catch (IOException e) {
                break;
            }
            i++;
        }

        adapter = new SessionAdapter(new ArrayList<>(sessions), this);
        sessionsList.setAdapter(adapter);
        sessionsList.refreshDrawableState();
        sessionPlusFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SessionCreateActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("index", sessions.size());
                startActivity(intent);
            }
        });
    }

    private void play(Session session) {
        Intent intent = new Intent(MainActivity.this, ExerciseActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("session_exercise", session);
        startActivity(intent);
    }

    @Override
    public void onClick(int position, View view) {
        if (view instanceof ImageView) {
            Intent intent = new Intent(MainActivity.this, SessionCreateActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("index", position);
            intent.putExtra("session", sessions.get(position));
            startActivity(intent);
        }
        if (view instanceof LinearLayout)
            play(sessions.get(position));
    }
}
