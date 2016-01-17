/**
 * 
 */
package gr.uom.se.util.concurrent;

/**
 * @author Elvis Ligu
 */
public interface TaskQueue {
   
   /**
    * Add this task to the queue in order to schedule it some time in the future.
    * 
    * @param task to be added in the queue, and executed some time in the future.
    */
   void enqueueAndSchedule(Runnable task);
   
   /**
    * If there are tasks in the queue that are not scheduled due to
    * some policy then schedule them.
    * <p>
    * Note this may schedule only some of the enqueued tasks (depending on the implementation).
    * If there are no tasks in the queue it will schedule nothing.
    */
   void scheduleRemaining();
   
   /**
    * Initiate a shutdown event without preventing the enqueued tasks to be scheduled.
    * <p>
    * Any attempt to call {@link #scheduleRemaining()} will work. However if {@link #enqueueAndSchedule(Runnable)}
    * is called it will throw an exception. NOTE: this operation will not affect running or scheduled tasks.
    */
   void shutdown();
   
   /**
    * Initiate a shutdown event by discarding any queued tasks to be scheduled.
    * <p>
    * Any attempt to call {@link #scheduleRemaining()} or {@link #enqueueAndSchedule(Runnable)} will throw
    * an exception. NOTE: this operation will not affect running or scheduled tasks.
    */
   void shutdownNow();
}
