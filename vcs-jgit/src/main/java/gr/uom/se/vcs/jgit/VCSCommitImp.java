/**
 * 
 */
package gr.uom.se.vcs.jgit;

import gr.uom.se.vcs.VCSChange;
import gr.uom.se.vcs.VCSCommit;
import gr.uom.se.vcs.VCSDirectory;
import gr.uom.se.vcs.VCSFileDiff;
import gr.uom.se.vcs.VCSResource;
import gr.uom.se.vcs.exceptions.VCSRepositoryException;
import gr.uom.se.vcs.exceptions.VCSResourceNotFoundException;
import gr.uom.se.vcs.jgit.utils.RevUtils;
import gr.uom.se.vcs.jgit.utils.TreeUtils;
import gr.uom.se.vcs.jgit.walker.DiffCollector;
import gr.uom.se.vcs.jgit.walker.filter.commit.CommitFilter;
import gr.uom.se.vcs.jgit.walker.filter.commit.OptimizedCommitFilter;
import gr.uom.se.vcs.jgit.walker.filter.resource.OptimizedResourceFilter;
import gr.uom.se.vcs.jgit.walker.filter.resource.ResourceFilter;
import gr.uom.se.vcs.walker.ChangeVisitor;
import gr.uom.se.vcs.walker.CommitVisitor;
import gr.uom.se.vcs.walker.ResourceVisitor;
import gr.uom.se.vcs.walker.filter.VCSFilter;
import gr.uom.se.vcs.walker.filter.commit.VCSCommitFilter;
import gr.uom.se.vcs.walker.filter.resource.VCSResourceFilter;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand.ListMode;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevSort;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilterGroup;


/**
 * Implementation of {@link VCSCommit} based on JGit library.
 * <p>
 * 
 * This is an immutable object and is considered thread safe.
 * 
 * @author Elvis
 * @since 0.0.1
 * @version 0.0.1
 * @see VCSCommit
 */
public class VCSCommitImp implements VCSCommit {

   
   /**
    * JGit commit that is linked to this commit.
    * <p>
    */
   protected RevCommit commit;

   /**
    * A class to hold a person ident in case we need synchronization on cached
    * fields (author and committer).
    */
   private class CachedObjectHolder<T> {
      protected T cache;
   }

   /**
    * The author that originated the commit.
    * <p>
    * 
    * This field is for caching purposes
    */
   protected CachedObjectHolder<PersonIdent> author = new CachedObjectHolder<PersonIdent>();

   /**
    * The committer that committed this revision.
    * <p>
    * This filed is for caching purposes.
    */
   protected CachedObjectHolder<PersonIdent> committer = new CachedObjectHolder<PersonIdent>();

   /**
    * JGit repository from where this commit comes from.
    * <p>
    */
   protected Repository repo;

   /**
    * Keep cached the next commits of this one.
    * <p>
    * 
    * In order to find next commits (children) we have to parse all heads that
    * this commit is merged into, and walk all of them until we reach this
    * commit. So it is a better idea to keep the children cached. If children
    * collection is null that means they are not calculated, if it is not, they
    * are calculated so don't calculate them.
    */
   protected CachedObjectHolder<List<VCSCommit>> children = new CachedObjectHolder<List<VCSCommit>>();

   /**
    * Creates a commit that is linked to a JGit commit and repository.
    * <p>
    * 
    * @param commit
    *           must not be null
    * @param repository
    *           must not be null
    */
   public VCSCommitImp(final RevCommit commit, final Repository repository) {

      ArgsCheck.notNull("commit", commit);
      ArgsCheck.notNull("repository", repository);

      // We need to parse again this commit so we can ensure all his parents
      // and children will be ok
      RevWalk walk = null;
      try {
         walk = new RevWalk(repository);
         this.commit = walk.parseCommit(commit);
      } catch (final IOException e) {
         throw new IllegalStateException("revision " + commit.getName()
               + " can not be parsed");
      } finally {
         if (walk != null) {
            walk.release();
         }
      }
      this.repo = repository;
   }

   /**
    * {@inheritDoc}
    * <p>
    * {@link RevCommit#getName()} is used as the id of this resource.
    * 
    * @return SHA-1 of this commit id as specified by Git
    */
   @Override
   public String getID() {
      return this.commit.getName();
   }

   /**
    * {@inheritDoc}
    * <p>
    * If the field {@link #author} is null it will be pulled from
    * {@link #commit} and will be cached to this field.
    */
   @Override
   public String getAuthor() {
      return this.getAuthorIdent().getEmailAddress();
   }

   /**
    * Synchronized method to init properly the cached field {@link #author}.
    * <p>
    * 
    * @return author ident
    */
   private PersonIdent getAuthorIdent() {
      // getAuthorIdent() is a very resource expensive method
      // thus we keep author cached so next time it will be needed
      // it will remain within this commit
      synchronized (author) {
         if (this.author.cache == null) {
            this.author.cache = this.commit.getAuthorIdent();
         }
      }
      return this.author.cache;
   }

