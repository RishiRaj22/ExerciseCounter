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

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import me.itsrishi.exercisecounter.R;
import me.itsrishi.exercisecounter.listeners.RecyclerViewCheckedListener;
import me.itsrishi.exercisecounter.listeners.RecyclerViewClickListener;
import me.itsrishi.exercisecounter.models.AlarmTime;

/**
 * @author Rishi Raj
 */

public class AlarmAdapter extends RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder> {
    private RecyclerViewClickListener clickListener;
    private RecyclerViewCheckedListener checkListener;
    private List<AlarmTime> alarmTimes;

    public AlarmAdapter(List<AlarmTime> alarmTimes, RecyclerViewClickListener clickListener, RecyclerViewCheckedListener checkListener) {
        this.alarmTimes = alarmTimes;
        this.clickListener = clickListener;
        this.checkListener = checkListener;
    }

    @Override
    public AlarmViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.alarm_layout, parent, false);
        return new AlarmViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AlarmViewHolder holder, int position) {
        position = holder.getAdapterPosition();
        AlarmTime alarmTime = alarmTimes.get(position);
        for (int i = 0; i < 7; i++) {
            TextView textView = holder.textViews[i];
            int val = 1 << i;
            if ((val & alarmTime.getRepeatDays()) == 0)
                textView.setVisibility(View.GONE);
            else textView.setVisibility(View.VISIBLE);
        }
        int hours = alarmTime.getHours();
        int mins = alarmTime.getMins();
        boolean am = hours / 12 == 0;
        hours %= 12;
        holder.currTime.setText(String.format(Locale.ENGLISH, "%02d:%02d", hours, mins));
        holder.amOrPm.setText(am ? "AM" : "PM");
        holder.active.setChecked(alarmTime.isActive());
        holder.position = position;
    }

    @Override
    public int getItemCount() {
        return alarmTimes.size();
    }

    public List<AlarmTime> getAlarmTimes() {
        return alarmTimes;
    }

    public void setAlarmTimes(List<AlarmTime> alarmTimes) {
        this.alarmTimes = alarmTimes;
    }

    class AlarmViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindViews({R.id.mon, R.id.tue, R.id.wed, R.id.thu, R.id.fri, R.id.sat, R.id.sun})
        TextView[] textViews;
        @BindView(R.id.set_time)
        TextView currTime;
        @BindView(R.id.set_am_or_pm)
        TextView amOrPm;
        @BindView(R.id.alarm_active)
        CheckBox active;
        @BindView(R.id.alarm_card_container)
        CardView container;
        int position;

        AlarmViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            container.setOnClickListener(this);
        }

        @OnCheckedChanged(R.id.alarm_active)
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            checkListener.onChecked(position, isChecked);
        }

        @Override
        public void onClick(View v) {
            Log.d("Alarm Adapter", "onClick: " + v);
            clickListener.onClick(position, v);
        }
    }
}
