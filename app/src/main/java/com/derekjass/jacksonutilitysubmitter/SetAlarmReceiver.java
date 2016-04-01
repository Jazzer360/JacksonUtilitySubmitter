package com.derekjass.jacksonutilitysubmitter;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;

import java.util.Calendar;

public class SetAlarmReceiver extends BroadcastReceiver {

    private SharedPreferences mPrefs;
    private Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.mContext = context;
        mPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        AlarmManager alarmManager =
                (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent i = new Intent(context, NotificationReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(
                context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);

        boolean notificationsEnabled = mPrefs.getBoolean(
                context.getString(R.string.pref_enable_notifications),
                true);

        if (notificationsEnabled) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP, getAlarmTime(), pi);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP, getAlarmTime(), pi);
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, getAlarmTime(), pi);
            }
        } else {
            alarmManager.cancel(pi);
        }
    }

    private long getAlarmTime() {
        long lastSubmit = mPrefs.getLong(
                mContext.getString(R.string.pref_last_submit),
                0);
        int notifTime = mPrefs.getInt(
                mContext.getString(R.string.pref_notification_time),
                720);

        if (lastSubmit == 0) {
            lastSubmit = System.currentTimeMillis();
            mPrefs.edit().putLong(
                    mContext.getString(R.string.pref_last_submit),
                    lastSubmit).apply();
        }

        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(lastSubmit);

        c.add(Calendar.MONTH, 1);

        c.set(Calendar.DAY_OF_MONTH, 1);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        c.add(Calendar.MINUTE, notifTime);

        while (c.getTimeInMillis() < System.currentTimeMillis()) {
            c.add(Calendar.DAY_OF_MONTH, 1);
        }

        return c.getTimeInMillis();
    }
}
