/**
 * 
 */
package gr.uom.se.vcs.analysis.version;

import gr.uom.se.util.pattern.processor.ResultProcessor;
import gr.uom.se.vcs.VCSCommit;
import gr.uom.se.vcs.analysis.version.provider.ConnectedVersionProvider;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Collect all authors/committers for each version.
 * <p>
 * If a commit is passed twice for processing this class will still work because
 * authors are stored in a set and no duplicates will be present.
 * <p>
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public class AuthorVersionProcessor extends
      CommitVersionProcessor implements
      ResultProcessor<VCSCommit, Map<String, Set<String>>> {

   /**
    * The authors per commit collected so far.
    * <p>
    * While processing entities this will be a write only set, and this will be
    * read only when results are required.
    */
   private Map<String, Set<String>> authors;
   private ReadWriteLock authorsLock = new ReentrantReadWriteLock();

   /**
    * True if this processor should collect the authors, false fo committers.
    * <p>
    */
   private final boolean collectAuthors;

   /**
    * Create an instance based on the given commits and their respective versionProvider
    * (labels)).
    * <p>
    * If the commit versionProvider are unknown you can use the commit id as a tag (label).
    * Note that versions are copied to a new map so you can freely modify it
    * while processor is running. This will collect the authors for each
    * version. If you want to collect the committers use the next constructor
    * with false for authors.
    * 
    * @param versions
    *           the commits and their respective versionProvider (labels). Must not be null
    *           not or empty or contain any null key or value.
    * @param id
    *           the id of this processor
    */
   public AuthorVersionProcessor(ConnectedVersionProvider versionProvider, String id) {
      this(versionProvider, id, true);
   }

   /**
    * Create an instance based on the given commits and their respective versionProvider
    * (labels)).
    * <p>
    * If the commit versionProvider are unknown you can use the commit id as a tag (label).
    * Note that versions are copied to a new map so you can freely modify it
    * while processor is running.
    * 
    * @param versions
    *           the commits and their respective versionProvider (labels). Must not be null
    *           not or empty or contain any null key or value.
    * @param id
    *           the id of this processor
    * @param authors
    *           true if the authors field of the committer should be collected,
    *           and false if the committer field should be collected
    */
   public AuthorVersionProcessor(ConnectedVersionProvider versionProvider, String id,
         boolean authors) {
      super(versionProvider, (id == null ? "AUTH-" + generateDefaultId() : id));
      collectAuthors = authors;
   }

   @Override
   protected boolean process(String ver, VCSCommit entity) {
      String name = null;
      if (collectAuthors) {
         name = entity.getAuthor();
      } else {
         name = entity.getCommiter();
      }
      authors.get(ver).add(name);
      return true;
   }

   @Override
   protected void stopping() { /* Nothing to do here */
   }

   @Override
   protected void starting() {
      // We need to clean up the previous results.
      // Because we are sure that we are locked with
      // running write lock that mean the state will not change,
      // and we are sure that the processor is not running
      // so we can proceed with confidence, but will first need to
      // acquire a lock on results/
      authorsLock.writeLock().lock();
      try {
         // Will reset all counters
         if (authors == null) {
            authors = new TreeMap<String, Set<String>>();
         }
         
         for (String ver : versionProvider.getNames()) {
            Set<String> vAuthors = authors.get(ver);
            if (vAuthors == null) {
               vAuthors = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
               authors.put(ver, vAuthors);
            } else {
               vAuthors.clear();
            }
         }
      } finally {
         // Release the lock here
         authorsLock.writeLock().unlock();
      }
   }

   @Override
   public Map<String, Set<String>> getResult() {

      runningLock.readLock().lock();
      try {
         if (running) {
            throw new IllegalStateException(
                  "can't get the result while running");
         }
         authorsLock.readLock().lock();
         try {
            Map<String, Set<String>> vAuthors = new TreeMap<String, Set<String>>();
            for (String ver : authors.keySet()) {
               vAuthors.put(ver, new TreeSet<String>(authors.get(ver)));
            }
            return vAuthors;
         } finally {
            authorsLock.readLock().unlock();
         }
      } finally {
         runningLock.readLock().unlock();
      }
   }
}
