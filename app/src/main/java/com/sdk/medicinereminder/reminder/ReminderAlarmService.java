package com.sdk.medicinereminder.reminder;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;

import com.sdk.medicinereminder.AddReminderActivity;
import com.sdk.medicinereminder.R;
import com.sdk.medicinereminder.data.AlarmReminderContract;


public class ReminderAlarmService extends IntentService {
    private static final String TAG = ReminderAlarmService.class.getSimpleName();

    private static final int NOTIFICATION_ID = 42;

    Cursor cursor;
    //This is a deep link intent, and needs the task stack
    public static PendingIntent getReminderPendingIntent(Context context, Uri uri) {
        Intent action = new Intent(context, ReminderAlarmService.class);
        action.setData(uri);
        return PendingIntent.getService(context, 0, action, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public ReminderAlarmService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Uri uri = intent.getData();

        Notification.Builder builder = new Notification.Builder(this);

        //Display a notification to view the task details
        Intent action = new Intent(this, AddReminderActivity.class);
        action.setData(uri);
        PendingIntent operation = TaskStackBuilder.create(this)
                .addNextIntentWithParentStack(action)
                .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        //Grab the task description
        if(uri != null){
            cursor = getContentResolver().query(uri, null, null, null, null);
        }

        String description = "";
        try {
            if (cursor != null && cursor.moveToFirst()) {
                description = AlarmReminderContract.getColumnString(cursor, AlarmReminderContract.AlarmReminderEntry.KEY_TITLE);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }


        android.app.TaskStackBuilder stackBuilder = android.app.TaskStackBuilder.create(this);
        stackBuilder.addNextIntentWithParentStack(action);

        PendingIntent notifyPendingIntent = stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);

        //add properties to the builder
        builder.setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getApplicationContext().getResources(),
                        R.mipmap.ic_launcher))
                .setContentTitle(description)
                .setContentText(description)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setColor(getResources().getColor(R.color.colorPrimary))
                .setVibrate(new long[]{100, 200, 300, 400, 500})
                .setAutoCancel(true)
                .setContentIntent(operation)
                //.setSubText(message)
                .setOnlyAlertOnce(true);

        builder.setContentIntent(notifyPendingIntent);

//        Notification note = new NotificationCompat.Builder(this, "C1") //Added String channeld to constructor arguments and create notification channel
//                .setContentTitle(getString(R.string.reminder_title))
//                .setContentText(description)
//                .setSmallIcon(R.drawable.ic_add_alert_black_24dp)
//                .setContentIntent(operation)
//                .setAutoCancel(true)
//                .build();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("channel1", "Reminder", NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Reminder");
            channel.setShowBadge(true);
            channel.canShowBadge();
            channel.enableLights(true);
            channel.setLightColor(Color.RED);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500});
            manager.createNotificationChannel(channel);

            builder.setChannelId("channel1");
        }

        manager.notify(NOTIFICATION_ID, builder.build());
    }
}