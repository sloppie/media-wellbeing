package com.sloppie.mediawellbeing.service.util;

import android.util.Log;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;

/**
 * This class is used to hold all the the active {@link java.util.concurrent.ExecutorService} that
 * are running in the background, this allows for all these services to be accessible and allow
 * for cancellation by the {@link com.sloppie.mediawellbeing.service.FilterService} that is managing
 * the overlay that is on the screeen. This is with effort to make the whole rendering on the screen
 * service more efficient computationally.
 * This class ensures that any new ExecutorService added is in order of the UPDATE_ID and ensures
 * that the most recent ExecutorService continues running and that all other ExecutorService are
 * stopped using {@code ExecutorService.shutdownNow} to help save up on resources.
 */
public class ServiceArrayBlockingQueue {

    /** this is a list of the services that are currently active in the FilterService*/
    final ArrayList<ExecutorService> activeServices;
    final ArrayList<Integer> activeServiceIDs;

    public ServiceArrayBlockingQueue() {
        activeServices = new ArrayList<>();
        activeServiceIDs = new ArrayList<>();
    }

    /**
     * This function is used to handle the addition of new Executors to the Service Blocking Queue.
     * To ensure that only threads that are only added if they are the most recent, as a first line
     * of defence, if the UPDATE_ID that accompanies this executor service is less than the current
     * most recent addition, by default this service is not added to the pool and will thus be
     * shutdown immediately.
     *
     * @return a boolean on whether the ExecutorService was added.
     */
    public synchronized boolean blockingPut(ExecutorService newService, int UPDATE_ID) {
        boolean added = false;

        if (activeServices.size() > 0) {
            int mostRecent = activeServiceIDs.get((activeServiceIDs.size() - 1));
            if (mostRecent < UPDATE_ID) {
                activeServices.add(newService);
                activeServiceIDs.add(UPDATE_ID);
                added = true;
                Log.d("ServiceArrBlockingQueue", "Service added");
            }
        } else {
            activeServices.add(newService);
            activeServiceIDs.add(UPDATE_ID);
            Log.d("ServiceArrBlockingQueue", "Service added");
            added = true;
        }

        return added;
    }

    /**
     * This is used to shutdown an ExecutorService that is currently executing. This does so by
     * referencing the activeServiceIDs field to find a the index of the ExecutorService.
     * in the Event that the ExecutorService has already been spliced out and execution stopped,
     * this means that this Thread was not able to be terminated thus is just let t finish off
     * executing.
     * @param UPDATE_ID this is the ID associated with the Executor service that wants to be
     *                  terminated.
     * @param isComplete this helps determine the method that will be used when shutting down the
     *                   ExecutorService.
     */
    public synchronized void blockingRemove(int UPDATE_ID, boolean isComplete) {
        if (activeServiceIDs.contains(UPDATE_ID)) {
            int serviceIndex = activeServiceIDs.indexOf(UPDATE_ID);
            // remove the service and the reference to its ID
            if (!isComplete) {
                activeServices.get(serviceIndex).shutdownNow(); // shutdown all active threads
            } else {
                // shutdown after all Threads have finished execution as this service has updated
                // the Layout
                activeServices.get(serviceIndex).shutdown();
            }
            activeServices.remove(serviceIndex);
            activeServiceIDs.remove(serviceIndex);
            Log.d("ServiceArrBlockingQueue", "Service removed");
        } else {
            Log.d("ServiceArrBlockingQueue", "Service already removed");
        }
    }
}
