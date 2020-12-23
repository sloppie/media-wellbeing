package com.sloppie.mediawellbeing.service;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.view.WindowMetrics;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
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
    public static String TAG = "com.sloppie.mediawellbeing.service:ContentFilteringService";
    public static final int NOTIFICATION_ID = 101;

    WindowManager windowManager = null;
    WindowManager.LayoutParams windowLayoutParams = null;
    View rootView = null;
    RelativeLayout relativeLayout = null;
    int DISPLAY_CUTOUT = 0;

    private int UPDATE_ID = 1;
    private Handler myHandler;
    private Handler relativeLayoutHandler = null;

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            DISPLAY_CUTOUT = getDisplay().getCutout().getSafeInsetTop();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // null safety if the process is being recreated
        // TODO: spawn PRODUCER/CONSUMER threads to help with the monitoring

        // this handler makes sure that the operations are carried out on the main thread
        myHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                if (msg.arg1 == 1) {
                    try {
                        windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
                        windowManager.addView(relativeLayout, windowLayoutParams);
                        Log.d(TAG, "Layout Updated");
                    } catch (Exception e) {
                        Log.d(TAG, e.toString());
                        relativeLayout = new RelativeLayout(getBaseContext());
                        windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
                        windowManager.addView(relativeLayout, windowLayoutParams);
                        Log.d(TAG, "Layout Updated");
                    }
                } else if (msg.arg1 == 2) {
                    if (relativeLayout == null) {
                        relativeLayout = new RelativeLayout(getBaseContext());
                    }
                } else if (msg.arg1 == 3) {
                    // TODO: test whether that now they are all on the main thread, one can use RelativeLayout#updateViewLayout instead of having to create a new one
                    relativeLayout.removeAllViews();
                    relativeLayout = new RelativeLayout(getBaseContext());
                } else if (msg.arg1 == 4) {
                    View newView = new View(getBaseContext());
                    Bundle layoutParamsBundle = msg.getData();
                    int width = layoutParamsBundle.getInt("width");
                    int height = layoutParamsBundle.getInt("height");
                    int x = layoutParamsBundle.getInt("x");
                    int y = layoutParamsBundle.getInt("y");
                    GradientDrawable backgroundGradientDrawable = new GradientDrawable();
                    backgroundGradientDrawable.setColor(0x00FFFFFF);
                    backgroundGradientDrawable.setStroke(1, 0xFFFFFFFF);
                    newView.setBackground(backgroundGradientDrawable);

                    RelativeLayout.LayoutParams rlp =
                            new RelativeLayout.LayoutParams(width, height);
                    rlp.leftMargin = x;
                    rlp.topMargin = y;

                    relativeLayout.addView(newView, rlp);
                }
            }
        };

        if (intent != null) {
            // get window manager
            windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

            // get screen dimensions from active window display
            Point screenPoints = new Point();
            Display activeDisplay = getDisplay();
            activeDisplay.getRealSize(screenPoints);

            // create an based on the android OS version, this is because the SYSTEM_OVERLAY
            // version does not work for versions later than O, thus all devices Supported have to
            // be taken into consideration.
            int OVERLAY_TYPE = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) ?
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY:
                    WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;

            windowLayoutParams = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    OVERLAY_TYPE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
                            WindowManager.LayoutParams.FLAG_DIM_BEHIND,
                    PixelFormat.TRANSLUCENT);

            windowLayoutParams.dimAmount = 0.0f;

            rootView = new View(getBaseContext());

            Message msg = myHandler.obtainMessage();
            msg.arg1 = 2;
            myHandler.sendMessage(msg);

            try {
                Message myMessage = myHandler.obtainMessage();
                myMessage.arg1 = 1;
                myHandler.sendMessage(myMessage);
            } catch (Exception e) {
                Log.d(TAG, e.toString());
                Toast.makeText(
                        getBaseContext(),
                        "Permission to display over other apps required",
                        Toast.LENGTH_SHORT).show();
            }
        }

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
                Message message = myHandler.obtainMessage();
                message.arg1 = 1;
                myHandler.sendMessage(message);
            } catch (Exception e) {
                Log.d(TAG, e.toString());
            }
        }
    }

    @Override
    public synchronized void updateRelativeLayout(
            int width, int height, int x, int y, int UPDATE_ID) {
        if (UPDATE_ID == this.UPDATE_ID) {
            Bundle extraData = new Bundle();
            extraData.putInt("width", width);
            extraData.putInt("height", height);
            extraData.putInt("x", x);
            extraData.putInt("y", y);

            Message msg = myHandler.obtainMessage();
            msg.arg1 = 4;
            msg.setData(extraData);
            myHandler.sendMessage(msg);
        }
    }

    @Override
    public void updateOverlayLayout(AccessibilityNodeInfo rootNode) {
        // spawn threads to traverse node
        UPDATE_ID++; // increase the update ID before spawining the threads
        // create a new RelativeLayout for the updated window
        Message msg = myHandler.obtainMessage();
        msg.arg1 = 3;
        myHandler.sendMessage(msg);
//        relativeLayout.removeAllViews();
        NodeTraverser nodeTraverser = new NodeTraverser(this, rootNode, UPDATE_ID);
        Thread traverserThread = new Thread(nodeTraverser);
        traverserThread.start();
    }

    @Override
    public void destroyOverlay() {
        stopForeground(true);
        stopSelf();
        Log.d(TAG, "Stopping Service");
    }

    @Override
    public int getDISPLAY_CUTOUT() {
        return DISPLAY_CUTOUT;
    }
}
