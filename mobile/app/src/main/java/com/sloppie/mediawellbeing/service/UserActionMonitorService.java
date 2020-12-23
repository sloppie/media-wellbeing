package com.sloppie.mediawellbeing.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Build;
import android.util.Log;
import android.util.Size;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.sloppie.mediawellbeing.receiver.ActiveDisplayBroadcastReceiver;
import com.sloppie.mediawellbeing.util.BaseDataStructure;
import com.sloppie.mediawellbeing.util.HybridStack;

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

    // this refers to the app that is currently active
    private String ACTIVE_APP = null;

    private HybridStack packageStack;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate() {
        super.onCreate();
        packageStack = new HybridStack();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Closing up service");
        Intent destroyServiceIntent = new Intent(CLOSE_FOREGROUND_SERVICE);
        sendBroadcast(destroyServiceIntent);
        super.onDestroy();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        setACTIVE_APP((String) event.getPackageName()); // set the package name each time
        int eventType = event.getEventType();
        String contentDescription = (String) event.getContentDescription();
        AccessibilityNodeInfo accessibilityNodeInfo = event.getSource();

        AccessibilityNodeInfo rootView = this.getRootInActiveWindow();
        Intent activeDisplayIntent =
                new Intent(UPDATE_OVERLAY);
        activeDisplayIntent.putExtra(ROOT_NODE_INFO, rootView);
        sendBroadcast(activeDisplayIntent);
    }

    @Override
    public void onInterrupt() {
// pass
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        AccessibilityServiceInfo nodeInfo = new AccessibilityServiceInfo();
        nodeInfo.eventTypes = AccessibilityEvent.TYPE_VIEW_CLICKED | AccessibilityEvent.TYPE_VIEW_FOCUSED;
        // test app packages
        nodeInfo.packageNames = new String[] {"com.spotify.music", "com.whatsapp"};
        nodeInfo.feedbackType = AccessibilityServiceInfo.FEEDBACK_VISUAL;
        setServiceInfo(nodeInfo);
        Log.d(TAG, "Service Connected");

        try {
            Toast.makeText(getApplicationContext(), "This is a test toast", Toast.LENGTH_LONG).show();

            // create Foreground Service
            Intent monitorServiceIntent = new Intent(getApplicationContext(), ContentFilteringService.class);
            ComponentName serviceComponentName = startService(monitorServiceIntent);

            if (serviceComponentName != null) {
                Log.d(TAG, "Service Started");
            }

        } catch (Exception e) {
            Log.d(TAG, e.toString());
        }
    }

    private void setACTIVE_APP(String packageName) {
        if (ACTIVE_APP != null) {
            // if the active app has changed:
            //  1. update the ACTIVE_APP variable
            //  2. update the stack
            //  3. Broadcast the change to the Monitor service
            if (ACTIVE_APP.compareTo(packageName) != 0) {
                ACTIVE_APP = packageName;
                // TODO: refactor to prevent initialisation every time we add a new package name
                packageStack.push(new MonitoredApp(packageName));
            }
        }
    }
}
