package com.csce4623.ahnelson.todolist;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkChangeReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        try
        {
            //If not online, sends out notification that internet is lost
            if (!isOnline(context)) {
                NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                Intent reminderIntent = new Intent(context, HomeActivity.class);
                PendingIntent contentIntent = PendingIntent.getActivity(context, 0, reminderIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                Notification notification = new Notification.Builder(context, "my_channel_01")
                        .setContentTitle("To-Do Reminder")
                        .setContentText("No Internet Access!")
                        .setContentIntent(contentIntent)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setAutoCancel(true)
                        .build();

                mNotificationManager.notify(0, notification);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    //Determines if user has internet access
    private boolean isOnline(Context context) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            return (netInfo != null && netInfo.isConnected());
        } catch (NullPointerException e) {
            e.printStackTrace();
            return false;
        }
    }
}