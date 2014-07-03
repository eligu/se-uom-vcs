package gr.uom.se.util.pattern.processor;

import gr.uom.se.util.validation.ArgsCheck;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * An abstract class for all processor queues.
 * <p>
 * 
 * Before running the method <code>start</code> should be called. When this
 * method is called this will put all processors to a queue of <b>running
 * processors</b>. Each time an entity comes, it will be delivered to all
 * processors that are in the running queue. If a processor doesn't need any
 * more entities and return false when its <code>process</code> method is
 * called, it will be moved to a list of <b>stopped processors</b>.
 * <p>
 * If a processor throws an exception while processing an entity the exception
 * will be catch and put to a list of <b>thrown exceptions</b>. When the queue
 * is stopped, all processors that are in the running queue will be moved to
 * stopped processors queue, and all processors in the stopped queue will be
 * stopped. Those that stopped preliminary are not stopped by the queue at
 * processing stage but only when this queue is stopped (the processor itself
 * should be responsible for knowing its state and stopping it self if something
 * gets wrong or wants to stop early). At this point all cached exceptions
 * messages will be aggregated to one and an exception will throw (if there are
 * cached exceptions).
 * <p>
 * If there are not processors in the queue of running processors, this will not
 * accept any entity, and will always return false when an entity comes for
 * processing.
 * <p>
 * If a processor is added to this queue while is running, it will be added to
 * running processors too. If a processor is removed while this queue is running
 * it will be moved from running to stopped queue (if it is there), and will be
 * stopped when the queue will stop. The stopped processors queue and the thrown
 * exceptions will clear after this queue starts again.
 * <p>
 * This queue is thread safe and implementations should not break the invariants.
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 * @param <T>
 *           the type of entity
 */
