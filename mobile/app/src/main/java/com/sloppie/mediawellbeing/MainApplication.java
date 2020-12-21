package com.sloppie.mediawellbeing;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class MainApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            String CHANNEL_ID = "MonitorService";
            CharSequence monitorChannelName = getString(R.string.monitor_channel_name);
            String monitorChannelDescription =
                    getString(R.string.monitor_channel_description);
            int monitorNotificationImportance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel monitorNotifChannel = new NotificationChannel(
                    CHANNEL_ID,
                    monitorChannelName,
                    monitorNotificationImportance);
            monitorNotifChannel.setDescription(monitorChannelDescription);
            notificationManager.createNotificationChannel(monitorNotifChannel);
        }
    }
}
