package com.derekjass.jacksonutilitysubmitter;

import java.util.Calendar;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;

public class SetAlarmReceiver extends BroadcastReceiver {

	private SharedPreferences mPrefs;
	private Context mContext;

	@TargetApi(Build.VERSION_CODES.KITKAT)
	@Override
	public void onReceive(Context context, Intent intent) {
		this.mContext = context;
		PreferenceManager.setDefaultValues(context, R.xml.preferences, false);
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
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
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
			mPrefs.edit().putLong(
					mContext.getString(R.string.pref_last_submit),
					System.currentTimeMillis()).commit();
		}

		Calendar c = Calendar.getInstance();
		if (lastSubmit != 0) {
			c.setTimeInMillis(lastSubmit);
		}

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
