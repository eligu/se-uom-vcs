package gr.uom.se.util.pattern.processor;

import gr.uom.se.util.validation.ArgsCheck;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A simple abstract processor used to simplify the creation of anonymous
 * processors.
 * <p>
 * 
 * A default processor id is provided if null is passed at constructor. The
 * default id will contain the string {@code APROCESSOR} appended an instance
 * number (such as {@code APROCESSOR12}). Subclasses that want to change the
 * default string may create a static block of code and change the static field
 * {@link #DEFAULT_PID}. If they want to change the whole default id
 * implementation they may override the method {@link #generateDefaultId()}.
 * <p>
 * This processor is thread safe and will acquire a write lock in the state of
 * running ({@link #running} before {@link #start()} or {@link #stop()} and call
 * the corresponding method {@link #starting()} or {@link #stopping()}). After
 * the finish of this method it will release this lock. The {@link #starting()}
 * and {@link #stopping()} methods has an empty implementation in order to
 * simplify the building of anonymous processors with simple work flow, and they
 * are required to be overridden if a full work flow is required.
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public abstract class AbstractProcessor<T> implements Processor<T> {

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
   protected static String DEFAULT_PID = "APROCESSOR";

   /**
    * @return a default id for this type of processor
    */
   protected String generateDefaultId() {
      return DEFAULT_PID + instances.incrementAndGet();
   }

   /**
    * The id of this processor.
    * <p>
    */
   protected final String id;

   /**
    * The lock of running.
    * <p>
    */
   protected final ReadWriteLock runningLock = new ReentrantReadWriteLock();

   /**
    * Flag that indicates if this queue is running.
    * <p>
    */
   protected Boolean running = false;

   /**
    * Create a new instance by setting a default id.
    * <p>
    */
   public AbstractProcessor() {
      this(null);
   }

   /**
    * Create a new instance with the given id.
    * <p>
    * 
    * @param id
    *           of this processor. If null a default id will be provided.
    */
   public AbstractProcessor(String id) {
      if (id == null) {
         id = generateDefaultId();
      } else {
         ArgsCheck.notEmpty("id", id);
      }
      this.id = id.trim();
   }

   @Override
   public void stop() throws InterruptedException {
      // We need a write lock on running state so no one can execute any other
      // code that is based on the state of this processor. Because we are
      // cleaning our state here.
      runningLock.writeLock().lock();
      try {
         if (!running) {
            return;
         }
         stopping();
         
      } finally {
         // Set this queue as running
         running = false;
         runningLock.writeLock().unlock();
      }
   }

   /**
    * This method should only be used if the implementation is ok with the
    * default implementation of {@link #stop()} method. If not the the
    * {@link #stop()} method should be overridden.
    * <p>
    * @throws InterruptedException
    */
   protected void stopping() throws InterruptedException {}

   @Override
   public void start() {
      // We need a write lock on running state so no one can execute any other
      // code that is based on the state of this processor. Because we are
      // cleaning our state here.
      runningLock.writeLock().lock();
      try {
         if (running) {
            return;
         }
         starting();
         // Set this queue as running
         running = true;
      } finally {
         runningLock.writeLock().unlock();
      }
   }

   /**
    * This method should only be used if the implementation is ok with the
    * default implementation of {@link #start()} method. If not the
    * {@link #start()} method should be overridden.
    * <p>
    */
   protected void starting() {}

   @Override
   public String getId() {
      return id;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean isStarted() {
      runningLock.readLock().lock();
      try {
         return running;
      } finally {
         runningLock.readLock().unlock();
      }
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((id == null) ? 0 : id.hashCode());
      return result;
   }

   /**
    * Check if this processor is equal to another processor.
    * <p>
    * Two processors are equal if they are instances of {@link Processor}
    * interface and have the same id.
    * <p>
    * <b>Warning</b>: This may return true if a processor and a queue have the
    * same id
    */
   @Override
   public boolean equals(Object obj) {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (Processor.class.isAssignableFrom(obj.getClass()))
         return false;
      AbstractProcessor<?> other = (AbstractProcessor<?>) obj;
      if (id == null) {
         if (other.id != null)
            return false;
      } else if (!id.equals(other.id))
         return false;
      return true;
   }
}
