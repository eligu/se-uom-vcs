/**
 * 
 */
package gr.uom.se.vcs;

import gr.uom.se.vcs.exceptions.VCSRepositoryException;
import gr.uom.se.vcs.walker.CommitVisitor;

import java.util.Collection;
import java.util.Set;

/**
 * The main entry that gives access to a VCS repository.
 * <p>
 * 
 * A repository object must be linked to a local path which contains the
 * contents of a VCS repository or to a remote path. If the repository is cloned
 * from a remote path then the two paths must be specified (the local and the
 * remote).
 * <p>
 * 
 * When a repository is created, in order to have access to its commits you must
 * select a branch. Generally an implementation may decide to have the master
 * branch (a.k.a. trunk) selected by default. However this is not required, thus
 * you can not rely on this.
 * <p>
 * 
 * A repository must be able to provide all the basic functionality that are
 * required to access a VCS repository. It must be able to open a local
 * repository or to clone a remote one to the local path, or to update a cloned
 * repository from a remote. It must recognize at least the following entities:
 * 
 * <li>{@link VCSResource}
 * <li>{@link VCSCommit}
 * 
 * <br>
 * <br>
 * additionally it may be able to handle:<br>
 * <br>
 * 
 * <li> {@link VCSBranch}
 * <li> {@link VCSTag}
 * 
 * <br>
 * <br>
 * All the above entities must be independent, that is, having an instance of
 * one should be able to access different functionality without the need of
 * additional class. That decision is made due to simplify API and low the
 * burden of learning and using it.
 * 
 * @author Elvis Ligu
 * @since 0.0.1
 * @version 0.0.1
 */
public interface VCSRepository {

   /**
    * Get the local path where this repository is stored.
    * <p>
    * 
    * @return the path from where this repository is loaded
    */
   String getLocalPath();

   /**
    * Get the remote path of this repository if any.
    * <p>
    * 
    * Usually if this repository is cloned from a remote.
    * 
    * @return the origin path from where this repository was cloned
    */
   String getRemotePath();

   /**
    * Clone the remote repository to the local path.
    * <p>
    * 
    * Local path must be an empty directory if it exists. If local path doesn't
    * exist it will create it.
    * 
    * @throws VCSRepositoryException
    *            when a problem occurs during clone
    */
   void cloneRemote() throws VCSRepositoryException;

   /**
    * Get from remote locations all updates if any.
    * <p>
    * 
    * @throws VCSRepositoryException
    *            when a problem occurs during update
    */
   void update() throws VCSRepositoryException;

   /**
    * Return all branches that this repository has.
    * <p>
    * 
    * @return the branches of this repository
    * @throws VCSRepositoryException
    *            when a problem occurs while reading repository
    */
   Collection<VCSBranch> getBranches() throws VCSRepositoryException;

   /**
    * Return the selected branch.
    * <p>
    * 
    * In order to support different VCSs each commit is required to be in a
    * branch. Implementations for VCSs that do not support branching are
    * required to emulate a master branch which will be the only branch within
    * repository. Other implementations should select by default a branch in
    * order to eliminate exceptions and other situations during commit requests.
    * In most situations the master branch is selected by default.
    * 
    * @return the selected branch
    * @throws VCSRepositoryException
    *            when a problem occurs while reading repository
    */
   VCSBranch getSelectedBranch() throws VCSRepositoryException;

   /**
    * Set the given branch as the current on to use within repository.
    * <p>
    * 
    * This method may have no effect in implementations of VCSs that do not
    * support branching.
    * 
    * @param branch
    *           the branch to select
    * @throws VCSRepositoryException
    *            when a problem occurs while selecting the branch
    */
   void selectBranch(VCSBranch branch) throws VCSRepositoryException;

   /**
    * Return all tags this repository have.
    * <p>
    * 
    * @return the tags of this repository
    * @throws VCSRepositoryException
    *            when a problem occurs while reading the repository
    */
   Collection<VCSTag> getTags() throws VCSRepositoryException;

   /**
    * Resolve the commit with the specified id.
    * <p>
    * 
    * @param cid
    *           commit id
    * @return the resolved commit
    * @throws VCSRepositoryException
    *            if the commit can not be resolved
    */
   VCSCommit resolveCommit(String cid) throws VCSRepositoryException;

   /**
    * Resolve the branch with the specified id.
    * <p>
    * 
    * @param bid
    *           branch id
    * @return the resolved branch
    * @throws VCSRepositoryException
    *            if the branch can not be resolved
    */
   VCSBranch resolveBranch(String bid) throws VCSRepositoryException;

   /**
    * Resolve a tag with the specified tag string.
    * <p>
    * 
    * @param tag
    *           the label of tag
    * @return the resolved tag
    * @throws VCSRepositoryException
    *            if the tag can not be resolved
    */
   VCSTag resolveTag(String tag) throws VCSRepositoryException;

   /**
    * Return the HEAD commit (a.k.a. the newest commit).
    * <p>
    * 
    * Some VCSs support native branching, so in order to resolve the HEAD commit
    * a branch must be selected. In general the MASTER branch will be selected
    * by default. If there is not a selected branch, an exception will be
    * thrown. This method never returns null.
    * 
    * @return the HEAD commit this repository points to
    * @throws VCSRepositoryException
    *            if a branch is not selected or a problem occurs reading
    *            repository
    */
   public VCSCommit getHead() throws VCSRepositoryException;

   /**
    * Return the first commit of this repository (the oldest one).
    * <p>
    * 
    * @return the first commit that was made to repository
    * @throws VCSRepositoryException
    *            if a branch is not yet selected or while reading the repository
    */
   public VCSCommit getFirst() throws VCSRepositoryException;

   /**
    * Close this repository (if any created, or cloned).
    * <p>
    * 
    * It is advisable to close repository because some implementations, may
    * require to close a lot of resources.
    */
   public abstract void close();

   /**
    * Walk all commits at once.
    * <p>
    * In most cases walking a single commit is enough, however there are cases
    * when the user wants to walk several commits at once. This is usually
    * required when all commits of a repository are required and the user can
    * walk all heads of the branches in the same time. If the implementation
    * doesn't support branching, then there is no sense to walk all commits at
    * once.
    * 
    * @param commits
    *           the commits to walks (usually heads of branches)
    * @param visitor
    *           to visit each commit
    * @param descending
    *           true for walking from newer to older, false from older to newer.
    * @throws VCSRepositoryException
    *            if a problem occurs during walking
    */
   public void walkAll(Set<VCSCommit> commits,
         CommitVisitor visitor, boolean descending)
         throws VCSRepositoryException;
}
