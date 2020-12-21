package com.sloppie.mediawellbeing.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.util.Log;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.sloppie.mediawellbeing.receiver.ActiveDisplayBroadcastReceiver;
import com.sloppie.mediawellbeing.util.BaseDataStructure;
import com.sloppie.mediawellbeing.util.HybridStack;

public class TestService extends AccessibilityService {
    static String TAG = "com.sloppie.mediawellbeing.service:AccessibilityService";

    static public class MonitoredApp implements BaseDataStructure<String> {
        final String key;

        public MonitoredApp(String key) {
            this.key = key;
        }

        @Override
        public String getKey() {
            return null;
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

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        setACTIVE_APP((String) event.getPackageName()); // set the package name each time
        int eventType = event.getEventType();
        String contentDescription = (String) event.getContentDescription();
        AccessibilityNodeInfo accessibilityNodeInfo = event.getSource();

        AccessibilityNodeInfo rootView = this.getRootInActiveWindow();
        // traverse rootView
        try {

        } catch (Exception e) {
            Log.d(TAG, e.toString());
        }
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
        this.setServiceInfo(nodeInfo);
        Log.d(TAG, "Service Connected");
        try {
            Toast.makeText(getApplicationContext(), "This is a test toast", Toast.LENGTH_LONG).show();
            Log.d(TAG, "TestService#onCreate ran");
            WindowManager windowManager =
                    (WindowManager) getSystemService(Context.WINDOW_SERVICE);
            WindowManager.LayoutParams newViewParams = new WindowManager.LayoutParams(
                    300,
                    400,
                    200,
                    200,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    PixelFormat.TRANSPARENT
            );
//            newViewParams.alpha = 100;
//            windowManager.addView(new RelativeLayout(getApplicationContext()), newViewParams);
            Log.d(TAG, "TestService#onCreate setWindow");
            // create ForeGround Service
            Intent monitorServiceIntent = new Intent(getApplicationContext(), MonitorService.class);
            ComponentName serviceComponentName = startService(monitorServiceIntent);

            if (serviceComponentName != null) {
                Log.d(TAG, "Service Started");
            }

            // send Explicit intent
            Intent newIntent = new Intent(getBaseContext(), ActiveDisplayBroadcastReceiver.class);
            sendBroadcast(newIntent);

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
