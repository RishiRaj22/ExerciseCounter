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

package me.itsrishi.exercisecounter.views;

import android.app.DialogFragment;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatCheckBox;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TimePicker;

import me.itsrishi.exercisecounter.R;
import me.itsrishi.exercisecounter.listeners.AlarmDialogStateListener;
import me.itsrishi.exercisecounter.models.AlarmTime;

/**
 * @author Rishi Raj
 */

public class AlarmDialogFragment extends DialogFragment implements CompoundButton.OnCheckedChangeListener {
    public static final int NEW_INDEX = -1;
    AlarmTime alarm;
    static AlarmDialogStateListener listener;
    int index;
    TimePicker timePicker;
    AppCompatCheckBox days[] = new AppCompatCheckBox[7];
    AppCompatButton clearAll, markAll, submit, cancel;

    /**
     * Get an instance of AlarmDialogFragment
     *
     * @param alarm
     * @param index Index of the alarmTime if it exists or -1 if it doesnot
     * @return
     */
    public static AlarmDialogFragment getInstance(AlarmDialogStateListener listener,AlarmTime alarm, int index) {
        AlarmDialogFragment.listener = listener;
        AlarmDialogFragment fragment = new AlarmDialogFragment();
        Bundle args = new Bundle();
        args.putParcelable("alarm", alarm);
        args.putInt("index", index);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        alarm = getArguments().getParcelable("alarm");
        index = getArguments().getInt("index");
        View view = inflater.inflate(R.layout.alarm_create, container);
        initTimePicker(view);
        initDaysList(view);
        initButtons(view);
        return view;
    }

    private void initTimePicker(View view) {
        timePicker = (TimePicker) view.findViewById(R.id.time);
        setHour(alarm.getHours());
        setMinute(alarm.getMins());
    }

    private void initDaysList(View view) {
        days[0] = (AppCompatCheckBox) view.findViewById(R.id.checkbox_monday);
        days[1] = (AppCompatCheckBox) view.findViewById(R.id.checkbox_tuesday);
        days[2] = (AppCompatCheckBox) view.findViewById(R.id.checkbox_wednesday);
        days[3] = (AppCompatCheckBox) view.findViewById(R.id.checkbox_thursday);
        days[4] = (AppCompatCheckBox) view.findViewById(R.id.checkbox_friday);
        days[5] = (AppCompatCheckBox) view.findViewById(R.id.checkbox_saturday);
        days[6] = (AppCompatCheckBox) view.findViewById(R.id.checkbox_sunday);
        Log.d("day", "Alarm Repeat days: " + alarm.getRepeatDays());
        for (int i = 0; i < 7; i++) {
            int val = 1 << i;
            Log.d("day", "Val: " + val);
            if ((val & alarm.getRepeatDays()) == 0)
                days[i].setChecked(false);
            days[i].setOnCheckedChangeListener(this);
        }
    }

    private void initButtons(View view) {
        clearAll = (AppCompatButton) view.findViewById(R.id.days_clear);
        markAll = (AppCompatButton) view.findViewById(R.id.days_mark);
        submit = (AppCompatButton) view.findViewById(R.id.submit_button);
        cancel = (AppCompatButton) view.findViewById(R.id.cancel_button);
        clearAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < 7; i++) {
                    days[i].setChecked(false);
                }
            }
        });
        markAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < 7; i++) {
                    days[i].setChecked(true);
                }
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onCancel();
                dismiss();
            }
        });
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alarm.setHours(getHour());
                alarm.setMins(getMinute());
                dismiss();
                listener.onSubmit(index, alarm);
            }
        });
    }

    private byte getHour() {
        int hours;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            hours = timePicker.getHour();
        } else hours = timePicker.getCurrentHour();
        return (byte) hours;
    }

    private void setHour(byte hours) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            timePicker.setHour(hours);
        } else timePicker.setCurrentHour((int) hours);
    }

    private byte getMinute() {
        int mins;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mins = timePicker.getMinute();
        } else mins = timePicker.getCurrentMinute();
        return (byte) mins;
    }

    private void setMinute(byte minutes) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            timePicker.setMinute(minutes);
        } else timePicker.setCurrentMinute((int) minutes);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.checkbox_monday:
                setRepeatDays(isChecked, AlarmTime.MONDAY);
                break;
            case R.id.checkbox_tuesday:
                setRepeatDays(isChecked, AlarmTime.TUESDAY);
                break;
            case R.id.checkbox_wednesday:
                setRepeatDays(isChecked, AlarmTime.WEDNESDAY);
                break;
            case R.id.checkbox_thursday:
                setRepeatDays(isChecked, AlarmTime.THURSDAY);
                break;
            case R.id.checkbox_friday:
                setRepeatDays(isChecked, AlarmTime.FRIDAY);
                break;
            case R.id.checkbox_saturday:
                setRepeatDays(isChecked, AlarmTime.SATURDAY);
                break;
            case R.id.checkbox_sunday:
                setRepeatDays(isChecked, AlarmTime.SUNDAY);
                break;
            default:
                Log.d("TAG", "Checkbox click for checkbox having text " + buttonView.getText() + " not set");
        }
    }

    private void setRepeatDays(boolean isChecked, byte day) {
        if (isChecked) {
            alarm.setRepeatDays((byte) (alarm.getRepeatDays() | day));
        } else {
            int val = alarm.getRepeatDays() | day;
            if (val != 0)
                alarm.setRepeatDays((byte) (alarm.getRepeatDays() - day));
        }
    }
}
