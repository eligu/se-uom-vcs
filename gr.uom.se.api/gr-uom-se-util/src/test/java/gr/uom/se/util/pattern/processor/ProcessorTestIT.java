package gr.uom.se.util.pattern.processor;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.TestCase;

import org.junit.Test;

/**
 * A test class for testing processors API.
 * <p>
 * There are tests for three processor queues, simulating their operation with
 * two custom processors. For each queue there are a lot of tests to perform. We
 * test each queue independently and a mix of them by checking whether two
 * queues running the same processors with the same data return the same result.
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public class ProcessorTestIT extends TestCase {

   /**
    * Test the single prime processor.
    * <p>
    * This will also test {@link AbstractProcessor} class.
    */
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

   /**
    * Test SerialProcessorQueue.
    * <p>
    * 
    * @throws InterruptedException
    * @see {@link SerialProcessorQueue}
    */
   @Test
   public void testSerialQueue() throws InterruptedException {
      SerialProcessorQueue<Integer> queue = new SerialProcessorQueue<Integer>();
      testQueue(queue);
   }

   /**
    * A general test for DefaultParallelProcessorQueue, is the same as testQueue but will call
    * shutdown in the end of the test.
    * <p>
    * 
    * @throws InterruptedException
    * @see {@link DefaultParallelProcessorQueue}
    */
   @Test
   public void testThreadQueue() throws InterruptedException {

      DefaultParallelProcessorQueue<Integer> queue = new DefaultParallelProcessorQueue<Integer>(8, null);
      testThreadQueue(queue);
   }

   /**
    * A test for BlockingParallelProcessorQueue.
    * <p>
    * Works the same as testThreadQueue().
    * 
    * @throws InterruptedException
    * @see {@link BlockingParallelProcessorQueue}
    */
   @Test
   public void testBlockQueue() throws InterruptedException {
      DefaultParallelProcessorQueue<Integer> queue = new BlockingParallelProcessorQueue<Integer>(8, 100, null);
      testThreadQueue(queue);
   }

   @Test
   public void testMixQueue() throws InterruptedException {
      MixQueue<Integer> queue = new MixQueue<Integer>(2, true, 1000, false,
            null);
      testMixQueue(queue);
      queue.shutdown();
      queue = new MixQueue<Integer>(2, true, 1000, true, null);
      testMixQueue(queue);
      queue.shutdown();
   }

   /**
    * A combined test for all queues.
    * <p>
    * Will create each queue instance, and then will test each queue
    * independently by calling {@link #testQueue(ProcessorQueue pq)}. Then will
    * combine each two queues to run them together and check the results for
    * equality ({@link #testBothQueues(ProcessorQueue q1, ProcessorQueue q2)}).
    * Generally speaking this test shows if the processors running in parallel
    * compute the same result as the processors running in sequential.
    * 
    * @throws InterruptedException
    */
   @Test
   public void testAll() throws InterruptedException {
      DefaultParallelProcessorQueue<Integer> tQueue = new DefaultParallelProcessorQueue<Integer>(8, null);
      DefaultParallelProcessorQueue<Integer> bQueue = new BlockingParallelProcessorQueue<Integer>(8, 100, null);
      SerialProcessorQueue<Integer> sQueue = new SerialProcessorQueue<Integer>();
      MixQueue<Integer> mQueue = new MixQueue<Integer>(2, true, 1000, true,
            null);

      testMixQueue(mQueue);
      testQueue(tQueue);
      testQueue(bQueue);
      testQueue(sQueue);
      testBothQueues(tQueue, bQueue);
      testBothQueues(tQueue, sQueue);
      testBothQueues(sQueue, bQueue);
      tQueue.shutdown();
      bQueue.shutdown();
      mQueue.shutdown();
   }

   /**
    * Test thread queue by calling {@link #testQueue(ProcessorQueue pq)} and
    * shutdown the queue after test.
    * <p>
    * 
    * @param queue
    * @throws InterruptedException
    */
   private void testThreadQueue(DefaultParallelProcessorQueue<Integer> queue)
         throws InterruptedException {
      testQueue(queue);
      queue.shutdown();
   }

   /**
    * Test two queues by testing them first independently and then by creating
    * two processors that compute data. There are two processors that compute
    * primes and two that compute average. All of them have identical
    * configuration. Two kind of processors are passed to first queue and the
    * other two kind to the second queue. Queues are ran together two times,
    * calling accordingly start/stop, and their results are checked for
    * equality.
    * 
    * @param queue1
    * @param queue2
    * @throws InterruptedException
    */
   public void testBothQueues(ProcessorQueue<Integer> queue1,
         ProcessorQueue<Integer> queue2) throws InterruptedException {
      // Independent test of queues
      testQueue(queue1);
      testQueue(queue2);

      // Create 4 processors to use with queues
      int start = 1;
      int end = 1000;

      PrimeCounterProcessor primeProcessor1 = new PrimeCounterProcessor(start,
            end);
      AvgProcessor avgProcessor1 = new AvgProcessor();

      PrimeCounterProcessor primeProcessor2 = new PrimeCounterProcessor(start,
            end);
      AvgProcessor avgProcessor2 = new AvgProcessor();

      // Clear both queues
      queue1.removeAll();
      assertEquals(0, queue1.getProcessorsCount());
      queue2.removeAll();
      assertEquals(0, queue2.getProcessorsCount());

      // Add processors to queues
      queue1.add(avgProcessor1);
      queue1.add(primeProcessor1);
      queue2.add(avgProcessor2);
      queue2.add(primeProcessor2);

      // Run both, first run
      queue1.start();
      queue2.start();
      for (int i = start; i <= end; i++) {
         queue1.process(i);
         queue2.process(i);
      }
      queue1.stop();
      queue2.stop();

      // Run both, second run
      queue1.start();
      queue2.start();
      for (int i = start; i <= end; i++) {
         queue1.process(i);
         queue2.process(i);
      }
      queue1.stop();
      queue2.stop();

      // Check the results of two same processors ran from two threads
      assertEquals(avgProcessor1.getResult(), avgProcessor2.getResult());

      Map<Integer, AtomicInteger> result1 = primeProcessor1.getResult();
      Map<Integer, AtomicInteger> result2 = primeProcessor2.getResult();

      for (Integer i : result1.keySet()) {
         AtomicInteger v1 = result1.get(i);
         AtomicInteger v2 = result2.get(i);
         assertNotNull(v1);
         assertNotNull(v2);
         assertEquals(v1.get(), v2.get());
      }
      for (Integer i : result2.keySet()) {
         AtomicInteger v1 = result1.get(i);
         AtomicInteger v2 = result2.get(i);
         assertNotNull(v1);
         assertNotNull(v2);
         assertEquals(v1.get(), v2.get());
      }
   }

   public void testMixQueue(MixQueue<Integer> queue)
         throws InterruptedException {

      // Create 4 processors to use with queues
      int start = 1;
      int end = 1000;

      PrimeCounterProcessor primeProcessor1 = new PrimeCounterProcessor(start,
            end);
      primeProcessor1.id = "PP1";
      AvgProcessor avgProcessor1 = new AvgProcessor();
      avgProcessor1.id = "AP1";
      PrimeCounterProcessor primeProcessor2 = new PrimeCounterProcessor(start,
            end);
      primeProcessor2.id = "PP2";
      AvgProcessor avgProcessor2 = new AvgProcessor();
      avgProcessor2.id = "AP2";
      Set<Processor<Integer>> set = new HashSet<Processor<Integer>>();
      set.add(avgProcessor1);
      set.add(primeProcessor1);
      set.add(avgProcessor2);
      set.add(primeProcessor2);

      queue.removeAll();
      assertEquals(0, queue.getProcessorsCount());

      // Add processors to queue
      queue.add(primeProcessor1);
      queue.add(avgProcessor1);
      queue.addParallel(primeProcessor2);
      queue.addParallel(avgProcessor2);

      // Test this queue
      testProcessor(queue);
      testContainment(queue, set);

      queue.removeAll();
      assertEquals(0, queue.getProcessorsCount());

      // Add processors to queue
      queue.add(primeProcessor1);
      queue.add(avgProcessor1);
      queue.addParallel(primeProcessor2);
      queue.addParallel(avgProcessor2);

      // Test Run 1: check results
      queue.start();
      for (int i = start; i <= end; i++) {
         queue.process(i);
      }
      queue.stop();

      // Check primes
      testPrimeResults(primeProcessor1);
      testPrimeResults(primeProcessor2);

      // Check avg
      assertEquals(calcAvg(start, end), avgProcessor1.getResult());
      assertEquals(calcAvg(start, end), avgProcessor2.getResult());

      queue.removeAll();
      assertEquals(0, queue.getProcessorsCount());

      // Add processors to queue
      queue.add(primeProcessor1);
      queue.add(avgProcessor1);
      queue.addParallel(primeProcessor2);
      queue.addParallel(avgProcessor2);
      
      // First run test
      testRun(queue, primeProcessor1, avgProcessor1, primeProcessor2, avgProcessor2);
      queue.removeAll();
      assertEquals(0, queue.getProcessorsCount());

      // Add processors to queue
      queue.add(primeProcessor1);
      queue.add(avgProcessor1);
      queue.addParallel(primeProcessor2);
      queue.addParallel(avgProcessor2);
      double avg1 = avgProcessor1.getResult();
      double avg2 = avgProcessor2.getResult();
      
      // Second run test (check for subsequent run)
      testRun(queue, primeProcessor1, avgProcessor1, primeProcessor2, avgProcessor2);

      // Check the results of two same processors ran from two threads
      assertEquals(avgProcessor1.getResult(), avg1);
      assertEquals(avgProcessor2.getResult(), avg2);
      // Check avg
      assertEquals(calcAvg(start, end), avgProcessor1.getResult());
      assertEquals(calcAvg(start, end), avgProcessor2.getResult());

      Map<Integer, AtomicInteger> result1 = primeProcessor1.getResult();
      Map<Integer, AtomicInteger> result2 = primeProcessor2.getResult();

      for (Integer i : result1.keySet()) {
         AtomicInteger v1 = result1.get(i);
         AtomicInteger v2 = result2.get(i);
         assertNotNull(v1);
         assertNotNull(v2);
         assertEquals(v1.get(), v2.get());
      }
      for (Integer i : result2.keySet()) {
         AtomicInteger v1 = result1.get(i);
         AtomicInteger v2 = result2.get(i);
         assertNotNull(v1);
         assertNotNull(v2);
         assertEquals(v1.get(), v2.get());
      }
   }

   /**
    * A full test of a queue.
    * <p>
    * All operations of the queue will be tested. Moreover processors will be
    * removed and or added while the queue is running. It will run the queue two
    * times. Also a queue is ran as single processor too and tests are
    * performed.
    * 
    * @param queue
    *           to test
    * @throws InterruptedException
    */
   public void testQueue(ProcessorQueue<Integer> queue)
         throws InterruptedException {
      int start = 1;
      int end = 3000;
      // Create processors to test the queue
      PrimeCounterProcessor primeProcessor = new PrimeCounterProcessor(start,
            end);
      AvgProcessor avgProcessor = new AvgProcessor();

      Set<Processor<Integer>> set = new HashSet<Processor<Integer>>();
      set.add(avgProcessor);
      set.add(primeProcessor);

      queue.removeAll();
      assertEquals(0, queue.getProcessorsCount());

      // Add processors to queue
      queue.add(primeProcessor);
      queue.add(avgProcessor);

      // Test this queue
      testProcessor(queue);
      testContainment(queue, set);

      // Test Run 1: check results
      queue.start();
      for (int i = start; i <= end; i++) {
         queue.process(i);
      }
      queue.stop();

      // Check primes
      testPrimeResults(primeProcessor);

      // Check avg
      assertEquals(calcAvg(start, end), avgProcessor.getResult());

      // First run test
      testRun(queue, primeProcessor, avgProcessor);
      // Second run test (check for subsequent run)
      testRun(queue, primeProcessor, avgProcessor);
   }

   /**
    * Run a queue which contains the following processors and check its
    * operations, while running, such as removing/adding a processor while
    * running.
    */
   void testRun(ProcessorQueue<Integer> queue,
         PrimeCounterProcessor primeProcessor, AvgProcessor avgProcessor)
         throws InterruptedException {
      // Test Run 2: check add/remove processor while running
      // Test Run 1: check results
      int start = primeProcessor.start;
      int end = primeProcessor.end;
      queue.start();
      int counter = 2;
      boolean removedPrime = false;
      boolean removedAvg = false;
      int avgCounter = 0;
      for (int i = start; i <= end; i++) {
         queue.process(i);
         if(queue.getProcessor(avgProcessor.getId()) != null) {
            avgCounter++;
         }
         if (i % 3 == 0) {
            if (!removedPrime) {
               counter--;
               queue.remove(primeProcessor);
               removedPrime = true;
            } else {
               counter++;
               queue.add(primeProcessor);
               removedPrime = false;
            }
         }
         if (i % 7 == 0) {
            if (!removedAvg) {
               counter--;
               queue.remove(avgProcessor);
               removedAvg = true;
            } else {
               counter++;
               queue.add(avgProcessor);
               removedAvg = false;
            }
         }

         if (removedPrime) {
            assertNull(queue.getProcessor(primeProcessor.getId()));
         } else {
            assertNotNull(queue.getProcessor(primeProcessor.getId()));
         }
         if (removedAvg) {
            assertNull(queue.getProcessor(avgProcessor.getId()));
         } else {
            assertNotNull(queue.getProcessor(avgProcessor.getId()));
         }

         if (counter == 0) {
            assertFalse(queue.process(null));
            assertTrue(removedPrime);
            assertTrue(removedAvg);
         }
         assertEquals(counter, queue.getProcessorsCount());
      }
      queue.stop();
      
      assertEquals(avgProcessor.numbers.size(), avgCounter);
      
   }

   /**
    * Run a queue which contains the following processors and check its
    * operations, while running, such as removing/adding a processor while
    * running.
    */
   void testRun(MixQueue<Integer> queue, PrimeCounterProcessor primeProcessor1,
         AvgProcessor avgProcessor1, PrimeCounterProcessor primeProcessor2,
         AvgProcessor avgProcessor2) throws InterruptedException {
      // Test Run 2: check add/remove processor while running
      // Test Run 1: check results
      int start = primeProcessor1.start;
      int end = primeProcessor1.end;
      queue.start();
      
      for (int i = start; i <= end; i++) {
         queue.process(i);
      }
      queue.stop();
      
      assertEquals(end, avgProcessor1.counter.get());
      assertEquals(end, avgProcessor2.counter.get());
      assertTrue(avgProcessor1.numbers.containsAll(avgProcessor2.numbers));
      assertTrue(avgProcessor2.numbers.containsAll(avgProcessor1.numbers));
      assertEquals(avgProcessor1.getResult(), avgProcessor2.getResult());
   }

   /**
    * Calculate the average of a range of numbers.
    * <p>
    * 
    * @param start
    *           of the range
    * @param end
    *           of the range
    * @return the average of the specified range
    */
   double calcAvg(int start, int end) {
      int sum = 0;
      for (int i = start; i <= end; i++) {
         sum += i;
      }
      return sum / (double) (end - start + 1);
   }

   /**
    * Test a processor's start/stop/isStarted operations.
    * <p>
    * 
    * @param processor
    *           to test
    * @throws InterruptedException
    */
   <T> void testProcessor(Processor<T> processor) throws InterruptedException {
      processor.start();
      assertTrue(processor.isStarted());
      processor.stop();
      assertTrue(!processor.isStarted());
      testRunBeforeStart(processor);
   }

   /**
    * Test results of prime processor.
    * <p>
    * 
    * @param primeProcessor
    *           to test
    */
   void testPrimeResults(PrimeCounterProcessor primeProcessor) {
      for (int i = primeProcessor.start; i <= primeProcessor.end; i++) {
         assertTrue(primeProcessor.getResult().get(i).get() > 0);
         if (i > 1) {
            assertTrue(primeProcessor.getResult().get(i).get() >= primeProcessor.primes
                  .get(i - 1).get());
         }
      }
   }

   /**
    * Test operations such as add/remove processors.
    * <p>
    * 
    * @param queue
    *           which contains the processors and will be tested
    * @param processors
    *           the processors of this queue
    */
   <T> void testContainment(ProcessorQueue<T> queue,
         Set<Processor<T>> processors) {
      assertEquals(processors.size(), queue.getProcessorsCount());
      for (Processor<T> p : processors) {
         Processor<T> other = queue.getProcessor(p.getId());
         assertNotNull(other);
         assertTrue(p.equals(other));
         queue.remove(p);
         assertEquals(processors.size() - 1, queue.getProcessorsCount());
         assertNull(queue.getProcessor(p.getId()));
         queue.add(p);
         assertEquals(processors.size(), queue.getProcessorsCount());
         queue.add(other);
         assertEquals(processors.size(), queue.getProcessorsCount());
      }
   }

   /**
    * Check if a queue can be executed before start.
    * <p>
    * Normally this will throw an exception if you try to call a queue's
    * <code>process(Object o)<code> method.
    * 
    * @param queue
    *           to test
    */
   <T> void testRunBeforeStart(Processor<T> queue) {
      // Check execution before start
      boolean thrown = false;
      try {
         queue.process(null);
      } catch (IllegalStateException e) {
         thrown = true;
      }
      assertTrue(thrown);
   }
}
