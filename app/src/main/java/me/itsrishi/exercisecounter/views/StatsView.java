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

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.view.View;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * @author Rishi Raj
 */

public class StatsView extends View implements OnScaleGestureListener, GestureDetector.OnGestureListener {
    public static final int DAY_VIEW = 0;
    public static final int WEEK_VIEW = 1;
    public static final int MONTH_VIEW = 2;
    public static final int GRAPH_PLOTS = 7;
    private static final int MAX_POINTS = 6;
    private static final String TAG = "STATS_VIEW";
    private static final float MARGIN_RATIO = 0.1f;
    private static final int VISIBLE_LABELS = 3;
    Calendar lastDay;
    int[] dayValues;
    int[] weekValues;
    int currentView;
    private int bgColor = Color.BLACK;
    private int accentColor = Color.RED;
    private int lineColor = Color.WHITE;
    private float drawingHeight;
    // Calculated values to draw graph
    private int max;
    private int dataPoints;
    private Paint paint;
    private float space;
    private float marginHeight;
    private float marginWidth;
    private int labelGap = 3;
    //State variables
    private float cameraX;
    private float zoom = 1;
    private float velocity;
    private int focussed = -1;
    private float WIDTH_MARGIN_RATIO = 0.1f;
    private boolean init = false;
    private float focusX;

    public StatsView(Context context) {
        super(context);
    }

    public StatsView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    /**
     * The values from the activity/fragment is added to the view from here
     * @param dayValues The values of array containing sessions completed per day
     * @param startDay The day the first session was performed
     * @param currentView The currentView, which is either DAY_VIEW or WEEK_VIEW
     */
    public void addValues(int[] dayValues, Calendar startDay, int currentView) {
        if(dayValues == null) {
            init = false;
            postInvalidate();
            return;
        }
        if(this.dayValues != dayValues)
            init = false;
        this.dayValues = dayValues;
        this.lastDay = startDay;
        init();
        setCurrentView(currentView);
    }

    /**
     * All the initialisation work is done here
     */
    private void init() {
        initTouch();
        paint = new Paint();
        paint.setColor(Color.WHITE);
        initWeekValues();
        Log.d(TAG, "Initialised");
        setWillNotDraw(false);
        init = true;
    }

    /**
     * This method calculates and stores sessions completed in week(starting from Sunday)
     * in an array that can be used to display the graph
     */
    private void initWeekValues() {
        Calendar startDay = (Calendar) lastDay.clone();
        startDay.add(Calendar.DATE, -dayValues.length + 1);
        int beginDay = startDay.get(Calendar.DAY_OF_WEEK);
        int firstWeekDays = 8 - beginDay;
        int weeks = (int) Math.ceil((double)(dayValues.length - firstWeekDays)/7) + 1;
        int count = 0;
        weekValues = new int[weeks];
        for (int i = 0; i < firstWeekDays && count < dayValues.length; i++) {
            weekValues[0] += dayValues[count];
            count++;
        }
        for (int i = 1; count < dayValues.length; i++) {
            for (int j = 0; j < 7 && count < dayValues.length; j++) {
                weekValues[i] += dayValues[count];
                count++;
            }
        }
    }

