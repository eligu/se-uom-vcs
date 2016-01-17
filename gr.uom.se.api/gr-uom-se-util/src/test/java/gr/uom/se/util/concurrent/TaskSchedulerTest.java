/**
 * 
 */
package gr.uom.se.util.concurrent;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Test;

/**
 * @author Elvis Ligu
 */
public class TaskSchedulerTest {

   static TaskType heavyTasks = TaskType.Enum.get(1, "serialTask");
   static TaskType moderateTasks = TaskType.Enum.get(2, "parallelTask");
   static TaskType quickTasks = TaskType.Enum.get(4, "quickTask");;
   static Logger logger = Logger.getLogger(TaskSchedulerTest.class.getName());

   /**
    * 
    */
   public TaskSchedulerTest() {
      // TODO Auto-generated constructor stub
   }

   @Test
   public void testScheduler() throws InterruptedException, ExecutionException {

      TaskScheduler scheduler = resolveScheduler();

      AtomicInteger threads = new AtomicInteger(0);
      AtomicInteger achievedMaxThreads = new AtomicInteger(0);
      AtomicInteger started = new AtomicInteger(0);
      AtomicInteger ended = new AtomicInteger(0);
      int maxThreads = 7;
      int iterations = 20;

      for (int i = 0; i < iterations; i++) {
         // Create 3 heavy (single running) tasks)
         createRequest(scheduler, heavyTasks, 3, started, ended, threads,
               achievedMaxThreads, maxThreads);
         // Create 10 moderateTasks (single running) tasks)
         createRequest(scheduler, moderateTasks, 10, started, ended, threads,
               achievedMaxThreads, maxThreads);
         // Create 10 moderateTasks (single running) tasks)
         createRequest(scheduler, quickTasks, 10, started, ended, threads,
               achievedMaxThreads, maxThreads);
         // Create 3 heavy (single running) tasks)
         Thread.sleep(13);
         createRequest(scheduler, heavyTasks, 3, started, ended, threads,
               achievedMaxThreads, maxThreads);
         // Create 10 moderateTasks (single running) tasks)
         createRequest(scheduler, moderateTasks, 10, started, ended, threads,
               achievedMaxThreads, maxThreads);
         // Create 10 moderateTasks (single running) tasks)
         createRequest(scheduler, quickTasks, 10, started, ended, threads,
               achievedMaxThreads, maxThreads);
         Thread.sleep(10);
         // Create 10 moderateTasks (single running) tasks)
         createRequest(scheduler, moderateTasks, 10, started, ended, threads,
               achievedMaxThreads, maxThreads);
         // Create 3 heavy (single running) tasks)
         createRequest(scheduler, heavyTasks, 3, started, ended, threads,
               achievedMaxThreads, maxThreads);
         // Create 10 moderateTasks (single running) tasks)
         createRequest(scheduler, quickTasks, 10, started, ended, threads,
               achievedMaxThreads, maxThreads);
      }
      scheduler.shutdown();

      synchronized (this) {
         while (!scheduler.isTerminated()) {
            wait(10000);
         }
      }

      logger.log(Level.INFO, "Started: " + started);
      logger.log(Level.INFO, "Ended: " + ended);
      logger.log(Level.INFO, "Max threads achieved: " + achievedMaxThreads);
      assertEquals(iterations * 69, started.get());
      assertEquals(iterations * 69, ended.get());
      assertEquals(maxThreads, achievedMaxThreads.get());
   }

   private void createRequest(TaskScheduler scheduler, TaskType type,
         int numOfTasks, AtomicInteger started, AtomicInteger ended,
         AtomicInteger threads, AtomicInteger achievedMaxThreads, int maxThreads)
         throws InterruptedException, ExecutionException {

      for (int i = 0; i < numOfTasks; i++) {
         TestTask task = new TestTask(started, ended, threads,
               achievedMaxThreads, maxThreads);

         scheduler.schedule(task, type);
      }
   }

   private TaskScheduler resolveScheduler() {
      TaskScheduler manager = new BlockingTaskScheduler(10000,
            Executors.newFixedThreadPool(10), heavyTasks, moderateTasks,
            quickTasks);
      return manager;
   }
}
