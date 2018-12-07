package com.car.carsquad.carapp;

import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import java.util.Map;

/**
 * Created by joshfreilich on 11/29/18.
 */

public class Notifier {

    private Context context;
    private NotificationManager manager;
    private static final String CHANNEL_ID = "notication_channel";
    private static final CharSequence CHANNEL_NAME = "Notication Channel";

    protected static final String MESSAGE = "MESSAGE";

    public Notifier(Context context) {
        this.context = context;
        this.createChannel();
    }

    public boolean createChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel notificationChannel =
                    new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance);
            NotificationManager notificationManager =
                    (NotificationManager) this.context.getSystemService(NotificationManager.class);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.BLUE);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long[]{100, 200, 100});
            notificationManager.createNotificationChannel(notificationChannel);
            manager = notificationManager;
            return true;
        }
        return false;
    }

    public boolean createGroup(String groupId, CharSequence groupName) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannelGroup(
                    new NotificationChannelGroup(groupId, groupName));
            return true;
        }
        return false;
    }

    public void createNotification(String text) {
        int notificationId = 69;
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this.context, CHANNEL_ID)
                .setSmallIcon(R.drawable.baseline_message_black_24dp)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        manager.notify(notificationId, builder.build());
    }

    public void addMessageNotification(Map<String, String> data) {
        String sender  = data.get("sender");
        String message = data.get("message");

        int notificationId = 69;
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.drawable.carlogo)
                        .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
                                R.drawable.baseline_person_black_24dp))
                        .setContentTitle(sender)
                        .setContentText(message)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        manager.notify(notificationId, builder.build());
    }


}
