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

/**
 * Model class for alarm time having time stored in 24 hour clock
 *
 * @author Rishi Raj
 */
public class AlarmTime implements Parcelable {
    public static final byte MONDAY = 1;
    public static final byte TUESDAY = 2;
    public static final byte WEDNESDAY = 4;
    public static final byte THURSDAY = 8;
    public static final byte FRIDAY = 16;
    public static final byte SATURDAY = 32;
    public static final byte SUNDAY = 64;
    @SuppressWarnings("unused")
    public static final Parcelable.Creator<AlarmTime> CREATOR = new Parcelable.Creator<AlarmTime>() {
        @Override
        public AlarmTime createFromParcel(Parcel in) {
            return new AlarmTime(in);
        }

        @Override
        public AlarmTime[] newArray(int size) {
            return new AlarmTime[size];
        }
    };
    private byte hours;
    private byte mins;
    private byte repeatDays;
    private boolean active;

    @SuppressWarnings("unused")
    public AlarmTime() {
    }

    public AlarmTime(byte mins,byte hours,byte repeatDays, boolean active) {
        this.mins = mins;
        this.hours = hours;
        this.repeatDays = repeatDays;
        this.active = active;
    }

    protected AlarmTime(Parcel in) {
        hours = in.readByte();
        mins = in.readByte();
        repeatDays = in.readByte();
        active = in.readByte() != 0x00;
    }

    public byte getHours() {
        return hours;
    }

    public void setHours(byte hours) {
        this.hours = hours;
    }

    public byte getMins() {
        return mins;
    }

    public void setMins(byte mins) {
        this.mins = mins;
    }

    public byte getRepeatDays() {
        return repeatDays;
    }

    public void setRepeatDays(byte repeatDays) {
        this.repeatDays = repeatDays;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(hours);
        dest.writeByte(mins);
        dest.writeByte(repeatDays);
        dest.writeByte((byte) (active ? 0x01 : 0x00));
    }
}
