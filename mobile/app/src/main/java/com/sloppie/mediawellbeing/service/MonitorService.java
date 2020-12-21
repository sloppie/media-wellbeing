package com.sloppie.mediawellbeing.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.sloppie.mediawellbeing.R;
import com.sloppie.mediawellbeing.receiver.ActiveDisplayBroadcastReceiver;

/**
 * This Service is started to allow the application to monitor a screen. This, ideally, is called
 * once the ever running Service (that monitors the actions being carried out in the system)
 * calls the service once it notices that a user is using an application that may be having photo
 * content.
 * After it is started, the screen spawns a {@link android.view.WindowManager} that is used to
 * create a transparent overlay that will be used to add filters dynamically in the case that the
 * content on the screen is noted to be explicit.
 * This service is stopped as soon as the Application (app that has photo content) is left.
 * This should help with the conservation of phone resources thus reducing battery drain.
 *
 * This service spawns threads that make sure that the methods used to scan and output results
 * run only when there is photo content to be consumed. (CONSERVATION OF RESOURCES)
 *
 * The Consumer thread adds {@link android.graphics.Bitmap} of the screenshots if there are pictures
 * present, and the Consumer thread scans the Bitmap and generates output that is relevant as to
 * whether or not a View that blurs the screen should be generated.
 *
 * This thread also receives events on when the content disappears so that it can remove the blur
 * filter placed. As such, this Service requires access to an interface that is updated on the
 * status of each View and when the coordinates should be removed.
 */
public class MonitorService extends Service {
    public static String TAG = "com.sloppie.mediawellbeing.service:MonitorService";

    @Override
    public void onCreate() {
        super.onCreate();
        // register a broadcast receiver
        ActiveDisplayBroadcastReceiver adbr = new ActiveDisplayBroadcastReceiver();



        // start the service as a FOREGROUND_SERVICE
        NotificationCompat.Builder monitorNotification = new NotificationCompat.Builder(
                getApplicationContext(), "MonitorService")
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_monitor)
                .setContentTitle(getString(R.string.monitor_notif_title))
                .setContentText(getString(R.string.monitor_notif_small_text))
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(getString(R.string.monitor_notif_big_text)))
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setSound(null)
                .setVibrate(null);
        // start the service
        startForeground(1, monitorNotification.build());
        Log.d(TAG, "Foreground Service started");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // null safety if the process is being recreated
        // TODO: spawn PRODUCER/CONSUMER threads to help with the monitoring
        if (intent != null) {
            // get window manager
            WindowManager windowManager =
                    (WindowManager) getSystemService(Context.WINDOW_SERVICE);

            // get screen dimensions from active window display
            Point screenPoints = new Point();
            getDisplay().getRealSize(screenPoints);

            // create an based on the android OS version, this is because the SYSTEM_OVERLAY
            // version does not work for versions later than O, thus all devices Supported have to
            // be taken into consideration.
            int OVERLAY_TYPE = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) ?
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY:
                    WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;

            // the FLAG_NOT_TOUCHABLE together with the FLAG_NOT_FOCUSABLE allow for the overlay to
            // pass all its interactions to the underlying window.
            WindowManager.LayoutParams overlayParams = new WindowManager.LayoutParams(
                    screenPoints.x,
                    screenPoints.y,
                    OVERLAY_TYPE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    PixelFormat.TRANSPARENT);

            try {
                windowManager.addView(new View(getBaseContext()), overlayParams);
                Log.d(TAG, "Overlay added");
            } catch (Exception e) {
                Log.d(TAG, e.toString());
                Toast.makeText(getBaseContext(), "Permission to display over other apps required", Toast.LENGTH_SHORT).show();
            }
            // handle all the data
            // start as a foreground service

        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
