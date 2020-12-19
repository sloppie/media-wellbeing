package com.sloppie.mediawellbeing.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Intent;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

public class TestService extends AccessibilityService {
    static String TAG = "com.sloppie.mediawellbeing.service:AccessibilityService";

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.d(TAG, "Received Event: " + event.getPackageName());
        if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_CLICKED) {
            Log.d(TAG, "Click Event");
        } else if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_SCROLLED) {
            Log.d(TAG, "Scroll Event");
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
        nodeInfo.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
        nodeInfo.packageNames = new String[] {"com.spotify.music", "com.whatsapp"};
        nodeInfo.feedbackType = AccessibilityServiceInfo.FEEDBACK_VISUAL;
        this.setServiceInfo(nodeInfo);
        Log.d(TAG, "Service Connected");
    }
}
