package com.sloppie.mediawellbeing.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.sloppie.mediawellbeing.R;

public class TestService extends AccessibilityService {
    static String TAG = "com.sloppie.mediawellbeing.service:AccessibilityService";

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate() {
        super.onCreate();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
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
        } catch (Exception e) {
            Log.d(TAG, e.toString());
        }
    }
}
