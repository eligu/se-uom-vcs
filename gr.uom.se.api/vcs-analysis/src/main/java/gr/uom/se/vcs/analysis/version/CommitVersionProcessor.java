package gr.uom.se.vcs.analysis.version;

import gr.uom.se.util.pattern.processor.Processor;
import gr.uom.se.util.validation.ArgsCheck;
import gr.uom.se.vcs.VCSCommit;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A processors that collects the commits based on their version.
 * <p>
 * 
 * Given a set of commits and their tag names, this processor will collect the
 * commits it gets (passed at {@link #process(VCSCommit)} method) and categorize
 * them by commit date. That is, each new commit arrived for processing if its
 * commit date is between one of the given commits (at constructor) and before
 * the next given commit (at constructor) it will collect it at a list that is
 * corresponding to the second given commit. For example, suppose we have the
 * following versions:
 * 
 * <pre>
 * 1.0.0 - commit fcb34 (1st of May 2003)
 * 1.0.1 - commit add1s (5 of December 2003)
 * 1.1.0 - commit 34f4g (19 of April 2004)
 * </pre>
 * 
 * The following commits that are processed will belong to:
 * 
 * <pre>
 * commit 12ffd (20 of April 2003)  - 1.0.0
 * commit fcb34 (1st of May 2003)   - 1.0.0 (this is the tag of version 1.0.0)
 * commit 9ka80 (30 of June 2003)   - 1.0.1
 * commit 233k1 (6 of December 2003)- 1.1.0
 * </pre>
 * 
 * Generally speaking this processor can be used when we want to count the
 * number of commits per each version. That is, if we are sure that each tag
 * corresponds to a version, we can query all versionProvider of the repository
 * and get their respective commits (note that a tag is a label for a given
 * commit) and create a map with keys the commits and with versionProvider
 * (String) the names of the tag. Then we can create an instance of this
 * processor passing the "commit tag map" to the constructor. Finally we can
 * walk all commits of the repository and pass each of them to the instance of
 * this processor.
 * <p>
 * Although, the recommended usage of this processor is to collect all commits
 * that are made prior to each software release, if the repository doesn't
 * contain info on releases, let say versionProvider aren't showing version
 * numbers, we can specify a release by each 100th commit. That is, suppose we
 * have 1000 commits in a repository and we want to collect the commits between
 * prior too 100th commit, between 100th and 200th, between 200th and 300th and
 * so on, we can pass to this constructor a map of keys-values 100th - "c100id",
 * 200th - "c200id", "c300id" (etc).
 * <p>
 * Since this processor is version agnostic, which means it consider a commit as
 * a version, a general approach in using this processor would be to collect all
 * commits that are made between two specified versions (commits). That is given
 * a number of commits (versions) we can use this processor to place each
 * processing commit between two others (given in constructor).
 * <p>
 * <b>Note:</b>this processor is branch agnostic, and it depends on the walker
 * that feeds commits to this processor (at {@link #process(VCSCommit)} method)
 * which commits are going to categorize. So if you want to collect all commits
 * for each version, ignoring from which branch they come, you can walk all
 * heads of each branch at once, starting this processor ({@link #start()})
 * before any walk is performed and stopping it ({@link #stop()}) after the last
 * branch walk.
 * <p>
 * This processor is thread safe. Based on this you can construct multiply
 * processors by a little effort. Every time that a new commit is coming this
 * will categorize it and pass it along with the version it belongs to
 * {@link #process(String, VCSCommit)} method. Each time the processor is
 * started the method {@link #starting()} will be called and for stopping
 * {@link #stopping()}. These methods are synchronized, by the running state of
 * this processor, that means while running any of this methods, the running
 * state is ensured that will not change.
 * <p>
 * Keep in mind that this processor doesn't check if you pass for processing the
 * same commit, so if you construct counter, based on this processor, you should
 * keep a set of all passed commits so you can check if you visited it before.
 * However while walking commits, usually the walker doesn't return the same
 * commit twice, but that depends on implementation.
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @param <T>
 * @since 0.0.1
 */
public abstract class CommitVersionProcessor implements Processor<VCSCommit> {

   /**
    * A flag that is true when this processor is running.
    * <p>
    * A processor is running if the start() method is called and no stop()
    * method.
    */
   protected Boolean running = false;
   protected ReadWriteLock runningLock = new ReentrantReadWriteLock();

   /**
    * The number of instances that are created until now.
    * <p>
    * Used for id.
    */
   private static AtomicInteger instances = new AtomicInteger(0);

   /**
    * The version provider used by this processor.
    * <p>
    */
   protected final VersionProvider versionProvider;

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean process(VCSCommit entity) {

      // The running here is thread safe because it is only
      // changed at stop or start methods, and they are synchronized
      // so no access to any of these methods will be performed
      // while this method is running
      runningLock.readLock().lock();
      try {
         if (!running) {
            throw new IllegalStateException(
                  "processor can not process any entity without first being started");
         }

         // We try to get a version name for the given commit
         // if there is not a name that means this commit
         // is not in a version
         String ver = versionProvider.findVersion(entity);

         // If this commit is within a known version then process it
         if (ver != null) {
            return process(ver, entity);
         }
      } finally {
         runningLock.readLock().unlock();
      }
      return true;
   }

   /**
    * Process the given commit that belongs to the given entity.
    * <p>
    * Subclasses that perform version based processing should implement this
    * method in order to ge each commit of a version.
    * 
    * @param ver
    *           the version of the given commit
    * @param entity
    *           the commit to process
    * @return true if we should keep processing more entities
    */
   protected abstract boolean process(String ver, VCSCommit entity);

   /**
    * {@inheritDoc}
    */
   @Override
   public void stop() throws InterruptedException {
      // There is nothing to do here but to set the value of
      // running to false;
      runningLock.writeLock().lock();
      try {
         if (!running) {
            return;
         }
         stopping();
         running = false;
      } finally {
         runningLock.writeLock().unlock();
      }
   }

   protected abstract void stopping();

   /**
    * {@inheritDoc}
    */
   @Override
   public void start() {
      // If this processor is already running, that is this method is called
      // but stop() is not, do nothing.
      runningLock.writeLock().lock();
      try {
         if (running) {
            return;
         }
         // Initialize the results, by discarding all the results collected so
         // far
         starting();
         running = true;
      } finally {
         runningLock.writeLock().unlock();
      }
   }

   protected abstract void starting();

   /**
    * The default processor id.
    * <p>
    */
   protected static String DEFAULT_PID = "CVP";

   /**
    * @return a default id for this type of processor
    */
   protected static String generateDefaultId() {
      return DEFAULT_PID + instances.incrementAndGet();
   }

   /**
    * The id of this processor.
    * <p>
    */
   protected final String id;

   public CommitVersionProcessor(VersionProvider versionProvider, String id) {

      ArgsCheck.notNull("versionProvider", versionProvider);

      this.versionProvider = versionProvider;
      if (id == null) {
         id = generateDefaultId();
      }
      this.id = id;
   }

   @Override
   public String getId() {
      return id;
   }

   @Override
   public boolean isStarted() {
      try {
         runningLock.readLock().lock();
         return running;
      } finally {
         runningLock.readLock().unlock();
      }
   }
}