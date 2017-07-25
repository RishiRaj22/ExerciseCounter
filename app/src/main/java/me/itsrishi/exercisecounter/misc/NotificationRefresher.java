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

package me.itsrishi.exercisecounter.misc;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

import me.itsrishi.exercisecounter.ExerciseActivity;
import me.itsrishi.exercisecounter.MainActivity;
import me.itsrishi.exercisecounter.R;
import me.itsrishi.exercisecounter.SettingsActivity;
import me.itsrishi.exercisecounter.models.AlarmTime;
import me.itsrishi.exercisecounter.models.Session;

import static android.content.Context.ALARM_SERVICE;
import static me.itsrishi.exercisecounter.misc.NotificationPublisher.ITERATION_COUNT;

/**
 * @author Rishi Raj
 */

public class NotificationRefresher extends BroadcastReceiver {
    private static final String TAG = "REFRESH";
    private Context context;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        expireOldNotifs();
        refreshNotifications();
        Intent nextIntent = new Intent(context, NotificationRefresher.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 5);
        long futureInMillis = calendar.getTimeInMillis() + 24 * 60 * 60 * 1000;
        //Change from currentTimeMillis to elapsedRealTime for alarmManager
        futureInMillis -= (System.currentTimeMillis() - SystemClock.elapsedRealtime());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, pi);
        } else alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, pi);
    }

    /**
     * Increment val so that if older notification is not cancelled,
     * as it got deleted or due to some unexpected situations, then the {@link NotificationPublisher}
     * will not show the notification.
     */
    private void expireOldNotifs() {
        SharedPreferences prefs = context.getSharedPreferences(SettingsActivity.PREFS,Context.MODE_PRIVATE);
        int val = prefs.getInt(NotificationPublisher.ITERATION_COUNT,0) + 1;
        prefs.edit().putInt(NotificationPublisher.ITERATION_COUNT,val).apply();
    }

    public void refreshNotifications() {
        ArrayList<Session> sessions = fetchSessionList();
        if (sessions == null)
            return;
        Calendar cal = Calendar.getInstance();
        int i = 0,j = 0;
        for (Session session : sessions) {
            ArrayList<AlarmTime> alarmTimes = session.getAlarmTimes();
            if(alarmTimes == null)
                continue;
            for (AlarmTime alarmTime : alarmTimes) {
                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                Log.d(TAG,"Canceled " + (i * 100 + j));
                notificationManager.cancel(i * 100 + j);
                byte val = (byte) (cal.get(Calendar.DAY_OF_WEEK) - 2);
                if (val < 0) val += 7;
                if (alarmTime.isActive() && (1 << val & alarmTime.getRepeatDays()) != 0) {
                    Calendar alarmCal = Calendar.getInstance();
                    alarmCal.set(Calendar.HOUR_OF_DAY, alarmTime.getHours());
                    alarmCal.set(Calendar.MINUTE, alarmTime.getMins());
                    alarmCal.set(Calendar.SECOND,0);
                    notifyFor(session, alarmCal, i * 100 + j);
                }
                j++;
            }
            i++;
        }
    }

    private void notifyFor(Session session, Calendar alarmCal,int id) {
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                .setContentTitle("Time for " + session.getName() + " session")
                .setContentText("Start your session now")
                .setSmallIcon(R.drawable.ic_notif)
                .setWhen(alarmCal.getTimeInMillis())
                .setPriority(Notification.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000})
                .setCategory(Notification.CATEGORY_EVENT)
                .setSound(RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_NOTIFICATION));
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntent(new Intent(context.getApplicationContext(), MainActivity.class));
        Intent intent = new Intent(context.getApplicationContext(), ExerciseActivity.class);
        intent.putExtra("session_exercise", session);
        stackBuilder.addNextIntent(intent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        id,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        notificationBuilder.setContentIntent(resultPendingIntent);
        Intent notificationIntent = new Intent(context, NotificationPublisher.class);
        int current = context.getSharedPreferences(SettingsActivity.PREFS, Context.MODE_PRIVATE)
                .getInt(ITERATION_COUNT, 0);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, id);
        notificationIntent.putExtra(NotificationPublisher.ITERATION_COUNT, current);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION, notificationBuilder.build());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, id, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        long timeBeforeNotif = SettingsActivity.getTimeBefNotif() * 60 * 1000;
        long delta = alarmCal.getTimeInMillis()
                - System.currentTimeMillis()
                - timeBeforeNotif;
        if (delta < - timeBeforeNotif) return; //If time has already passed then return
        if(delta < 0) delta = 0; //However, if time has not yet passed, show alarm immediately
        long futureInMillis = SystemClock.elapsedRealtime() + delta;
        Log.d(TAG, "Creating notification ID "+ id + " for session: " + session.getName()
                + " hour: " + alarmCal.get(Calendar.HOUR)
                + " minute: " + alarmCal.get(Calendar.MINUTE)
                + " iteration " + current
                + " with " + delta + "ms to go");
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, pendingIntent);
        } else
            alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, pendingIntent);
    }

    private ArrayList<Session> fetchSessionList() {
        ObjectMapper mapper = new ObjectMapper();
        ArrayList<Session> sessions;
        try {
            FileInputStream fileInputStream = context.openFileInput("sessions.json");
            sessions = mapper.readValue(fileInputStream, mapper.getTypeFactory().constructCollectionType(ArrayList.class, Session.class));
            fileInputStream.close();
        } catch (IOException ex) {
            ex.printStackTrace();
            sessions = new ArrayList<>();
        }
        return sessions;
    }
}
