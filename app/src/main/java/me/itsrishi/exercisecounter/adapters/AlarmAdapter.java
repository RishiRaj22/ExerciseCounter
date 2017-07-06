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

package me.itsrishi.exercisecounter.adapters;

import android.content.DialogInterface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.List;

import me.itsrishi.exercisecounter.R;
import me.itsrishi.exercisecounter.listeners.RecyclerViewClickListener;
import me.itsrishi.exercisecounter.models.AlarmTime;

import static me.itsrishi.exercisecounter.R.id.alarm_active;

/**
 * @author Rishi Raj
 */

public class AlarmAdapter extends RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder> {
    private List<AlarmTime> alarmTimes;
    RecyclerViewClickListener clickListener;

    public AlarmAdapter(List<AlarmTime> alarmTimes, RecyclerViewClickListener clickListener) {
        this.alarmTimes = alarmTimes;
        this.clickListener = clickListener;
    }

    @Override
    public AlarmViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.alarm_layout, parent, false);
        return new AlarmViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AlarmViewHolder holder, int position) {
        AlarmTime alarmTime = alarmTimes.get(position);
        for (int i = 0; i < 7; i++) {
            TextView textView = holder.textViews[i];
            int val = 1 >>> i;
            if ((val & alarmTime.getRepeatDays()) == 0)
                textView.setVisibility(View.INVISIBLE);
            else textView.setVisibility(View.VISIBLE);
        }
        int hours = alarmTime.getHours();
        int mins = alarmTime.getMins();
        boolean am = hours / 12 == 0;
        hours %= 12;
        holder.currTime.setText(String.format("%d:%d",hours,mins));
        holder.amOrPm.setText(am? "AM" : "PM");
        holder.active.setChecked(alarmTime.isActive());
        holder.position = position;
    }

    @Override
    public int getItemCount() {
        return alarmTimes.size();
    }

    class AlarmViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView[] textViews = new TextView[7];
        public TextView currTime, amOrPm;
        public CheckBox active;
        int position;

        public AlarmViewHolder(View itemView) {
            super(itemView);

            textViews[0] = (TextView) itemView.findViewById(R.id.mon);
            textViews[1] = (TextView) itemView.findViewById(R.id.tue);
            textViews[2] = (TextView) itemView.findViewById(R.id.wed);
            textViews[3] = (TextView) itemView.findViewById(R.id.thu);
            textViews[4] = (TextView) itemView.findViewById(R.id.fri);
            textViews[5] = (TextView) itemView.findViewById(R.id.sat);
            textViews[6] = (TextView) itemView.findViewById(R.id.sun);

            currTime = (TextView) itemView.findViewById(R.id.set_time);
            amOrPm = (TextView) itemView.findViewById(R.id.set_am_or_pm);
            active = (CheckBox) itemView.findViewById(alarm_active);
        }

        @Override
        public void onClick(View v) {
            clickListener.onClick(position,v);
        }
    }

    public List<AlarmTime> getAlarmTimes() {
        return alarmTimes;
    }

    public void setAlarmTimes(List<AlarmTime> alarmTimes) {
        this.alarmTimes = alarmTimes;
    }
}