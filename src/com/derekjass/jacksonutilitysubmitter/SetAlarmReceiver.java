package com.derekjass.jacksonutilitysubmitter;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class SetAlarmReceiver extends BroadcastReceiver {

	SharedPreferences prefs;

	@Override
	public void onReceive(Context context, Intent intent) {
		prefs = context.getSharedPreferences(
				PrefKeys.FILE, Context.MODE_PRIVATE);

		AlarmManager alarmManager =
				(AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

		Intent i = new Intent(context, NotificationReceiver.class);
		PendingIntent pi = PendingIntent.getBroadcast(
				context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);

		alarmManager.set(AlarmManager.RTC, getAlarmTime(), pi);
	}

	private long getAlarmTime() {
		long lastSubmit = prefs.getLong(PrefKeys.LAST_SUBMIT, 0);

		Calendar c = Calendar.getInstance();
		if (lastSubmit != 0) {
			c.setTimeInMillis(lastSubmit);
		}

		c.add(Calendar.MONTH, 1);

		c.set(Calendar.DAY_OF_MONTH, 1);
		c.set(Calendar.HOUR_OF_DAY, 12);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);

		while (c.getTimeInMillis() < System.currentTimeMillis()) {
			c.add(Calendar.DAY_OF_MONTH, 1);
		}

		return c.getTimeInMillis();
	}
}
