package gr.uom.se.util.concurrent;

/**
 * A task scheduler that maintains a thread pool and separate tasks based on
 * their types.
 * <p>
 * This scheduler will schedule tasks to run in a thread pool, however the
 * strategy of scheduling tasks may differ based on the provided type. It is up
 * to the implementation to decide which type of tasks he can accept and how to
 * deal with each type.
 *
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
 * {@link TaskType.Enum#get(int)} where he can pass the number of threads this
 * type of tasks may have. This type of tasks should be then passed to the
 * constructor of task scheduler.
 * <p>
 * When the system needs to perform quick tasks that do not make use of
 * resources (or use very little resources) such as notifiers they should be
 * scheduled with {@link #schedule(java.lang.Runnable)}.
 * <p>
 * Keep in mind that this scheduler will maintain a thread pool, therefore the
 * number of the threads that are available in pool may affect the function of
 * this scheduler. That is, if the thread pool has only a single thread that
 * means all the tasks will run in serial so calling any of the methods of this
 * scheduler is the same as calling
 * {@link #scheduleOnSingle(java.lang.Runnable)}. Generally speaking the thread
 * pool is limited so when calling {@link #schedule(java.lang.Runnable)} the
 * caller should expect its job to possibly wait for a thread to be available.
 * Although, practically the pool should be large enough to allow for quick
 * tasks to perform without waiting. On the other hand the waiting of moderate
 * tasks is highly connected to the number of threads that are available for
 * them. While the heavy duty tasks have only one thread in their disposal.
 *
 * @author Elvis Ligu
 */
public interface TaskScheduler {

   /**
    * Schedule a task based on its type.
    * <p>
    * How the task is scheduled and when it is going to be executed it is up to
    * the implementation to decide. However, if the scheduler doesn't recognize
    * this type of task he will throw an exception. Also if the scheduler is
    * terminated it will throw an exception if this method is called.
    *
    * @param task the task to be scheduled for execution. May not be null.
    * @param type the type of the task to be scheduled.
    */
   void schedule(Runnable task, TaskType type);

   /**
    * Schedule a task based on its type.
    * <p>
    * How the task is scheduled and when it is going to be executed it is up to
    * the implementation to decide. However, if the scheduler doesn't recognize
    * this type of task he will throw an exception. Also if the scheduler is
    * terminated it will throw an exception if this method is called.
    *
    * @param task the task to be scheduled for execution. May not be null.
    */
   void schedule(Task task);
   
   /**
    * Attempt to shutdown this scheduler without interrupting the tasks that are
    * running or those tasks that are scheduled.
    * <p>
    * This will not stop the running tasks, also it will not stop the previously
    * scheduled task to start execution some point in time. However any client
    * that attempts to schedule a new task will be not allowed (an exception
    * will be thrown.
    *
    * @throws InterruptedException is thrown when a task is abnormally
    * interrupted.
    */
   void shutdown() throws InterruptedException;

   /**
    * Cause this scheduler to shutdown immediately.
    * <p>
    * This call will prevent all queued tasks that were previously submitted to
    * be executed, and will prevent any client of this scheduler to submit a new
    * task. However whether a running task will be interrupted or not this is
    * not specified, unless the implementation explicitly states it.
    *
    * @throws java.lang.InterruptedException
    */
   void shutdownNow() throws InterruptedException;

   /**
    * Return true if the scheduler has been requested a shutdown.
    * <p>
    * However if there are tasks submitted and running, while this scheduler is
    * waiting this metho will return true. Use {@link #isTerminated()} to check
    * if this scheduler has no submitted tasks that are running or that will be
    * run in future.
    *
    * @return
    */
   boolean isShutdown();

   /**
    * Return true if the scheduler has been previously requested a shutdown and
    * all its threads has bean terminated.
    * <p>
    * If this method returns true that means this scheduler should not accept
    * other tasks for submitting. Also it means that all running tasks has
    * finished.
    *
    * @return
    */
   boolean isTerminated();

   /**
    * Return true if this scheduler can accept tasks of the given type.
    * <p>
    * @param type type of tasks to check if they can be scheduled by this
    * scheduler.
    * @return true if this type can be scheduled.
    */
   boolean canSchedule(TaskType type);
}
