package com.sloppie.mediawellbeing.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.sloppie.mediawellbeing.R;
import com.sloppie.mediawellbeing.receiver.ActiveDisplayBroadcastReceiver;
import com.sloppie.mediawellbeing.service.util.NodeTraverser;
import com.sloppie.mediawellbeing.service.util.ServiceArrayBlockingQueue;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


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
    // tag for logging
    public static String TAG = "com.sloppie.mediawellbeing.service:ContentFilteringService";
    // id used for this class' Foreground notifications
    public static final int NOTIFICATION_ID = 101;

    WindowManager windowManager = null;
    WindowManager.LayoutParams windowLayoutParams = null;
    View rootView = null;
    RelativeLayout relativeLayout = null;

    // this is essential as the API provided as of API LEVEL 29 does not use the Window coordinates
    // when getting the bound of the AccessibilityNodeInfo, as such, the DisplayCutout has to be
    // subtracted from the Rect bounds.
    // This will be refactored once when the AccessibilityNodeInfo also provides Window coordinates
    // and not Display coordinates
    int DISPLAY_CUTOUT = 0;

    // this variable is used to make sure spawned threads do not add to the RelativeLayout if there
    // is a more recent action that has been started
    private int UPDATE_ID = 1;

    private ServiceArrayBlockingQueue serviceArrayBlockingQueue = null;

    // this thread is used to carry out actions on the main thread
    private Handler mainThreadHandler;

    // receiver to be used to get nodes from the main app
    ActiveDisplayBroadcastReceiver activeDisplayBroadcastReceiver = null;

    @Override
    public void onCreate() {
        super.onCreate();

        // registerReceiver
        activeDisplayBroadcastReceiver =
                new ActiveDisplayBroadcastReceiver(this);
        // intent filter used to fetch actions from all broadcasts
        IntentFilter abdrIntentFilter = new IntentFilter();
        abdrIntentFilter.addAction(UserActionMonitorService.UPDATE_OVERLAY);
        abdrIntentFilter.addAction(UserActionMonitorService.CLOSE_FOREGROUND_SERVICE);

        registerReceiver(activeDisplayBroadcastReceiver, abdrIntentFilter); // register receiver

        // create the ExecutorMonitoringQueue
        serviceArrayBlockingQueue = new ServiceArrayBlockingQueue();

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

        // create an based on the android OS version, this is because the SYSTEM_OVERLAY
        // version does not work for versions later than O, thus all devices Supported have to
        // be taken into consideration.
        int OVERLAY_TYPE = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) ?
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY:
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;


        // this handler makes sure that the operations are carried out on the main thread
        // all UI operations must be handled by the same thread otherwise this results to the app
        // crashing due to View ownership issues
        mainThreadHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);

                // KEY:
                //  1 - Adding a new RelativeLayout and assigning the WindowManager
                //  2 - resetting the RelativeLayout
                //  3 - removing all the views from an existing RelativeLayout
                //  4 - Adding a bounded Rect with ImageView coordinates
                //  5 - Removing overlay by creating the Layout params of a new view to 0, 0 dim

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

                    // fetch the coordinates from the Bundle passed by the Thread that found the
                    // ImageView
                    Bundle layoutParamsBundle = msg.getData();
                    int width = layoutParamsBundle.getInt("width");
                    int height = layoutParamsBundle.getInt("height");
                    int x = layoutParamsBundle.getInt("x");
                    int y = layoutParamsBundle.getInt("y");

                    // create a Rectangle with a border of 1 stroke width
                    GradientDrawable backgroundGradientDrawable = new GradientDrawable();
                    backgroundGradientDrawable.setColor(0x00FFFFFF);
                    backgroundGradientDrawable.setStroke(1, 0xFFFFFFFF);
                    newView.setBackground(backgroundGradientDrawable);

                    RelativeLayout.LayoutParams rlp =
                            new RelativeLayout.LayoutParams(width, height);
                    rlp.leftMargin = x;
                    rlp.topMargin = y;

                    relativeLayout.addView(newView, rlp);
                } else if (msg.arg1 == 5){
                    // update the overlay to 0, 0 length and width seems to work
                    ((WindowManager) getSystemService(Context.WINDOW_SERVICE))
                            .addView(new View(getBaseContext()), new WindowManager.LayoutParams(
                                    0,
                                    0,
                                    OVERLAY_TYPE,
                                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                                    PixelFormat.TRANSLUCENT));
                }
            }
        };

        // null safety protection if this is a recreation of a view from a config change
        if (intent != null) {
            // get window manager
            windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

            // create the WindowManager.LayoutParams to be used by the Overlay
            windowLayoutParams = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    OVERLAY_TYPE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
                            WindowManager.LayoutParams.FLAG_DIM_BEHIND,
                    PixelFormat.TRANSLUCENT);

            windowLayoutParams.dimAmount = 0.0f; // this may be used to sensor content

            // create the relative layout to be used by the overlay
            Message msg = mainThreadHandler.obtainMessage();
            msg.arg1 = 2;
            mainThreadHandler.sendMessage(msg);

            try {
                // create the overlay and add the RelativeLayout created above
                Message myMessage = mainThreadHandler.obtainMessage();
                myMessage.arg1 = 1;
                mainThreadHandler.sendMessage(myMessage);
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
    public void onDestroy() {
        super.onDestroy();

        // clean up process
        unregisterReceiver(activeDisplayBroadcastReceiver);
        stopForeground(true);
        Message destroyMessage = mainThreadHandler.obtainMessage();
        destroyMessage.arg1 = 5;
        mainThreadHandler.sendMessage(destroyMessage);
        Log.d(TAG, "Service Destroyed");
    }

    // class methods
    /**
     * This method is used to carry out termination of stale Threads that will not update the
     * Layout as the UPDATE_ID is already stale.
     * @param UPDATE_ID this is the id corresponding to the ExecutorService in the BlockingQueue
     *                  that is going to be terminated.
     * @param isComplete this is the flag that helps determine the approach that will be used to
     *                   terminate the service.
     */
    private void stopExecutorService(int UPDATE_ID, boolean isComplete) {
        serviceArrayBlockingQueue.blockingRemove(UPDATE_ID, isComplete);
    }


    // FilterService interface methods
    @Override
    public synchronized void updateWindowManager(int UPDATE_ID) {
        if (UPDATE_ID == this.UPDATE_ID) {
            try {
                // update WindowManager with the inflated RelativeLayout
                Message message = mainThreadHandler.obtainMessage();
                message.arg1 = 1;
                mainThreadHandler.sendMessage(message);
                stopExecutorService(UPDATE_ID, true);
            } catch (Exception e) {
                Log.d(TAG, e.toString());
            }
        } else {
            stopExecutorService(UPDATE_ID, false); // stop the Thread as it is no longer needed
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

            // inflate the relative layout with the bounds gotten by the Thread
            Message msg = mainThreadHandler.obtainMessage();
            msg.arg1 = 4;
            msg.setData(extraData);
            mainThreadHandler.sendMessage(msg);
        } else {
            // stop the Thread as it is no longer needed
            stopExecutorService(UPDATE_ID, false);
        }
    }

    @Override
    public int getDISPLAY_CUTOUT() {
        return DISPLAY_CUTOUT;
    }


    // FilterService.OverlayUpdater interface
    @Override
    public synchronized void updateOverlayLayout(AccessibilityNodeInfo rootNode) {
        // spawn threads to traverse node
        ++UPDATE_ID; // increase the update ID before spawning the threads
        // create a new RelativeLayout for the updated window
        Message msg = mainThreadHandler.obtainMessage();
        msg.arg1 = 3;
        mainThreadHandler.sendMessage(msg);
        // create a new ThreadPool and then pass it on to the NodeTraverser and keep a record of it
        // in the ServiceArrayBlockingQueue
        ExecutorService newExecutorService = Executors.newCachedThreadPool();
        boolean added = serviceArrayBlockingQueue.blockingPut(newExecutorService, UPDATE_ID);
        if (added) { // only add if the it has been added to the service array queue
            NodeTraverser nodeTraverser = new NodeTraverser(
                    this, newExecutorService, rootNode, UPDATE_ID);
            newExecutorService.execute(nodeTraverser);
        }
    }

    @Override
    public void destroyOverlay() {
        // this method is exposed through FilterService.OverlayUpdater to help the
        // ActiveDisplayBroadcastReceiver call on clean up once it receives an Intent with the
        // action of UserActionMonitoringService#CLOSE_FOREGROUND_SERVICE from the
        // UserMonitoringService Service
        stopForeground(true);
        stopSelf();

        ((WindowManager) getSystemService(Context.WINDOW_SERVICE))
                .removeViewImmediate(relativeLayout);
        Log.d(TAG, "Stopped ContentFilteringService");
    }
}
