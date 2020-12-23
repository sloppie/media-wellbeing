package com.sloppie.mediawellbeing.service.util;

import android.graphics.Rect;
import android.util.Log;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.RelativeLayout;

import com.sloppie.mediawellbeing.service.FilterService;


/**
 * This is a utilitarian class that is run on different threads to ease the process of traversing
 * the nodes in the active window from the main UIThread. This thread has the ability to spawn other
 * threads as such has to keep active monitoring of all the active threads that were executed by it.
 * After all threads under this specific class complete execution, the thread also notifies its
 * parent thread to allow for the carrying out of the relevant action which includes recursively
 * calling {@link #childThreadComplete()} until the root parent thread gets the signal to call the
 * {@link com.sloppie.mediawellbeing.service.FilterService#updateWindowManager} to update the screen
 * after successful traversal and all the ImageViews have been found.
 */
public class NodeTraverser implements Runnable {
    // TAG for Logging
    public static String TAG = "com.sloppie.mediawellbeing.service.util:NodeTraverser";

    // this is the parent MonitoringService that will handle all the UIThread operations that need
    // to be carried out.
    private final FilterService filterService;
    // node being traversed by this specific Thread
    private final AccessibilityNodeInfo node;
    // the corresponding UPDATE_ID that will be used as an ID to validate whether a certain thread
    // is allowed to commit UI operations on the overlay.
    private final int UPDATE_ID;
    // if this is a child node containing a parent, this bears reference to the Node's parent,
    // otherwise this is null for the Root thread that is incharge of performing Window updates
    // after all it's child threads are complete traversing all the Nodes.
    private NodeTraverser parent = null;

    // this is used to keep track of how many child threads have been spawned.
    private int THREAD_COUNT = 0;
    // keeps track of how many Child threads have completed execution
    private int COMPLETE_THREAD_COUNT = 0;
    // notifies if this Thread instance is finished spawning child threads
    private boolean THREAD_SPAWN_COMPLETE = false;

    public NodeTraverser(
            FilterService filterService, AccessibilityNodeInfo node,
            int UPDATE_ID, NodeTraverser parent)
    {
        this.filterService = filterService;
        this.node = node;
        this.UPDATE_ID = UPDATE_ID;
        this.parent = parent;
    }

    public NodeTraverser(FilterService filterService, AccessibilityNodeInfo node, int UPDATE_ID) {
        this.filterService = filterService;
        this.node = node;
        this.UPDATE_ID = UPDATE_ID;
    }

    @Override
    public void run() {
        traverseNode(); // start recursive thread
    }

    /**
     * This method is used to traverse the AccessibilityNode from the rootViewInActiveWindow
     * to all the child nodes looking for all ImageViews present in a display. After an
     * ImageView is found, the method calls on the
     * {@link FilterService#updateRelativeLayout)} method to
     * update the RelativeLayout with the bounds of the found ImageView.
     * The actual updating is handled by the UIThread hence the inflation and positioning of View
     * is handled by the Looper.main thread through the FilterService class.
     */
    public void traverseNode() {
       // TODO: to reduce the node traversing useless nodes, add filters to only ViewGroups
        try {
            if (((String) node.getClassName()).compareTo("android.widget.ImageView") == 0) {
                // get bounds of the ImageView
                Rect imageViewBounds = new Rect();
                node.getBoundsInScreen(imageViewBounds);

                // this values will be used by the UIThread to create and position the View
                int width = imageViewBounds.right - imageViewBounds.left;
                int height = imageViewBounds.bottom - imageViewBounds.top;
                int x = imageViewBounds.left;
                // gets the Display Coordinates but subtracts the DisplayCutout
                // (gotten through FilterService) to convert to Window Coordinates.
                int y = (imageViewBounds.top - filterService.getDISPLAY_CUTOUT());

                // update the relative layout
                filterService.updateRelativeLayout(width, height, x, y, UPDATE_ID);

                // issue a complete signal to parent
                parent.childThreadComplete();
            } else if (node.getChildCount() != 0) {
                for (int i = 0; i < node.getChildCount(); i++) {
                    // initialise thread to traverse child and then update THREAD_COUNT
                    try {
                        // initialise childNode traverser
                        NodeTraverser childNodeTraverser =
                                new NodeTraverser(
                                        filterService, node.getChild(i), UPDATE_ID, this);
                        // initialise new Thread
                        Thread newThread = new Thread(childNodeTraverser);
                        // start the thread
                        newThread.start();
                        THREAD_COUNT++; // increase thread count
                    } catch (Exception e) {
                        Log.d(TAG, e.toString());
                    }
                }
                // indicates that this class will not create any more threads thus in the case of the
                // children threads completing, the thread should be allowed to
                // call Parent#childThreadComplete
            } else {
                parent.childThreadComplete();
            }

            THREAD_SPAWN_COMPLETE = true;
        } catch (Exception e) {
            Log.d(TAG, e.toString());
        }
    }

    // thread-safe

    /**
     * This method handles the communication of a child thread communicating that they have finished
     * their operations back to the parent. This helps the parent keep track of the state of
     * execution and make sure that it also only notifies it parent of execution once all the
     * children it has spawned have also completed execution.
     */
    public synchronized void childThreadComplete() {
        COMPLETE_THREAD_COUNT++;

        if (parent == null && ((COMPLETE_THREAD_COUNT == THREAD_COUNT) && THREAD_SPAWN_COMPLETE)) {
            filterService.updateWindowManager(UPDATE_ID);
        } else if (THREAD_COUNT == COMPLETE_THREAD_COUNT && THREAD_SPAWN_COMPLETE) {
            parent.childThreadComplete(); // notifies parent that the child thread is complete
        }
    }
}
