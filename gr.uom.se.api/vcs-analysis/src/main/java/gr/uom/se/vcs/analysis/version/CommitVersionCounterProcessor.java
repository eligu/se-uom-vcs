/**
 * 
 */
package gr.uom.se.vcs.analysis.version;

import gr.uom.se.util.pattern.processor.ResultProcessor;
import gr.uom.se.vcs.VCSCommit;
import gr.uom.se.vcs.analysis.version.provider.ConnectedVersionProvider;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * An implementation class for counting the number of commits for each version.
 * <p>
 * 
 * Each commit passed in process method must be unique, otherwise this may fail
 * to count the commits exactly.
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public class CommitVersionCounterProcessor extends CommitVersionProcessor
      implements ResultProcessor<VCSCommit, Map<String, Integer>> {

   /**
    * The counters collected so far.
    * <p>
    * While processing entities this will be a write only set, and this will be
    * read only when results are required.
    */
   Map<String, AtomicInteger> counters;
   protected ReadWriteLock countersLock = new ReentrantReadWriteLock();

   /**
    * Create an instance based on the given commits and their respective
    * versionProvider (labels)).
    * <p>
    * If the commit versionProvider are unknown you can use the commit id as a
    * tag (label). Note that versions are copied to a new map so you can freely
    * modify it while processor is running.
    * 
    * @param versions
    *           the commits and their respective versionProvider (labels). Must
    *           not be null not or empty or contain any null key or value.
    * @param id
    *           the id of this processor. If null a default id will be used.
    */
   public CommitVersionCounterProcessor(ConnectedVersionProvider versionProvider,
         String id) {
      super(versionProvider, (id == null ? "COMCOUNT-" + generateDefaultId()
            : id));
   }

   @Override
   protected boolean process(String ver, VCSCommit entity) {
      counters.get(ver).incrementAndGet();
      return true;
   }

   @Override
   protected void starting() {
      // We need to clean up the previous results.
      // Because we are sure that we are locked with
      // running write lock that mean the state will not change,
      // and we are sure that the processor is not running
      // so we can proceed with confidence, but will first need to
      // acquire a lock on results/
      countersLock.writeLock().lock();
      try {
         // Will reset all counters
         if (counters == null) {
            counters = new HashMap<String, AtomicInteger>();
         }
         // The versions map is read only so no need to lock it at all
         for (String ver : versionProvider.getNames()) {
            AtomicInteger counter = counters.get(ver);
            if (counter == null) {
               counter = new AtomicInteger();
               counters.put(ver, counter);
            }
            counter.set(0);
         }
      } finally {
         // Release the lock here
         countersLock.writeLock().unlock();
      }
   }

   @Override
   protected void stopping() { /* Don't need to do anything here */
   }

   @Override
   public Map<String, Integer> getResult() {

      runningLock.readLock().lock();
      try {
         if (running) {
            throw new IllegalStateException(
                  "can't get the result while running");
         }
         countersLock.readLock().lock();
         try {
            Map<String, Integer> vCounters = new TreeMap<String, Integer>();
            for (String ver : counters.keySet()) {
               vCounters.put(ver, counters.get(ver).get());
            }
            return vCounters;
         } finally {
            countersLock.readLock().unlock();
         }
      } finally {
         runningLock.readLock().unlock();
      }
   }
}
