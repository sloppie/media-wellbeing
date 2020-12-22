package com.sloppie.mediawellbeing.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import com.sloppie.mediawellbeing.service.FilterService;
import com.sloppie.mediawellbeing.service.UserActionMonitorService;

public class ActiveDisplayBroadcastReceiver extends BroadcastReceiver {
    public static final String TAG =
            "com.sloppie.mediawellbeing.receiver.ActiveBroadcastReceiver";

    private FilterService.OverlayUpdater overlayUpdater = null;

    public ActiveDisplayBroadcastReceiver() {
    }

    public ActiveDisplayBroadcastReceiver(FilterService.OverlayUpdater overlayUpdater) {
        this.overlayUpdater = overlayUpdater;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // handle UPDATE_OVERLAY action
        if (intent.getAction().compareTo(UserActionMonitorService.UPDATE_OVERLAY) == 0) {
            AccessibilityNodeInfo rootNode = intent.getParcelableExtra(
                    UserActionMonitorService.ROOT_NODE_INFO);
            // start the updateOverlay process
            if (overlayUpdater != null) {
                overlayUpdater.updateOverlayLayout(rootNode);
                Log.d(TAG, "Updating Overlay");
            } else {
                Log.d(TAG, "Could not find FilterService.OverlayUpdater class to handle action in Intent");
            }
        }
    }
}
