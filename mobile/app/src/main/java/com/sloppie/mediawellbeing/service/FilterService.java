package com.sloppie.mediawellbeing.service;

import android.content.Context;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.RelativeLayout;

/**
 * This is used as a Polymorphism primitive that enables update of each FilterService spawned's
 * windowManger using {@link #updateWindowManager} and access the the Layout used in the
 * FilterService using {@link #updateRelativeLayout} to enable update.
 */
public interface FilterService {
    /**
     * This method is used to enable update of the WindowManager created by the overlay outside the
     * MonitorService that spawned the WindowManager.
     * @param UPDATE_ID this is the ID given to a thread once it starts the traversing process.
     *                  if this thread does not equal the current ID allowed to update, this method
     *                  call will not be able to update the Window
     */
    void updateWindowManager(int UPDATE_ID);

    /**
     * This method is used to update the RelativeLayout that is contained in the WindowManager
     * before the WindowManager itself is updated. This method is made thread safe to prevent
     * unsynchronized thread access to this method.
     * @param width the width to be used in LayoutParams
     * @param height the height to be used in LayoutParams
     * @param x the leftMargin to be used in LayoutParams
     * @param y the topMargin to be used in LayoutParams
     * @param UPDATE_ID this is the UPDATE_ID corresponding to these threads
     */
    void updateRelativeLayout(
            int width, int height, int x, int y, int UPDATE_ID);

    /**
     * This method is used to get the insets of the cutout.
     * This helps the threads that get the outbound coordinates rect to readjust topMargin
     * accordingly as based on the DisplayCutout (notch) size.
     * @return the size of the DisplayCutout (notch)
     */
    int getDISPLAY_CUTOUT();

    /**
     * This method exposes the {@link android.app.Service#getBaseContext()} to the Threads
     * inspecting the Layout to enable spawning of new Views.
     *
     * @return the context that the WindowManager is executing in
     */
    Context getBaseContext();

    /**
     * This method is used to carry out termination of stale Threads that will not update the
     * Layout as the UPDATE_ID is already stale.
     * @param UPDATE_ID this is the id corresponding to the ExecutorService in the BlockingQueue
     *                  that is going to be terminated.
     * @param isComplete this is the flag that helps determine the approach that will be used to
     *                   terminate the service.
     */
    void stopExecutorService(int UPDATE_ID, boolean isComplete);

    /**
     * This is used by whichever class that implements it to act as an iterface between it and a
     * BroadcastReceiver service to allow the broadcastReceiver to call methods that allow it to
     * start the process of windowUpdate in the event that a window changes and/or updates.
     */
    interface OverlayUpdater {
        /**
         * This method is used to update the overlay of a monitoring service, this method is exposed
         * to the {@link com.sloppie.mediawellbeing.receiver.ActiveDisplayBroadcastReceiver} to
         * enable remote update to overlay as based on user events gathered through the
         * {@link UserActionMonitorService}
         *
         * @param rootNode this is the rootNode of the screen after changes were detected by the
         *                 UserActionMonitoringService.
         */
        void updateOverlayLayout(AccessibilityNodeInfo rootNode);

        /**
         * This methos is exposed to the
         * {@link com.sloppie.mediawellbeing.receiver.ActiveDisplayBroadcastReceiver} to allow
         * remote trigerring of the destruction of the foreground monitoring service once no app of
         * Monitoring interest is in user visibility.
         */
        void destroyOverlay();
    }
}
