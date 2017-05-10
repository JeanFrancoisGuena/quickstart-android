/**
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.firebase.quickstart.fcm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    public static final String ACTION_DATA_BROADCAST = MyFirebaseMessagingService.class.getName() + "DataBroadcast";

    private static final String TAG = "MyFirebaseMsgService";

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
        // traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages containing both notification
        // and data payloads are treated as notification messages. The Firebase console always sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        // TODO(developer): Handle FCM messages here.
        //sendBroadcastMessage(remoteMessage);

        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // handle some notification when message contains no data nor notification items
        if(remoteMessage.getData().size() == 0 && remoteMessage.getNotification() == null) {
            sendNotification("", "");
        }

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            sendData(remoteMessage.getData());
            /*String dataMsg = "";
            for (String key : remoteMessage.getData().keySet()) {
                String value = (String) remoteMessage.getData().get(key);
                dataMsg = String.format("%s\n%s: %s", dataMsg, key, value);
            }
           sendNotification(dataMsg);*/
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            String messageTitle = remoteMessage.getNotification().getTitle();
            Log.d(TAG, "Message Notification Title: " + messageTitle);
            String messageBody = remoteMessage.getNotification().getBody();
            Log.d(TAG, "Message Notification Body: " + messageBody);
            sendNotification(messageTitle, messageBody);
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }
    // [END receive_message]



    private void sendBroadcastMessage(RemoteMessage remoteMsg) {
        if (remoteMsg != null) {
            Intent intent = new Intent(ACTION_DATA_BROADCAST);
            //intent.putExtra("google.sent_time", remoteMsg.getSentTime());
            //intent.putExtra("from", remoteMsg.getFrom());
            if (remoteMsg.getData().size() > 0) {
                Log.d(TAG, "Message data payload to broadcast: " + remoteMsg.getData());
                for (String key : remoteMsg.getData().keySet()) {
                    String value = (String) remoteMsg.getData().get(key);
                    intent.putExtra(key, value);
                }
            }
            //intent.putExtra("google.message_id", remoteMsg.getMessageId());
            //intent.putExtra("collapse_key", remoteMsg.getCollapseKey());
            //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    private void sendNotification(String messageTitle, String messageBody) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_stat_ic_notification)
                .setContentTitle(messageTitle.isEmpty() ? "FCM Message": messageTitle)
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }

    private void sendData(Map<String,String> data) {
        if (data.size() > 0) {
            Intent intent = new Intent(this, MainActivity.class);
            //Intent intent = new Intent(ACTION_DATA_BROADCAST, null, this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);
            Log.d(TAG, "Message data payload to send: " + data);
            for (Map.Entry<String, String> entry : data.entrySet()) {
                intent.putExtra(entry.getKey(), entry.getValue());
            }
            startActivity(intent);
            //LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
    }
}
