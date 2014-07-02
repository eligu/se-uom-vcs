package gr.uom.se.util.pattern.processor;

import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.junit.Test;

public class ProcessorTest extends TestCase {

   /**
    * Path to test resources.
    * <p>
    */
   public static final String RESOURCES = "src/test/resources/";

   @Test
   public void testPrimeProcessor() throws InterruptedException {

      PrimeCounterProcessor primeProcessor = new PrimeCounterProcessor(1, 20);
      // Test processor
      testProcessor(primeProcessor);
      
      // Check the init processor
      for (int i = 1; i <= 20; i++) {
         assertEquals(0, primeProcessor.primes.get(i).get());
      }

      // Compute results
      primeProcessor.start();
      for (int i = 1; i <= 20; i++) {
         primeProcessor.process(i);
      }
      primeProcessor.stop();

      // Test results
      testPrimeResults(primeProcessor);
   }
   
   void testProcessor(Processor<Integer> processor) throws InterruptedException {
      processor.start();
      assertTrue(processor.isStarted());
      processor.stop();
      assertTrue(!processor.isStarted());
      testRunBeforeStart(processor);
   }
   
   void testPrimeResults(PrimeCounterProcessor primeProcessor) {
      for (int i = primeProcessor.start; i <= primeProcessor.end; i++) {
         assertTrue(primeProcessor.primes.get(i).get() > 0);
         if (i > 1) {
            assertTrue(primeProcessor.primes.get(i).get() >= primeProcessor.primes
                  .get(i - 1).get());
         }
      }
   }
   
   @Test
   public void testSerialQueue() throws InterruptedException {

      int start = 1;
      int end = 1000;

      PrimeCounterProcessor primeProcessor = new PrimeCounterProcessor(start,
            end);
      AvgProcessor avgProcessor = new AvgProcessor();
      SerialQueue<Integer> queue = new SerialQueue<Integer>();

      Set<Processor<Integer>> set = new HashSet<Processor<Integer>>();
      set.add(avgProcessor);
      set.add(primeProcessor);
      
      // Add processors to queue
      queue.add(primeProcessor);
      queue.add(avgProcessor);

      testContainment(queue, set);
      testRunBeforeStart(queue);
      
      queue.start();
      assertTrue(queue.isStarted());
      queue.stop();
      assertTrue(!queue.isStarted());
      
      // Test Run 1: check results
      queue.start();
      for (int i = start; i <= end; i++) {
         queue.process(i);
      }
      queue.stop();

      
   }

   <T> void testContainment(ProcessorQueue<T> queue, Set<Processor<T>> processors) {
      assertEquals(processors.size(), queue.getProcessorsCount());
      for (Processor<T> p : processors) {
         Processor<T> other = queue.getProcessor(p.getId());
         assertNotNull(other);
         assertTrue(p.equals(other));
         queue.remove(p);
         assertEquals(processors.size() - 1, queue.getProcessorsCount());
         assertNull(queue.getProcessor(p.getId()));
         queue.add(p);
         assertEquals(processors.size() , queue.getProcessorsCount());
         queue.add(other);
         assertEquals(processors.size() , queue.getProcessorsCount());
      }
   }

   void testRunBeforeStart(Processor<Integer> queue) {
      // Check execution before start
      boolean thrown = false;
      try {
         queue.process(0);
      } catch (IllegalStateException e) {
         thrown = true;
      }
      assertTrue(thrown);
   }
}
