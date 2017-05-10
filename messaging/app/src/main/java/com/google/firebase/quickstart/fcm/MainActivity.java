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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final String TOPIC = "webcom_notif";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // If a notification message is tapped, any data accompanying the notification
        // message is available in the intent extras. In this sample the launcher
        // intent is fired when the notification is tapped, so any accompanying data would
        // be handled here. If you want a different intent fired, set the click_action
        // field of the notification message to the desired intent. The launcher intent
        // is used when no click_action is specified.
        //
        // Handle possible data accompanying notification message.
        // [START handle_data_extras]
        Intent intent = getIntent();
        logIntent(intent, "FIRST INTENT RECEIVED");
        updateDataTextView(intent);
        // [END handle_data_extras]

        LocalBroadcastManager.getInstance(this).registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        //intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        //startActivity(intent);
                        updateDataTextView(intent);
                    }
                }, new IntentFilter(MyFirebaseMessagingService.ACTION_DATA_BROADCAST)
        );

        Button subscribeButton = (Button) findViewById(R.id.subscribeButton);
        subscribeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // [START subscribe_topics]
                FirebaseMessaging.getInstance().subscribeToTopic(TOPIC);
                // [END subscribe_topics]

                // Log and toast
                String msg = getString(R.string.msg_subscribed);
                Log.d(TAG, msg);
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });

        Button logTokenButton = (Button) findViewById(R.id.logTokenButton);
        logTokenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get token
                String token = FirebaseInstanceId.getInstance().getToken();

                // Log and toast
                String msg = getString(R.string.msg_token_fmt, token);
                Log.d(TAG, msg);
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });

        Button resetDataButton = (Button) findViewById(R.id.resetDataTextView);
        resetDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView dataTextView = (TextView) findViewById(R.id.dataTextView);
                dataTextView.setText(R.string.data_msg);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "ON RESUME");
        restoreDataTextView();
        startService(new Intent(this, MyFirebaseMessagingService.class));
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "ON PAUSE");
        stopService(new Intent(this, MyFirebaseMessagingService.class));
        saveDataTextView();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG, "ON NEW INTENT");
        logIntent(intent, "NEW INTENT RECEIVED");
        updateDataTextView(intent);
    }

    private void updateDataTextView(Intent intent) {
        logDataTextView("BEFORE UPDATE");
        restoreDataTextView();
        if (intent != null && intent.getExtras() != null) {
            Bundle extras = intent.getExtras();
            String msg = "";
            for (String key : extras.keySet()) {
                Object value = extras.get(key);
                if (key.equalsIgnoreCase("google.sent_time")
                        || key.equalsIgnoreCase("from")
                        || key.equalsIgnoreCase("google.message_id")
                        || key.equalsIgnoreCase("collapse_key"))
                {
                    Log.d(TAG, String.format("Ignored key: %s Value: %s", key, value));
                } else {
                    if (msg.isEmpty()) {
                        msg = String.format("Key: %s Value: %s", key, value);
                    } else {
                        msg = String.format("%s\nKey: %s Value: %s", msg, key, value);
                    }
                }
            }
            Log.d(TAG, String.format("Generated message:\n%s", msg));
            TextView dataTextView = (TextView) findViewById(R.id.dataTextView);
            String currentText = (String) dataTextView.getText();
            if (currentText.contains("Notification message should be displayed here...")) {
                dataTextView.setText(msg);
            } else {
                dataTextView.setText(String.format("%s\n%s", currentText, msg));
            }
        }
        saveDataTextView();
        logDataTextView("AFTER UPDATE");
    }

    private void saveDataTextView() {
        logDataTextView("BEFORE SAVE");
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        final TextView dataTextView = (TextView) findViewById(R.id.dataTextView);
        SharedPreferences.Editor edit = settings.edit();
        edit.putString("dataTextView", (String) dataTextView.getText());
        edit.apply();
        logDataTextView("AFTER SAVE");
    }

    private void restoreDataTextView() {
        logDataTextView("BEFORE RESTORE");
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        final TextView dataTextView = (TextView) findViewById(R.id.dataTextView);
        CharSequence userText = settings.getString("dataTextView", null);
        dataTextView.setText(userText);
        logDataTextView("AFTER RESTORE");
    }


    private void logDataTextView(String title) {
        final TextView dataTextView = (TextView) findViewById(R.id.dataTextView);
        Log.d(TAG, String.format("%s:\n%s", title, (String) dataTextView.getText()));
    }

    private void logIntent(Intent intent, String title){
        Log.d(TAG, String.format("%s: %s",title, intent));
        if (intent.getExtras() != null) {
            Bundle extras = intent.getExtras();
            String msg = "";
            for (String key : extras.keySet()) {
                Object value = extras.get(key);
                if (msg.isEmpty()) {
                    msg = String.format("Key: %s Value: %s", key, value);
                } else {
                    msg = String.format("%s\nKey: %s Value: %s", msg, key, value);
                }
            }
            Log.d(TAG, String.format("%s (extras):\n%s", title, msg));
        }

    }

}
