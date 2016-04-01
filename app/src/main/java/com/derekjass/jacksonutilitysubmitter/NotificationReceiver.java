package com.derekjass.jacksonutilitysubmitter;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;

public class NotificationReceiver extends BroadcastReceiver {

    private static final int NOTIFICATION_ID = 1;

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String uriString = prefs.getString(context.getString(R.string.pref_ringtone),
                "content://settings/system/notification_sound");
        Uri ringtone = Uri.parse(uriString);

        Notification.Builder builder = new Notification.Builder(context);
        builder.setSmallIcon(R.drawable.ic_stat_notify);
        builder.setContentTitle(context.getString(R.string.read_meters));
        builder.setContentText(context.getString(R.string.notification_body));
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(true);
        builder.setSound(ringtone);

        NotificationManager nManager =
                (NotificationManager) context.getSystemService(
                        Context.NOTIFICATION_SERVICE);
        //noinspection deprecation
        nManager.notify(NOTIFICATION_ID, builder.getNotification());

        context.sendBroadcast(new Intent(context, SetAlarmReceiver.class));
    }
}
