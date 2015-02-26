package gr.uom.se.util.concurrent;

import gr.uom.se.util.validation.ArgsCheck;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorCompletionService;

/**
 * A simple implementation of the {@link Executor} interface that would allow
 * the integration of {@link TaskScheduler} implementations to be incorporated
 * with other classes of java.concurrent.* package.
 * <p>
 * A typical example of using this task executor is when using a
 * {@link ExecutorCompletionService}, which is shown in the following example:
 * 
 * <pre>
 * int maxJobs = 1000;
 * TaskType type = TaskType.get(&quot;4_threads_tasks&quot;, 4);
 * TaskScheduler scheduler = new StaticTaskScheduler(maxJobs, threadPool, type);
 * Executor executor = new TaskExecutor(scheduler, type);
 * 
 * CompletitionService<T> service = new ExecutorCompletitionService<T>(executor);
 * 
 * int tasks = 10;
 * Runnable r1 = ...
 * Runnable r2 = ...
 * ...
 * // Those two tasks will be submitted to task scheduler
 * // as tasks of type 'type' (4 threads will be used). 
 * service.submit(r1);
 * service.submit(r2);
 * ...
 * 
 * // We can now wait for the tasks to complete
 * while(tasks == 10) {
 *    service.take();
 *    tasks--;
 * }
 * 
 * <pre>
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public class TaskExecutor implements Executor {

   /** The scheduler where all executes will be delegated */
   private final TaskScheduler scheduler;
   /** The type of the tasks to schedule all tasks to scheduler */
   private final TaskType type;

   /**
    * Create a new task executor based on the given scheduler and the given task
    * type.
    * <p>
    * Each call to {@link #execute(Runnable)} will be delegated to scheduler,
    * and the task will be considered of type as the one provided.
    * 
    * @param scheduler
    *           the scheduler where to submit tasks. Must not be null.
    * @param type
    *           of all tasks that will be executed by this executor. The
    *           scheduler must be able to execute this task type otherwise it
    *           will be thrown an exception.
    */
   public TaskExecutor(TaskScheduler scheduler, TaskType type) {
      ArgsCheck.notNull("scheduler", scheduler);
      ArgsCheck.notNull("type", type);
      if (!scheduler.canSchedule(type)) {
         throw new IllegalArgumentException(type
               + " can not be scheduled by scheduler " + scheduler);
      }
      this.scheduler = scheduler;
      this.type = type;
   }

   @Override
   public void execute(Runnable command) {
      scheduler.schedule(command, type);
   }
}