   /**
    * {@inheritDoc}
    * <p>
    * This method is fairly expensive, and you should cache a copy of this
    * message if you need multiply accesses at this message.
    */
   @Override
   public String getMessage() {
      return this.commit.getFullMessage();
   }

   /**
    * {@inheritDoc}
    * <p>
    * If the field {@link #committer} is null it will be pulled from
    * {@link #commit} and will be cached to this field.
    */
   @Override
   public String getCommiter() {
      return this.getCommiterIdent().getEmailAddress();
   }

   /**
    * Synchronized method to init properly the cached field {@link #committer}.
    * <p>
    * 
    * @return author ident
    */
   private PersonIdent getCommiterIdent() {
      // getCommitterIdent() is a very resource expensive method
      // thus we keep author cached so next time it will be needed
      // it will remain within this commit
      synchronized (committer) {
         if (this.committer.cache == null) {
            this.committer.cache = this.commit.getCommitterIdent();
         }
      }
      return this.committer.cache;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Date getCommitDate() {
      return this.getCommiterIdent().getWhen();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Date getAuthorDate() {
      return this.getAuthorIdent().getWhen();
   }

   /**
    * Given a JGit DiffEntry and the corresponding commits it was produced, it
    * will create a VCSChange.
    * <p>
    * 
    * This method will return null if entry doesn't correspond to a normal file
    * or a directory.
    * 
    * @param entry
    *           the diff
    * @param oldC
    *           the old commit
    * @param newC
    *           the new commit
    * @return a {@link VCSChange} if entry refers to a directory or a
    *         normal/executable file, null if not
    * @see DiffEntry#ChangeType
    * @see #createDirChange(DiffEntry, VCSCommitImp, VCSCommitImp)
    * @see #createFileChange(DiffEntry, VCSCommitImp, VCSCommitImp)
    */
   static VCSChange<?> createChange(final DiffEntry entry,
         final VCSCommitImp oldC, final VCSCommitImp newC) {

      VCSChange<?> change = null;

      // case for directory
      if (isDirDiff(entry)) {
         change = createDirChange(entry, oldC, newC);
         // case for file
      } else if (isFileDiff(entry)) {
         change = createFileChange(entry, oldC, newC);
      }

      return change;
   }

   /**
    * If entry's mode is {@link FileMode#TREE} then this will return true.
    * <p>
    * 
    * @param entry
    *           to check if it is a directory
    * @return true if entry is referred to a directory
    */
   static boolean isDirDiff(final DiffEntry entry) {

      final ChangeType type = entry.getChangeType();

      // if this is an add then we have to check only the new file mode
      // if this is a delete then we have to check only the old file mode
      if (type.equals(DiffEntry.ChangeType.ADD)) {

         return RevUtils.isDirMode(entry.getNewMode());

      } else if (type.equals(DiffEntry.ChangeType.DELETE)) {

         return RevUtils.isDirMode(entry.getOldMode());
      }

      // if there is modified/renamed/copied we must check both old and new
      // modes
      return RevUtils.isDirMode(entry.getNewMode())
            || RevUtils.isDirMode(entry.getOldMode());
   }

   /**
    * If entry's mode is {@link FileMode#EXECUTABLE_FILE} or
    * {@link FileMode#REGULAR_FILE} then this will return true.
    * <p>
    * 
    * @param entry
    *           to check if it is a file
    * @return true if entry is referred to a file
    */
   static boolean isFileDiff(final DiffEntry entry) {

      final ChangeType type = entry.getChangeType();

      // if this is an add then we have to check only the new file mode
      // if this is a delete then we have to check only the old file mode
      if (type.equals(DiffEntry.ChangeType.ADD)) {

         return RevUtils.isFileMode(entry.getNewMode());

      } else if (type.equals(DiffEntry.ChangeType.DELETE)) {

         return RevUtils.isFileMode(entry.getOldMode());
      }

      // if there is modified/renamed/copied we must check both old and new
      // modes
      return RevUtils.isFileMode(entry.getNewMode())
            || RevUtils.isFileMode(entry.getOldMode());
   }

   /**
    * Given a {@link DiffEntry} and the commits this diff was produced for,
    * return a {@link VCSFileDiffImp}.
    * <p>
    * 
    * <b>WARNING:</b> this method may fail if
    * {@link RevUtils#isFileMode(FileMode)} returns false.
    * 
    * @param entry
    *           the diff entry
    * @param oldC
    *           old commit
    * @param newC
    *           new commit
    * @return the corresponding {@link VCSFileDiff} of this entry
    */
   static VCSFileDiffImp<VCSFileImp> createFileChange(final DiffEntry entry,
         final VCSCommitImp oldC, final VCSCommitImp newC) {

      VCSResource oldR = null;
      VCSResource newR = null;

      final VCSChange.Type chType = RevUtils.changeType(entry.getChangeType());

      // if added then there is no need for the old resource
      // so create the old resource only if this is not an addition
      if (!chType.isAdd()) {
         oldR = new VCSFileImp(oldC, entry.getOldPath());
      }

      // if removed then there is no need for the new resource
      // so creates the new resource only if this is not a deletion
      if (!chType.isDelete()) {
         newR = new VCSFileImp(newC, entry.getNewPath());
      }

      return new VCSFileDiffImp<VCSFileImp>((VCSFileImp) newR,
            (VCSFileImp) oldR, chType);
   }

   /**
    * Given a {@link DiffEntry} and the commits this diff was produced for,
    * return a {@link VCSDirectoryImp}.
    * <p>
    * 
    * <b>WARNING:</b> this method may fail if
    * {@link RevUtils#isDirMode(FileMode)} returns false.
    * 
    * @param entry
    *           the dif entry
    * @param oldC
    *           old commit
    * @param newC
    *           new commit
    * @return the corresponding {@link VCSDirectory} of this entry
    */
   static VCSChange<?> createDirChange(final DiffEntry entry,
         final VCSCommitImp oldC, final VCSCommitImp newC) {

      VCSResource oldR = null;
      VCSResource newR = null;

      final VCSChange.Type chType = RevUtils.changeType(entry.getChangeType());

      // if added then there is no need for the old resource
      if (!chType.isAdd()) {
         oldR = new VCSDirectoryImp(oldC, entry.getOldPath());
      }

      // if removed then there is no need for the new resource
      if (!chType.isDelete()) {
         newR = new VCSDirectoryImp(newC, entry.getNewPath());
      }

      return new VCSChangeImp<VCSResourceImp>((VCSResourceImp) newR,
            (VCSResourceImp) oldR, chType);
   }

   /**
    * Check the two commits if they are from VCSCommitImp so we can have access
    * to their {@link RevCommit} object.
    * <p>
    * 
    * If not throw an {@link IllegalArgumentException}. If two commits are equal
    * return null, otherwise return the new commit at index 0 and the old one at
    * 1;
    * 
    * @param commit1
    *           the first commit
    * @param commit2
    *           the second commit
    * @return an array that contains the new commit at index 0, and the old one
    *         at index 1
    */
   private static VCSCommitImp[] checkCommitsForDiffAndReturnNewOld(
         final VCSCommit commit1, final VCSCommit commit2) {

      ArgsCheck.isSubtype("commit1", VCSCommitImp.class, commit1);
      ArgsCheck.isSubtype("commit2", VCSCommitImp.class, commit2);

      // If commits are equals return null
      if (AnyObjectId.equals(((VCSCommitImp) commit1).commit,
            ((VCSCommitImp) commit2).commit)) {
         return null;
      }

      // This array will have the new commit in position 0 and the old one in
      // position 1
      final VCSCommitImp[] commits = new VCSCommitImp[2];
      final short OLD = 1;
      final short NEW = 0;

      // Start by assuming commit1 is the new and commit2 the old
      commits[NEW] = (VCSCommitImp) commit1;
      commits[OLD] = (VCSCommitImp) commit2;

      // If commits[0] (at position new) is older than commits[1] (at position
      // old) swap the values
      if (commits[OLD].commit.getCommitTime() > commits[NEW].commit
            .getCommitTime()) {

         final VCSCommitImp temp = commits[NEW]; // the old one
         commits[NEW] = commits[OLD]; // assign the old to new
         commits[OLD] = temp;
      }

      return commits;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void walkChanges(final VCSCommit commit,
         final ChangeVisitor<VCSChange<?>> visitor)
         throws VCSRepositoryException {

      // The new commit is at position 0 and the old one at position 1
      // if commits is null that means the old and the new are equal so no
      // need to walk changes
      final VCSCommitImp[] commits = checkCommitsForDiffAndReturnNewOld(commit,
            this);
      if (commits == null) {
         return;
      }

      final short OLD = 1;
      final short NEW = 0;

      VCSResourceFilter<VCSResource> resourceFilter = visitor
            .getResourceFilter();

      // Use Diff collector to collect diffs
      // Limit the diff only to the specified paths if any
      final DiffCollector<DiffEntry> diffs = new DiffCollector<DiffEntry>(
            this.repo, commits[OLD].commit, commits[NEW].commit);

      if (resourceFilter != null) {
         OptimizedResourceFilter<VCSResource> of = ResourceFilter.parse(
               resourceFilter, null);
         if (of != null) {
            diffs.setPathFilters(of.getCurrent());
            resourceFilter = null;
         }
      }

      // Run diff entries until there are no more entries and visitor accept
      // each current entry
      // Create a VCSChange for each diff entry, if entry refers to TREE (aka
      // directory)
      // or REGULAR_FILE/EXECUTABLE_FILE, skip all others such as symlink and
      // other repository

      // Run this code if filters are null (it is applied directly to tree
      // walker)
      VCSFilter<VCSChange<?>> changeFilter = visitor.getFilter();

      if (resourceFilter == null && changeFilter == null) {
         for (final DiffEntry entry : diffs) {

            final VCSChange<?> change = createChange(entry, commits[OLD],
                  commits[NEW]);

            if (change != null) {
               // if the visitor returns false then we must stop the
               // iteration
               if (!visitor.visit(change)) {
                  break;
               }
            }
         }
         // This code will run if filter is not null. That means
         // the provided filter could not be parsed so we should
         // try to filter results manually
      } else {
         for (final DiffEntry entry : diffs) {

            final VCSChange<?> change = createChange(entry, commits[OLD],
                  commits[NEW]);

            if (change != null) {

               boolean include = true;
               // We must check first the resource filter, if it allows the
               // current
               // change's resource
               if (resourceFilter != null) {
                  if (change.getType().isAdd()) {
                     include = resourceFilter.include(change.getNewResource());
                  } else if (change.getType().isDelete()) {
                     include = resourceFilter.include(change.getOldResource());
                  } else {
                     // WARNING: in case we have a RENAME, or COPY probably the
                     // filter may allow the old but not new (or otherwise),
                     // however this code include the resource if the new or old
                     // are allowed, that means that possibly the visitor could
                     // get 'wrong' results. Actually we allow this because if
                     // a filter allows the old but not the new, that means that
                     // the visitor may need info for the old resource (even
                     // in cases it is coppied/renamed.
                     include = resourceFilter.include(change.getNewResource())
                           || resourceFilter.include(change.getOldResource());
                  }
               }

               if (include && (changeFilter != null)) {
                  include = changeFilter.include(change);
               }

               // if the visitor returns false then we must stop the
               // iteration
               if (include) {
                  if (!visitor.visit(change)) {
                     break;
                  }
               }
            }
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public List<VCSChange<?>> getChanges(final VCSCommit commit)
         throws VCSRepositoryException {
      return this.getChanges(commit, true, (String[]) null);
   }

   /**
    * {@inheritDoc}
    * <p>
    * <b>WARNING:</b> this method will never consider <code>recursive</code>
    * argument because Git doesn't keep track of directories. So it will
    * probably not return any diff for a directory.
    */
   @Override
   public List<VCSChange<?>> getChanges(final VCSCommit commit,
         final boolean recursive, final String... paths)
         throws VCSRepositoryException {

      ArgsCheck.notNull("commit", commit);

      /*
       * We will use walkChanges to collect all changes. This will add an
       * additional computation for each change (visit() will be use for each
       * change) however this is preferable because we don't want to repeat code
       * here
       */

      // The list where we collect all changes
      final List<VCSChange<?>> changes = new ArrayList<VCSChange<?>>();

      // The visitor to use to collect changes
      final ChangeVisitor<VCSChange<?>> visitor = new ChangeVisitor<VCSChange<?>>() {

         /**
          * Get each change and add it to changes list
          * 
          * @param entity
          * @return
          */
         @Override
         public boolean visit(final VCSChange<?> entity) {
            changes.add(entity);
            return true;
         }

         @Override
         public <F extends VCSFilter<VCSChange<?>>> F getFilter() {
            return null;
         }

         @Override
         public <R extends VCSResource> VCSResourceFilter<R> getResourceFilter() {
            if (paths == null) {
               return null;
            }
            ArgsCheck.containsNoNull("paths", (Object[]) paths);
            return new OptimizedResourceFilter<R>(
                  PathFilterGroup.createFromStrings(paths));
         }
      };

      // Run walkChanges to collect changes
      this.walkChanges(commit, visitor);

      return changes;
   }

   /**
    * {@inheritDoc}
    * 
    * <p>
    * WARNING: this method will never consider <code>recursive</code> argument
    * because Git doesn't keep track of directories. So it will probably not
    * return any diff for a directory.
    */
   @Override
   public void walkFileChanges(final VCSCommit commit,
         final ChangeVisitor<VCSFileDiff<?>> visitor)
         throws VCSRepositoryException {

      ArgsCheck.notNull("commit", commit);
      ArgsCheck.notNull("visitor", commit);
      /*
       * We will use walkChanges to collect all changes. This will add an
       * additional computation for each change (visit() will be use for each
       * change and check if each change is a file change) however this is
       * preferable because we don't want to repeat code here
       */

      // Create a new wrapper filter
      // If we have a change filter then we need to create a
      // new wrap filter because the provided filter by visitor is
      // of type file diff change.
      VCSFilter<VCSChange<?>> filter = null;
      if (visitor.getFilter() != null) {
         final VCSFilter<VCSFileDiff<?>> f = visitor.getFilter();
         filter = new VCSFilter<VCSChange<?>>() {

            @Override
            public boolean include(VCSChange<?> entity) {
               return VCSFileDiff.class.isAssignableFrom(entity.getClass())
                     && f.include((VCSFileDiff<?>) entity);

            }
         };
      }
      final VCSFilter<VCSChange<?>> wrapFilter = filter;

      // The visitor to use to collect changes
      final ChangeVisitor<VCSChange<?>> walkVisitor = new ChangeVisitor<VCSChange<?>>() {

         /**
          * Get each change and add it to changes list
          * 
          * @param entity
          * @return
          */
         @Override
         public boolean visit(final VCSChange<?> entity) {
            if (VCSFileDiffImp.class.isAssignableFrom(entity.getClass())) {
               return visitor.visit((VCSFileDiff<?>) entity);
            }
            return true;
         }

         @SuppressWarnings("unchecked")
         @Override
         public VCSFilter<VCSChange<?>> getFilter() {
            return wrapFilter;
         }

         @Override
         public <R extends VCSResource> VCSResourceFilter<R> getResourceFilter() {
            return visitor.getResourceFilter();
         }
      };

      // Run walkChanges to collect changes
      this.walkChanges(commit, walkVisitor);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public List<VCSFileDiff<?>> getFileChanges(final VCSCommit commit)
         throws VCSRepositoryException {

      return this.getFileChanges(commit, true, (String[]) null);
   }

   /**
    * {@inheritDoc}
    * <p>
    * <b>WARNING:</b>: this method will never consider <code>recursive</code>
    * argument because Git doesn't keep track of directories. However whether
    * this will include file changes under the specified paths (if they are
    * dirs) this is lied at {@link org.eclipse.jgit.api.DiffCommand}
    * implementation of JGit.
    */
   @Override
   public List<VCSFileDiff<?>> getFileChanges(final VCSCommit commit,
         final boolean recursive, final String... paths)
         throws VCSRepositoryException {

      ArgsCheck.notNull("commit", commit);

      /*
       * We will use walkChanges to collect all changes. This will add an
       * additional computation for each change (visit() will be use for each
       * change) however this is preferable because we don't want to repeat code
       * here
       */

      // The list where we collect all changes
      final List<VCSFileDiff<?>> changes = new ArrayList<VCSFileDiff<?>>();

      // The visitor to use to collect changes
      final ChangeVisitor<VCSChange<?>> visitor = new ChangeVisitor<VCSChange<?>>() {

         /**
          * Get each change and add it to changes list
          * 
          * @param entity
          * @return
          */
         @Override
         public boolean visit(final VCSChange<?> entity) {
            if (VCSFileDiffImp.class.isAssignableFrom(entity.getClass())) {
               changes.add((VCSFileDiff<?>) entity);
            }
            return true;
         }

         @Override
         public <F extends VCSFilter<VCSChange<?>>> F getFilter() {
            return null;
         }

         @Override
         public <R extends VCSResource> VCSResourceFilter<R> getResourceFilter() {
            if (paths == null) {
               return null;
            }
            ArgsCheck.containsNoNull("paths", (Object[]) paths);
            return new OptimizedResourceFilter<R>(
                  PathFilterGroup.createFromStrings(paths));
         }
      };

      // Run walkChanges to collect changes
      this.walkChanges(commit, visitor);

      return changes;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean isMergeCommit() {
      return this.commit.getParentCount() > 1;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Collection<VCSCommit> getNext() throws VCSRepositoryException {

      synchronized (this.children) {
         // Check first if children are previously calculated
         // if so return them, otherwise calculate them
         if (this.children.cache != null) {
            return this.children.cache;
         }

         this.children.cache = new ArrayList<VCSCommit>();
         RevWalk revWalk = null;

         try {

            // Check first if this commit is a head that has no children, if
            // true then
            // getMergingHeads will return an empty list
            final List<RevCommit> heads = this.getMergingHeads();
            if (heads.isEmpty()) {
               return new ArrayList<VCSCommit>(this.children.cache);
            }

            // Walker to walk the commits until we find this one
            revWalk = new RevWalk(this.repo);

            // The current commit that we need to know its children
            final RevCommit required = revWalk.parseCommit(this.commit);

            // The walking will start from each head we found
            revWalk.markStart(heads);

            // The sorting here is important
            // We set sorting topology so the children will be parsed first
            // And the commit walking descending so the newest first
            revWalk.sort(RevSort.TOPO, true);
            revWalk.sort(RevSort.COMMIT_TIME_DESC, true);

            // Start from the current commit, the first one probably will be a
            // head
            // until the current commit is equal to this commit.
            // For each commit that we parse check if it has parent this commit
            RevCommit current = null;
            while (((current = revWalk.next()) != null)
                  && !AnyObjectId.equals(current, required)) {

               // If required (this commit) is parent of current
               // we found a child
               if (RevUtils.isParent(required, current)) {
                  this.children.cache.add(new VCSCommitImp(current, this.repo));
               }
            }

            // We must ensure no one can modify our cache
            return new ArrayList<VCSCommit>(this.children.cache);

         } catch (final IOException e) {
            throw new VCSRepositoryException(e);
         } catch (final GitAPIException e) {
            throw new VCSRepositoryException(e);
         } finally {
            if (revWalk != null) {
               revWalk.release();
            }
         }
      }
   }

   /**
    * Check all the branches of repository this commit belongs to, and find all
    * heads this commit is merged into.
    * 
    * @return the heads this commit is reachable from, if empty that means this
    *         commit is a head which has no children
    * @throws GitAPIException
    * @throws IOException
    * @throws VCSRepositoryException
    */
   private List<RevCommit> getMergingHeads() throws GitAPIException,
         IOException, VCSRepositoryException {

      final List<Ref> refs = new Git(this.repo).branchList()
            .setListMode(ListMode.ALL).call();
      final RevWalk walk = new RevWalk(this.repo);
      final RevCommit base = walk.parseCommit(this.commit);
      final List<RevCommit> heads = new ArrayList<RevCommit>();

      for (final Ref ref : refs) {

         final RevCommit head = walk.parseCommit(ref.getObjectId());

         // Case when this commit is head
         if (AnyObjectId.equals(head, base)) {
            continue;
         }

         // If this commit is newer then the current head
         // do nothing else if is ancestor of head add it to list
         if (RevUtils.isAncestor(base, head, this.repo)) {
            heads.add(head);
         }
      }

      return heads;

   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Collection<VCSCommit> getPrevious() throws VCSRepositoryException {

      final List<VCSCommit> parents = new ArrayList<VCSCommit>();

      try {
         for (final RevCommit p : RevUtils.parentOf(this.commit, this.repo)) {
            parents.add(new VCSCommitImp(p, this.repo));
         }
      } catch (final MissingObjectException e) {
         throw new VCSRepositoryException(e);
      } catch (final IncorrectObjectTypeException e) {
         throw new VCSRepositoryException(e);
      } catch (final IOException e) {
         throw new VCSRepositoryException(e);
      }
      return parents;
   }

   /**
    * {@inheritDoc} {@link VCSResource.Type#NONE}
    */
   @Override
   public VCSResource getResource(final String path)
         throws VCSRepositoryException, VCSResourceNotFoundException {

      // The walk will be returned only if the path is available and is a
      // known
      // path (file or dir) otherwise an exception will be thrown. If
      // path is not correct an IllegalArgumentException will be thrown,
      // if its not available or is unknown a VCSResourceNotFoundException will
      // be
      // thrown
      // and if there is a problem with the repository a
      // VCSRepositoryException

      ArgsCheck.notNull("path", path);

      final TreeWalk walk = TreeUtils.getTreeWalkForPath(this.commit,
            this.repo, path);

      // Construct the path accordingly
      final VCSResource.Type type = RevUtils.resourceType(walk.getFileMode(0));

      if (type.equals(VCSResource.Type.FILE)) {

         return new VCSFileImp(this, path);

      } else if (type.equals(VCSResource.Type.DIR)) {

         return new VCSDirectoryImp(this, path);
      }

      throw new VCSResourceNotFoundException("uknown path " + path);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean isResourceAvailable(final String path) {
      TreeWalk walk = null;
      try {
         // We just need to create a walker, with recursive true and check if
         // the walker gives as
         // any entry, because each entry will be a file then we are sure our
         // path is a prefix of
         // given entry
         walk = TreeUtils.createTreeWalk(this.repo, this.commit, true, path);

         return walk.next();

      } catch (final IOException e) {
      } finally {
         if (walk != null) {
            walk.release();
         }
      }
      return false;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void walkTree(final ResourceVisitor<VCSResource> visitor)
         throws VCSRepositoryException {

      ArgsCheck.notNull("visitor", visitor);
      ArgsCheck.isTrue("includeDirs || includeFiles", visitor.includeDirs()
            || visitor.includeFiles());

      VCSResourceFilter<VCSResource> filter = visitor.getFilter();

      TreeWalk walker = null;
      try {
         walker = TreeUtils.createTreeWalk(this.repo, this.commit,
               !visitor.includeDirs(), (String[]) null);

         if (filter != null) {
            OptimizedResourceFilter<VCSResource> of = ResourceFilter.parse(
                  filter, null);
            if (of != null) {
               walker.setFilter(of.getCurrent());
               filter = null;
            }
         }

         if (filter == null) {
            this.walkTreeNoFilter(visitor, walker);
         } else {
            this.walkTreeWithFilter(visitor, walker);
         }

      } catch (final IOException e) {
         throw new VCSRepositoryException(e);
      } finally {
         walker.release();
      }
   }

   /**
    * Walk the tree and visit each entry without using visitor's filter.
    * <p>
    * In case the visitor's filter is parsed and converted to a JGit tree
    * filter, the user can apply this filter to the walker, and use this method
    * as this is far more efficient.
    * <p>
    * Whether this should be recursive or not, it depends on includeDirs(). User
    * must ensure that dirs || files is true.
    * 
    * @param visitor
    *           that will visit each resource
    * @param walker
    *           to walk the tree
    * @throws VCSRepositoryException
    *            if a problem occurs reading repository's object
    */
   void walkTreeNoFilter(final ResourceVisitor<VCSResource> visitor,
         final TreeWalk walker) throws VCSRepositoryException {

      boolean dirs = visitor.includeDirs();
      boolean files = visitor.includeFiles();
      walker.setRecursive(!dirs);

      try {
         while (walker.next()) {

            // We want to check first if dirs are allowed,
            // then if this is a sub tree, and then if
            // this a regular tree, because we do not want to enter
            // submodules
            if (dirs && walker.isSubtree()
                  && RevUtils.isDirMode(walker.getFileMode(0))) {

               VCSDirectoryImp dir = new VCSDirectoryImp(this,
                     walker.getPathString());

               if (!visitor.visit(dir)) {
                  return;
               }

               walker.enterSubtree();

            } else if (files && RevUtils.isFileMode(walker.getFileMode(0))) {

               VCSFileImp file = new VCSFileImp(this, walker.getPathString());

               // if visitor wants to stop tree traversal then return
               if (!visitor.visit(file)) {
                  return;
               }
            }
         }

      } catch (IOException e) {
         throw new VCSRepositoryException(e);
      } finally {
         walker.release();
      }
   }

   /**
    * Walk the tree and visit each entry using visitor's filter.
    * <p>
    * This assumes that visitor's filter can not be parsed and converted to a
    * JGit tree filter, so it will use the filter. Also it doesn't check if the
    * filter is null, so be sure to check that the filter is not null.
    * 
    * <p>
    * Whether this should be recursive or not, it depends on includeDirs(). User
    * must ensure that dirs || files is true.
    * 
    * @param visitor
    *           that will visit each resource
    * @param walker
    *           to walk the tree
    * @throws VCSRepositoryException
    *            if a problem occurs reading repository's object
    */
   void walkTreeWithFilter(final ResourceVisitor<VCSResource> visitor,
         final TreeWalk walker) throws VCSRepositoryException {

      VCSResourceFilter<VCSResource> filter = visitor.getFilter();
      boolean dirs = visitor.includeDirs();
      boolean files = visitor.includeFiles();
      walker.setRecursive(!dirs);

      try {
         while (walker.next()) {

            final FileMode mode = walker.getFileMode(0);
            if (dirs && walker.isSubtree() && RevUtils.isDirMode(mode)) {

               VCSDirectoryImp dir = new VCSDirectoryImp(this,
                     walker.getPathString());

               if (filter.include(dir)) {
                  if (!visitor.visit(dir)) {
                     return;
                  }
               }

               // continue to enter this directory
               if (filter.enter(dir)) {
                  walker.enterSubtree();
               }

            } else if (files && RevUtils.isFileMode(mode)) {

               VCSFileImp file = new VCSFileImp(this, walker.getPathString());

               if (filter.include(file)) {
                  if (!visitor.visit(file)) {
                     return;
                  }
               }
            }
         }
      } catch (IOException e) {
         throw new VCSRepositoryException(e);
      } finally {
         walker.release();
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void checkout(final String path, final String... paths)
         throws VCSRepositoryException {

      ArgsCheck.notNull("path", path);

      TreeWalk walker = null;
      final File dir = new File(path);

      // Check if the current path exists and is not directory
      if (dir.exists() && !dir.isDirectory()) {
         throw new VCSRepositoryException("path is a file: "
               + dir.getAbsolutePath());
      }

      // If the directory exists try to clean it,
      // if not try to create it
      try {
         if (dir.exists()) {
            FileUtils.cleanDirectory(dir);
         } else {
            FileUtils.forceMkdir(dir);
         }
      } catch (final IOException e) {
         throw new VCSRepositoryException(e);
      }

      /*
       * Here will walk the tree for the given commit. The tree walking will be
       * recursive that is only the files will be written to disk, all empty
       * directories will be ignored.
       */
      try {
         walker = TreeUtils.createTreeWalk(this.repo, this.commit, true, paths);

         while (walker.next()) {

            // A check to be sure that the current entry is a regular file
            final FileMode mode = walker.getFileMode(0);
            if (RevUtils.isFileMode(mode)) {

               // Get the path for the current entry
               final String filePath = walker.getPathString();

               // Create the file object for the current entry
               final File file = new File(dir, filePath);

               // We have to check if the containing directory of the
               // current entry
               // doesn't exists then create it
               if (!file.getParentFile().exists()) {
                  FileUtils.forceMkdir(file.getParentFile());
               }

               // Open the stream to write contents to
               final FileOutputStream target = new FileOutputStream(file);

               try {
                  // get the first object of this walker within tree
                  final ObjectId objectId = walker.getObjectId(0);

                  // If this is a zero id (usually denotes null object id)
                  // then do nothing
                  if (objectId.equals(ObjectId.zeroId())) {
                     continue;
                  }

                  // Get the loader
                  final ObjectLoader loader = this.repo.open(objectId);

                  // use the loader to copy the contents to the stream
                  loader.copyTo(target);
               } finally {
                  target.flush();
                  target.close();
               }
            }
         }

      } catch (final IOException e) {
         try {
            FileUtils.cleanDirectory(dir);
         } catch (final IOException e1) {
         }
         throw new VCSRepositoryException(e);
      } finally {
         walker.release();
      }
   }

   /**
    * {@inheritDoc}
    * <p>
    * <b>WARNING:</b> The tree filter will be applied only if the given resource
    * filter can be parsed to a JGit tree filter. In any other situation the
    * filter will be discarded because it requires a lot of resource computation
    * to check for each commit its tree. Usually users should use simple path
    * filters to check if the given paths are modified in the specified commit.
    * 
    */
   @Override
   public void walkCommits(final CommitVisitor<VCSCommit> visitor,
         boolean descending) throws VCSRepositoryException {

      ArgsCheck.notNull("visitor", visitor);

      // We have to parse all commits within repository
      // For each commit we have to determine if it is reachable from
      // this commit

      // Start with a RevWalk
      final RevWalk walk = new RevWalk(this.repo);
      final RevCommit bHEAD;

      try {

         // Resolve this commit
         final ObjectId oid = this.commit.getId();
         bHEAD = walk.parseCommit(oid);

         // Start the walk from the head
         walk.markStart(bHEAD);

         // Trying to set tree filters if possible
         // First we must check if we can parse this filter and
         // convert it to a JGit tree filter. If so we set
         // the provided filter to null so it will not be used,
         // but applied directly to walker
         VCSResourceFilter<VCSResource> resourceFilter = visitor
               .getResourceFilter();
         if (resourceFilter != null) {
            OptimizedResourceFilter<VCSResource> of = ResourceFilter.parse(
                  resourceFilter, null);
            if (of != null) {
               walk.setTreeFilter(of.getCurrent());
            } else {
               throw new IllegalStateException(
                     "The current resource filter can not be parsed. Try using a simple one, with the default filters at "
                           + VCSResourceFilter.class.getPackage().getName());
            }
         }

         // We check if we can parse and convert the provided commit filter
         // to a JGit commit filter, if so we apply this filter directly to
         // walker, and set the provided filter to null so it will not be used,
         // otherwise use manually the provided filter.
         VCSCommitFilter<VCSCommit> commitFilter = visitor.getFilter();
         if (commitFilter != null) {
            OptimizedCommitFilter<VCSCommit> of = CommitFilter.parse(
                  commitFilter, null);
            if (of != null) {
               walk.setRevFilter(of.getCurrent());
               commitFilter = null;
            }
         }

         // Set the order of commits
         if (descending) {
            walk.sort(RevSort.COMMIT_TIME_DESC, true);
            walk.sort(RevSort.REVERSE, false);
         } else {
            walk.sort(RevSort.REVERSE, true);
            walk.sort(RevSort.COMMIT_TIME_DESC, false);
         }

         // All commits that are ancestors of the current HEAD
         // will be accessible with walk.next(), so collect the commits until
         // there is no other commit to walk
         RevCommit current = null;
         while ((current = walk.next()) != null) {
            VCSCommit commit = new VCSCommitImp(current, this.repo);
            if (commitFilter != null) {
               if (commitFilter.include(commit)) {
                  if (!visitor.visit(commit)) {
                     return;
                  }
               }
            } else if (!visitor.visit(commit)) {
               return;
            }
         }

      } catch (final IOException e) {
         throw new VCSRepositoryException(e);
      } finally {
         walk.release();
      }
   }

   @Override
   public String toString() {
      return this.commit.getName();
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((commit == null) ? 0 : commit.hashCode());
      result = prime * result + ((repo == null) ? 0 : repo.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      VCSCommitImp other = (VCSCommitImp) obj;
      if (commit == null) {
         if (other.commit != null)
            return false;
      } else if (!commit.equals(other.commit))
         return false;
      if (repo == null) {
         if (other.repo != null)
            return false;
      } else if (!repo.getDirectory().equals(other.repo.getDirectory()))
         return false;
      return true;
   }
}
