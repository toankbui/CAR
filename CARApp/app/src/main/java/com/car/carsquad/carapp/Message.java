package com.car.carsquad.carapp;

import android.content.Context;

import java.sql.Timestamp;
import java.util.HashMap;

/**
 * Created by joshfreilich on 12/3/18.
 */

public class Message {

    private static final String NOTIFICATION_ID = "MESSAGE";

    private String sender, recipToken, message;
    private User senderUser;
    private String timestamp;

    public Message(String sender, String recipient, String message) {
        this.sender = sender;
        this.recipToken = recipient;
        this.message = message;

    }

    public static String getNotificationId() {
        return NOTIFICATION_ID;
    }

    public String getSender() {
        return sender;
    }

    public String getRecipToken() {
        return recipToken;
    }

    public String getMessage() {
        return message;
    }

    public User getSenderUser() {
        return senderUser;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public Message(User sender, String message) {
        senderUser = sender;
        this.message = message;
        timestamp = sender.time;

    }

//    public void sendToServer(Context context) {
//        ServerUploader server = new ServerUploader(context);
//        HashMap<String, String> hm = new HashMap<>();
//        hm.put("sender_first_name", sender.getFirstName());
//        hm.put("sender_last_name", sender.getLastName());
//        String fcmToken = "";
//        hm.put("recipient_fcm_token", fcmToken);
//        hm.put("message_text", message);
//        server.addRequest(FCMReceiver.SERVER_URL, hm);
//    }

    public static void sendMessage(Message message, Context context) {
        ServerUploader server = new ServerUploader(context);
        HashMap<String, String> hm = new HashMap<>();
        hm.put("sender_name", message.sender);
        hm.put("recipient_token", message.recipToken);
        hm.put("message", message.message);
        server.addRequest(FCMReceiver.MESSAGE_SERVER_URL, hm);
    }
}
