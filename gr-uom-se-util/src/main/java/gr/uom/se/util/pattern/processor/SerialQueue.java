/**
 * 
 */
package gr.uom.se.util.pattern.processor;

import gr.uom.se.util.validation.ArgsCheck;

import java.util.Iterator;

/**
 * A serial implementation of a processor that keeps a queue of processors to
 * whom should send the new item passed at {@code process()} method.
 * <p>
 * This processor acts as a queue of processors, wrapping a list of processors.
 * When a new data is ready for processing, and the call
 * {@link #process(Object)} is called, this will delegate the call to each
 * processor in the queue in a serial fashion.
 * <p>
 * Hints:
 * <ul>
 * <li>a) use this queue when you are sure that the available processor are not
 * consuming too much time to process a single piece of data (entity), so they
 * can't block each other. One such example is processor that produces the mean
 * of all the received data (let say it takes Integer numbers). Such works can
 * be better accomplished in serial, avoiding the resources consumed by thread
 * management.</li>
 * <li>
 * b) although it is not advisable to do so, this queue can be part of another
 * queue. When we know for sure that given a set of data, a group of processor
 * need to perform a quick task on each data, and an other group needs to
 * perform some very expensive computations, we can add a serial queue (that
 * will keep all the lightweight processor) and a parallel queue (that will keep
 * all expensive processor) to a serial queue.</li>
 * </ul>
 * Before starting any processing of data, it is required that {@link #start()}
 * method should be invoked. This will delegate to each queued processor so any
 * of them may make initializations.
 * <p>
 * After all data are consumed a call to {@link #stop()} is required, in order
 * to inform all queued processors and or to clean its internal state.
 * <p>
 * This queue is synchronized, that is it can be shared by different threads.
 * However keep in mind that the invariants may be broken if this class is
 * subclassed.
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public class SerialQueue<T> extends AbstractProcessorQueue<T> {

   /**
    * Set the static field to define a default id for all processors
    * of this class.
    */
   static {
      DEFAULT_PID = "SQUEUE";
   }
   
   /**
    * Creates a new instance of this queue.
    * <p>
    */
   public SerialQueue() {
      this(null);
   }

   /**
    * Creates a new instance of this queue with the given id.
    * <p>
    * If id is null then it will generate a default id.
    * 
    * @param id
    *           of this processor
    */
   public SerialQueue(String id) {
      super(id);
   }

   @Override
   protected boolean processThis(T entity) {
      // Delegate the method call to all queued processors
      Iterator<Processor<T>> it = runningProcessors.iterator();

      // If a processor returns false, that means
      // he should stop receiving entries.
      // If the processor throws an exception we should
      // keep processing to another processor
      while (it.hasNext()) {
         Processor<T> p = it.next();
         try {
            if (!p.process(entity)) {
               toBeStopped.add(p);
            }
         } catch (Exception e) {
            // Collect this exception
            thrownExceptionsLock.writeLock().lock();
            try {
               thrownExceptions.add(new InterruptedException(e.getMessage()));
            } finally {
               thrownExceptionsLock.writeLock().unlock();
            }
         } // end of try
      } // end of wile
      return true;
   }

   /**
    * Add the processor to the first place of this queue.
    * <p>
    * While in serial queue this method makes sense because entities are
    * processed by each processor in serial.
    * 
    * @param processor
    *           to be added to the head of this queue
    */
   public void addFirst(Processor<T> processor) {

      ArgsCheck.notNull("processor", processor);

      if (processor.equals(this)) {
         throw new IllegalArgumentException(
               "adding a queue processor to its self is not allowed");
      }

      // The state of running should not be changed while performing this
      // tasks as it may cause problems in the future (start/stop)
      runningLock.readLock().lock();
      try {
         // We may release the processors lock because
         // we are sure the state of running will not be
         // changed
         processorsLock.writeLock().lock();
         boolean added = false;
         try {
            if (!processors.contains(processor)) {
               processors.add(0, processor);
               added = true;
            }
         } finally {
            processorsLock.writeLock().unlock();
         }

         // If this queue is running we should add this processor to the running
         // queue too
         if (added && running) {
            runningProcessorsLock.writeLock().lock();
            try {
               runningProcessors.add(0, processor);
            } finally {
               runningProcessorsLock.writeLock().unlock();
            }
         }
      } finally {
         runningLock.readLock().unlock();
      }
   }
}
