/**
 * 
 */
package gr.uom.se.vcs.analysis.version;

import gr.uom.se.util.pattern.processor.Processor;
import gr.uom.se.util.pattern.processor.ResultProcessor;
import gr.uom.se.util.validation.ArgsCheck;
import gr.uom.se.vcs.VCSChange;
import gr.uom.se.vcs.VCSCommit;
import gr.uom.se.vcs.VCSFileDiff;
import gr.uom.se.vcs.VCSResource;
import gr.uom.se.vcs.analysis.util.CommitEdits;
import gr.uom.se.vcs.exceptions.VCSRepositoryException;
import gr.uom.se.vcs.walker.ChangeVisitor;
import gr.uom.se.vcs.walker.filter.VCSFilter;
import gr.uom.se.vcs.walker.filter.resource.VCSResourceFilter;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A processor that computes changes between versions.
 * <p>
 * There are two types of changes to produce for each version, a) changes of
 * each commit of a version does, and b) changes between a version v1 and its
 * previous v2. The user can tell to processor which kind of changes wants to
 * calculate, at creation time.
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public class VersionChangeProcessor extends CommitVersionProcessor implements
      ResultProcessor<VCSCommit, Map<String, Set<CommitEdits>>> {

   /**
    * The kind of modification types to collect for.
    * <p>
    */
   private final EnumSet<VCSChange.Type> types;

   /**
    * This lock will be used in case this processor runs in parallel and will
    * lock the changes.
    * <p>
    */
   private ReadWriteLock changesLock = new ReentrantReadWriteLock();
   /**
    * Here will be stored each new change for each commit for a given version.
    * <p>
    */
   private SortedMap<String, Set<CommitEdits>> changes;

   /**
    * This will be used to lock version changes when used in parallel.
    * <p>
    */
   private ReadWriteLock versionChangesLock = new ReentrantReadWriteLock();

   /**
    * Here will be stored changes from current version to previous.
    * <p>
    */
   private SortedMap<String, CommitEdits> versionChanges;

   /**
    * True if we should count changes for each commit within a version.
    * <p>
    */
   private final boolean changesForAll;
   /**
    * True if we should count count changes between two versions (diff their
    * commits)
    * <p>
    */
   private final boolean changesForVersion;

   /**
    * This processor will be used to pass each calculated commit edit.
    * <p>
    * If this processor is not null then no values will be stored in this
    * in changes and version changes structures, but they will be passed
    * to this processor.
    */
   private final Processor<CommitEdits> commitEditsProcessor;

   /**
    * Create an instance based on the given version provider.
    * <p>
    * This processor will collect info for all types of changes.
    * 
    * @param versionProvider
    *           the provider of versions. Must not be null.
    * @param id
    *           the id of this processor. If null a default id will be provided
    */
   public VersionChangeProcessor(VersionProvider versionProvider, String id) {

      this(versionProvider, id, (VCSChange.Type[]) null);
   }

   /**
    * Create an instance based on the given version provider.
    * <p>
    * 
    * @param versionProvider
    *           the provider of versions. Must not be null.
    * @param id
    *           the id of this processor, If null a default id will be provided
    * @param edits
    *           specify the type of changes to collect info for. If null all
    *           types of changes will be collected
    */
   public VersionChangeProcessor(VersionProvider versionProvider, String id,
         VCSChange.Type... edits) {

      this(versionProvider, id, null, null, true, true, edits);
   }

   // Change the default pid for all family of these processors
   static {
      DEFAULT_PID = "VER_CH_PRO";
   }

   /**
    * Create an instance based on the given version provider.
    * <p>
    * You can specify change filters and or resource filters to be used with
    * this processors. If you specify resource filter than this will return
    * changes that modify only those resources this filter allows.
    * <p>
    * 
    * @param versionProvider
    *           the provider of versions. Must not be null.
    * @param id
    *           the id of this processor, If null a default id will be provided
    * @param edits
    *           specify the type of changes to collect info for. If null all
    *           types of changes will be collected.
    * @param changeFilter
    *           to filter only some specific changes. Null is allowed.
    * @param resourceFilter
    *           to filter changes only to resources this filter allows. Null is
    *           allowed.
    * @param intermediateCommits
    *           true if the changes for each commit of a version should be
    *           computed.
    * @param versionCommits
    *           true if the changes between two subsequent versions should be
    *           computed.
    */
   public VersionChangeProcessor(VersionProvider versionProvider, String id,
         VCSFilter<VCSFileDiff<?>> changeFilter,
         VCSResourceFilter<VCSResource> resourceFilter,
         boolean intermediateCommits, boolean versionCommits,
         VCSChange.Type... edits) {

      this(versionProvider, id, null, changeFilter, resourceFilter, intermediateCommits, versionCommits, edits);
   }
   
   /**
    * Create an instance based on the given version provider.
    * <p>
    * You can specify change filters and or resource filters to be used with
    * this processors. If you specify resource filter than this will return
    * changes that modify only those resources this filter allows.
    * <p>
    * 
    * @param versionProvider
    *           the provider of versions. Must not be null.
    * @param id
    *           the id of this processor, If null a default id will be provided
    *           @param commitEditsProcessor if this processor is not null then
    *           all calculated commit edits will be passed to this processor,
    *           and results will not be saved at this object.
    * @param edits
    *           specify the type of changes to collect info for. If null all
    *           types of changes will be collected.
    * @param changeFilter
    *           to filter only some specific changes. Null is allowed.
    * @param resourceFilter
    *           to filter changes only to resources this filter allows. Null is
    *           allowed.
    * @param intermediateCommits
    *           true if the changes for each commit of a version should be
    *           computed.
    * @param versionCommits
    *           true if the changes between two subsequent versions should be
    *           computed.
    */
   public VersionChangeProcessor(VersionProvider versionProvider, 
         String id,
         Processor<CommitEdits> commitEditsProcessor,
         VCSFilter<VCSFileDiff<?>> changeFilter,
         VCSResourceFilter<VCSResource> resourceFilter,
         boolean intermediateCommits, boolean versionCommits,
         VCSChange.Type... edits) {

      super(versionProvider, id);
      if (edits == null) {
         edits = VCSChange.Type.values();
      }
      if (edits.length == 0) {
         throw new IllegalArgumentException(
               "you must specify at least a type, or pass null to include all types");
      }
      ArgsCheck.isTrue("intermediateCommits || versionCommits",
            intermediateCommits || versionCommits);

      types = EnumSet.copyOf(Arrays.asList(edits));
      this.changeFilter = changeFilter;
      this.resourceFilter = resourceFilter;
      this.changesForAll = intermediateCommits;
      this.changesForVersion = versionCommits;
      this.commitEditsProcessor = commitEditsProcessor;
   }

   @Override
   protected boolean process(String ver, VCSCommit entity) {

      try {
         // If we are required to compute changes per version than do this
         // Allow to proceed to other changes, because a version commit is
         // considered a commit of the version it points (the last one)
         if (changesForVersion && versionProvider.isVersion(entity)) {
            processCommitVersion(ver, entity);
         }

         // We skip the merge commits because any change
         // in the child commits of this merge is incorporated
         // to the merge commit itself. However this will not work
         // If we have a change filter that collects changes only
         // for merge commits.
         // WARNING: this will produce invalid results if a merge commit
         // of version v1 is produced as a result of a commit of v2. However
         // this is probably a best strategy because we assume that all versions
         // are at master branch or at least are accessible one from each other.
         // Since we are calculating efforts and other quality changes in
         // a version, we can assume that a merged commit is not an individual
         // effort, on the other hand the parents of this commit are changes
         // that
         // are probably visible by this version
         if (changesForAll && !entity.isMergeCommit()) {

            Collection<VCSCommit> parents = entity.getPrevious();
            if (!parents.isEmpty()) {
               // Here the parents will have exact one commit, because
               // this is not a merge commit
               CommitEdits ce = this.walkChanges(parents.iterator().next(),
                     entity);
               // At this point the walking is finished so we
               // can add to the changes
               if(commitEditsProcessor != null) {
                  return commitEditsProcessor.process(ce);
               }
               changes.get(ver).add(ce);
            }
         }

      } catch (VCSRepositoryException e) {
         throw new IllegalStateException(e);
      }

      return true;
   }

   /**
    * This a special case of processing, where we want only to get the changes
    * between two releases (versions). So the given commit must be the commit
    * that corresponds to the given version.
    * <p>
    * The changes will be between the current version commit and the previous
    * version commit.
    * 
    * @param ver
    *           the version tag
    * @param commit
    *           of this version
    * @return true if the processing should continue
    */
   private boolean processCommitVersion(String ver, VCSCommit commit) {

      try {

         VCSCommit previousCommit = versionProvider.getPrevious(commit);
         // Now prepare to take the changes
         // Usually the first version has not a previous one
         if (previousCommit != null) {
            CommitEdits ce = this.walkChanges(previousCommit, commit);
            // At this point the walking is finished so we
            // can add to the changes
            if(commitEditsProcessor != null) {
               return commitEditsProcessor.process(ce);
            }
            versionChanges.put(ver, ce);
         }

         return true;
      } catch (VCSRepositoryException e) {
         throw new IllegalStateException(e);
      }
   }

   private CommitEdits walkChanges(VCSCommit oldC, VCSCommit newC)
         throws VCSRepositoryException {
      DiffVisitor visitor = new DiffVisitor(oldC, newC, changeFilter,
            resourceFilter);
      // Wait until the changes are walked
      oldC.walkFileChanges(newC, visitor);
      return visitor.ce;
   }

   @Override
   protected void stopping() {
      if(commitEditsProcessor != null) {
         try {
            commitEditsProcessor.stop();
         } catch (InterruptedException e) {
            throw new IllegalStateException(e);
         }
      }
   }

   /**
    * {@inheritDoc}
    * <p>
    * While this processor is not running you can get the results by calling
    * {@link #getResult()}, however when the processor is started the results
    * will be overwritten and you can not get the results while running.
    */
   @Override
   protected void starting() {

      // Lock down the changes so we can easily
      // write the changes
      changesLock.writeLock().lock();
      try {
         // This method is called once before any processing is
         // done, so we can easily create a new changes
         // structure. However we choose to clear all the values
         // for each version so we do not create a lot of objects
         // again and again on each run (if the processor is rerun)
         if (changes == null) {
            // If the changes is null that means this processor
            // is first run so we should init the changes
            changes = new TreeMap<String, Set<CommitEdits>>();
            for (String ver : versionProvider.getVersionNames()) {
               changes
                     .put(ver,
                           Collections
                                 .newSetFromMap(new ConcurrentHashMap<CommitEdits, Boolean>()));
            }
         } else {
            // The changes is not null so this is a rerun, and we
            // just need to clear old values
            for (String ver : versionProvider.getVersionNames()) {
               changes.get(ver).clear();
            }
         }
         if(commitEditsProcessor != null) {
            commitEditsProcessor.start();
         }
      } finally {
         changesLock.writeLock().unlock();
      }
      // We can release the changes lock before because this method will be
      // called
      // only if the processor is stopped. And while stopped only a caller that
      // wants
      // to read results will have access to these structures
      // so we can allow the caller to get the result asap.
      versionChangesLock.writeLock().lock();
      try {
         if (versionChanges == null) {
            // If the changes is null that means this processor
            // is first run so we should init the changes
            versionChanges = new TreeMap<String, CommitEdits>();

         } else {
            // The changes is not null so this is a rerun, and we
            // just need to clear old values
            versionChanges.clear();
         }
      } finally {
         versionChangesLock.writeLock().unlock();
      }
   }

   /**
    * {@inheritDoc}
    * <p>
    * At this point return edits for each commit within a version.
    */
   @Override
   public Map<String, Set<CommitEdits>> getResult() {

      // Here we should lock the running state so no change on the state of the
      // processor will be made. However we can return result only when this
      // processor is stopped.
      runningLock.readLock().lock();
      try {
         // Do not allow this method to be called if the processor is running
         if (running) {
            throw new IllegalStateException(
                  "can not return a result while running");
         }

         // Lock the changes until we read them and return to the caller
         changesLock.readLock().lock();
         try {
            if (changes == null) {
               return Collections.emptyMap();
            }
            return Collections.unmodifiableSortedMap(changes);
         } finally {
            changesLock.readLock().unlock();
         }
      } finally {
         runningLock.readLock().unlock();
      }
   }

   /**
    * @return the changes from one version to its previous one. That is, the
    *         changes produced when diffing the commit of a version with the
    *         commit of the previous version. This shows what are the real
    *         differences between two versions, avoiding the changes that were
    *         made in the middle (from time the previous version started until
    *         the new one).
    */
   public Map<String, CommitEdits> getVersionsChanges() {
      // Here we should lock the running state so no change on the state of the
      // processor will be made. However we can return result only when this
      // processor is stopped.
      runningLock.readLock().lock();
      try {

         // Do not allow this method to be called if he processor is running
         if (running) {
            throw new IllegalStateException(
                  "can not return a result while running");
         }

         versionChangesLock.readLock().lock();
         try {
            if (versionChanges == null) {
               return Collections.emptyMap();
            }
            return Collections.unmodifiableSortedMap(versionChanges);
         } finally {
            versionChangesLock.readLock().unlock();
         }

      } finally {
         runningLock.readLock().unlock();
      }
   }

   /**
    * The change filter to be used within the visitor.
    * <p>
    */
   private VCSFilter<VCSFileDiff<?>> changeFilter;
   /**
    * The resource filter to be used within visitor.
    * <p>
    */
   private VCSResourceFilter<VCSResource> resourceFilter;

   /**
    * The visitor to be used to compute edits.
    * <p>
    * 
    * @author Elvis Ligu
    * @version 0.0.1
    * @since 0.0.1
    */
   private class DiffVisitor implements ChangeVisitor<VCSFileDiff<?>> {

      /**
       * Change filter.
       */
      private VCSFilter<VCSFileDiff<?>> changeFilter;
      /**
       * Resource filter.
       */
      private VCSResourceFilter<VCSResource> resourceFilter;
      /**
       * Where the edits for each commit will be stored.
       */
      private CommitEdits ce;

      /**
       * Creates a visitor based on the given newC and filters.
       * <p>
       * 
       * @param newC
       * 
       * @param newC
       *           to store the edits for
       * @param changeFilter
       *           to filter the changes
       * @param resourceFilter
       *           to filter the changes based on resources
       */
      public DiffVisitor(VCSCommit oldC, VCSCommit newC,
            VCSFilter<VCSFileDiff<?>> changeFilter,
            VCSResourceFilter<VCSResource> resourceFilter) {

         this.ce = new CommitEdits(oldC, newC, null);
         this.changeFilter = changeFilter;
         this.resourceFilter = resourceFilter;
      }

      @SuppressWarnings("unchecked")
      @Override
      public <F extends VCSFilter<VCSFileDiff<?>>> F getFilter() {
         return (F) changeFilter;
      }

      @Override
      public boolean visit(VCSFileDiff<?> entity) {

         // We should visit only entities that have a change type
         // that we are looking for
         if (types.contains(entity.getType())) {
            // Simply put the edits to the visitor
            ce.add(entity);
         }
         // Always return true
         return true;
      }

      @SuppressWarnings("unchecked")
      @Override
      public <R extends VCSResource> VCSResourceFilter<R> getResourceFilter() {
         return (VCSResourceFilter<R>) resourceFilter;
      }
   }
}