    /**
     * Initialises all the touch listeners
     */
    private void initTouch() {
        final ScaleGestureDetector scaleGestureDetector = new ScaleGestureDetector(this.getContext(), this);
        final GestureDetector gestureDetector = new GestureDetector(this.getContext(), this);
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int val = (int) ((event.getX() + cameraX) / (space * zoom) + 0.5f);
                if (val < dataPoints) {
                    focussed = val;
                } else focussed = -1;
                postInvalidate();
                scaleGestureDetector.onTouchEvent(event);
                gestureDetector.onTouchEvent(event);
                return true;
            }
        });
    }

    /**
     * Used to change view from DAY_VIEW to WEEK_VIEW and vice versa
     * @param viewToSet The view to be set
     */
    public void setCurrentView(int viewToSet) {
        if(dayValues == null)
            return;
        this.currentView = viewToSet;
        max = 0;
        switch (viewToSet) {
            case DAY_VIEW:
                for (int b : dayValues) {
                    if (b > max)
                        max = b;
                }
                break;
            case WEEK_VIEW:
                for (int b : weekValues) {
                    if (b > max)
                        max = b;
                }
                break;
            default:
                Log.e(TAG, "Max calculation for required view " + viewToSet + " not implemented yet");
        }
        calculateDataPoints();
        calculateSizes();
        invalidate();
    }

    /**
     * Used to set value of dataPoints when changing currentView
     */
    private void calculateDataPoints() {
        switch (currentView) {
            case DAY_VIEW:
                dataPoints = dayValues.length;
                break;
            case WEEK_VIEW:
                dataPoints = weekValues.length;
                break;
            default:
                Log.e(TAG,"Data point calculation not implemented yet");
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        clearScreen(canvas);
        if (!init) {
            postInvalidateDelayed(50);
            Log.e(TAG, "Data not set");
            return;
        }
        drawGraph(canvas);
    }

    private void clearScreen(Canvas canvas) {
        if(paint == null)
            paint = new Paint();
        int prevColor = paint.getColor();
        paint.setColor(bgColor);
        canvas.drawRect(0, 0, getRight(), getBottom(), paint);
        paint.setColor(prevColor);
    }

    private void drawGraph(Canvas canvas) {
        Log.d(TAG, "CAMERA X" + cameraX);
        Log.d(TAG, "CAMERA ZOOM" + zoom);
        if (cameraX < -marginWidth)
            cameraX = -marginWidth;
        if (zoom < 0.2f)
            zoom = 0.2f;
        if (zoom > 3)
            zoom = 3;
        if (cameraX > space * zoom * dataPoints - getWidth())
            cameraX = space * zoom * dataPoints - getWidth();
        labelGap = (int) (getWidth() / ((VISIBLE_LABELS - 1) * space * zoom));
        if (labelGap <= 0)
            labelGap = 1;
        drawMarkings(canvas);
        int start = (int) (cameraX / (space * zoom) - 1);
        int num = (int) (getWidth() / (space * zoom) + 2);
        for (int i = start; i <= start + num && i < dataPoints; i++) {
            if (i < 0) continue;
            drawLineFrom(canvas, i);
            drawPoint(canvas, i);
        }
    }

    private void drawMarkings(Canvas canvas) {
        int prevColor = paint.getColor();
        paint.setColor(Color.DKGRAY);
        paint.setTextSize(marginWidth / 1.2f);
        if (max > GRAPH_PLOTS) {
            for (int i = 0; i <= GRAPH_PLOTS; i++) {
                float ratio = (float) (max * i / GRAPH_PLOTS) / max;
                float y = marginHeight + drawingHeight * ratio;
                canvas.drawLine(0, y, getWidth(), y, paint);
                canvas.drawText((int) (max - ratio * max) + "", 0, y, paint);
            }
        } else {
            for (int i = 0; i <= max; i++) {
                float y = marginHeight + drawingHeight * i / max;
                canvas.drawLine(0, y, getWidth(), y, paint);
                canvas.drawText(max - i + "", 0, y, paint);
            }
        }
        paint.setColor(prevColor);
    }

    private void drawLineFrom(Canvas canvas, int pos) {
        int prevColor = paint.getColor();
        float startX = space * zoom * (pos) - cameraX;
        float endX = space * zoom * (pos + 1) - cameraX;
        float startY = 0, endY = 0;
        switch (currentView) {
            case DAY_VIEW:
                if (pos + 1 >= dataPoints) {
                    Log.d(TAG, "Line not drawn from last index");
                    return;
                }
                startY = dayValues[pos] * drawingHeight / max;
                endY = dayValues[pos + 1] * drawingHeight / max;
                break;
            case WEEK_VIEW:
                if (pos + 1 >= dataPoints) {
                    Log.d(TAG, "Line not drawn from last index");
                    return;
                }
                startY = weekValues[pos] * drawingHeight / max;
                endY = weekValues[pos + 1] * drawingHeight / max;
                break;
            default:
                Log.e(TAG, "Draw line for required view " + currentView + "not implemented yet");
        }
        paint.setColor(lineColor);
        canvas.drawLine(startX, getHeight() - marginHeight - startY, endX, getHeight() - marginHeight - endY, paint);
        paint.setColor(prevColor);
    }


    private void drawPoint(Canvas canvas, int pos) {
        int prevColor = paint.getColor();
        paint.setColor(accentColor);
        float x, y;
        x = space * zoom * (pos) - cameraX;
        y = 0;
        switch (currentView) {
            case DAY_VIEW:
                y = dayValues[pos] * drawingHeight / max;
                break;
            case WEEK_VIEW:
                y = weekValues[pos] * drawingHeight / max;
                break;
            default:
                Log.e(TAG, "Draw point for required view " + currentView + "not implemented yet");
        }
        canvas.drawCircle(x, getHeight() - marginHeight - y, marginHeight / (3 / zoom), paint);

        if (pos % labelGap == 0) {
            calculateFontSize(false);
            String txt = getDateTextForPosition(pos, false);
            canvas.drawText(txt, x - paint.measureText(txt) / 2, getHeight(), paint);
        }
        if (focussed == pos) {
            calculateFontSize(true);
            String txt = getDateTextForPosition(pos, true);
            canvas.drawText(txt, (getWidth() - paint.measureText(txt)) / 2, marginHeight, paint);
        }
        paint.setColor(prevColor);
    }

    private void calculateFontSize(boolean isOverlay) {
        paint.setTextSize(40);
        String txt = null;
        switch (currentView) {
            case DAY_VIEW:
                txt = "20/07";
                if (isOverlay)
                    txt = "20/";
                break;
            case WEEK_VIEW:
                txt = "20/07 -> 26/07";
                if (isOverlay)
                    txt = "20/07 -"; /*If val is added, then it is the main display text,
                     whose size should be bigger*/
                break;
            default:
                Log.e(TAG, "Calculate font size not implemented for view " + currentView);
        }
        paint.setTextSize(40 * (space * zoom * (labelGap * 0.6f)) / paint.measureText(txt));
    }

    /**
     * Gets the text which can be used to mark a date
     * @param pos The position for which the text is to be found out
     * @param withVal If the value at that position is to be added at the end of the date
     * @return
     */
    @NonNull
    private String getDateTextForPosition(int pos, boolean withVal) {
        Calendar cal, cal2;
        SimpleDateFormat dateFormat;
        String ret;
        int val;
        dateFormat = new SimpleDateFormat("dd/MM");
        switch (currentView) {
            case DAY_VIEW:
                val = dayValues[pos];
                cal = (Calendar) lastDay.clone();
                cal.add(Calendar.DATE, -(dataPoints - pos - 1));
                ret = dateFormat.format(cal.getTime());
                break;
            case WEEK_VIEW:
                val = weekValues[pos];
                cal = (Calendar) lastDay.clone();
                cal.add(Calendar.DATE, -dayValues.length + 1);
                int daysLeftInWeek = 7 - cal.get(Calendar.DAY_OF_WEEK);
                cal2 = (Calendar) cal.clone();
                cal2.add(Calendar.DATE, daysLeftInWeek);
                if (pos != 0) {
                    cal = (Calendar) cal2.clone();
                    cal.add(Calendar.DATE, (pos - 1) * 7 + 1);
                    cal2.add(Calendar.DATE, pos * 7);
                }
                ret = dateFormat.format(cal.getTime()) + " -> " +
                        dateFormat.format(cal2.getTime());
                break;
            default:
                Log.e(TAG, "Text corresponding to graph touch event not implemented yet");
                return null;
        }
        if (withVal)
            ret += " : " + val;
        Log.d(TAG, "LABEL GAP:" + labelGap);
        Log.d(TAG, "TEXT SIZE:" + paint.getTextSize());
        paint.setColor(Color.GRAY);
        return ret;
    }

    public int getCurrentView() {
        return currentView;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        calculateSizes();
    }

    private void calculateSizes() {
        marginHeight = getHeight() * MARGIN_RATIO;
        int visiblePoints;
        if (dataPoints < MAX_POINTS)
            visiblePoints = dataPoints;
        else visiblePoints = MAX_POINTS;
        space = getWidth() / (visiblePoints + 1);
        drawingHeight = getHeight() - 2 * marginHeight;
        marginWidth = getWidth() * WIDTH_MARGIN_RATIO;
        cameraX = space * zoom * (dataPoints - 1) - getWidth()/2;
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
        Log.d(TAG, "onScale" + detector.getScaleFactor());
        zoom *= detector.getScaleFactor();
        focusX *= detector.getScaleFactor();
        cameraX += focusX * (detector.getScaleFactor() - 1);
        postInvalidate();
        return true;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        Log.d(TAG, "onScaleBegin" + detector.getScaleFactor());
        zoom *= detector.getScaleFactor();
        focusX = detector.getFocusX() + cameraX;
        cameraX += focusX * (detector.getScaleFactor() - 1);
        Log.d(TAG,"FOCUS X : "+ focusX);
        postInvalidate();
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {
        zoom *= detector.getScaleFactor();
        postInvalidate();
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        cameraX += distanceX;
        postInvalidate();
        Log.d(TAG, "DX:" + distanceX);
        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, final float velocityX, float velocityY) {
        Log.d(TAG, "OnFling " + velocityX);
        this.velocity = velocityX;
        cameraX -= velocity / 25;
        invalidate();
        postDelayed(new Fling(), 30);
        return true;
    }

    class Fling implements Runnable {
        @Override
        public void run() {
            cameraX -= velocity / 25;
            velocity /= 1.15f;
            invalidate();
            if (Math.abs(velocity) > 0.25f)
                postDelayed(new Fling(), 30);
        }
    }
}
