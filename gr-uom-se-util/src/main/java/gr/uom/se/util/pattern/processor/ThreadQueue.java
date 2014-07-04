/**
 * 
 */
package gr.uom.se.util.pattern.processor;

import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * An implementation of a processors queue running in parallel.
 * <p>
 * 
 * When a new entity comes ({@link #process(Object)} method) this queue will
 * create a new task and push it to the thread pool which will run the task some
 * time in the future. Keep in mind that having a lot of entities to process
 * will create a lot of tasks and that may cause problems to the thread pool,
 * however this queue will never block, but only when {@link #stop()} is called.
 * While stopping this will wait all unfinished tasks to finish their job. If
 * you want to balance the memory consumed and the running time you can use a
 * blocking queue instead which will block if a maximum limit of tasks is
 * scheduled for run. See {@link BlockingQueue}.
 * 
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public class ThreadQueue<T> extends AbstractProcessorQueue<T> {

   /**
    * Used to execute each processor in a different thread.
    * <p>
    */
   protected ExecutorService threadPool;

   /**
    * The maximum number of processors allowed to run simultaneously.
    * <p>
    */
   public static final int MAX_NUM_OF_RUNNING_THREADS = 16;

   /**
    * The number of total tasks currently submitted to the executor and are
    * waiting.
    * <p>
    */
   protected AtomicInteger tasksSubmitted;

   /**
    * Use this service to control tasks submitted for execution.
    * <p>
    */
   protected CompletionService<T> lock = null;

   /**
    * Set the static field to define a default id for all processors of this
    * class.
    */
   static {
      DEFAULT_PID = "TQUEUE";
   }

   /**
    * Creates a new instance based on the given thread pool.
    * <p>
    * Note, this is important when we want to have a single thread pool for the
    * whole application, so every task created by this processor will be
    * submitted to this thread. The exact behavior of the given thread pool
    * relies upon the creator of this queue, and any termination of the pool
    * would terminate this queue tasks.
    * 
    * @param pool
    *           to which all tasks produced by this processor will be submitted
    */
   public ThreadQueue(ExecutorService pool, String id) {
      super(id);
      if (pool == null) {
         throw new IllegalArgumentException("pool must not be null");
      }
      init(pool);
   }

   /**
    * Creates a new instance that will maintain a private thread pool when
    * processing entities.
    * <p>
    * 
    * This is useful when we don't need a central thread pool and we just need
    * to create simple processors queue that will process each entity in
    * parallel. If the application must have full control on the number of
    * running threads it creates, and the thread pool should be centralized then
    * use {@link #ThreadQueue(ExecutorService)}.
    * 
    * @param threads
    *           the number of simultaneously running runningThreads. Must be
    *           greater then 1 but not greater then
    *           {@link #MAX_NUM_OF_RUNNING_THREADS}.
    */
   public ThreadQueue(int threads, String id) {
      this(checkAndCreateExecutor(threads), id);
   }

   /**
    * Used to initialize this object.
    * 
    * @param pool
    */
   private void init(ExecutorService pool) {
      threadPool = pool;
      tasksSubmitted = new AtomicInteger(0);
      lock = new ExecutorCompletionService<T>(threadPool);
   }

   /**
    * Will check the number of threads and create an executor service based on
    * the number of threads.
    * 
    * @param threads
    *           must be greater than 0 and less than
    *           {@value #MAX_NUM_OF_RUNNING_THREADS}
    * @return a new fixed thread pool
    */
   private static ExecutorService checkAndCreateExecutor(int threads) {
      if (threads < 1 || threads > MAX_NUM_OF_RUNNING_THREADS) {
         throw new IllegalArgumentException(
               "The number of runningThreads must be between " + 1 + " and "
                     + MAX_NUM_OF_RUNNING_THREADS);
      }
      return Executors.newFixedThreadPool(threads);
   }

   @Override
   protected boolean processThis(final T entity) {
      // Delegate the method call to all queued runningProcessors
      Iterator<Processor<T>> it = runningProcessors.iterator();

      // If a processor returns false, that means
      // he should stop receiving entries.
      // If the processor throws an exception we should
      // keep processing to another processor
      while (it.hasNext()) {

         final ThreadProcessor p = (ThreadProcessor) it.next();

         // This is the value from previous process so if it was false
         // then we should remove this processor from the running queue,
         // otherwise we should submit another task
         if (p.shouldContinue.get()) {
            submitTaskFor(p, entity);
         } else {
            toBeStopped.add(p);
         }
      } // end of wile
      return true;
   }

   /**
    * Submit a task for the given processor and the given entity. The
    * implementation makes sure that when a task is submitted the taskSubmitted
    * counter will increase, and when the task is finished it should decrease by
    * one this counter. This queue relies on tasksSubmitted counter to work
    * properly, because it uses it when it is stopped, to wait all unfinished
    * tasks to finish.
    * <p>
    * Subclasses may override this method in order to define a new
    * implementation of how the tasks are submitted.
    * 
    * @param p
    *           the processor for the entity
    * @param entity
    *           the entity to be processed in parallel
    */
   protected void submitTaskFor(final Processor<T> p, final T entity) {
      // Increment the number of tasks submitted
      tasksSubmitted.incrementAndGet();

      // Submit the task. Each submitted task must decrease the
      // number of tasks after finish.
      lock.submit(new Callable<T>() {

         @Override
         public T call() throws Exception {
            try {
               p.process(entity);
               return null;
            } catch (Exception e) {
               thrownExceptions.add(new InterruptedException("PID " + p.getId()
                     + " " + e.getMessage()));
               return null;
            } finally {
               tasksSubmitted.decrementAndGet();
            }
         }
      });
   }

   /**
    * {@inheritDoc}
    * <p>
    * This implementation will wait first for all tasks to be stopped and then
    * will call the super implementation to stop this queue.
    */
   @Override
   protected void stopping() throws InterruptedException {
      // Here we do not want to shut down the executor
      // because it may be useful for an other execution,
      // thus we just wait all pending tasks to
      // finish before stopping any processor
      try {
         while (tasksSubmitted.get() > 0) {
            lock.take();
         }
      } finally {
         super.stopping();
      }
   }

   @Override
   public Processor<T> getProcessor(String pid) {
      Processor<T> p = super.getProcessor(pid);
      if (p != null) {
         return ((ThreadProcessor) p).p;
      }
      return null;
   }

   /**
    * {@inheritDoc}
    * <p>
    * Each added processor will be wrapped to a new {@link ThreadProcessor}.
    */
   @Override
   public void add(Processor<T> processor) {
      super.add(new ThreadProcessor(processor));
   }

   /**
    * A container used to remove a processor
    */
   private ThreadProcessor removeProcessor = new ThreadProcessor(null);

   @Override
   public void remove(Processor<T> processor) {
      if (processor.getClass().equals(ThreadProcessor.class)) {
         super.remove(processor);
      } else {
         synchronized (removeProcessor) {
            removeProcessor.p = processor;
            super.remove(removeProcessor);
         }
      }
   }

   /**
    * Shut down this queue, by shutting down any remaining threads.
    * <p>
    * 
    * @throws InterruptedException
    */
   public void shutdown() throws InterruptedException {
      this.threadPool.shutdown();
      this.threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
   }

   /**
    * This is a wrapper processor to a real one, and is used to keep the state
    * of the process method for the next execution so when the processor returns
    * false, because it is running in parallel we should be able to know after
    * next execution if we should keep passing any new entries or not. If not
    * then we should stop this processor by moving it to stopped queues.
    */
   protected class ThreadProcessor implements Processor<T> {

      Processor<T> p;
      volatile AtomicBoolean shouldContinue = new AtomicBoolean(true);

      public ThreadProcessor(Processor<T> p) {
         this.p = p;
      }

      @Override
      public boolean process(T entity) {
         boolean cont = shouldContinue.get();
         if (cont) {
            cont = p.process(entity);
            shouldContinue.set(cont);
         }
         return cont;
      }

      @Override
      public void stop() throws InterruptedException {
         this.p.stop();
      }

      @Override
      public void start() {
         this.p.start();
      }

      @Override
      public String getId() {
         return p.getId();
      }

      @Override
      public int hashCode() {
         final int prime = 31;
         int result = 1;
         result = prime * result + ((p == null) ? 0 : p.hashCode());
         return result;
      }

      @Override
      public boolean equals(Object obj) {
         if (this == obj)
            return true;
         if (obj == null)
            return false;
         if (!Processor.class.isAssignableFrom(obj.getClass()))
            return false;
         Processor<?> otherP = null;
         if (getClass() == obj.getClass()) {
            @SuppressWarnings("unchecked")
            ThreadProcessor other = (ThreadProcessor) obj;
            otherP = other.p;
         } else {
            otherP = (Processor<?>) obj;
         }
         if (p == null) {
            if (otherP != null)
               return false;
         } else if (!p.equals(otherP))
            return false;
         return true;
      }

      @Override
      public boolean isStarted() {
         return p.isStarted();
      }
   }
}
