package com.derekjass.jacksonutilitysubmitter;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

public class NotificationReceiver extends BroadcastReceiver {

    private static final int NOTIFICATION_ID = 1;

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context);
        builder.setSmallIcon(R.drawable.ic_stat_notify);
        builder.setContentTitle(context.getString(R.string.read_meters));
        builder.setContentText(context.getString(R.string.notification_body));
        builder.setDefaults(Notification.DEFAULT_ALL);
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(true);

        NotificationManager nManager =
                (NotificationManager) context.getSystemService(
                        Context.NOTIFICATION_SERVICE);
        nManager.notify(NOTIFICATION_ID, builder.build());

        context.sendBroadcast(new Intent(context, SetAlarmReceiver.class));
    }
}
