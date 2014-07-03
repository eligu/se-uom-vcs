package gr.uom.se.util.pattern.processor;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * A processor that calculates the average value of the given numbers, to be
 * used in tests.
 * <p>
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public class AvgProcessor extends AbstractProcessor<Integer> implements
      ResultProcessor<Integer, Double> {

   AtomicInteger sum = new AtomicInteger(0);
   AtomicInteger counter = new AtomicInteger(0);

   @Override
   public boolean process(Integer entity) {
      runningLock.readLock().lock();
      try {
         if (!running) {
            throw new IllegalStateException(
                  "Processor can not process because it is not running");
         }
         sum.addAndGet(entity);
         counter.incrementAndGet();
      } finally {
         runningLock.readLock().unlock();
      }
      return true;
   }

   @Override
   protected void starting() {
      sum.set(0);
      counter.set(0);
   }

   @Override
   public Double getResult() {
      runningLock.readLock().lock();
      try {
         if (running) {
            throw new IllegalArgumentException(
                  "Processor can not compute result because it is still running");
         }
         if (counter.get() == 0) {
            return 0.0;
         } else {
            return sum.get() / (double) counter.get();
         }
      } finally {
         runningLock.readLock().unlock();
      }
   }
}