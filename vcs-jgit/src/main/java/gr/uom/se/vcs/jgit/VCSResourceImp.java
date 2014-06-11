package gr.uom.se.vcs.jgit;

import gr.uom.se.vcs.VCSChange;
import gr.uom.se.vcs.VCSCommit;
import gr.uom.se.vcs.VCSResource;
import gr.uom.se.vcs.exceptions.VCSRepositoryException;
import gr.uom.se.vcs.jgit.utils.RevUtils;
import gr.uom.se.vcs.jgit.utils.TreeUtils;
import gr.uom.se.vcs.jgit.walker.DiffCollector;
import gr.uom.se.vcs.walker.Visitor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.filter.PathFilter;


/**
 * Implementation of {@link VCSResource} based on JGit library.
 * <p>
 * 
 * This is an immutable object and is considered thread safe.
 * 
 * @author Elvis Ligu
 * @since 0.0.1
 * @version 0.0.1
 * @see VCSResource
 */
public class VCSResourceImp implements VCSResource {

   /**
    * The commit this resource is connected to.
    * <p>
    */
   protected VCSCommitImp commit;

   /**
    * The path of this resource instance, in repository.
    * <p>
    * 
    * The path is always relative to repository's local path.
    */
   protected String path;

   /**
    * The type of this resource.
    * <p>
    * 
    * @see VCSResource.Type
    */
   protected VCSResource.Type type;

   /**
    * Used as an object holder to keep the cached objects, during
    * synchronization.
    */
   private class CachedObjectHolder<T> {
      T object;
   }

   /**
    * This is used to cache the change type of this resource based an a previous
    * commit.
    * <p>
    */
   protected CachedObjectHolder<VCSChange.Type> changeType = new CachedObjectHolder<VCSChange.Type>();

   /**
    * Cache for deletions, because they are expensive to calculate.
    * <p>
    * If this field is not null that means they are already calculated.
    */
   protected CachedObjectHolder<List<VCSCommit>> deletions = new CachedObjectHolder<List<VCSCommit>>();

   /**
    * Cache for additions because they are expensive to calculate.
    * <p>
    * If this field is not null that means they are already calculated.
    */
   protected CachedObjectHolder<List<VCSCommit>> additions = new CachedObjectHolder<List<VCSCommit>>();

