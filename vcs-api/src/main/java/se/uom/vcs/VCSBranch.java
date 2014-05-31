/**
 * 
 */
package se.uom.vcs;

import java.util.Collection;

import se.uom.vcs.exceptions.VCSRepositoryException;
import se.uom.vcs.walker.CommitVisitor;

/**
 * Represents a branch of a {@link VCSRepository}.
 * <p>
 * 
 * Some VCSs support branching (Git) and some others not (SVN). In case a VCS
 * does not support branching, the implementation can choose to emulate only the
 * master branch (a.k.a. trunk) and to provide only one branch, or to provide
 * techniques to discover branching system by tracking directory copies which
 * implies branches (such as SVN, where the branching is made by copying one
 * directory to another location).
 * <p>
 * Use {@link VCSRepository#getBranches()} to get all the given branches of
 * repository. By providing a branch name which you can obtain by calling
 * {@link VCSRepository#resolveBranch(String)} you can get a specific branch.
 * <p>
 * <b>NOTE:</b> Users should read carefully the documentation of the current
 * implementation. Generally speaking when a repository is initialized-created,
 * calling {@link VCSRepository#getSelectedBranch()}, will return the
 * <code>master</code> branch, however this is not a strict requirement.
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public interface VCSBranch {

   /**
    * Get the ID of this branch.
    * <p>
    * 
    * In some VCSs the id can be the same as its name, however the purpose of
    * obtaining the id of a branch is to resolve it later in time calling
    * {@link VCSRepository#resolveBranch(String)}.
    * 
    * @return this branch id
    */
   String getID();

   /**
    * Get the <code>head</code> commit this branch is at (the newest one).
    * <p>
    * The <code>head</code> commit is the last commit of this branch. For VCSs
    * that do not support branching, the returned commit should be the last one.
    * However, for VCSs that support branching, the returned commit may not be
    * the last commit of the current repository.
    * 
    * @return last commit of this branch (a.k.a. head)
    * @throws VCSRepositoryException
    *            if the head can not be retrieved
    */
   VCSCommit getHead() throws VCSRepositoryException;

   /**
    * Get all commits of this branch.
    * <p>
    * A commit belongs to a branch only if it is reachable from this branch,
    * that is it contains a previous history of this branch.
    * <p>
    * <b>NOTE:</b> if the implementation does not support branching this method
    * may return all commits of repository.
    * <p>
    * 
    * @return all the commits of this branch
    * @throws VCSRepositoryException
    *            when commit retrieving encounters a problem
    */
   Collection<VCSCommit> getAllCommits() throws VCSRepositoryException;

   /**
    * Check if the given commit is contained within this branch.
    * <p>
    * <b>NOTE:</b> for VCSs that support direct acyclic graph (DAG) of commits
    * this will return true even if the given commit is an ancestor of the first
    * commit of this branch (NOT repository). That means, the given commit
    * contains previous history of this branch.
    * <p>
    * Depending on the implementation, this method may start from head of this
    * branch and walk back all commits until it reaches the given commit. On the
    * other hand, if the current VCS does not support branching it may return
    * true for all commits that are older than this branch's head (usually this
    * would be the only branch, the master branch).
    * <p>
    * 
    * @param commit
    *           to check if its contained in this branch
    * @return true if the given commit is contained in this branch
    * @throws VCSRepositoryException
    *            if a problem occurs during repository querying
    */
   boolean isContained(VCSCommit commit) throws VCSRepositoryException;

   /**
    * Return the name of this branch.
    * <p>
    * In some situations the name of the branch is the same as its ID. The name
    * is usually used for readability, however users should not rely on this, as
    * the implementation is free to return any value (readable or not).
    * 
    * @return the name of this branch
    */
   String getName();

   /**
    * Walk commits of this branch.
    * <p>
    * A commit belongs to a branch only if it is reachable from this branch,
    * that is it contains a previous history of this branch. This method should
    * start from the <code>head</code> commit of this branch and walk backward
    * in time all ancestors of the current commit. In other words, starting from
    * the latest commit, get its parents, and for each of them get their
    * parents. This method is preferred, because it provides the possibility to
    * limit the results by setting filters.
    * <p>
    * If a resource filter is specified:
    * <ul>
    * <li>will visit all the commits that contains the paths which are allowed
    * by the filter</li>.
    * <li>if MODIFIED filter is specified whether in combination with other
    * filters or not, will return all the commits that modify the paths allowed
    * by the filter.
    * </ul>
    * <p>
    * If a commit filter is specified, only those commits that are allowed by
    * this filter will be visited.
    * <p>
    * <b>NOTE:</b> if the implementation does not support branching this method
    * may return all commits of repository.
    * <p>
    * 
    * @param visitor
    *           to accept the commits
    * @param descending
    *           if true the commits will be passed from new to old, if false
    *           they will be passed from old to new
    * @throws VCSRepositoryException
    *            if a problem occurs during walk
    */
   void walkCommits(CommitVisitor<VCSCommit> visitor, boolean descending)
         throws VCSRepositoryException;
}
