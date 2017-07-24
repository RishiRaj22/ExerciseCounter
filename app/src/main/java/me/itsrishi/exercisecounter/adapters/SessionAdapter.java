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

package me.itsrishi.exercisecounter.adapters;

import android.app.NotificationManager;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.itsrishi.exercisecounter.R;
import me.itsrishi.exercisecounter.listeners.RecyclerViewClickListener;
import me.itsrishi.exercisecounter.models.AlarmTime;
import me.itsrishi.exercisecounter.models.Exercise;
import me.itsrishi.exercisecounter.models.Session;

/**
 * @author Rishi Raj
 */

public class SessionAdapter extends RecyclerView.Adapter<SessionAdapter.SessionHolder> {
    private ArrayList<Session> sessions;
    private RecyclerViewClickListener listener;
    private boolean isEditable;

    public SessionAdapter(ArrayList<Session> sessions) {
        this(sessions, null, false);
    }

    public SessionAdapter(ArrayList<Session> sessions, RecyclerViewClickListener listener, boolean isEditable) {
        this.sessions = sessions;
        this.listener = listener;
        this.isEditable = isEditable;
    }

    @Override
    public SessionHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.session_layout, parent, false);
        return new SessionHolder(view);
    }

    @Override
    public void onBindViewHolder(SessionHolder holder, int position) {
        position = holder.getAdapterPosition();
        holder.sessionName.setText(sessions.get(position).getName());
        int time = 0;
        for (Exercise exercise :
                sessions.get(position).getExercises()) {
            time += (int) ((exercise.getTurns() * exercise.getTimePerTurn())
                    + (exercise.getTurns() - 1) * exercise.getGapBetweenTurns());
        }
        time += sessions.get(position).getGapBetweenExercises()
                * (sessions.get(position).getExercises().size() - 1);
        String totalTime = time / 60 + "m " + time % 60 + "s";
        holder.sessionTime.setText(totalTime);
        holder.position = position;
        Calendar calendar = Calendar.getInstance();
        for (AlarmTime alarmTime : sessions.get(position).getAlarmTimes()) {
            byte val = (byte) (calendar.get(Calendar.DAY_OF_WEEK) - 2);
            if (val < 0) val += 7;
            if (alarmTime.isActive() && (1 << val & alarmTime.getRepeatDays()) != 0) {
                if(calendar.get(Calendar.HOUR_OF_DAY)<= alarmTime.getHours()) {
                if(calendar.get(Calendar.MINUTE) <= alarmTime.getMins()) {
                    holder.scheduledTime.setText(
                            String.format(Locale.ENGLISH,
                                    "%02d:%02d",
                                    alarmTime.getHours(),
                                    alarmTime.getMins()));
                    break;
                }
            }
            }
        }
    }

    @Override
    public int getItemCount() {
        if (sessions == null) return 0;
        return sessions.size();
    }

    public ArrayList<Session> getSessions() {
        return sessions;
    }

    public void setSessions(ArrayList<Session> sessions) {
        this.sessions = sessions;
        notifyDataSetChanged();
    }

    class SessionHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        int position;
        @BindView(R.id.session_name)
        TextView sessionName;
        @BindView(R.id.scheduled_time)
        TextView scheduledTime;
        @BindView(R.id.session_time)
        TextView sessionTime;
        @BindView(R.id.session_edit_button)
        ImageView sessionEdit;
        @BindView(R.id.session_metadata)
        LinearLayout sessionMetadata;

        SessionHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
            sessionEdit.setVisibility(isEditable ? View.VISIBLE : View.INVISIBLE);
            sessionEdit.setOnClickListener(this);
            sessionMetadata.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (listener != null)
                listener.onClick(position, v);
        }

    }
}