   /**
    * Creates a new instance given the following arguments.
    * <p>
    * 
    * @param commit
    *           this resource is at
    * @param path
    *           of this resource
    * @param type
    *           of this resource
    */
   public VCSResourceImp(final VCSCommitImp commit, final String path,
         final VCSResource.Type type) {

      ArgsCheck.notNull("commit", commit);
      ArgsCheck.notEmpty("path", path);
      ArgsCheck.notNull("tpe", type);

      // Check if resource with the given path is available at the given
      // commit
      if (!commit.isResourceAvailable(path)) {
         throw new IllegalArgumentException(
               "resource can not be created because it is not available in the given commit");
      }

      this.commit = commit;
      this.path = path;
      this.type = type;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public VCSCommit getCommit() {
      return this.commit;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public String getPath() {
      return this.path;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Type getType() {
      return this.type;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void walkAllCommits(final Visitor<VCSCommit> visitor)
         throws VCSRepositoryException {

      final Git git = new Git(this.commit.repo);

      try {
         final LogCommand logCommand = git.log().all().addPath(this.path);

         for (final RevCommit rc : logCommand.call()) {
            visitor.visit(new VCSCommitImp(rc, this.commit.repo));
         }

      } catch (final RevisionSyntaxException e) {
         throw new VCSRepositoryException(e);
      } catch (final MissingObjectException e) {
         throw new VCSRepositoryException(e);
      } catch (final IncorrectObjectTypeException e) {
         throw new VCSRepositoryException(e);
      } catch (final AmbiguousObjectException e) {
         throw new VCSRepositoryException(e);
      } catch (final IOException e) {
         throw new VCSRepositoryException(e);
      } catch (final NoHeadException e) {
         throw new VCSRepositoryException(e);
      } catch (final GitAPIException e) {
         throw new VCSRepositoryException(e);
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Collection<VCSCommit> getAllCommits() throws VCSRepositoryException {

      final Git git = new Git(this.commit.repo);
      final List<VCSCommit> commits = new ArrayList<VCSCommit>();
      try {
         final LogCommand logCommand = git.log().all().addPath(this.path);

         for (final RevCommit rc : logCommand.call()) {
            commits.add(new VCSCommitImp(rc, this.commit.repo));
         }

      } catch (final RevisionSyntaxException e) {
         throw new VCSRepositoryException(e);
      } catch (final MissingObjectException e) {
         throw new VCSRepositoryException(e);
      } catch (final IncorrectObjectTypeException e) {
         throw new VCSRepositoryException(e);
      } catch (final AmbiguousObjectException e) {
         throw new VCSRepositoryException(e);
      } catch (final IOException e) {
         throw new VCSRepositoryException(e);
      } catch (final NoHeadException e) {
         throw new VCSRepositoryException(e);
      } catch (final GitAPIException e) {
         throw new VCSRepositoryException(e);
      }

      return commits;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Collection<VCSCommit> getCreationCommits()
         throws VCSRepositoryException {

      synchronized (this.additions) {
         // Check first the cache if we already found the creation commits
         if (this.additions.object != null) {
            return new ArrayList<VCSCommit>(this.additions.object);
         }

         // Walk all commits that change this resource
         // and check if one of them add this
         final Git git = new Git(this.commit.repo);

         try {

            final List<VCSCommit> creations = new ArrayList<VCSCommit>();

            final LogCommand logCommand = git.log().all().addPath(this.path);

            for (final RevCommit rc : logCommand.call()) {
               if (isAddition(rc, this.commit.repo, this.path)) {
                  creations.add(new VCSCommitImp(rc, this.commit.repo));
               }
            }

            this.additions.object = new ArrayList<VCSCommit>(creations);
            return creations;

         } catch (final RevisionSyntaxException e) {
            throw new VCSRepositoryException(e);
         } catch (final MissingObjectException e) {
            throw new VCSRepositoryException(e);
         } catch (final IncorrectObjectTypeException e) {
            throw new VCSRepositoryException(e);
         } catch (final AmbiguousObjectException e) {
            throw new VCSRepositoryException(e);
         } catch (final IOException e) {
            throw new VCSRepositoryException(e);
         } catch (final NoHeadException e) {
            throw new VCSRepositoryException(e);
         } catch (final GitAPIException e) {
            throw new VCSRepositoryException(e);
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Collection<VCSCommit> getDeletionCommits()
         throws VCSRepositoryException {

      synchronized (this.deletions) {
         // check first the cache if we already found the creation commits
         if (this.deletions.object != null) {
            return new ArrayList<VCSCommit>(this.deletions.object);
         }

         // walk all commits that change this resource
         // and check if one of them add this
         final Git git = new Git(this.commit.repo);

         try {

            final List<VCSCommit> deletions = new ArrayList<VCSCommit>();

            final LogCommand logCommand = git.log().all().addPath(this.path);

            for (final RevCommit rc : logCommand.call()) {
               if (isDeletion(rc, this.commit.repo, this.path)) {
                  deletions.add(new VCSCommitImp(rc, this.commit.repo));
               }
            }

            this.deletions.object = deletions;
            return new ArrayList<VCSCommit>(deletions);

         } catch (final RevisionSyntaxException e) {
            throw new VCSRepositoryException(e);
         } catch (final MissingObjectException e) {
            throw new VCSRepositoryException(e);
         } catch (final IncorrectObjectTypeException e) {
            throw new VCSRepositoryException(e);
         } catch (final AmbiguousObjectException e) {
            throw new VCSRepositoryException(e);
         } catch (final IOException e) {
            throw new VCSRepositoryException(e);
         } catch (final NoHeadException e) {
            throw new VCSRepositoryException(e);
         } catch (final GitAPIException e) {
            throw new VCSRepositoryException(e);
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean isModified() throws VCSRepositoryException {

      return checkChangeType(VCSChange.Type.MODIFIED, this);
   }

   @Override
   public boolean isAdded() throws VCSRepositoryException {

      return checkChangeType(VCSChange.Type.ADDED, this);
   }

   /**
    * Check this resource if it is changed as type implies in this commit. This
    * will check the parents of resource's commit, and make a diff with each of
    * them, if there were a parent from which this resource has been changed
    * then returns true
    * <p>
    * 
    * @param type
    * @return
    * @throws VCSRepositoryException
    */
   public static boolean checkChangeType(final VCSChange.Type type,
         final VCSResourceImp resource) throws VCSRepositoryException {

      // We cache the change of this resource
      // so we can check if there is a cached change type
      synchronized (resource.changeType) {

         if (resource.changeType.object != null) {
            return resource.changeType.object.equals(type);
         }

         // We must get all parent commits
         // and do a diff to each parent,
         // if we find a DiffEntry that this
         // resource was added/modified then return true,
         // otherwise false
         final RevCommit thisCommit = resource.commit.commit;
         final RevCommit[] parents = thisCommit.getParents();

         // The first commit will have no parents, but all files are added
         if ((parents.length == 0)) {
            return type.isAdd();
         }

         // Check for each parent by diffing with this commit
         for (final RevCommit parentCommit : parents) {

            // Collecting diffs of this commit with the current parent
            final DiffCollector<DiffEntry> diffs = new DiffCollector<DiffEntry>(
                  resource.commit.repo, parentCommit, thisCommit);
            diffs.setPathFilters(PathFilter.create(resource.path));

            final Collection<DiffEntry> coll = diffs.collect();

            // If there is a diff entry that is the same as the given type
            // then return true
            for (final DiffEntry e : coll) {

               final VCSChange.Type t = RevUtils.changeType(e.getChangeType());
               if (t.equals(type)) {

                  String path = null;
                  if (t.isModify() || t.isAdd() || t.isRename() || t.isCopy()) {

                     path = e.getNewPath();

                  } else if (t.isDelete()) {
                     path = e.getOldPath();
                  }

                  if ((path != null) && path.equals(resource.path)) {
                     resource.changeType.object = type;
                     return true;
                  }
               }
            }
         }
      }
      return false;
   }

   /**
    * Check if the specified path was added at the given commit. It will check
    * all parents, if this path is present at the given commit but not present
    * to any of its parents that means it was just added.
    * <p>
    * <b>WARNING:</b> This method may return true even in case there is a RENAME
    * because it checks if the given path is not present all parents of
    * the given commit, but present at this commit. When we have a rename
    * usually a path is present at least one of parents of the current commit
    * that made the rename, however it is not present at the current commit.
    * 
    * @param commit
    * @param repo
    * @param path
    * @return
    * @throws IOException
    */
   private static boolean isAddition(final RevCommit commit,
         final Repository repo, final String path) throws IOException {

      // If this path doesn't exist in this commit then it is not
      // an addition for sure
      if (!TreeUtils.existPath(commit, repo, path)) {
         return false;
      }

      // If this path is available at least at one of its parents then
      // its not an addition, we assume this because there can not be
      // an addition and a modification together, there would be a conflict
      final RevCommit[] parents = commit.getParents();
      for (final RevCommit parentCommit : parents) {
         if (TreeUtils.existPath(parentCommit, repo, path)) {
            return false;
         }
      }

      return true;
   }

   /**
    * Check if the specified path was deleted at the given commit. It will check
    * all parents, if this path is not present to the given commit but is
    * present to any of its parents that means it was just added.
    * <p>
    * <b>WARNING:</b> This method may return true even in case there is a RENAME
    * because it checks if the given path is present at one of the parents of
    * the given commit, but not present at this commit. When we have a rename
    * usually a path is present at least one of parents of the current commit
    * that made the rename, however it is not present at the current commit.
    * 
    * @param commit
    * @param repo
    * @param path
    * @return
    * @throws IOException
    */
   public static boolean isDeletion(final RevCommit commit,
         final Repository repo, final String path) throws IOException {

      // If this path exists in this commit then it is not
      // a deletion for sure
      if (TreeUtils.existPath(commit, repo, path)) {
         return false;
      }

      // If this path is available at least at one of its parents then
      // its a deletion, we assume this because if a path is available
      // at one parent commit that will be available to all children unless
      // it is removed from a child
      final RevCommit[] parents = commit.getParents();
      for (final RevCommit parentCommit : parents) {
         if (TreeUtils.existPath(parentCommit, repo, path)) {
            return true;
         }
      }

      return false;
   }

   @Override
   public String toString() {

      return "[" + this.type + " " + this.path + " " + this.commit.getID()
            + "]";
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public VCSResource getParent() {

      // Will check from last character to find a file separator
      int index = this.path.length();
      while (--index > -1) {
         if (path.charAt(index) == '/') {
            break;
         }
      }
      // If a file separator is found and is not at first character
      // that means this resource has a parent
      String parentPath = null;
      if (index > 0) {
         parentPath = this.path.substring(0, index);
         try {
            return this.commit.getResource(parentPath);
         } catch (Exception ex) {
            throw new IllegalStateException("Parent resource at " + parentPath
                  + " is not available");
         }
      }
      return null;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((commit == null) ? 0 : commit.hashCode());
      result = prime * result + ((path == null) ? 0 : path.hashCode());
      result = prime * result + ((type == null) ? 0 : type.hashCode());
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
      VCSResourceImp other = (VCSResourceImp) obj;
      if (commit == null) {
         if (other.commit != null)
            return false;
      } else if (!commit.equals(other.commit))
         return false;
      if (path == null) {
         if (other.path != null)
            return false;
      } else if (!path.equals(other.path))
         return false;
      if (type != other.type)
         return false;
      return true;
   }
}