public abstract class AbstractProcessorQueue<T> extends AbstractProcessor<T>
      implements ProcessorQueue<T> {

   /**
    * The list of running processors to whom each entity will be delivered.
    * <p>
    */
   protected List<Processor<T>> runningProcessors;
   /**
    * The lock of runningProcessors.
    * <p>
    */
   protected final ReadWriteLock runningProcessorsLock = new ReentrantReadWriteLock();
   /**
    * The list where each processor that should not accept any more entities is
    * stored.
    * <p>
    */
   protected List<Processor<T>> stoppedProcessors;
   /**
    * The lock of stoppedProcessors.
    * <p>
    */
   protected final ReadWriteLock stoppedProcessorsLock = new ReentrantReadWriteLock();
   /**
    * The list where all original processors are stored.
    * <p>
    * This is used to keep the iteration order of processors.
    */
   protected List<Processor<T>> processors;
   /**
    * The lock of processors.
    * <p>
    */
   protected final ReadWriteLock processorsLock = new ReentrantReadWriteLock();
   
   /**
    * A list of collected exceptions, while running.
    * <p>
    */
   protected List<InterruptedException> thrownExceptions;
   /**
    * The lock of thrownExceptions.
    * <p>
    */
   protected final ReadWriteLock thrownExceptionsLock = new ReentrantReadWriteLock();

   /**
    * Set the default processor id.
    * <p>
    */
   static {
      DEFAULT_PID = "AQueue";
   }

   /**
    * Add the element in the specified collection if its not there.
    * <p>
    * 
    * @param collection
    *           to add the element
    * @param element
    *           to be added to collection
    * @return true if the element was added
    */
   protected static <T> boolean addIfNot(Collection<T> collection, T element) {
      if (!collection.contains(element)) {
         return collection.add(element);
      }
      return false;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void add(Processor<T> processor) {

      ArgsCheck.notNull("processor", processor);

      // This check makes sure that we do not fall into a
      // recursive mode
      if (processor.equals(this)) {
         throw new IllegalArgumentException(
               "adding a queue processor to it self is not allowed");
      }

      // The state of running should not be changed while performing this
      // task as it may cause problems in the future (start/stop)
      runningLock.readLock().lock();
      try {
         // Requires a lock for all processors queue
         // And add the processor to this queue
         processorsLock.writeLock().lock();
         boolean added = false;
         try {
            added = addIfNot(processors, processor);
         } finally {
            processorsLock.writeLock().unlock();
         }

         // If this queue is running we should add this processor to the running
         // queue too, that will allow the processor to immediately start
         // getting
         // entities
         if (added && running) {
            runningProcessorsLock.writeLock().lock();
            try {
               addIfNot(runningProcessors, processor);
            } finally {
               runningProcessorsLock.writeLock().unlock();
            }
         }
      } finally {
         runningLock.readLock().unlock();
      }
   }

   /**
    * Creates a new processors queue.
    * <p>
    */
   public AbstractProcessorQueue() {
      this(null);
   }

   /**
    * Creates a new processors queue with the given id.
    * <p>
    * 
    * @param id
    *           of this queue. If null a default id will be given.
    */
   public AbstractProcessorQueue(String id) {
      super(id);
      runningProcessors = new ArrayList<Processor<T>>(10);
      stoppedProcessors = new ArrayList<Processor<T>>(10);
      processors = new ArrayList<Processor<T>>(10);
      thrownExceptions = new ArrayList<InterruptedException>();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void remove(Processor<T> processor) {

      ArgsCheck.notNull("processor", processor);

      // The state of running should not be changed while performing this
      // tasks as it may cause problems in the future (start/stop)
      runningLock.readLock().lock();
      try {
         // We may release the processors lock because
         // we are sure the state of running will not be
         // changed
         processorsLock.writeLock().lock();
         boolean removed = false;
         try {
            removed = processors.remove(processor);
         } finally {
            processorsLock.writeLock().unlock();
         }

         if (removed && running) {
            scheduleForStop(processor);
         }
      } finally {
         runningLock.readLock().unlock();
      }
   }

   /**
    * Schedule a processor for stop by removing it from the running queue and
    * adding to stopped queue. A lock on running state must be held in order to
    * call this method.
    * 
    * @param processor
    */
   private void scheduleForStop(Processor<T> processor) {
      // If the queue is running we must first check to see if the
      // processor is in the running queue, if so we must remove it
      // from there and put in the stopped processors so when this
      // queue is stopped we don't miss any thrown exception.
      // As long as the running state is locked we can perform these
      // tasks quietly.
      if (running) {
         runningProcessorsLock.writeLock().lock();
         boolean removed = false;
         try {
            removed = runningProcessors.remove(processor);
         } finally {
            runningProcessorsLock.writeLock().unlock();
         }
         // We first release the running processors so the queue can
         // normally process
         if (removed) {
            stoppedProcessorsLock.writeLock().lock();
            try {
               addIfNot(stoppedProcessors, processor);
            } finally {
               stoppedProcessorsLock.writeLock().unlock();
            }
         }
      }
   }

   @Override
   protected void stopping() throws InterruptedException {

      // A queue may have multiply interruption exceptions
      // because any processor that is stopped may throw
      // an exception. Therefore we collect all exception
      // messages to one place and throw an exception with the
      // messages if any of the queued processors throws an
      // exception

      // Also we do not need to hold any other lock, but the running lock
      // as long as this is a write lock no other method that requires
      // running lock can perform
      Processor<T> p = null;

      try {
         // Try to stop each processor, and do not stop if any
         // of the processors throws an exception, but collect
         // their exception messages
         Iterator<Processor<T>> it = runningProcessors.iterator();
         while (it.hasNext()) {
            p = it.next();
            // Put this processor to the list of stopped
            // processors so the queue can be used again after
            // a restart
            addIfNot(stoppedProcessors, p);
         }

         // Clear the running processors so no more memory is
         // occupied
         runningProcessors.clear();

         // Stop each stopped processor
         it = stoppedProcessors.iterator();
         while (it.hasNext()) {
            p = it.next();
            // Stop all processors that are in the queue of early stopped
            // processors
            try {
               p.stop();
            } catch (Exception e) {
               thrownExceptions.add(new InterruptedException("PID " + p.getId()
                     + " " + e.getMessage()));
            }
         }
         stoppedProcessors.clear();

      } catch (Exception e) {
         thrownExceptions.add(new InterruptedException("PID " + this.getId()
               + e.getMessage()));
      } finally {

         // Check if any of the processors has thrown an exception,
         // if so throw an exception too
         InterruptedException toBeThrown = null;
         if (!thrownExceptions.isEmpty()) {
            StringBuilder exceptMsg = new StringBuilder("Processor ").append(
                  getId()).append(" stopped with the following exceptions:");
            for (Exception e : thrownExceptions) {
               exceptMsg.append("\n").append(e.getMessage());
            }

            // Clear the thrown exceptions
            thrownExceptions.clear();
            toBeThrown = new InterruptedException(exceptMsg.toString());
         }

         if (toBeThrown != null)
            throw toBeThrown;
      }
   }

   @Override
   protected void starting() {
      // Schedule all processors for running
      runningProcessors.addAll(processors);

      // Start each processor now
      for (Processor<?> p : runningProcessors) {
         p.start();
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public synchronized String getId() {
      return id;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Processor<T> getProcessor(String pid) {
      processorsLock.readLock().lock();
      try {
         Iterator<Processor<T>> it = processors.iterator();
         while (it.hasNext()) {
            Processor<T> p = it.next();
            if (pid.equals(p.getId())) {
               return p;
            }
         }
         return null;
      } finally {
         processorsLock.readLock().unlock();
      }
   }

   /**
    * This is a cache that we do not need synchronization, because we just write
    * the processors that should be removed from the running queue. So even if
    * another thread writes the same processor while a thread is writing it
    * there is no problem in duplicates.
    */
   protected final List<Processor<T>> toBeStopped = new ArrayList<Processor<T>>();

   @Override
   public boolean process(T entity) {

      // The running should not be changed while processing an entity.
      // The running value is changed only on stop and start methods
      runningLock.readLock().lock();
      try {
         if (!running) {
            throw new IllegalStateException(
                  "processor can not process any entity without first being started");
         }

         // While we are at this point no changes should be made to the running
         // queue, we get first a read lock and then change it to a write lock
         // if its needed
         runningProcessorsLock.readLock().lock();
         boolean process = true;
         try {
            // Do this in case all the processors are removed
            // while running so we will avoid acquiring locks and
            // terminate early
            if (runningProcessors.isEmpty()) {
               return false;
            }

            try {
               process = processThis(entity);
            } catch (Exception e) {
               // Collect this exception
               thrownExceptionsLock.writeLock().lock();
               try {
                  thrownExceptions
                        .add(new InterruptedException(e.getMessage()));
               } finally {
                  thrownExceptionsLock.writeLock().unlock();
               }
            }

            // At this point we must remove all processors that are to be
            // stopped from the running queue.
            // We must release the read lock
            // and try to get a write lock
            runningProcessorsLock.readLock().unlock();
            runningProcessorsLock.writeLock().lock();
            stoppedProcessorsLock.writeLock().lock();
            try {
               // toBeStopped is only changed while a thread has a write lock on
               // running processors. At this point we have a write lock to
               // running
               // processors so no one can change toBeStopped

               for (Processor<T> p : toBeStopped) {
                  runningProcessors.remove(p);
                  addIfNot(stoppedProcessors, p);
               }
               // must clear the to be removed
               toBeStopped.clear();
               // We must acquire again the running processors lock so
               // we can read if he is empty or not
               runningProcessorsLock.readLock().lock();
            } finally {
               stoppedProcessorsLock.writeLock().unlock();
               runningProcessorsLock.writeLock().unlock();
            }

            // Check this again because the running processors may have changed
            if (runningProcessors.isEmpty()) {
               return false;
            }
         } finally {
            runningProcessorsLock.readLock().unlock();
         }
         return process;
      } finally {
         runningLock.readLock().unlock();
      }
   }

   /**
    * All methods to {@link #process(Object)} will be delegated to this method.
    * This method can throw any exception, and it will be cached and put to
    * thrownExceptions collection. However it is recommended to catch the
    * exceptions and put them in the thrownExceptions list.
    * <p>
    * Any time a processor is going to be removed from running queue put it to
    * the list of toBeStopped processors so the implementation of this class
    * {@link #process(Object)} will collect it and schedule for stopping. This
    * method is called in safe, that is, while executing, the state of this
    * queue (if running or not) and the running queue will be locked in read
    * mode.
    * <p>
    * DO NOT modify the running queue as it increases the risk of code break.
    * You can not acquire a write lock on running state or running queue in the
    * implementation o this method.
    * <p>
    * The subclasses should implement only the essentials of processing an
    * entity and do not worry for the other queue management aspects.
    * 
    * @param entity
    * @return
    */
   protected abstract boolean processThis(T entity);

   /**
    * {@inheritDoc}
    */
   @Override
   public void removeAll() {
      runningLock.readLock().lock();
      processorsLock.writeLock().lock();
      if (running) {
         runningProcessorsLock.writeLock().lock();
         stoppedProcessorsLock.writeLock().lock();
         thrownExceptionsLock.writeLock().lock();
      }
      try {
         List<Processor<T>> copy = new ArrayList<Processor<T>>(processors);
         for(Processor<T> p : copy) {
            this.remove(p);
         }
      } finally {
         if (running) {
            thrownExceptionsLock.writeLock().lock();
            stoppedProcessorsLock.writeLock().lock();
            runningProcessorsLock.writeLock().lock();
         }
         processorsLock.writeLock().unlock();
         runningLock.readLock().unlock();
      }
   }

   public int getProcessorsCount() {
      processorsLock.readLock().lock();
      try {
         return processors.size();
      } finally {
         processorsLock.readLock().unlock();
      }
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((id == null) ? 0 : id.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      @SuppressWarnings("unchecked")
      AbstractProcessorQueue<T> other = (AbstractProcessorQueue<T>) obj;
      if (id == null) {
         if (other.id != null)
            return false;
      } else if (!id.equals(other.id))
         return false;
      return true;
   }
}