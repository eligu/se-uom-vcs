/**
 * 
 */
package gr.uom.se.vcs.analysis.util;

import gr.uom.se.util.pattern.processor.Processor;
import gr.uom.se.util.validation.ArgsCheck;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public abstract class KeyValueProcessor<E, K, V> implements Processor<E> {

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
   private static final String DEFAULT_PID = "KEY_VALUE";

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

   protected final ConcurrentHashMap<K, V> values = new ConcurrentHashMap<K, V>();

   private boolean running = false;

   private final ReadWriteLock runningLock = new ReentrantReadWriteLock();

   public KeyValueProcessor(String id) {
      if (id == null) {
         id = generateDefaultId();
      }
      this.id = id;
   }

   public V getValue(K key) {
      ArgsCheck.notNull("key", key);
      runningLock.readLock().lock();
      try {
         if (running) {
            throw new IllegalStateException("can't get value while running");
         }

         return values.get(key);
      } finally {
         runningLock.readLock().unlock();
      }
   }
   
   public Set<K> getKeys() {
      runningLock.readLock().lock();
      try {
         if (running) {
            throw new IllegalStateException("can't get keys while running");
         }

         return Collections.unmodifiableSet(values.keySet());
      } finally {
         runningLock.readLock().unlock();
      }
   }

   @Override
   public boolean process(E entity) {

      runningLock.readLock().lock();
      try {
         return processThis(entity);
      } finally {
         runningLock.readLock().unlock();
      }
   }

   protected abstract boolean processThis(E entity);

   @Override
   public void stop() throws InterruptedException {
      runningLock.writeLock().lock();
      if (!running) {
         return;
      }
      stopThis();
      running = false;
      runningLock.writeLock().unlock();
   }
   
   protected void stopThis() {};

   @Override
   public void start() {
      runningLock.writeLock().lock();
      if (running) {
         return;
      }
      try {
         values.clear();
         startThis();
         running = true;
      } finally {
         runningLock.writeLock().unlock();
      }
   }
   
   protected void startThis() {};

   @Override
   public String getId() {
      return id;
   }

   @Override
   public boolean isStarted() {
      runningLock.readLock().lock();
      try {
         return running;
      } finally {
         runningLock.readLock().unlock();
      }
   }
}
