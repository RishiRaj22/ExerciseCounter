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

package me.itsrishi.exercisecounter.activities;

import com.stephentuso.welcome.BasicPage;
import com.stephentuso.welcome.WelcomeActivity;
import com.stephentuso.welcome.WelcomeConfiguration;

import me.itsrishi.exercisecounter.R;

/**
 * @author Rishi Raj
 */

public class MyWelcomeActivity extends WelcomeActivity {
    @Override
    protected WelcomeConfiguration configuration() {
        return new WelcomeConfiguration.Builder(this)
                .defaultBackgroundColor(R.color.colorPrimary)
                .page(new BasicPage(R.drawable.icon, "Welcome", "Make managing your workout sessions a breeze"))
                .page(new BasicPage(R.drawable.session_create_activity, "Adding sessions", "You add or edit your workout sessions by tapping the + button or the edit button next to session name"))
                .page(new BasicPage(R.drawable.exercise_create_activity, "Adding exercises", "Each session consists of multiple exercises"))
                .page(new BasicPage(R.drawable.alarm_activity, "Adding reminders", "You get notified in advance of the upcoming sessions"))
                .page(new BasicPage(R.drawable.stats_view, "Statistics", "See your progress visualised in beautiful graphs by pressing the graph icon"))
                .page(new BasicPage(R.drawable.playing_view_paused, "Controls", "When you are doing exercises, simply tap the circle to see your controls, swipe to pause, rewind and fast forward"))
                .canSkip(false)
                .build();
    }
}
