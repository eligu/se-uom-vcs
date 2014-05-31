/**
 * 
 */
package se.uom.vcs;

import java.util.Collection;

import se.uom.vcs.exceptions.VCSRepositoryException;
import se.uom.vcs.walker.Visitor;

/**
 * A resource represents the state of a file or directory in repository at a
 * given revision.
 * <p>
 * 
 * A resource should be able to give access to all its information, included but
 * not limited to commit (this resource was retrieved from), path, type and
 * commits that modify it. There are specialized versions of this class (see
 * {@link VCSFile},{@link VCSDirectory}) that gives access to resource contents.
 * 
 * @author Elvis Ligu
 * @since 0.0.1
 * @version 0.0.1
 * @see {@link VCSFile}
 * @see {@link VCSDirectory}
 */
public interface VCSResource {

   /**
    * Resource type.
    * <p>
    * 
    * @author elvis
    * 
    */
   public static enum Type {
      FILE, DIR, NONE
   }

   /**
    * Get the commit (or revision) this resource is linked to.
    * <p>
    * 
    * @return the commit of this resource
    * @see VCSCommit
    */
   VCSCommit getCommit();

   /**
    * Get the path of this resource.
    * <p>
    * 
    * @return the path of this resource
    */
   String getPath();

   /**
    * Get the type of the resource.
    * <p>
    * 
    * @return the type of this resource
    * @see Type
    * 
    */
   Type getType();

   /**
    * Return the commits that created this resource.
    * <p>
    * 
    * Note that a resource may be created multiply times during its life within
    * VCS, that is, when this resource was created and a deletion occurs and
    * again a recreation. This method will return a list of commits that created
    * this resource. Usually there is only one commit that creates this
    * resource.
    * 
    * @return a collection containing all the commits where this resource was
    *         created
    * @throws VCSRepositoryException
    *            if reading objects from repository is impossible
    */
   Collection<VCSCommit> getCreationCommits() throws VCSRepositoryException;

   /**
    * 
    * Return the commits that deleted this resource.
    * <p>
    * 
    * Note that a resource may be created/deleted multiply times during its life
    * within VCS, that is when this resource was created and a deletion occurs
    * and again a recreation. This method will return a list of commits that
    * deleted this resource. Usually there is only one commit that deletes this
    * resource.
    * 
    * @return a collection containing all the commits where this resource was
    *         created
    * @throws VCSRepositoryException
    *            if reading objects from repository is impossible
    */
   Collection<VCSCommit> getDeletionCommits() throws VCSRepositoryException;

   /**
    * Return all the commits that changed this resource from its beginning until
    * the end of its life.
    * <p>
    * 
    * Creation and deletion commits will not be included within the results.
    * 
    * @return a collections containing all the commits that modify this resource
    * @throws VCSRepositoryException
    *            if reading objects from repository is impossible
    */
   Collection<VCSCommit> getAllCommits() throws VCSRepositoryException;

   /**
    * Returns true if this resource was created at this commit.
    * <p>
    * 
    * There are VCSs that support merging two or more commits into one, that
    * means it has to produce differences with all the parents of the commit
    * this resource is at (see {@link #getCommit()}. Thus, the use of this
    * method is expected to require significant processing power.
    * <p>
    * However if this returns true, that doesn't mean the resource was first
    * added in the repository, there may be previous deletions and creations.
    * 
    * @return true if this resource was added at this commit
    * @throws VCSRepositoryException
    *            if reading objects from repository is impossible
    * @see VCSResource#isFirstAdded()
    */
   boolean isAdded() throws VCSRepositoryException;

   /**
    * Returns true if this resource was modified at this commit.
    * <p>
    * 
    * Note that for some VCSs which supports directed acyclic graphs for
    * commits, must check all the previous parents (usually one or two) to
    * decide if this resource was modified during this commit. Thus, the use of
    * this method is expected to require significant processing power.
    * 
    * @return true if this resource was modified at this commit
    * @throws VCSRepositoryException
    *            if reading objects from repository is impossible
    */
   boolean isModified() throws VCSRepositoryException;

   /**
    * Walk all commits that modify this resource.
    * <p>
    * 
    * The walking starts from HEAD commit and goes backward until the first
    * commit that created this resource is found.
    * <p>
    * This method is expected to work as calling the method
    * {@link VCSCommit#walkCommits(CommitVisitor, boolean)} of the commit this
    * resource is at with a path filter showing this resource's path, combined
    * with a modified filter.
    * 
    * 
    * @param visitor
    *           that will accept all commits
    * @throws VCSRepositoryException
    *            if reading objects from repository is impossible
    */
   public abstract void walkAllCommits(Visitor<VCSCommit> visitor)
         throws VCSRepositoryException;

   /**
    * Returns the parent resource this is contained.
    * <p>
    * 
    * If this resource is at root of repository the parent might be null,
    * however it depends on the implementation, that is there might be a special
    * case when the root directory of repository is represented by a resource.
    * 
    * @return the directory this resource is contained. If null than this
    *         resource is a top level resource, is not under any directory.
    */
   public VCSResource getParent();
}
