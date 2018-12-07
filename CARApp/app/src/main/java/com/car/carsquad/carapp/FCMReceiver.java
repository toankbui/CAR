package com.car.carsquad.carapp;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;

public class FCMReceiver extends FirebaseMessagingService {
    private static final String TAG = "FCMReceiver";
    protected static final String SERVER_URL = "http://acsweb.ucsd.edu/~jdfreili/car/handlerequests.php";
    protected static final String MESSAGE_SERVER_URL = "http://acsweb.ucsd.edu/~jdfreili/car/notifications/messages.php";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        if(remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            boolean scheduleLater = false; // TODO: add some functionality
            if(scheduleLater) {
                scheduleJob();
            } else {
                handleNow(remoteMessage);
            }
        }

        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notifaction Body: " + remoteMessage.getNotification().getBody());
        }
    }

    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);
//        sendRegistrationToServer(token);
    }

    private void scheduleJob() {
        // TODO scheduler?
    }

    private void handleNow(RemoteMessage remoteMessage) {
        Map<String, String> data = remoteMessage.getData();
        String notification_type = data.get("type");
        Notifier notify = new Notifier(getApplicationContext());
        switch(notification_type) {
            case Notifier .MESSAGE:
                Log.d(TAG,"notification message: " + data.get("message"));
                notify.addMessageNotification(data);
                break;
        }

    }

    private void sendRegistrationToServer(String token) {
        ServerUploader uploader = new ServerUploader(getApplicationContext());
        HashMap<String, String> hm = new HashMap<>();
        hm.put("token", token);
        uploader.addRequest(SERVER_URL, hm);
    }

}
