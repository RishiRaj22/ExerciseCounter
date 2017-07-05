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

package me.itsrishi.exercisecounter.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author Rishi Raj
 */

public class Exercise implements Parcelable {
    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Exercise> CREATOR = new Parcelable.Creator<Exercise>() {
        @Override
        public Exercise createFromParcel(Parcel in) {
            return new Exercise(in);
        }

        @Override
        public Exercise[] newArray(int size) {
            return new Exercise[size];
        }
    };
    private String name;
    private int turns;
    private float timePerTurn;
    private float gapBetweenTurns = 1;
    private int bodyPart;
    private boolean autoplay;

    public Exercise() {
        name = "Exercise";
        turns = 10;
        timePerTurn = 0.6f;
        autoplay = true;
    }

    public Exercise(String name, int turns, float timePerTurn, float gapBetweenTurns,
                    int bodyPart, boolean autoplay) {
        this.name = name;
        this.turns = turns;
        this.timePerTurn = timePerTurn;
        this.gapBetweenTurns = gapBetweenTurns;
        this.bodyPart = bodyPart;
        this.autoplay = autoplay;
    }


    public Exercise(String name, int turns, float timePerTurn, float gapBetweenTurns, int bodyPart) {
        this(name, turns, timePerTurn, gapBetweenTurns, bodyPart, true);
    }

    protected Exercise(Parcel in) {
        name = in.readString();
        turns = in.readInt();
        timePerTurn = in.readFloat();
        gapBetweenTurns = in.readFloat();
        bodyPart = in.readInt();
        autoplay = in.readByte() != 0x00;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTurns() {
        return turns;
    }

    public void setTurns(int turns) {
        this.turns = turns;
    }

    public float getTimePerTurn() {
        return timePerTurn;
    }

    public void setTimePerTurn(float timePerTurn) {
        this.timePerTurn = timePerTurn;
    }

    public float getGapBetweenTurns() {
        return gapBetweenTurns;
    }

    public void setGapBetweenTurns(float gapBetweenTurns) {
        this.gapBetweenTurns = gapBetweenTurns;
    }

    public int getBodyPart() {
        return bodyPart;
    }

    public void setBodyPart(int bodyPart) {
        this.bodyPart = bodyPart;
    }

    public boolean getAutoplay() {
        return autoplay;
    }

    public void setAutoplay(boolean autoplay) {
        this.autoplay = autoplay;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeInt(turns);
        dest.writeFloat(timePerTurn);
        dest.writeFloat(gapBetweenTurns);
        dest.writeInt(bodyPart);
        dest.writeByte((byte) (autoplay ? 0x01 : 0x00));
    }
}