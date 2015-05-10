/**
 * 
 */
package gr.uom.se.vcs.analysis;

import gr.uom.se.util.pattern.processor.BlockingParallelProcessorQueue;
import gr.uom.se.util.pattern.processor.ParallelProcessorQueue;
import gr.uom.se.util.pattern.processor.Processor;
import gr.uom.se.util.pattern.processor.ProcessorQueue;
import gr.uom.se.util.pattern.processor.SerialProcessorQueue;
import gr.uom.se.util.pattern.processor.DefaultParallelProcessorQueue;
import gr.uom.se.vcs.walker.Visitor;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Abstract implementation of {@link Visitor} with capabilities of running
 * multiply processors for visited entity.
 * <p>
 * 
 * This class merge the functionality of a processor, a queue of processors and
 * a visitor (see {@link Processor}, {@link ProcessorQueue}). That means, while
 * visiting some VCS entities (suppose commits) we can have multiply processors
 * each of one performing a unique computation based on visited entities. In
 * such situation is preferable to make only one walk to entities and each
 * visited entity pass to a processor, than making a walk for each processor.
 * Using an analyzer one can specify a set of processors that process the same
 * type of entities, and pass the analyzer as a visitor to one of the available
 * methods at VCS API.
 * <p>
 * An analyzer makes possible to run a set of processors in parallel, by
 * maintaining a parallel processors queue (see
 * {@link DefaultParallelProcessorQueue} and or in the main thread (in serial,
 * see {@link SerialProcessorQueue}). When using an analyzer to visit entities
 * make sure all light weight processors to run in serial (for example a commit
 * counter processor is preferable to run in serial, while a processor that
 * analyze the source code of a commit is preferable to run in parallel). In
 * order to run a processor in serial use {@link ProcessorQueue#add(Processor)}
 * method, and to run it in parallel use {@link #addParallel(Processor)}. You
 * can add both serial and parallel processors in the same time, however be sure
 * to invoke first {@link #start()} method to notify the queue that a work is
 * going to start. After start you may pass the analyzer to a method that
 * accepts visitor, to walk entities. It is very important to call
 * {@link #stop()} method after the work is done. It will wait any thread to
 * finish its job (and so a processor) and make any actions to finalize the
 * results. A typical usage of an analyzer would be:
 * 
 * <pre>
 * 
 * Analyzer&lt;VCSCommit&gt; analyzer = Analyzer.&lt;VCSCommit&gt; builder().
 *       setThreads(8).                // Default is 4
 *       setTaskQueueSize(10000)       // Default is 100
 *       addSerial(serComProcessor1).  // add this processor to run in serial
 *       addSerial(serComProcessor2).  // add this processor to run in serial
 *       addParallel(parComProcessor). // add this processor to run in parallel
 *       build(CommitAnalyzer.class);  // finally build the analyzer
 * 
 * VCSCommit commit = repo.getBranch(&quot;master&quot;).head();
 * // Start the analyzing of commits
 * analyzer.start();
 * commit.walkCommits(analyzer, true);
 * // Important to stop the analyzer before using any processor
 * analyzer.stop();
 * 
 * // Now use the processors
 * parComProcessor.getTheResults(); // do something with the results
 * serComProcessor1.getReport();    // do something with this report
 * 
 * // !!! VERY IMPORTANT !!!
 * // If your application doesn't need any more the analyzer in the future
 * // you should shut it down in order to stop any background threads.
 * // Otherwise you may leave running threads in background. However if
 * // you plan to use the analyzer in subsequent calls you can leave it
 * // as is. Once the analyzer is shut down it can not be used in the future.
 * // To shut it down you should do:
 * analyzer.shutDownThreads();
 * 
 * </pre>
 * 
 * You can use the same analyzer in different walks with different processors,
 * however these criteria should meet:
 * <ul>
 * <li>The analyzer is generic, that means all entities that he visits are of
 * the same type, so the processor should process entities of the same type.</li>
 * <li>While visiting entities only the processors that makes sense of visiting
 * them should be in the analyzer. For example suppose p1 is a commit counter
 * processor, and p2 is a commit source processor, and you want to use the p2 to
 * count the added lines of code in each source file of the visiting commit, and
 * the p1 to count the commits visiting so far, so you can make an average of
 * added lines per each commit. Now suppose you have processor p3 which search
 * for all commits that modify a certain file and measure the average complexity
 * of this file. The p3 doesn't make sense to use alongside with p2.</li>
 * <li>If the current use of the analyzer contains processors that are from a
 * previous use and are not needed to the current use, they should be removed
 * because they can consume resources. For example after running the analyzer
 * with a processor p1 and p2 (see above) you may remove p2 and add p3 in order
 * to process entities for the need of p3 (see above).</li>
 * <li>An analyzer may be reused but first should be stopped and started again,
 * however once you shut down analyzer he can't be used.</it>
 * <li>Generally speaking an analyzer should not be shared amongst threads as it
 * may cause synchronization problems, such as starting stopping analyzer.</li>
 * <li>A best practice is to cache an analyzer and reuse it whenever its needed,
 * although keep in mind that an analyzer is generic, which means not all types
 * of processors may be used with the same analyzer.</li>
 * <li>it is very <b>important</b> that all processors used in parallel should
 * be synchronized well (thread safe), for example an entity counter when used
 * in parallel may have concurrency problems if not thread safe, and so the
 * results it produces may have inconsistency problem.
 * </ul>
 * 
 * Keep in mind that it makes perfect sense to use a single processor in
 * parallel. That is, the analyzer doesn't share the work amongst parallel
 * processors but among subsequent calls of {@link #process(Object)} method,
 * which means the same processor may be processing in parallel two different
 * entities. For example a processor that perform static analysis on the source
 * code for each version of a software (a version is a tag, so a commit) may be
 * performing for two different versions static analysis in parallel. However if
 * processor requires a lot of memory you should use very few threads, as it may
 * cause memory problems (the static analysis may require a lot of memory).
 * Works running in parallel with a lot of I/O operations may block quickly so
 * do not use a lot of threads. Generally speaking the number of threads may
 * ideally be same as the number of CPU cores. More threads should be used in
 * cases the most of work is done (ideally) within the CPU memory itself or the
 * main memory, and less in case you have a lot of I/O operations for very
 * little CPU processing.
 * <p>
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
@SuppressWarnings("unchecked")
public class Analyzer<T> implements ProcessorQueue<T>, Visitor<T> {

   /**
    * The default size of pending tasks when some processors are scheduled to
    * run in parallel.
    * <p>
    */
   public static final int DEFAULT_TASK_QUEUE_SIZE = 100;

   /**
    * The default number of threads when some of the processors will run in
    * parallel.
    * <p>
    */
   public static final int DEFAULT_THREADS_NO = 4;

   /**
    * The parallel queue of the processors.
    * <p>
    * This will be added to serial queue.
    */
   protected ParallelProcessorQueue<T> parallelProcessors;
   /**
    * The lock of parallelProcessors field
    */
   protected ReadWriteLock parallelProcessorsLock = new ReentrantReadWriteLock();

   /**
    * The serial queue of the processors
    */
   protected SerialProcessorQueue<T> serialProcessors;
   /**
    * The lock of serialProcessors field
    */
   protected ReadWriteLock serialProcessorsLock = new ReentrantReadWriteLock();

   /**
    * The number of threads, if any parallel processor is available.
    * <p>
    * This is a read only field and is initialized only when constructing this
    * analyzer.
    */
   protected int threads;
   /**
    * True if this analyzer should use a blocking queue.
    * <p>
    * This is a read only field and is initialized only when constructing this
    * analyzer.
    */
   protected boolean blocking;
   /**
    * The number of tasks that can be submitted before this analyzer blocks.
    * <p>
    * This is a read only field and is initialized only when constructing this
    * analyzer.
    */
   protected int tasks;

   /**
    * The number of instances that are created until now.
    * <p>
    * Used for id.
    */
   private static final AtomicInteger instances = new AtomicInteger(0);

   /**
    * The default processor id.
    * <p>
    */
   private static final String DEFAULT_PID = "Analyzer";

   /**
    * @return a default id for this type of processor
    */
   protected static String generateDefaultId() {
      return DEFAULT_PID + instances.incrementAndGet();
   }

   /**
    * The id of this analyzer (processor)
    */
   private final String id;

   /**
    * @return the analyzer builder. A utility class that help in creating an
    *         analyzer with very little effort. In order to build an analyzer
    *         check that the class extending Analyzer has a nullary constructor.
    */
   public static <T> Builder<T> builder() {
      return new Builder<T>();
   }

   /**
    * Constructor used by a builder to build an analyzer.
    * <p>
    */
   protected Analyzer() {
      id = generateDefaultId();
   }

   /**
    * Default analyzer implementations will be stored here.
    */
   private static final Map<Class<?>, Class<? extends Analyzer<?>>> DEFAULT_ANALYZERS = new HashMap<Class<?>, Class<? extends Analyzer<?>>>();

   /** The lock for default analyzers */
   private static final ReadWriteLock analyzersLock = new ReentrantReadWriteLock();

   /**
    * Check if there is a default analyzer for the given type.
    * <p>
    * 
    * This method may throw an exception if the type is not correct.
    * 
    * @param clazz
    *           the type that the analyzer looking for will be processing
    * @return the analyzer if any, or null.
    */
   public static <K, A extends Analyzer<K>> Class<A> lookup(Class<K> clazz) {
      analyzersLock.readLock().lock();
      try {
         return (Class<A>) DEFAULT_ANALYZERS.get(clazz);
      } finally {
         analyzersLock.readLock().unlock();
      }
   }

   /**
    * Register an analyzer for the given type.
    * <p>
    * Note that this will replace any previous analyzer if it was already
    * registered.
    * <p>
    * Each registered analyzer is considered the default one.
    * 
    * @param type
    *           the type which the registered analyzer works with
    * @param analyzer
    *           the analyzer of the given type
    */
   public static <K, A extends Analyzer<K>> void register(Class<K> type,
         Class<A> analyzer) {
      analyzersLock.writeLock().lock();
      try {
         DEFAULT_ANALYZERS.put(type, analyzer);
      } finally {
         analyzersLock.writeLock().unlock();
      }
   }

   /**
    * Creates a new instance based on given processors.
    * <p>
    * The processors added will be ran in serial, so you may leave this argument
    * null or empty and add the processors using this class methods.
    * <p>
    * 
    * @param processors
    *           that this analyzer will run in serial
    */
   public Analyzer(Collection<Processor<T>> processors) {
      this();
      init(processors);
   }

   /**
    * Initialize this instance.
    * <p>
    * 
    * @param processors
    *           to be ran in serial. May be null or empty.
    */
   private void init(Collection<Processor<T>> processors) {
      serialProcessors = new SerialProcessorQueue<T>();
      if (processors != null) {
         for (Processor<T> processor : processors) {
            if (processor == null) {
               throw new IllegalArgumentException(
                     "processors must not contain a null element");
            }
            this.add(processor);
         }
      }
      threads = DEFAULT_THREADS_NO;
      blocking = true;
      tasks = DEFAULT_TASK_QUEUE_SIZE;
   }

   /**
    * Each time a parallel processor is added or removed this method will check
    * if the parallel processors queue is initialized or not. If not it will
    * create a new thread queue.
    * <p>
    */
   private void startParallel() {
      parallelProcessorsLock.writeLock().lock();
      try {
         if (parallelProcessors == null) {
            if (blocking) {
               parallelProcessors = new BlockingParallelProcessorQueue<T>(
                     threads, tasks, "BPARPRO");
            } else {
               parallelProcessors = new DefaultParallelProcessorQueue<T>(
                     threads, "PARPRO");
            }
            serialProcessors.addFirst(parallelProcessors);
         }
      } finally {
         parallelProcessorsLock.writeLock().unlock();
      }
   }

   /**
    * Add a processor to the parallel queue.
    * <p>
    * Each processor added with this method will be run in parallel. Note that
    * if this processor is contained in the queue of serial processors, than it
    * will be moved to parallel processors.
    * 
    * @param processor
    *           to add to parallel queue. Must not be null.
    */
   public void addParallel(Processor<T> processor) {
      serialProcessors.remove(processor);
      startParallel();
      parallelProcessors.add(processor);
   }

   /**
    * 
    * Shut down the threads if there is any.
    * <p>
    * In order to shut down the threads this processor should stop. Please keep
    * in mind that while using a parallel processors queue the best way to
    * ensure that all threads will stop after performing the tasks is to call
    * this method. In order to avoid having running threads a good pattern of
    * using parallel queues is:
    * 
    * <pre>
    * try {
    *       parQueue.start();
    *       ...
    *       parQueue.stop;
    *    
    *    } finally {
    *       // If a processor throws an exception
    *       // while working in parallel and or serial
    *       // the stop() method will collect all thrown
    *       // exceptions and throw one when called.
    *       // Therefore you should always ensure that threads
    *       // will terminate successfully.
    *       parQueue.shutDown();
    *    }
    * 
    * @throws InterruptedException
    */
   public void shutDown() throws InterruptedException {

      if (serialProcessors.isStarted()) {
         throw new IllegalStateException("processor is not stopped");
      }

      parallelProcessorsLock.writeLock().lock();
      try {
         if (parallelProcessors != null) {
            parallelProcessors.shutdown();
            parallelProcessors = null;
         }
      } finally {
         parallelProcessorsLock.writeLock().unlock();
      }
   }

   /**
    * Remove a processor from running queue.
    * <p>
    * 
    * Note that this method will remove the processor from this queue too, so no
    * more entities will accept.
    * 
    * @param processor
    *           to be removed from this queue. Must not be null.
    */
   public void remove(Processor<T> processor) {
      this.serialProcessors.remove(processor);
      parallelProcessorsLock.writeLock().lock();
      try {
         if (parallelProcessors != null) {
            parallelProcessors.remove(processor);
         }
      } finally {
         parallelProcessorsLock.writeLock().unlock();
      }
   }

   /**
    * {@inheritDoc}
    * <p>
    * This will delegate each call to {@link #process(Object)} method.
    */
   @Override
   public boolean visit(T entity) {
      return serialProcessors.process(entity);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void stop() throws InterruptedException {
      serialProcessors.stop();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void start() {
      if (!serialProcessors.isStarted() && parallelProcessors != null) {
         serialProcessors.remove(parallelProcessors);
         serialProcessors.addFirst(parallelProcessors);
      }
      serialProcessors.start();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public String getId() {
      return id;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean isStarted() {
      return serialProcessors.isStarted();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void add(Processor<T> processor) {
      serialProcessors.add(processor);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void removeAll() {
      serialProcessors.removeAll();
      if (parallelProcessors != null) {
         parallelProcessors.removeAll();
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean process(T entity) {
      return serialProcessors.process(entity);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Processor<T> getProcessor(String pid) {
      Processor<T> pro = serialProcessors.getProcessor(pid);
      if (pro != null) {
         return pro;
      }
      return parallelProcessors != null ? parallelProcessors.getProcessor(pid)
            : null;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public int getProcessorsCount() {
      return serialProcessors.getProcessorsCount()
            + parallelProcessors.getProcessorsCount();
   }

   /**
    * A utility class that helps constructing processors.
    * <p>
    * Do not share a builder with multiply threads.
    * 
    * @author Elvis Ligu
    * @version 0.0.1
    * @since 0.0.1
    * @param <T>
    */
   public static class Builder<T> {

      private Set<Processor<T>> serial;
      private Set<Processor<T>> parallel;
      private int threads;
      private int taskSize;
      private Boolean blockingQueue;
      private ParallelProcessorQueue<T> pqueue;

      private Builder() {
         init();
      }

      private void init() {
         serial = new LinkedHashSet<Processor<T>>();
         parallel = new LinkedHashSet<Processor<T>>();
         threads = DEFAULT_THREADS_NO;
         taskSize = DEFAULT_TASK_QUEUE_SIZE;
         blockingQueue = true;
         pqueue = null;
      }

      /**
       * Add the processor to the serial queue.
       * <p>
       * 
       * @param processor
       *           to run in serial. Must not be null.
       * @return this builder
       */
      public Builder<T> addSerial(Processor<T> processor) {
         if (processor == null) {
            throw new IllegalArgumentException();
         }
         this.serial.add(processor);
         return this;
      }

      /**
       * Add the processor to the parallel queue.
       * <p>
       * 
       * @param processor
       *           to run in parallel. Must not be null.
       * @return this builder
       */
      public Builder<T> addParallel(Processor<T> processor) {
         if (processor == null) {
            throw new IllegalArgumentException();
         }
         this.parallel.add(processor);
         return this;
      }

      /**
       * Specify a specialized version of a parallel queue.
       * <p>
       * If the queue is defined at this method, when calling
       * {@link #build(Class)} the number of threads, the task queue size, and
       * the non blocking properties will not be taken in consideration.
       * 
       * @param queue
       *           a specialized parallel queue
       * @return this builder.
       */
      public Builder<T> setParallelQueue(ParallelProcessorQueue<T> queue) {
         this.pqueue = queue;
         return this;
      }

      /**
       * Set the number of threads that the analyzer will run parallel
       * processors.
       * <p>
       * Default number of threads is 4. No effect if all processors are running
       * in serial.
       * 
       * @param threads
       *           the number of threads. Must be not be 0 or negative.
       * @return this builder
       */
      public Builder<T> setThreads(int threads) {
         if (threads < 2) {
            throw new IllegalArgumentException("threads must be greater than 1");
         }
         this.threads = threads;
         return this;
      }

      /**
       * Set the number of tasks that will wait to be performed until this queue
       * blocks.
       * <p>
       * For each new entity that arrives, the analyzer will pass it to each
       * processor it contains. If some processors are running in parallel then
       * this will schedule each new entity to run when a thread is available.
       * Setting a maximum number of tasks, helps this queue to block the caller
       * of process() method until the number of tasks already scheduled to run
       * gets lower than this maximum. This will have no effect if all
       * processors are running in serial. Non blocking property must be set to
       * false in order to take effect this method.
       * 
       * @param size
       *           of scheduled tasks until this queue blocks
       * @return
       */
      public Builder<T> setTaskQueueSize(int size) {
         if (size < threads) {
            throw new IllegalArgumentException(
                  "task queue size must be at least the number of threads");
         }
         this.taskSize = size;
         return this;
      }

      /**
       * Set if this processor should block or not if the maximum number of
       * scheduled tasks exceed a limit set by {@link #setTaskQueueSize(int)} .
       * <p>
       * If true than the tasks size will have no effect.
       * <p>
       * 
       * @param nonBlocking
       *           true if the analyzer should be non blocking
       * @return this builder
       */
      public Builder<T> setNonBlocking(boolean nonBlocking) {
         this.blockingQueue = !nonBlocking;
         return this;
      }

      /**
       * Build the analyzer with the build parameters this builder has.
       * <p>
       * The class should determine the type of the analyzer to build. The class
       * implementation denoted from clazz argument must have a nullary
       * (non-argument) constructor).
       * 
       * @param clazz
       *           of the analyzer to be build
       * @return the new analyzer build with this builder parameters. After this
       *         method you can keep using the builder, however all its
       *         properties will be initialized just as a new one.
       */
      public <A extends Analyzer<T>> A build(Class<A> clazz) {
         try {
            A instance = clazz.newInstance();
            instance.blocking = blockingQueue;
            instance.threads = threads;
            instance.tasks = taskSize;
            instance.serialProcessors = new SerialProcessorQueue<T>();

            if (!parallel.isEmpty()) {
               if (pqueue == null) {
                  if (blockingQueue) {
                     pqueue = new BlockingParallelProcessorQueue<T>(threads,
                           taskSize, "PARPRO");
                  } else {
                     pqueue = new DefaultParallelProcessorQueue<T>(threads,
                           "PARPRO");
                  }
               }
               for (Processor<T> p : parallel) {
                  pqueue.add(p);
               }
            }
            if (pqueue != null) {
               instance.add(pqueue);
               instance.parallelProcessors = pqueue;
            }
            for (Processor<T> p : serial) {
               instance.add(p);
            }
            init();
            return instance;
         } catch (InstantiationException e) {
            throw new IllegalArgumentException(
                  clazz
                        + " can not be created, probably it doesn't have a visible nullary constructor");

         } catch (IllegalAccessException e) {
            throw new IllegalArgumentException(
                  clazz
                        + " can not be created, probably it doesn't have a visible nullary constructor");
         }
      }
   }
}
