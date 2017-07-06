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

package me.itsrishi.exercisecounter.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * @author Rishi Raj
 */

public class Session implements Parcelable {
    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Session> CREATOR = new Parcelable.Creator<Session>() {
        @Override
        public Session createFromParcel(Parcel in) {
            return new Session(in);
        }

        @Override
        public Session[] newArray(int size) {
            return new Session[size];
        }
    };
    private String name;
    private int gapBetweenExercises;
    private ArrayList<Exercise> exercises;
    private ArrayList<AlarmTime> alarmTimes;

    public Session(String name, int gapBetweenExercises, ArrayList<Exercise> exercises, ArrayList<AlarmTime> alarmTimes) {
        this.name = name;
        this.gapBetweenExercises = gapBetweenExercises;
        this.exercises = exercises;
        this.alarmTimes = alarmTimes;
    }

    public Session(String name, int gapBetweenExercises, ArrayList<Exercise> exercises) {
        this(name, gapBetweenExercises, exercises, null);
    }

    public Session(Parcel in) {
        name = in.readString();
        gapBetweenExercises = in.readInt();

        exercises = null;
        if (in.readByte() == 0x01) {
            exercises = new ArrayList<>();
            in.readList(exercises, Exercise.class.getClassLoader());
        }

        alarmTimes = null;
        if (in.readByte() == 0x01) {
            in.readList(alarmTimes, AlarmTime.class.getClassLoader());
        }
    }

    public Session() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getGapBetweenExercises() {
        return gapBetweenExercises;
    }

    public void setGapBetweenExercises(int gapBetweenExercises) {
        this.gapBetweenExercises = gapBetweenExercises;
    }

    public ArrayList<Exercise> getExercises() {
        return exercises;
    }

    public void setExercises(ArrayList<Exercise> exercises) {
        this.exercises = exercises;
    }

    public ArrayList<AlarmTime> getAlarmTimes() {
        return alarmTimes;
    }

    public void setAlarmTimes(ArrayList<AlarmTime> alarmTimes) {
        this.alarmTimes = alarmTimes;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeString(name);
        dest.writeInt(gapBetweenExercises);
        if (exercises == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(exercises);
        }
        if (alarmTimes == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) 0x01);
            dest.writeValue(alarmTimes);
        }
    }

}
