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

package me.itsrishi.exercisecounter;

import android.support.test.espresso.Espresso;
import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.assertion.ViewAssertions;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Random;

import me.itsrishi.exercisecounter.activities.MainActivity;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.swipeRight;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.matcher.ViewMatchers.isChecked;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;


/**
 * @author Rishi Raj
 */
@RunWith(AndroidJUnit4.class)
public class SessionInstrumentedTest {
    private static final int SIZE = 3; // Keep it single digit
    private static final String SESSION = "Session #";
    private static final String EXERCISE = "Ex #";

    @Rule
    public ActivityTestRule<MainActivity> sessionCreateActivityTestRule = new ActivityTestRule<>(MainActivity.class, false, true);

    @Test
    public void populateWithRandomExercises() throws Exception {
        for (int index = 0; index < SIZE; index++) {
            int sessionGap = (int) (Math.abs(Math.sin(index) * 10));
            String sessionName = SESSION + index;
            onView(withId(R.id.session_plus_fab)).perform(click());
            onView(withId(R.id.session_name_set)).perform(typeText(sessionName));
            onView(withId(R.id.session_gap_set)).perform(typeText("" + sessionGap));

            for (int j = 0; j < 3; j++) {
                Random random = new Random();
                String name = EXERCISE + j;
                int turns = random.nextInt(15) + 1;
                float tpt = random.nextFloat() * 8 + 1;
                float gbt = random.nextFloat() * 4 + 1;
                boolean autoplay = random.nextBoolean();
                tpt = (float) (Math.round(tpt * 100.0) / 100.0);
                gbt = (float) (Math.round(gbt * 100.0) / 100.0);
                onView(withId(R.id.exercise_plus_fab)).perform(click());
                onView(withId(R.id.exercise_name)).perform(typeText(name));
                onView(withId(R.id.num_turns)).perform(typeText("" + turns));
                onView(withId(R.id.time_per_turn)).perform(typeText("" + tpt));
                onView(withId(R.id.gap_between_turns)).perform(typeText("" + gbt));
                if (!autoplay)
                    onView(withId(R.id.autoplay)).perform(ViewActions.click());
                onView(withId(R.id.exercise_submit)).perform(click());

                onView(withId(R.id.session_gap_set)).check(ViewAssertions.matches(withText("" + sessionGap)));
                onView(withId(R.id.session_name_set)).check(ViewAssertions.matches(withText(sessionName)));

                //Check whether the exercise is added in displayed sessions
                onView(withId(R.id.exercise_edit_list))
                        .perform(RecyclerViewActions.actionOnItemAtPosition(j, click()));
                if (autoplay)
                    onView(withId(R.id.autoplay)).check(ViewAssertions.matches(isChecked()));
                else onView(withId(R.id.autoplay)).check(ViewAssertions.matches(not(isChecked())));
                onView(withId(R.id.exercise_name)).check(ViewAssertions.matches(withText(name)));
                onView(withId(R.id.num_turns)).check(ViewAssertions.matches(withText(Matchers.startsWith("" + turns))));
                onView(withId(R.id.time_per_turn)).check(ViewAssertions.matches(withText(Matchers.startsWith("" + tpt))));
                onView(withId(R.id.gap_between_turns)).check(ViewAssertions.matches(withText(Matchers.startsWith("" + gbt))));

                Espresso.closeSoftKeyboard();
                Espresso.pressBack();
                onView(withText("yes")).perform(click());
            }
            onView(withId(R.id.session_submit)).perform(click());
        }
        onView(withId(R.id.sessions_list)).check(RecyclerViewItemCountAssertion.withItemCount(SIZE));
    }

    @Test
    public void deleteAlExercises() throws Exception {
        onView(withId(R.id.sessions_list)).perform(RecyclerViewActions.actionOnItemAtPosition(0, swipeRight()));
        onView(withText(R.string.yes)).perform(click());
        onView(withId(R.id.sessions_list)).check(RecyclerViewItemCountAssertion.withItemCount(SIZE - 1));

        onView(withId(R.id.sessions_list)).perform(RecyclerViewActions.actionOnItemAtPosition(1, swipeRight()));
        onView(withText(R.string.no)).perform(click());
        onView(withId(R.id.sessions_list)).check(RecyclerViewItemCountAssertion.withItemCount(SIZE - 1));
    }

}
