/**
 * 
 */
package gr.uom.se.util.pattern.processor;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Implementation of a processors queue running in parallel, based on thread
 * queue.
 * <p>
 * This implementation differs from its super class in that it blocks the
 * current thread that creates entities for processing if the number of waiting
 * tasks (entities to be processed) exceeds an upper limit.
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public class BlockingQueue<T> extends ThreadQueue<T> {

   /**
    * The maximum number of tasks that are allowed to be submitted
    * simultaneously.
    * <p>
    */
   protected int maxTasksAllowed = 0;

   /**
    * We use this queue to control the number of pending tasks. When a task is
    * created an object is inserted to the queue. If the queue is full then the
    * thread will block, thus no more tasks are submitted. On the other hand,
    * each finishing task must remove an object from this queue, and that will
    * cause the thread that submit tasks to submit a new task.
    */
   private ArrayBlockingQueue<Object> queue = null;
   /**
    * This is a dummy object that is inserted to the blocking queue.
    */
   private Object queueObject = new Object();
   
   /**
    * Creates a new instance that will maintain a private thread pool when
    * processing entities.
    * <p>
    * 
    * A thread pool is of type {@link ExecutorService}.
    * 
    * @param threads
    *           the number of simultaneously running threads. Must be greater
    *           then 1 but not greater then {@link #MAX_NUM_OF_RUNNING_THREADS}.
    */
   public BlockingQueue(int threads, String id) {
      this(threads, threads, id);
   }

   /**
    * Creates a new instance that will maintain a private thread pool when
    * processing entities.
    * <p>
    * The parameter {@code taskQueueSize} is used to determine the number of
    * tasks that are submitted. For each new entity a new task will be created
    * and submitted to the thread pool. If the current coming entity makes the
    * number of unfinished submitted tasks to exceed the {@code taskQueueSize}
    * then the thread calling {@code process()} will block until a worker thread
    * from the pool is finished.
    * 
    * @param the
    *           number of simultaneously running threads. Must be greater then 2
    *           but not greater then {@link #MAX_NUM_OF_RUNNING_THREADS}.
    * @param taskQueueSize
    *           the maximum number of submitted tasks that will remain in queue
    *           until they are finished.
    */
   public BlockingQueue(int threads, int taskQueueSize, String id) {

      super(threads, id);
      if (taskQueueSize < threads) {
         throw new IllegalArgumentException(
               "taskQueueSize must be equal or greater then the number of threads");
      }

      this.tasksSubmitted = new AtomicInteger(0);
      this.maxTasksAllowed = taskQueueSize;
      queue = new ArrayBlockingQueue<Object>(maxTasksAllowed);
   }

   /**
    * Set the static field to define a default id for all processors
    * of this class.
    */
   static {
      DEFAULT_PID = "BQUEUE";
   }
   
   protected void submitTaskFor(final Processor<T> p, final T entity) {
      // Increment the number of tasks submitted
      tasksSubmitted.incrementAndGet();

      // If the number of tasks that are currently submitted
      // is equal to the maximum allowed number, then we
      // block until one of the running tasks will terminate.
      // The strategy here is to keep a blocking queue so
      // when it is full the current thread that supply
      // entities will block. Each queueObject in the queue
      // will be remove from a finishing task, that will
      // allow this thread to place another task as it will
      // unblock by queue. Note that we use the same queueObject
      // here so there is no unnecessary memory
      // consumption.
      try {
         queue.put(queueObject);
      } catch (InterruptedException e) {
         thrownExceptions.add(e);
      }

      // Submit a task to execute the processing of entity
      lock.submit(new Callable<T>() {
         @Override
         public T call() throws Exception {
            try {
               p.process(entity);
               return null;
            } catch (Exception e) {
               thrownExceptions.add(new InterruptedException("PID " + p.getId()
                     + e.getMessage()));
               return null;
            } finally {
               tasksSubmitted.decrementAndGet();
               queue.poll();
            }
         }
      });
   }
}
