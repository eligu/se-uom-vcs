/**
 * 
 */
package gr.uom.se.util.concurrent;

import gr.uom.se.util.validation.ArgsCheck;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Elvis Ligu
 */
public class BlockingTaskScheduler extends TaskListenerAdapter implements TaskScheduler {

   /**
    * Used to find the consumer of the given task type.
    */
   private final Map<TaskType, TaskQueue> queues;

   /**
    * The number of all tasks submitted, that has not yet been executed.
    */
   private final AtomicInteger allTasksSize = new AtomicInteger(0);

   /**
    * The max number of tasks allowed to wait for execution.
    */
   private final int maxSize;

   /**
    * A default number of maximum tasks waiting for execution.
    */
   public static final int MAX_TASKS = Integer.MAX_VALUE;

   /**
    * The thread pool where tasks should be submitted for execution.
    */
   private final ExecutorService pool;

   /**
    * A flag that indicates whether a request for shutdown has been performed.
    */
   private volatile boolean shutdown = false;
   /**
    * A flag that indicates whether a request for shutdown now has been
    * performed.
    */
   private volatile boolean waitForShutdown = false;
   
   /**
    * Used when a shutdown request has been received. If the thread pool was not
    * provided this flag will be true, otherwise false. The shutdown of the pool
    * should be performed only if the pool is private, and not provided during
    * construction.
    */
   private final boolean privatePool;
   
   /**
    * Create a new instance providing the number of maximum queue size, a thread
    * pool and the types of tasks that this scheduler should manage.
    * <p>
    * If the pool is null it will be created, and maintained by this scheduler.
    * The type of the pool that is created is a cached thread pool that creates
    * threads on request.
    *
    * @param maxSize
    *           the maximum number of tasks that are waiting to be executed.
    *           This size must be greater than 0. If 0 is provided it will be a
    *           maximum default of {@link #MAX_TASKS}. A good size would be the
    *           sum of all threads required for each type plus a reasonable
    *           number of tasks, that will not cause memory leaks, in the
    *           meantime will not cause often the clients to hung when
    *           submitting tasks. If the size is smaller than the number of
    *           threads required for all types of tasks not all the threads will
    *           be utilized by the tasks.
    *           <p>
    * @param service
    *           the thread pool where all tasks will be submitted for execution.
    *           If null is provided a privately cached thread pool will be
    *           maintained and constructed by this scheduler.
    *           <p>
    * @param startWorkers
    *           if {@code true} is specified it will start all worker threads
    *           before the constructor returns. A worker thread is dedicated to
    *           one type of tasks and will handle all tasks that are of the some
    *           type. For each task type it will wait the worker thread to
    *           start. Will wait at most 1000ms for a worker, if the worker is
    *           not started it will throw an exception. It will poll the state
    *           of the worker at 1ms interval. This is a safe operation to start
    *           all workers at the creation of this scheduler, because it will
    *           ensure that all workers will be available before any task is
    *           submitted. However if there are cases when there are a lot of
    *           task types but it is unsure that clients will submit tasks of
    *           all those types then it is better to set this parameter to
    *           {@code false} avoiding to have a lot of waiting workers. On the
    *           other hand having workers to start lazily may cause problems
    *           latter when a task of the given type is started. Depending on
    *           the thread pool if all threads are to busy a worker may need
    *           more than 1000ms to start up.
    * @param types
    *           the types of the tasks that this scheduler should accept for
    *           executing. For each type there will be a dedicated thread called
    *           consumer. A consumer will maintain a private queue to store all
    *           the coming tasks of the given type. The consumer will mostly
    *           wait all the time, unless there is a task in its queue and all
    *           the scheduled for running tasks are less then the number of the
    *           max threads. In that case the consumer will schedule the task.
    *           If there are no tasks in consumer's queue he will wait until a
    *           caller submit a task of its type. In that case he will wake up
    *           and schedule the task.
    */
   public BlockingTaskScheduler(int maxSize, ExecutorService service, TaskType... types) {
      if (maxSize < 1) {
         throw new IllegalArgumentException();
      } else if (maxSize == 0) {
         maxSize = MAX_TASKS;
      }
      this.maxSize = maxSize;

      checkTypes(types);

      if (service == null) {
         service = Executors.newCachedThreadPool();
         privatePool = true;
      } else {
         privatePool = false;
      }
      this.pool = service;

      if (types == null || types.length == 0) {
         types = new TaskType[] { TaskType.Enum.UNLIMITED };
      }
      queues = new HashMap<>(types.length);
      createConsumers(types);
   }
   
