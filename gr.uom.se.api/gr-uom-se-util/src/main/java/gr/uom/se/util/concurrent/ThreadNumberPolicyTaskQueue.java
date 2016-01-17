/**
 * 
 */
package gr.uom.se.util.concurrent;

import gr.uom.se.util.validation.ArgsCheck;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Elvis Ligu
 */
public class ThreadNumberPolicyTaskQueue implements TaskQueue {

   /** Tasks queue */
   private final Queue<Runnable> queue;

   /** Number of maximum threads this consumer should allow */
   private final int maxThreads;

   /**
    * The thread pool where tasks should be submitted for execution.
    */
   private final ExecutorService pool;

   /** Number of currently scheduled jobs (considered as running) */
   private final AtomicInteger scheduled = new AtomicInteger(0);

   /** Tell whether this queue is shutdown. */
   private volatile boolean shutdown = false;
   private volatile boolean shutdownNow = false;

   private final TaskListener taskListener;
   
   /**
    * 
    */
   public ThreadNumberPolicyTaskQueue(ExecutorService pool, int threads, TaskListener listener) {
      ArgsCheck.notNull("pool", pool);
      this.pool = pool;
      ArgsCheck.greaterThanOrEqual("threads", "1", threads, 1);
      maxThreads = threads;
      this.queue = new ConcurrentLinkedQueue<Runnable>();
      if(listener == null) {
         listener = new TaskListenerAdapter();
      }
      this.taskListener = listener;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void enqueueAndSchedule(Runnable task) {
      // Enqueue first the task
      ArgsCheck.notNull("task", task);

      // Check if this is shutdown throw an exception
      if (shutdown) {
         throw new IllegalStateException("queue is shutdown");
      }
      // Enqueue the task
      this.queue.add(task);
      
      // Schedule any remaining
      scheduleRemaining();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void scheduleRemaining() {
      if (queue.isEmpty()) {
         return;
      }

      if (scheduled.get() >= maxThreads) {
         return;
      }

      synchronized (this) {
         // Double check pattern
         if (queue.isEmpty()) {
            return;
         }

         // If there are more than allowed scheduled just return
         // otherwise try to schedule
         while (scheduled.get() < maxThreads) {
            // Take the next task
            Runnable task = queue.poll();
            if (task == null) {
               return;
            }
            // Try to schedule a task
            try {
               scheduled.incrementAndGet(); // Increment the number of scheduled
                                            // tasks
               pool.submit(new TaskWrapper(task, this));
               
               // Send notification
               taskListener.submitted(task);
               
            } catch (Exception ex) {
               scheduled.decrementAndGet();
               taskListener.abborted(task);
               throw new RuntimeException(ex);
            }
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void shutdown() {
      this.shutdown = true;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public synchronized void shutdownNow() {
      shutdown();
      shutdownNow = true;
      this.queue.clear();
   }

   /**
    * A task is a wrapper for each new task that is submitted to the scheduler.
    * When a task finishes its job, it will notify the consumer if the consumer
    * is waiting because the number of maximum threads he can handle has been
    * reached. If a shutdown request has been received it will prevent the
    * enclosing task to run, also it will notify the consumer for any kind of
    * shutdown requests that it has finished.
    */
   private static class TaskWrapper implements Runnable {

      final ThreadNumberPolicyTaskQueue queue;
      final Runnable runnable;

      public TaskWrapper(Runnable runnable, ThreadNumberPolicyTaskQueue queue) {
         this.runnable = runnable;
         this.queue = queue;
      }

      @Override
      public void run() {
         boolean completed = false;
         try {
            // Terminate if shutdown now has
            // been requested
            if (queue.shutdownNow) {
               return;
            }
            queue.taskListener.started(runnable);
            runnable.run();
            completed = true;
         } finally {

            queue.scheduled.decrementAndGet();
            queue.scheduleRemaining();

            // Notify the scheduler if he is waiting for
            // tasks to submit
            if(completed) {
               queue.taskListener.completed(runnable);
            } else {
               queue.taskListener.abborted(runnable);
            }
         }
      }
   }
}
