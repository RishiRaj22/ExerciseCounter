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
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchSessionList();
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

    private void fetchSessionList() {
        sessions = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        int i = 0;
        while (true) {
            try {
                FileInputStream inputStream = this.openFileInput(String.format("sessions_%d.json", i));
                sessions.add(mapper.readValue(inputStream, Session.class));
                inputStream.close();
            } catch (IOException e) {
                break;
            }
            i++;
        }
        adapter = new SessionAdapter(new ArrayList<>(sessions), this);
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
    }

    private void play(Session session) {
        Intent intent = new Intent(MainActivity.this, ExerciseActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("session_exercise", session);
        startActivity(intent);
    }

    private void deleteSession(int index) {

        String[] files = this.fileList();
        FileChannel source, dest;
        int i;
        for (i = index; i <= files.length; i++) {
            try {
                FileInputStream inputStream = this.openFileInput(String.format("sessions_%d.json", i + 1));
                source = inputStream.getChannel();
                FileOutputStream outputStream = this.openFileOutput(String.format("sessions_%d.json", i), MODE_PRIVATE);
                dest = outputStream.getChannel();
                dest.transferFrom(source, 0, source.size());
                source.close();
                dest.close();
            } catch (IOException e) {
                break;
            }
        }
        deleteFile(String.format("sessions_%d.json", i));
        fetchSessionList();
    }

    @Override
    public void onClick(int position, View view) {
        if (view instanceof ImageView) {
//            deleteSession(position);
            Intent intent = new Intent(MainActivity.this, SessionCreateActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("index", position);
            intent.putExtra("session", sessions.get(position));
            startActivity(intent);
        }
        if (view instanceof LinearLayout)
            play(sessions.get(position));
    }

    public void onSwipe(final int position, View v) {
        String sessionName = sessions.get(position).getName();
        new AlertDialog.Builder(this)
                .setCancelable(false)
                .setIcon(R.drawable.ic_delete_white_24dp)
                .setTitle("Delete " + sessionName)
                .setMessage("Are you sure you want to delete " + sessionName + "?")
                .setPositiveButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        fetchSessionList();
                    }
                })
                .setNegativeButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        long time = System.nanoTime();
                        deleteSession(position);
                        long delta = System.nanoTime() - time;
                        double d = delta / Math.pow(10, 9);
                        Log.d("TIME_DUR", String.format("Deletion of %d took %f time", position, d));
                    }
                })
                .show();
    }
}
