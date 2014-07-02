/**
 * 
 */
package gr.uom.se.util.pattern.processor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public class PrimeCounterProcessor extends AbstractProcessor<Integer> implements ResultProcessor<Integer, Map<Integer, AtomicInteger>>{

   Map<Integer, AtomicInteger> primes;
   final int start;
   final int end;
   public PrimeCounterProcessor(int start, int end) {
     this.start = start;
     this.end = end;
   }

   @Override
   protected void starting() {
      primes = new HashMap<Integer, AtomicInteger>();
      // Init map
      for (int i = start; i <= end; i++) {
         primes.put(i, new AtomicInteger(0));
      }
   }
   
   @Override
   public boolean process(Integer entity) {
      runningLock.readLock().lock();
      try {
         if(!running) {
            throw new IllegalStateException("can not process entities while not running");
         }
         // 1 and 2 are primes for sure
         if(entity >= 1)
            primes.get(entity).incrementAndGet();
         if(entity >= 2) {
            primes.get(entity).incrementAndGet();
         }
         for (int i = entity; i > 2; i--) {
            if (i % 2 == 0) {
               continue;
            }
            boolean prime = true;
            for (int j = i - 1; j > 2; j--) {
               if (i % j == 0) {
                  prime = false;
                  break;
               }
            }
            if (prime)
               primes.get(entity).incrementAndGet();
         }
         return true;
      } finally {
         runningLock.readLock().unlock();
      }
   }

   @Override
   public Map<Integer, AtomicInteger> getResult() {
      runningLock.readLock().lock();
      try {
         if(running) {
            throw new IllegalStateException("Can not get results while running");
         }
         return primes;
      } finally {
         runningLock.readLock().unlock();
      }
   }
}
