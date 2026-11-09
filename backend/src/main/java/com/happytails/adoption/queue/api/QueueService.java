
package com.happytails.adoption.queue.api;

public interface QueueService {

    /**
     * Checks whether the application is first in its animal's queue.
     */
    boolean isFirstInQueue(Long applicationId);

    /**
     * Returns the application's current queue position.
     * Position 1 means first in queue.
     * Returns 0 if the application is not in the active queue.
     */
    int getQueuePosition(Long applicationId);

    /**
     * Marks an application as WALK_SCHEDULED
     * after a walk has been successfully booked.
     */
    void markWalkScheduled(Long applicationId);
}
