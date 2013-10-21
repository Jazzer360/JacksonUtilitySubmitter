package com.derekjass.jacksonutilitysubmitter;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class SetAlarmReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		AlarmManager alarmManager =
				(AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

		Intent i = new Intent(context, NotificationReceiver.class);
		PendingIntent pi = PendingIntent.getBroadcast(
				context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);

		Calendar c = Calendar.getInstance();
		c.set(Calendar.DAY_OF_MONTH, 1);
		c.set(Calendar.HOUR_OF_DAY, 12);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		while (c.getTimeInMillis() < System.currentTimeMillis()) {
			c.add(Calendar.MONTH, 1);
		}

		alarmManager.set(AlarmManager.RTC, c.getTimeInMillis(), pi);
	}
}
