package com.sloppie.mediawellbeing.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.Size;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.sloppie.mediawellbeing.receiver.ActiveDisplayBroadcastReceiver;
import com.sloppie.mediawellbeing.util.BaseDataStructure;
import com.sloppie.mediawellbeing.util.HybridStack;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This class registers as an {@link android.accessibilityservice.AccessibilityService} to allow
 * the app to receive all actions going through the application. This helps the Service to start
 * and stop the {@link ContentFilteringService} when an app whose content is to be monitored is
 * brought into focus.
 */
public class UserActionMonitorService extends AccessibilityService {
    static String TAG = "com.sloppie.mediawellbeing.service:AccessibilityService";

    // this string is used as the Key to pass the rootNode as a parcelable to the BroadcastReceiver
    public static final String ROOT_NODE_INFO =
            "com.sloppie.mediawellbeing.UserActionMonitorService.ROOT_NODE_INFO";

    // this string stores the action that will be in a broadcast to make sure that the service is
    // closed if the active app is not among the monitored apps.
    public static final String CLOSE_FOREGROUND_SERVICE =
            "com.sloppie.mediawellbeing.UserActionMonitorService.CLOSE_FOREGROUND_SERVICE";

    // this action is used to notify that the overlay being used should be updated as the the
    // window has changed
    public static final String UPDATE_OVERLAY =
            "com.sloppie.mediawellbeing.UserActionMonitorService.UPDATE_OVERLAY";

    public static final ArrayList<String> activePackages = new ArrayList<>();

    /**
     * This class is used to wrap the the package name to allow for it to be used in the data
     * manipulation utilities being used in the application.
     */
    static public class MonitoredApp implements BaseDataStructure<String> {
        final String key;

        public MonitoredApp(String key) {
            this.key = key;
        }

        @Override
        public String getKey() {
            return key;
        }
    }

    // this keeps track of the active stack to allow stop and start of the FilterService depending
    // on whether the stack is empty
    private HybridStack packageStack;

    // this is a global variable used to keep track of the active package
    private String ACTIVE_APP = null;

    // this update_id that is used to make sure that the Threads spawned only update if they are the
    // most recent that are representative of the screen
    private int UPDATE_ID = 0;

    // this is used as a handle to the main Thread that all Spawned threads can use to access the
    // main Thread to perform UI actions
    private Handler mainThreadHandler = null;

    // AccessibilityService method Overrides
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate() {
        super.onCreate();
        packageStack = new HybridStack(); // initialise stack
        mainThreadHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Closing up service");

        // if the packageStack isnt empty, it means that also the ContentFilteringService is also
        // running thus needs to be cancelled and also help perform clean up of the receiver
        // associated with the FilterService
        if (!packageStack.isEmpty()) {
            // handle on the main thread
            mainThreadHandler.post(() -> {
                Intent destroyServiceIntent = new Intent(CLOSE_FOREGROUND_SERVICE);
                sendBroadcast(destroyServiceIntent);
                Log.d(TAG, "Sending intent");
            });
        }

        super.onDestroy();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        String activePackage = (String) event.getPackageName();
        ACTIVE_APP = activePackage;
        UPDATE_ID++;

        // this is because this thread is no longer of use
        // TODO find out if there's a method to kill the thread from here to save even more computation power
        new Thread(accesibilityEventsHandler).start();
    }

    @Override
    public void onInterrupt() {
// pass
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        // add all relevant packages that the user want to be monitored
        // PROBABLY FETCHED FROM A DATABASE
        activePackages.add("com.spotify.com");
        activePackages.add("com.whatsapp");

        AccessibilityServiceInfo nodeInfo = new AccessibilityServiceInfo();
        nodeInfo.eventTypes = AccessibilityEvent.TYPE_VIEW_CLICKED;
        // test app packages
        nodeInfo.feedbackType = AccessibilityServiceInfo.FEEDBACK_VISUAL;
        setServiceInfo(nodeInfo);
        Log.d(TAG, "Service Connected");
    }

    // THREAD TO HANDLE AccessibilityEvents outside the main app thread
    private Runnable accesibilityEventsHandler = () -> {
        String activePackage = ACTIVE_APP;

        // check if this is a package of interest
        if (activePackages.contains(activePackage)) {
            boolean serviceStarted = false;

            // start the ContentFiltering service if the packageStack is empty
            if (packageStack.isEmpty()) {
                try {
                    // create Foreground Service
                    Intent monitorServiceIntent = new Intent(getApplicationContext(), ContentFilteringService.class);
                    ComponentName serviceComponentName = startService(monitorServiceIntent);

                    if (serviceComponentName != null) {
                        serviceStarted = true;
                        packageStack.push(new MonitoredApp(activePackage));
                        Log.d(TAG, "Service Started");
                    }

                } catch (Exception e) {
                    Log.d(TAG, e.toString());
                }
            } else {
                // if the packageStack is not empty, it means that the service is already running
                serviceStarted = true;
                packageStack.push(new MonitoredApp(activePackage));
            }

            // send the event to the app of FilterService
            if (serviceStarted) {
                AccessibilityNodeInfo rootView = this.getRootInActiveWindow();
                Intent activeDisplayIntent = new Intent(UPDATE_OVERLAY);
                activeDisplayIntent.putExtra(ROOT_NODE_INFO, rootView);
                activeDisplayIntent.putExtra("UPDATE_ID", UPDATE_ID);
                sendBroadcast(activeDisplayIntent);
            }
        } else {
            // end the content Filtering service as the stack does not contain a package that needs
            // monitoring
            if (!packageStack.isEmpty()) {
                packageStack.pop();
                Intent destroyService = new Intent(CLOSE_FOREGROUND_SERVICE);
                sendBroadcast(destroyService); // close service
                Log.d(TAG, "Ending service");
            }
        }
    };

}
