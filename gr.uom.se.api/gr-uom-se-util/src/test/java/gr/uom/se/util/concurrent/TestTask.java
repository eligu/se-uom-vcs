package gr.uom.se.util.concurrent;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public class TestTask implements Runnable {

   private final AtomicInteger started;
   private final AtomicInteger ended;
   private final AtomicInteger threads;
   private final int maxThreads;
   private final AtomicInteger achievedMaxThreads;

   public TestTask(AtomicInteger started, AtomicInteger ended,
         AtomicInteger threads, AtomicInteger achievedMaxThreads, int max) {
      this.started = started;
      this.ended = ended;
      this.threads = threads;
      this.achievedMaxThreads = achievedMaxThreads;
      this.maxThreads = max;
   }

   @Override
   public void run() {
      started.incrementAndGet();
      int num = threads.getAndIncrement();
      achievedMaxThreads.compareAndSet(num, num + 1);
      Random r = new Random();
      for (int z = 0; z < 10000000; z++) {
         r.nextInt(100000);
      }
      ended.incrementAndGet();
      threads.decrementAndGet();
   }

   boolean achievedMaxThreads() {
      return achievedMaxThreads.get() == maxThreads;
   }

   int started() {
      return started.get();
   }

   int ended() {
      return ended.get();
   }

   boolean haEnded(int num) {
      return ended.get() == num;
   }

   boolean hasStarted(int num) {
      return started.get() == num;
   }
}