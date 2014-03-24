/**
 * 
 */
package se.uom.vcs;

import java.util.Collection;

import se.uom.vcs.exceptions.VCSRepositoryException;
import se.uom.vcs.walker.Visitor;

/**
 * A resource represents the state of file or directory in repository at a given revision.<p>
 * 
 * A resource should be able to give access to all its informations, included but not limited to
 * commit (this resource was retrieved from), path, type and commits that modify it. There are
 * specialized versions of this class (see {@link VCSFile},{@link VCSDirectory}) that gives
 * access to resource contents.
 * 
 * @author Elvis Ligu
 * @since 0.0.1
 * @version 0.0.1
 * @see {@link VCSFile} 
 * @see {@link VCSDirectory}
 */
public interface VCSResource {

	/**
	 * Resource type.<p>
	 * 
	 * @author elvis
	 *
	 */
	public static enum Type {
		FILE, DIR, NONE
	}
	
	/**
	 * Get the commit (or revision) this resource is linked to.<p>
	 * 
	 * @return
	 */
	VCSCommit getCommit();
	
	/**
	 * Get the path of this resource.<p>
	 * 
	 * @return
	 */
	String getPath();
	
	/**
	 * Get the type of the resource.<p>
	 * 
	 * @return
	 * @see Type
	 * 
	 */
	Type getType();
	
	/**
	 * Return the commits that created this resource.<p> 
	 * 
	 * Note that a resource may be created multiply times during its life within VCS,
	 * that is, when this resource was created and a deletion occurs and again a recreation. 
	 * This method will return a list of commits that created this resource.
	 * Usually there is only one commit that creates this resource.
	 * 
	 * @return
	 * @throws VCSRepositoryException 
	 */
	Collection<VCSCommit> getCreationCommits() throws VCSRepositoryException;
	
	/**
	 *
	 * Return the commits that deleted this resource.<p> 
	 * Note that a resource may be created/deleted multiply times during its life within VCS, 
	 * that is when this resource was created and a deletion occurs and again a recreation. 
	 * This method will return a list of commits that deleted this resource.
	 * Usually there is only one commit that deletes this resource.
	 * 
	 * @return
	 * @throws VCSRepositoryException 
	 */
	Collection<VCSCommit> getDeletionCommits() throws VCSRepositoryException;
	
	/**
	 * Return all commits that changed this resource from its beginning until the end of its life.<p>
	 * 
	 * Creation and deletion commits will not be included within the results.
	 * 
	 * @return
	 * @throws VCSRepositoryException 
	 */
	Collection<VCSCommit> getAllCommits() throws VCSRepositoryException;
	
	/**
	 * Returns true if this resource was created at this commit.<p>
	 * 
	 * There are VCSs that support merging two or more commits into one, that means it has to produce differences
	 * with all parents of the commit this resource is at (see {@link #getCommit()}.
	 * 
	 * However if this returns true, that doesn't mean the resource was first added in the repository,
	 * there may be previous deletions and creations.
	 * 
	 * @return
	 * @throws VCSRepositoryException 
	 * @see VCSResource#isFirstAdded()
	 */
	boolean isAdded() throws VCSRepositoryException;
	
	/**
	 * Returns true if this resource was modified at this commit.<p> 
	 * Note that for some VCSs which supports directed acyclic graphs for commits, must check all the
	 * previous parents (usually one or two) to decide if this resource was modified during this commit.
	 *  
	 * @return
	 * @throws VCSRepositoryException 
	 */
	boolean isModified() throws VCSRepositoryException;

	/**
	 * Walk all commits that modify this resource.<p>
	 * 
	 * The walking starts from HEAD commit and goes backward until the first
	 * commit that created this resource is found.
	 * 
	 * @param visitor
	 * @throws VCSRepositoryException
	 */
	public abstract void walkAllCommits(Visitor<VCSCommit> visitor)
			throws VCSRepositoryException;
}
