/**
 * 
 */
package gr.uom.se.util.pattern.processor;

import gr.uom.se.util.validation.ArgsCheck;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * An implementation of a mixed queue, where some processor will run in parallel
 * and some will run in serial.
 * <p>
 * This implementation uses a {@link SerialQueue} and one of the implementations
 * of {@link ThreadQueue}. It allows the use of a blocking queue, that can be
 * specified in the constructor's parameters. Also you can specify a shared
 * thread pool in case you have a central thread pool used for all tasks in your
 * application.
 * <p>
 * This queue has two strategies for executing the processors. The first
 * strategy is to add the parallel queue to the serial queue, and when each
 * entity is coming for processing it will delegate to {@link #process(Object)}
 * method of each queue. This strategy has the advantage of not creating a
 * thread where each serial processor will run. On the other hand it will block
 * the parallel queue from processing any entity until the serial processors
 * finishes their job. The second strategy is to add the serial queue to the
 * parallel queue. By doing so, the serial queue will run in a thread of
 * parallel queue and will not block any parallel processor to execute entities.
 * However if all threads are occupied by parallel processors, the serial queue
 * may be delayed until a new thread is released. Also, when this strategy is
 * used all serial processors are not guaranteed to process entities in any
 * order, consequently they should be thread safe.
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public class MixQueue<T> extends AbstractProcessor<T> implements ThreadQueue<T> {

   /**
    * The serial queue of processors.
    * <p>
    */
   protected SerialQueue<T> serialQueue;
   /**
    * The parallel queue of processors.
    * <p>
    */
   protected ThreadQueueImp<T> parallelQueue;
   /**
    * A reference to the queue that will be used as the main queue.
    * <p>
    * If the first strategy is used, the parallel queue will be added to serial
    * queue, thus the serial queue will be the main queue and it will accept all
    * calls to {@link #process(Object)} method. If the second strategy is used
    * the serial queue will be added to parallel queue, thus the parallel queue
    * will be the main queue.
    */
   private ProcessorQueue<T> mainQueue;
   /**
    * A lock used when access to processors is requested.
    * <p>
    * The two queues are considered as one, so when a processor is
    * added/removed/resolved it will do so on both queues.
    */
   private ReadWriteLock queueLock = new ReentrantReadWriteLock();

   /**
    * Set the static field to define a default id for all processors of this
    * class.
    */
   static {
      DEFAULT_PID = "MIXQUEUE";
   }

   /**
    * True if the second strategy will be used.
    * <p>
    * The first strategy is to add the parallel queue to the serial queue and
    * uses the serial as the main queue. And second strategy will add the serial
    * to parallel queue.
    */
   protected final boolean parallelToSerial;

   public MixQueue(int threads, boolean blocking, int taskQueueSize,
         boolean parallelToSerial, String id) {

      this(ThreadQueueImp.checkAndCreateExecutor(threads), blocking,
            taskQueueSize, parallelToSerial, id);
   }

   public MixQueue(ExecutorService threadPool, boolean blocking,
         int taskQueueSize, boolean parallelToSerial, String id) {
      super(id);
      ArgsCheck.notNull("service", threadPool);
      if (blocking) {
         parallelQueue = new BlockingQueue<T>(threadPool, taskQueueSize, id);
      } else {
         parallelQueue = new ThreadQueueImp<T>(threadPool, id);
      }
      serialQueue = new SerialQueue<T>(id);
      this.parallelToSerial = parallelToSerial;
      initQueues();
   }

   /**
    * Set the strategy of running the queues.
    * <p>
    * The strategy will be based on {@link #parallelToSerial} value. Use this
    * method at constructor or when all processors are removed.
    */
   private void initQueues() {
      if (parallelToSerial) {
         mainQueue = parallelQueue;
      } else {
         mainQueue = serialQueue;
      }
   }

   /**
    * {@inheritDoc}
    * <p>
    * The processor will be added to the serial queue of processors. If the
    * processor is contained in the parallel queue it will be removed from there
    * first. Keep in mind that this queue doesn't allow two same processors to
    * be add, so if a processor is already in the serial queue it will do
    * nothing. The equals method of two processors will be used to determine if
    * a processor is equal to another one.
    * <p>
    * Use {@link #addParallel(Processor)} to add a processor to the parallel
    * queue.
    */
   @Override
   public void add(Processor<T> processor) {
      runningLock.writeLock().lock();
      if(running) {
         throw new IllegalStateException("can not add processor while running");
      }
      queueLock.writeLock().lock();
      try {
         if (serialQueue.getProcessor(processor.getId()) == null) {
            serialQueue.add(processor);
            parallelQueue.remove(processor);
         }

         // The case when main queue is the serial queue
         // That means the parallel queue is contained within
         // serial queue.
         // If the parallel queue remains empty we should
         // remove it from the serial queue, because it will
         // cause errors in processing.
         if (!parallelToSerial) {
            if (parallelQueue.getProcessorsCount() == 0) {
               serialQueue.remove(parallelQueue);
            }
            // We should ensure that the serial queue which contains
            // at least a processor must be within parallel queue
         } else {
            if (serialQueue.getProcessorsCount() < 1) {
               throw new IllegalStateException(
                     "serial queue should contain at least the last added processor");
            }
            if (parallelQueue.getProcessor(serialQueue.getId()) == null) {
               parallelQueue.add(serialQueue);
            }
         }

      } finally {
         queueLock.writeLock().unlock();
         runningLock.writeLock().unlock();
      }
   }

   /**
    * Add a processor to the parallel queue of processors.
    * <p>
    * If the processor is contained in the serial queue it will be removed from
    * there first. Keep in mind that this queue doesn't allow two same
    * processors to be add, so if a processor is already in the parallel queue
    * it will do nothing. The equals method of the processor will be used to
    * determine if a processor is equal to another one.
    * 
    * @param processor
    *           to be added to the parallel queue
    */
   public void addParallel(Processor<T> processor) {
      runningLock.writeLock().lock();
      if(running) {
         throw new IllegalStateException("can not add processor while running");
      }
      queueLock.writeLock().lock();
      try {
         if (parallelQueue.getProcessor(processor.getId()) == null) {
            parallelQueue.add(processor);
            serialQueue.remove(processor);
         }

         // The case when main queue is the parallel queue
         // That means the serial queue is contained within
         // parallel queue.
         // If the serial queue remains empty we should
         // remove it from the parallel queue, because it will
         // cause errors in processing.
         if (parallelToSerial) {
            if (serialQueue.getProcessorsCount() == 0) {
               parallelQueue.remove(serialQueue);
            }
            // We should ensure that the parallel queue which contains
            // at least a processor must be within serial queue
         } else {
            if (parallelQueue.getProcessorsCount() < 1) {
               throw new IllegalStateException(
                     "parallel queue should contain at least the last added processor");
            }
            if (serialQueue.getProcessor(parallelQueue.getId()) == null) {
               serialQueue.add(parallelQueue);
            }
         }
      } finally {
         queueLock.writeLock().unlock();
         runningLock.writeLock().unlock();
      }
   }

   /**
    * {@inheritDoc}
    * <p>
    * This will remove a processor from this queue. The processor may be removed
    * from the serial queue and or from the parallel queue, depending where this
    * processor is queued.
    */
   @Override
   public void remove(Processor<T> processor) {
      runningLock.writeLock().lock();
      if(running) {
         throw new IllegalStateException("can not remove processor while running");
      }
      queueLock.writeLock().lock();
      try {
         parallelQueue.remove(processor);
         serialQueue.remove(processor);

         // The case when main queue is the parallel queue
         // That means the serial queue is contained within
         // parallel queue.
         if (parallelToSerial) {
            // If the serial queue remains empty we should
            // remove it from the parallel queue, because it will
            // cause errors in processing.
            if (serialQueue.getProcessorsCount() == 0) {
               parallelQueue.remove(serialQueue);
            }
         } else {
            // If the parallel queue remains empty we should
            // remove it from the serial queue, because it will
            // cause errors in processing.
            if (parallelQueue.getProcessorsCount() == 0) {
               serialQueue.remove(parallelQueue);
            }
         }
      } finally {
         queueLock.writeLock().unlock();
         runningLock.writeLock().unlock();
      }
   }

   /**
    * {@inheritDoc}
    * <p>
    * The serial queue and the parallel queue will be clean after this method
    * call.
    */
   @Override
   public void removeAll() {
      runningLock.writeLock().lock();
      if(running) {
         throw new IllegalStateException("can not remove processors while running");
      }
      queueLock.writeLock().lock();
      try {
         parallelQueue.removeAll();
         serialQueue.removeAll();
      } finally {
         queueLock.writeLock().unlock();
         runningLock.writeLock().lock();
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean process(T entity) {
      runningLock.readLock().lock();
      try {
         assertRunning(running);
         return mainQueue.process(entity);
      } finally {
         runningLock.readLock().unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Processor<T> getProcessor(String pid) {
      queueLock.readLock().lock();
      try {
         Processor<T> p = serialQueue.getProcessor(pid);
         if (p == null) {
            p = parallelQueue.getProcessor(pid);
         }
         return p;
      } finally {
         queueLock.readLock().unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public int getProcessorsCount() {
      queueLock.readLock().lock();
      try {
         // The count here is -1 because one of the queues
         // contains the other, depending on the strategy used
         int count = serialQueue.getProcessorsCount()
               + parallelQueue.getProcessorsCount();

         // The parallel queue is the main queue
         // so it may contain the serial queue
         if (parallelToSerial) {
            if (serialQueue.getProcessorsCount() > 0) {
               count--;
            }
            // The serial queue is the main queue
            // so it may contain the parallel queue
         } else {
            if (parallelQueue.getProcessorsCount() > 0) {
               count--;
            }
         }
         return count;
      } finally {
         queueLock.readLock().unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected void starting() {
      queueLock.writeLock().lock();
      try {
         mainQueue.start();
      } finally {
         queueLock.writeLock().unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected void stopping() throws InterruptedException {
      queueLock.writeLock().lock();
      try {
         parallelQueue.stop();
         serialQueue.stop();
      } finally {
         queueLock.writeLock().unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void shutdown() throws InterruptedException {
      runningLock.readLock().lock();
      try {
         parallelQueue.shutdown();
      } finally {
         runningLock.readLock().unlock();
      }
   }
}
