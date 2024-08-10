package com.example.calendarapp.Utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.calendarapp.R;

public class ReminderBroadcast extends BroadcastReceiver {

    private static final String ACTION_REMIND = "com.example.calendarapp.ACTION_REMIND";

    @Override
    public void onReceive(Context context, Intent intent) {
        // Check if the received intent has the expected action
        if (ACTION_REMIND.equals(intent.getAction())) {
            String title = intent.getStringExtra("title");
            String eventTime = intent.getStringExtra("eventTime");
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "notify")
                    .setSmallIcon(R.drawable.ic_clock)
                    .setContentTitle(title + " Reminder")
                    .setContentText("Your event is at " + eventTime)
                    .setPriority(NotificationCompat.PRIORITY_HIGH);

            NotificationManagerCompat manager = NotificationManagerCompat.from(context);
            try{
                manager.notify(200, builder.build());
            }catch (SecurityException e){
                Log.d("ReminderBroadcast", "not worked");
            }
        } else {
            Log.e("ReminderBroadcast", "Received unexpected intent action: " + intent.getAction());
        }
    }
}
