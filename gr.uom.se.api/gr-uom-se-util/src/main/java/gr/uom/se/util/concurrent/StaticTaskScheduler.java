package gr.uom.se.util.concurrent;

import gr.uom.se.util.validation.ArgsCheck;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * An implementation of a static task scheduler that wraps an
 * {@link ExecutorService} a.k.a thread pool.
 * <p>
 * Static means that, all the types of tasks that can be submitted to this
 * scheduler should be specified at the construction. Any other task of an
 * unknown type that is submitted to this scheduler will cause an exception.
 * <p>
 * When the system needs to perform a heavy task which uses most of the
 * available resources it is advisable to do those tasks in serial otherwise two
 * tasks of this kind may cause memory leaks for the entire system. In order to
 * have tasks that performs such heavy duty works it should be stated at the
 * creation of instances of this implementation. Generally speaking to have
 * tasks of this type a client is not required to construct a new
 * {@link TaskType} object. He can use {@link TaskType.Enum#SINGLE_THREAD} as a
 * type. If clients need such tasks they should provide this type when
 * constructing instances of this scheduler. A client then may call
 * {@link #schedule(Runnable, TaskType)} for a task and pass as a type the
 * {@link TaskType.Enum#SINGLE_THREAD}.
 * <p>
 * When the system needs to perform tasks that makes a moderate use of resources
 * then they can be run in parallel, however the number of those tasks that can
 * be run in parallel may be limited. In order to have tasks of this type that
 * can be scheduled in parallel but the number of threads running them should be
 * limited, the client should create a task type such as
 * {@link TaskType.Enum#get(int, Object)} where he can pass the number of
 * threads this type of tasks may have. This type of tasks should be then passed
 * to the constructor of task scheduler.
 * <p>
 * When the system needs to perform quick tasks that do not make use of
 * resources (or use very little resources) such as notifiers he may define a
 * type that specifies a very large number of threads, or may use the predefined
 * type {@link TaskType.Enum#UNLIMITED}. However keep in mind that if the thread
 * pool has a very limited number of threads and there are a lot of tasks of
 * this type, then they can cause starvation for other task types (such as those
 * described above) because the threads will be shared amongst all types of
 * tasks. Although in real scenario this is not always true because when a task
 * of a given type has finished the oldest task of the same type will be picked
 * for execution (implementing a FIFO algorithm), so whether a task suffer from
 * starvation or not it depends from the types of submitted tasks.
 * <p>
 * This scheduler will need at least x threads where x is the number of types of
 * tasks that are specified during construction. That because it will start a
 * thread (a.k.a consumer) for each type of task that will take care of
 * scheduling all the tasks of the given type. However those threads will
 * consume very little resources and will try not interfering with the running
 * tasks. Mostly they will sleep, and each time a task of a specific type is
 * available its consumer will wake up to schedule it for execution.
 * <p>
 * Generally speaking the thread pool is limited so when calling
 * {@link #schedule(java.lang.Runnable)} the caller should expect its job to
 * possibly wait for a thread to be available. Although, practically the pool
 * should be large enough to allow for quick tasks to perform without waiting.
 * On the other hand the waiting of moderate tasks is highly connected to the
 * number of threads that are available for them. While the heavy duty tasks
 * have only one thread in their disposal.
 * <p>
 * A proposed strategy of creating this schedule is to define a thread pool that
 * has enough threads to handle all types of tasks, such as in the following
 * snippet:
 * 
 * <pre>
 *  // Define a thread pool of 60 threads
 *  ExecutorService threadPool = Executors.newFixedThreadPool(60);
 * 
 *  // Specify the number of maximum tasks that can be submitted
 *  // Any client that submit a task after this limit has been reached
 *  // will hung (will wait) until a previous task has finished
 *  int maxSize = 5000;
 * 
 *  // Define heavy duty tasks that should not run in parallel otherwise the
 *  // system may suffer from memory
 *  TaskType heavyTasks = TaskType.Enum.SINGLE_THREAD;
 * 
 *  // Define moderate tasks that only 4 of them may run in parallel
 *  TaskType moderateTasks = TaskType.Enum.get(4, "MODERATE_TASKS");
 * 
 *  // Define quick tasks that should run in parallel, however only
 *  // 52 of them may run simultaneously
 *  TaskType quickTasks = TaskType.Enum.get(52, "QUICK_TASKS");
 * 
 *  // Create the scheduler with the given parameters
 *  TaskScheduler scheduler = new StaticTaskScheduler(
 *  maxSize, threadPool, heavyTasks, moderateTasks, quickTasks);
 * 
 *  // Submit some job
 *  Runnable r1 = ...
 *  Runnable r2 = ...
 *  Runnable r3 = ...
 * 
 *  scheduler.schedule(r1, heavyTasks);
 *  scheduler.schedule(r2, moderateTasks);
 *  scheduler.schedule(r3, quickTasks);
 * 
 *  // Notify shceduler to shutdown, without interrupting the
 *  // already submitted tasks. All the consumers will be interrupted
 *  // to
 *  shceduler.shutdown(); // this may wait long time
 * 
 *  // Or notify the scheduler to shutdown immediately
 *  // by stopping him to schedule new tasks for execution.
 *  // This will cause all tasks that are waiting in queue to
 *  // be dismissed, however the running tasks will not be
 *  // interrupted
 *  scheduler.shutdownNow(); // Wait for running tasks to stop and shutdown
 * </pre>
 *
 * In the previous example the scheduler was provided a thread pool. This is
 * very important in managed environments such as application servers which
 * provide instances of ManagedExecutorService. Using this scheduler the
 * applications may incorporate policies for different types of tasks without
 * the need to interfere with the environment, because the scheduler is used as
 * a wrapper for the thread pool. So the application doesn't need to know the
 * specifics of the environment and how to tell the server to maintain different
 * pools for different types of tasks. On the other hand when a scheduler is
 * used in non managed environments such as standalone applications, the
 * application may choose to have a task scheduler as a central point for
 * managing its threads. That been told when a scheduler is not provided a
 * thread pool it will create an instance of cached thread pool
 * (Executors.newCachedThreadPool()) and will use as many threads as needed
 * depending on the needs of the application.
 * <p>
 * <b>NOTE</b>: When shutting down the scheduler if the thread pool was provided
 * provided it will not affect the pool at all, it will just tell the scheduler
 * to stop accepting any new tasks. Depending on what type of shutdown has been
 * requested it will executes all tasks that are waiting (if a shutdown) and
 * then shutdown, or will discard any waiting task (if a shutdown now). This is
 * crucial in managed environments to not stop the managed thread pool. On the
 * other hand if the thread pool was not provided it will be created and
 * maintained by this scheduler, thus when shutting down the scheduler will
 * shutdown the pool to.
 * <p>
 * NOTE: this scheduler will not use any memory at all if there are no tasks
 * waiting. It will not create queues of fix sizes, instead it will uses linked
 * queues that are added and removed tasks as they are submitted or executed.
 * <p>
 * <b>WARNING:</b> When creating tasks using the scheduler make sure the tasks
 * you are creating doesn't create other tasks using the scheduler. That will
 * potentially create conditions for deadlock, especially in case the queue of
 * the scheduler is near the end (almost full) and a task you create is blocked
 * because its waiting to submit a new task to scheduler. In such case the first
 * task will wait for the second to finish but the second will block at
 * submitting a task to the scheduler. This may happen when a job creates
 * multiply tasks that can exceed the queue size of this scheduler!
 * 
 * @author Elvis Ligu
 */
public class StaticTaskScheduler implements TaskScheduler {

   /**
    * Used to find the consumer of the given task type.
    */
   private final Map<TaskType, Consumer> consumers;

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
   private final AtomicBoolean shutdown = new AtomicBoolean(false);
   /**
    * A flag that indicates whether a request for shutdown now has been
    * performed.
    */
   private final AtomicBoolean waitForShutdown = new AtomicBoolean(false);

   /**
    * The number of running customers at this moment, used when shutdown is
    * initialized.
    */
   private final AtomicInteger runningCustomers;

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
   public StaticTaskScheduler(int maxSize, ExecutorService service,
         TaskType... types) {
      if (maxSize < 0) {
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
      consumers = new HashMap<>(types.length);
      runningCustomers = new AtomicInteger(types.length);
      createConsumers(types);
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
         Consumer consumer = new Consumer(
               new ConcurrentLinkedQueue<Runnable>(), type.getThreadSize());
         consumers.put(type, consumer);
         pool.submit(consumer);
      }
   }

   /**
    * Check task types before performing any operation. If a type is null it
    * will throw an exception. If a type's thread size is also less than 1 it
    * will throw an exception.
    *
    * @param types
    *           the task types to check.
    */
   private void checkTypes(TaskType... types) {
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
      if (shutdown.get() || waitForShutdown.get()) {
         throw new RuntimeException("this scheduler has been shutdown");
      }
      Consumer consumer = consumers.get(type);
      if (consumer == null) {
         throw new IllegalArgumentException(
               "there is not a defined task type for: " + type);
      }
      if (!consumer.running.get()) {
         throw new RuntimeException("runner is unavailable for tasks of type: "
               + type);
      }
      schedule(consumer, task);
   }

   /**
    * Wait if the number of submitted tasks that are waiting exceed the number
    * of maximum tasks allowed. While waiting it will be notified by a a task
    * that it has finished so he can submit the task to the consumer.
    */
   private void schedule(Consumer consumer, Runnable task) {

      waitIfFull();
      scheduleToConsumer(consumer, task);
   }

   private void waitIfFull() {
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
               wait();
            }
         }
      } catch (InterruptedException ex) {
         throw new RuntimeException(ex);
      }
   }

   private void scheduleToConsumer(Consumer consumer, Runnable task) {
      allTasksSize.incrementAndGet();
      // Add the task to consumer's queue and notify him
      // if he is waiting for a task to be added
      consumer.queue.add(task);
      synchronized (consumer) {
         consumer.notify();
      }
   }

   @Override
   public boolean canSchedule(TaskType type) {
      Consumer consumer = consumers.get(type);
      return consumer != null && consumer.running.get();
   }

   /**
    * {@inheritDoc}
    * <p>
    * Initiate a shutdown but wait all other submitted threads to finish their
    * execution. However do not accept any more tasks. If the thread pool is
    * maintained by this scheduler then shutdown the pool to.
    *
    * @throws java.lang.InterruptedException
    */
   @Override
   public void shutdown() throws InterruptedException {
      waitForShutdown.set(true);
      notifyAndWait();
      if (privatePool) {
         pool.shutdown();
      }
   }

   private void notifyAndWait() throws InterruptedException {
      // Notify first the consumers because some of them
      // may be waiting for scheduler to submit new tasks
      // and wait so we should tell them that we initiated a
      // wait for shutdown
      for (Consumer c : this.consumers.values()) {
         synchronized (c) {
            c.notify();
         }
      }
      // Will be notified by consumers that it
      // has stopped the work
      synchronized (this) {
         while (runningCustomers.get() > 0) {
            wait();
         }
      }
   }

   /**
    * {@inheritDoc}
    * <p>
    * Shutdown immediately. If the thread pool is maintained by this scheduler
    * it will call this pool to shutdown now. If the pool was provided at
    * construction time it will initiate a shutdown event, that will cause any
    * submitted task that are not yet executed to be discarded, however will not
    * break the execution of tasks that are already executing. Calls to this
    * method will release as soon as possible.
    */
   @Override
   public void shutdownNow() throws InterruptedException {
      shutdown.set(true);
      notifyAndWait();
      if (privatePool) {
         pool.shutdownNow();
      }
   }

   @Override
   public boolean isShutdown() {
      return shutdown.get() || waitForShutdown.get();
   }

   @Override
   public boolean isTerminated() {
      return runningCustomers.get() == 0;
   }

   /**
    * A consumer is a special thread that maintains a queue and is bounded to
    * one type of tasks. All task of the same type should be submitted to the
    * same consumer, therefore to its queue. A consumer will get each task from
    * its queue and submit it to the thread pool until he reaches the maximum
    * number of threads he can use. After that he will wait for a task of the
    * same type to finish its work and lower the number of running tasks, by
    * notifying this consumer. If the consumer has no tasks in his queue he will
    * wait until the scheduler notifies him for a new task that has arrived. If
    * a request for shutdown has been issued the consumer will stop submitting
    * any new tasks to the thread pool, will terminate him self, will wait for
    * all running tasks to finish and will notify the scheduler that has
    * finished. In a wait termination request the procedure is the same, however
    * he will continue to submit all the tasks of his queue.
    */
   private class Consumer implements Runnable {

      /** Tasks queue */
      Queue<Runnable> queue;
      /** Number of maximum threads this consumer should allow */
      final int maxThreads;
      /** Number of currently scheduled jobs (considered as running) */
      final AtomicInteger scheduled = new AtomicInteger(0);
      /** Flag when it is true the consumer is running */
      final AtomicBoolean running;

      public Consumer(Queue<Runnable> queue, int threads) {
         this.queue = queue;
         this.maxThreads = threads;
         running = new AtomicBoolean(false);
      }

      @Override
      public void run() {

         try {
            // Set running true, in case it is needed by
            // scheduler
            running.set(true);

            while (true) {
               // Try to get the next task
               // Wait if no task is available
               Runnable task = nextTask();

               // If task is null that means there has been a shutdown request
               // while trying to get the task so we stop here
               if (task == null) {
                  return;
               }
               // Terminate if shutdown now
               // has been called, do not make any scheduling at this
               // point
               if (shutdown.get()) {
                  // Notify the scheduler once because he is waiting
                  // for this consumer to shutdown
                  waitAndNotify();
                  return;
               }

               // Wait until a task finishes and lower the number
               // of running threads for this consumer, and notifying
               // this.
               waitForAvailableThread();

               // A thread is available so now we can schedule the given task
               task = new TaskWrapper(task, this);
               // Submit the task here after waiting or not
               pool.submit(task);
            }
         } catch (InterruptedException ex) {
            // Notify scheduler that I am stopped
            notifyScheduler();
            // and throw an uncaught exception
            throw new RuntimeException(ex);
         }
      }

      private Runnable nextTask() throws InterruptedException {
         Runnable task = null;
         // Try to get the next task from queue
         // and if a task is not available than wait
         // for scheduler to add a task to this queue
         while ((task = queue.poll()) == null) {
            // If the scheduler has been requested
            // a shut down do not wait for new tasks
            // just return
            if (waitForShutdown.get() || shutdown.get()) {
               // Notify the scheduler once because he is waiting
               // for this consumer to shutdown
               waitAndNotify();
               return null;
            }
            // Should wait until the scheduler
            // notify this for new task
            synchronized (this) {
               // Query again the queue
               // at this point we will be sure that scheduler
               // will not notify us if he is adding a task to
               // our queue
               if ((task = queue.poll()) != null) {
                  break;
               }
               wait();
            }
         }
         return task;
      }

      private void waitForAvailableThread() throws InterruptedException {
         while (scheduled.get() >= maxThreads) {
            // Should wait until a task notify this
            synchronized (this) {
               // Check again here after entering the synchronized block
               // to ensure that we do not enter in sleep while a task
               // has finished but didn't got the lock of this consumer
               // before this
               if (scheduled.get() < maxThreads) {
                  // Do not wait if there is room to submit
                  // the task
                  continue;
               }
               wait();
            }
         }
      }

      private void waitAndNotify() throws InterruptedException {
         try {
            waitMyTasks();
         } finally {
            notifyScheduler();
            // Clear my queue to free memory
            queue.clear();
         }
      }

      private void notifyScheduler() {
         // Notify the scheduler once because he is waiting
         // for this consumer to shutdown
         synchronized (StaticTaskScheduler.this) {
            running.set(false);
            runningCustomers.decrementAndGet();
            StaticTaskScheduler.this.notify();
         }
      }

      private void waitMyTasks() throws InterruptedException {
         while (scheduled.get() > 0) {
            synchronized (this) {
               wait();
            }
         }
      }
   }

   /**
    * A task is a wrapper for each new task that is submitted to the scheduler.
    * When a task finishes its job, it will notify the consumer if the consumer
    * is waiting because the number of maximum threads he can handle has been
    * reached. If a shutdown request has been received it will prevent the
    * enclosing task to run, also it will notify the consumer for any kind of
    * shutdown requests that it has finished.
    */
   private class TaskWrapper implements Runnable {

      final Consumer consumer;
      final Runnable runnable;

      public TaskWrapper(Runnable runnable, Consumer consumer) {
         this.runnable = runnable;
         this.consumer = consumer;
         consumer.scheduled.incrementAndGet();
      }

      @Override
      public void run() {
         try {
            // Terminate if shutdown now has
            // been requested
            if (shutdown.get()) {
               return;
            }

            runnable.run();
         } finally {
            // Notify consumer
            // if he is waiting because his queue reached his maximum
            consumer.scheduled.decrementAndGet();
            synchronized (consumer) {
               consumer.notify();
            }

            // Notify the scheduler if he is waiting for
            // tasks to submit
            allTasksSize.decrementAndGet();
            synchronized (StaticTaskScheduler.this) {
               StaticTaskScheduler.this.notify();
            }
         }
      }
   }
}
