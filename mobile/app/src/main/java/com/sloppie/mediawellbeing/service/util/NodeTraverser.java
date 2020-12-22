package com.sloppie.mediawellbeing.service.util;

import android.graphics.Rect;
import android.util.Log;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.RelativeLayout;

import com.sloppie.mediawellbeing.service.FilterService;


public class NodeTraverser implements Runnable {
    // TAG for Logging
    public static String TAG = "com.sloppie.mediawellbeing.service.util:NodeTraverser";

    private final FilterService filterService;
    private final AccessibilityNodeInfo node;
    private final int UPDATE_ID;
    private NodeTraverser parent = null;

    private int THREAD_COUNT = 0;
    private int COMPLETE_THREAD_COUNT = 0;
    private boolean THREAD_SPAWN_COMPLETE = false;

    public NodeTraverser(
            FilterService filterService, AccessibilityNodeInfo node,
            int UPDATE_ID, NodeTraverser parent) {
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
     */
    public void traverseNode() {
       // TODO: to reduce the node traversing useless nodes, add filters to only ViewGroups
        try {
            if (((String) node.getClassName()).compareTo("android.widget.ImageView") == 0) {
                // TODO: create a View with the Rect coordinates and a boundary and inflate the RelativeLayout with the LayoutParams coordinates
                Rect imageViewBounds = new Rect();
                node.getBoundsInScreen(imageViewBounds);
                int width = imageViewBounds.right - imageViewBounds.left;
                int height = imageViewBounds.bottom - imageViewBounds.top;
                int x = imageViewBounds.left;
                int y = imageViewBounds.top;

                View newImageViewContainer = new View(filterService.getBaseContext());
                RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(width, height);
                rlp.leftMargin = x;
                rlp.topMargin = y;
                Log.d(TAG, imageViewBounds.toShortString());

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
            Log.d(TAG, "HERE: " + e.toString());
        }
    }

    // thread-safe
    public synchronized void childThreadComplete() {
        COMPLETE_THREAD_COUNT++;

        if (parent == null && ((COMPLETE_THREAD_COUNT == THREAD_COUNT) && THREAD_SPAWN_COMPLETE)) {
            filterService.updateWindowManager(UPDATE_ID);
        } else if (THREAD_COUNT == COMPLETE_THREAD_COUNT && THREAD_SPAWN_COMPLETE) {
            parent.childThreadComplete(); // notifies parent that the child thread is complete
        }
    }
}