   /**
    * Check task types before performing any operation. If a type is null it
    * will throw an exception. If a type's thread size is also less than 1 it
    * will throw an exception.
    *
    * @param types
    *           the task types to check.
    */
   private static void checkTypes(TaskType... types) {
      if (types == null) {
         return;
      }
      for (TaskType type : types) {
         if (type == null) {
            throw new IllegalArgumentException(
                  "types must not contain any null");
         }
         if (type.getThreadSize() < 1) {
            throw new IllegalArgumentException();
         }
      }
   }
   
   /**
    * For each type of task create a consumer that will maintain a queue and
    * will pick up from queue the oldest submitted task (if any) if to submit it
    * to thread pool for execution.
    *
    * @param types
    *           the types of tasks
    */
   private void createConsumers(TaskType... types) {
      for (TaskType type : types) {
         ThreadNumberPolicyTaskQueue queue = new ThreadNumberPolicyTaskQueue(
               pool, type.getThreadSize(), this);
         queues.put(type, queue);
      }
   }
   
   @Override
   public void schedule(Task task) {
      ArgsCheck.notNull("task", task);
      schedule(task, task.getType());
   }

   /**
    * {@inheritDoc}
    * <p>
    * If a shutdown has been initialized it will throw an exception. If the
    * number of submitted tasks reaches the limit of maximum tasks allowed to
    * submit it will hang up the client until a task has completed its execution
    * and the number of tasks is lower by one. It will throw a runtime exception
    * if he is interrupted while waiting for a task to execute in order to free
    * space for new tasks.
    *
    */
   @Override
   public void schedule(Runnable task, TaskType type) {
      if (shutdown || waitForShutdown) {
         throw new RuntimeException("this scheduler has been shutdown");
      }
      TaskQueue consumer = queues.get(type);
      if (consumer == null) {
         throw new IllegalArgumentException(
               "there is not a defined task type for: " + type);
      }
      
      // Make an atempt to start consumer if he is not started
      schedule(consumer, task);
   }

   /**
    * Wait if the number of submitted tasks that are waiting exceed the number
    * of maximum tasks allowed. While waiting it will be notified by a a task
    * that it has finished so he can submit the task to the consumer.
    */
   private void schedule(TaskQueue queue, Runnable task) {

      scheduleOrWaitIfFull(queue, task);
   }

   private void scheduleOrWaitIfFull(TaskQueue queue, Runnable task) {
      try {
         // Wait until a task finishes
         // and notify this
         while (allTasksSize.get() >= maxSize) {
            synchronized (this) {
               // We should check here again to ensure that
               // no task has finished and we didn't got any
               // signal
               if (allTasksSize.get() < maxSize) {
                  continue; // Better make a double check before submitting
               }
               wait(100000); // Just to ensure that will not wait endlessly
               if(isShutdown()) {
                  return;
               }
            }
         }
         scheduleToConsumer(queue, task);
      } catch (InterruptedException ex) {
         throw new RuntimeException(ex);
      }
   }

   private void scheduleToConsumer(TaskQueue queue, Runnable task) {
      // Add the task to consumer's queue and notify him
      // if he is waiting for a task to be added
      allTasksSize.incrementAndGet();
      queue.enqueueAndSchedule(task);
   }

   @Override
   public boolean canSchedule(TaskType type) {
      TaskQueue consumer = queues.get(type);
      return consumer != null && !isShutdown();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public synchronized void completed(Runnable task) {
      allTasksSize.decrementAndGet();
      notify();
      if(privatePool && isTerminated()) {
         pool.shutdown();
      }
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public void abborted(Runnable task) {
      completed(task);
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   public synchronized void shutdown() throws InterruptedException {
      shutdown = true;
      for(TaskQueue queue : queues.values()) {
         queue.shutdown();
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public synchronized void shutdownNow() throws InterruptedException {
      shutdown = true;
      for(TaskQueue queue : queues.values()) {
         queue.shutdownNow();
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean isShutdown() {
      return shutdown; // Optimistic view
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean isTerminated() {
      return isShutdown() && allTasksSize.get() == 0; // Optimistic view
   }
}
