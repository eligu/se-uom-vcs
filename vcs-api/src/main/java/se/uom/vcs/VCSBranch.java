/**
 * 
 */
package se.uom.vcs;

import java.util.Collection;

import se.uom.vcs.exceptions.VCSRepositoryException;
import se.uom.vcs.walker.CommitVisitor;

/** 
 * Represents a branch of a {@link VCSRepository}.<p>
 * 
 * Some VCSs support branching (Git) and some others not (SVN). In case VCS
 * does not support branching, the implementation can choose to emulate only the master
 * branch (a.k.a. trunk) and to provide only one branch, or to provide techniques to
 * discover branching system by tracking directory copies which implies branches
 * (such as SVN when the branching is made by copying one directory to another location).<p>
 * 
 * Use {@link VCSRepository#getBranches()} to get all the given branches of repository.
 * By providing a branch name which you can obtain by calling {@link VCSRepository#resolveBranch(String)}
 * you can get a specific branch.
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public interface VCSBranch {

	/**
	 * Get the ID of this branch.<p>
	 * 
	 * In some VCSs the id can be the same as its name, however the purpose of obtaining the id of
	 * a branch is to resolve it later in time calling {@link VCSRepository#resolveBranch(String)}.
	 * 
	 * @return 
	 * 		this branch id
	 */
	String getID();

	/**
	 * Get the head commit this branch is at (the newest one).<p>
	 * 
	 * The head commit is the last commit of this branch. VCSs that do not support
	 * branching should have this head as the last commit in repository too. However,
	 * those that do support branching may have this head not the last commit in the
	 * repository.
	 * 
	 * @return
	 * 		last commit of this branch (a.k.a. head)
	 * @throws 
	 * 		VCSRepositoryException if the head can not be retrieved 
	 */
	VCSCommit head() throws VCSRepositoryException;

	/**
	 * Get all commits of this branch.<p>
	 * A commit belongs to a branch only if it is reachable from this branch, 
	 * that is it contains a previous history of this branch.<p>
	 * 
	 * <b>NOTE:</b> if the implementation does not support branching this method may return all commits of repository.<p>
	 * 
	 * @return
	 * 		all the commits of this branch
	 * @throws 
	 * 		VCSRepositoryException when commit retrieving encounters a problem
	 */
	Collection<VCSCommit> getAllCommits() throws VCSRepositoryException;

	/**
	 * Check if the given commit is contained within this branch.<p> 
	 * 
	 * <b>NOTE:</b> for VCSs that support direct acyclic graph of commits this will return true even if the given commit is an ancestor 
	 * of the first commit of this branch (NOT repository).
	 *  
	 * @param 
	 * 		commit to check if its contained in this branch
	 * @return
	 * 		true if the given commit is contained in this branch
	 * @throws 
	 * 		VCSRepositoryException if a problem occurs during repository querying 
	 */
	boolean isContained(VCSCommit commit) throws VCSRepositoryException;

	/**
	 * Return the name of this branch.<p>
	 * 
	 * @return
	 * 		the name of this branch
	 */
	public abstract String getBranchName();

	// TODO Have to rewrite comments for path limiting visitor and modifying visitors
	/**
	 * Walk commits of this branch.<p>
	 * 
	 * A commit belongs to a branch only if it is reachable from this branch, 
	 * that is it contains a previous history of this branch.
	 * <p>
	 * If a resource filter is specified, the changes will be limited only to those
	 * resources that this filter allows.
	 * <p>
	 * If a commit filter is specified, only those commits that are allowed by this
	 * filter will be visited.
	 * <p>
	 * <b>NOTE:</b> if the implementation does not support branching this method may return all commits of repository.<p>
	 * 
	 * @param 
	 * 		visitor to accept the commits
	 * @throws 
	 * 		VCSRepositoryException if a problem occurs during walk
	 */
	void walkCommits(CommitVisitor<VCSCommit> visitor) throws VCSRepositoryException;
}
