package com.sloppie.mediawellbeing.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;

import com.sloppie.mediawellbeing.service.FilterService;
import com.sloppie.mediawellbeing.service.UserActionMonitorService;

public class ActiveDisplayBroadcastReceiver extends BroadcastReceiver {
    public static final String TAG =
            "ActiveBroadcastReceiver";

    private FilterService.OverlayUpdater overlayUpdater = null;

    /**
     * Default constructor
     */
    public ActiveDisplayBroadcastReceiver() {
    }

    /**
     * This initialised the BroadcastReceiver with reference to the FilterService that initialised
     * it to help it access vital methods from the {@link FilterService.OverlayUpdater} that help
     * with the manipulation and clean up of the active Overlay put up the FilterService such as
     * clean up and update methods.
     *
     * @param overlayUpdater this is an interface exposing FilterService methods that help with the
     *                       manipulation of the Overlay initialised by the FilterService
     *                       responsible for registering the BroadcastReceiver.
     */
    public ActiveDisplayBroadcastReceiver(FilterService.OverlayUpdater overlayUpdater) {
        this.overlayUpdater = overlayUpdater;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, action);

        // handle UPDATE_OVERLAY action
        if (action.compareTo(UserActionMonitorService.UPDATE_OVERLAY) == 0) {
            AccessibilityNodeInfo rootNode = intent.getParcelableExtra(
                    UserActionMonitorService.ROOT_NODE_INFO);
            int UPDATE_ID = intent.getIntExtra("UPDATE_ID", -1);
            // start the updateOverlay process
            if (overlayUpdater != null) {
                if (UPDATE_ID != -1) {
                    overlayUpdater.updateOverlayLayout(rootNode, UPDATE_ID);
                    Log.d(TAG, "Updating Overlay");
                }
            } else {
                Log.d(TAG, "Could not find FilterService.OverlayUpdater class to handle action in Intent");
            }
        } else if (action.compareTo(UserActionMonitorService.CLOSE_FOREGROUND_SERVICE) == 0) {
            // CLOSES THE FOREGROUND SERVICE
            // stop the service
            Log.d(TAG, "Destroying Overlay");
            overlayUpdater.destroyOverlay();
        }
    }
}
