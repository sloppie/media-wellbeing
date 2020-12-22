package com.sloppie.mediawellbeing.service;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.core.app.NotificationCompat;

import com.sloppie.mediawellbeing.R;
import com.sloppie.mediawellbeing.receiver.ActiveDisplayBroadcastReceiver;
import com.sloppie.mediawellbeing.service.util.NodeTraverser;


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
public class ContentFilteringService extends Service implements FilterService,
        FilterService.OverlayUpdater {
    public static String TAG = "com.sloppie.mediawellbeing.service:MonitorService";
    public static final int NOTIFICATION_ID = 101;

    WindowManager windowManager = null;
    WindowManager.LayoutParams windowLayoutParams = null;
    View rootView = null;
    RelativeLayout relativeLayout = null;

    private int UPDATE_ID = 1;

    @Override
    public void onCreate() {
        super.onCreate();

        // registerReceiver
        ActiveDisplayBroadcastReceiver activeDisplayBroadcastReceiver =
                new ActiveDisplayBroadcastReceiver(this);
        IntentFilter abdrIntentFilter = new IntentFilter(UserActionMonitorService.UPDATE_OVERLAY);
        registerReceiver(activeDisplayBroadcastReceiver, abdrIntentFilter);

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
        startForeground(NOTIFICATION_ID, monitorNotification.build());
        Log.d(TAG, "Foreground Service started");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // null safety if the process is being recreated
        // TODO: spawn PRODUCER/CONSUMER threads to help with the monitoring
        Handler myHandler = new Handler(Looper.getMainLooper());
        myHandler.post(() -> {
            if (intent != null) {
                // get window manager
                windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

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
                windowLayoutParams = new WindowManager.LayoutParams(
                        screenPoints.x,
                        screenPoints.y,
                        OVERLAY_TYPE,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
                                WindowManager.LayoutParams.FLAG_DIM_BEHIND,
                        PixelFormat.TRANSLUCENT);

                windowLayoutParams.dimAmount = 0.0f;

                rootView = new View(getBaseContext());

                relativeLayout = new RelativeLayout(getBaseContext());

                try {
                    windowManager.addView(relativeLayout, windowLayoutParams);
                    Log.d(TAG, "Overlay added");
                } catch (Exception e) {
                    Log.d(TAG, e.toString());
                    Toast.makeText(
                            getBaseContext(),
                            "Permission to display over other apps required",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public synchronized void updateWindowManager(int UPDATE_ID) {
        if (UPDATE_ID == this.UPDATE_ID) {
            try {
                Handler myHandler = new Handler(Looper.getMainLooper());

                myHandler.post(() -> {
                    windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
                    windowManager.addView(relativeLayout, windowLayoutParams);
                    Log.d(TAG, "Layout Updated");
                });
            } catch (Exception e) {
                Log.d(TAG, e.toString());
            }
        }
    }

    @Override
    public synchronized void updateRelativeLayout(
            View newView, RelativeLayout.LayoutParams viewLayoutParams, int UPDATE_ID) {
        if (UPDATE_ID == this.UPDATE_ID) {
            relativeLayout.addView(newView, viewLayoutParams);
        }
    }

    @Override
    public void updateOverlayLayout(AccessibilityNodeInfo rootNode) {
        // spawn threads to traverse node
        UPDATE_ID++; // increase the update ID before spawining the threads
        // create a new RelativeLayout for the updated window
        relativeLayout.removeAllViews();
        NodeTraverser nodeTraverser = new NodeTraverser(this, rootNode, UPDATE_ID);
        Thread traverserThread = new Thread(nodeTraverser);
        traverserThread.start();
    }
}
