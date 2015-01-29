/**
 * 
 */
package gr.uom.se.vcs.analysis;

import gr.uom.se.util.pattern.processor.ResultProcessor;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A simple thread safe counter processor that counts each entity that he
 * visits.
 * <p>
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public class CounterProcessor<T> implements ResultProcessor<T, Integer> {

   private final AtomicInteger counter = new AtomicInteger(0);
   private final AtomicBoolean running = new AtomicBoolean(false);

   /**
    * 
    */
   public CounterProcessor() {
   }

   @Override
   public boolean process(T entity) {
      if (entity != null) {
         counter.incrementAndGet();
         return true;
      }
      return false;
   }

   @Override
   public void stop() throws InterruptedException {
      running.compareAndSet(true, false); 
   }

   @Override
   public void start() {
      if (running.compareAndSet(false, true)) {
         counter.set(0);
      }
   }

   @Override
   public String getId() {
      return "CNT";
   }

   @Override
   public boolean isStarted() {
      return running.get();
   }

   @Override
   public Integer getResult() {
      return counter.get();
   }
}
