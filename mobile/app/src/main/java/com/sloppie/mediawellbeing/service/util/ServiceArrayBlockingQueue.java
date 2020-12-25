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
                Log.d("ServiceArrBlockingQueue", "Service added with UPDATE_ID" + UPDATE_ID);
            }
        } else {
            activeServices.add(newService);
            activeServiceIDs.add(UPDATE_ID);
            Log.d("ServiceArrBlockingQueue", "Service added" + (UPDATE_ID));
            added = true;
        }

        return added;
    }

    /**
     * This is used to shutdown an ExecutorService that is currently executing. This does so by
     * referencing the activeServiceIDs field to find a the index of the ExecutorService.
     * in the Event that the ExecutorService has already been spliced out and execution stopped,
     * this means that this Thread was not able to be terminated thus is just let it finish off
     * executing.
     * This function also handles the clean up of all failed Executor shutdowns of all services that
     * were unable to close after completion and are now just stake.
     * @param UPDATE_ID this is the ID associated with the Executor service that wants to be
     *                  terminated.
     * @param isComplete this helps determine the method that will be used when shutting down the
     *                   ExecutorService.
     */
    public synchronized void blockingRemove(int UPDATE_ID, boolean isComplete) {
        // there is a bug that hasnt been fixed that allows for Executors that have finished
        // executing to remain not shut down. As such, these executors need to be removed and
        // manually shutdown.
        ArrayList<Integer> staleExecutors = new ArrayList<>();

        if (activeServiceIDs.contains(UPDATE_ID)) {
            int serviceIndex = activeServiceIDs.indexOf(UPDATE_ID);

            // remove the service and the reference to its ID
            if (!isComplete) {
                activeServices.get(serviceIndex).shutdownNow(); // shutdown all active threads
                Log.d("ServiceArrBlockingQueue", "STALE_UPDATE Service removed");
            } else {
                // shutdown after all Threads have finished execution as this service has updated
                // the Layout
                activeServices.get(serviceIndex).shutdown();
                Log.d("ServiceArrBlockingQueue", "Service removed, FRESH_UPDATE");
            }
            activeServices.remove(serviceIndex);
            activeServiceIDs.remove(serviceIndex);

        } else {
            Log.d("ServiceArrBlockingQueue", "Service already removed");
        }

        // a stale Executor is qualified by being an UPDATE_ID smaller than the UPDATE_ID that
        // was scheduled for removal from the activeServices.
        // find all stale Executors while shutting them down
        for (int i=0; i<activeServiceIDs.size(); i++) {
            if (activeServiceIDs.get(i) < UPDATE_ID) {
                staleExecutors.add(i);
                activeServices.get(i).shutdownNow(); // shutdown the stale executor
            }
        }

        // removing all shutdown stale Executors
        for (int i=0; i<staleExecutors.size(); i++) {
            int removableIndex = staleExecutors.get(i);
            activeServices.remove(removableIndex);
            activeServiceIDs.remove(removableIndex);
        }
    }
}
