/**
 * 
 */
package se.uom.vcs;

import se.uom.vcs.exceptions.VCSRepositoryException;

/**
 * Represents a tag of a {@link VCSRepository}.<p>
 * 
 * A tag is a special label attached to a commit in order to get quick
 * reference to e given commit. Usually a version release of a software
 * is the source code at a given commit tagged with a label (i.e. v1.2).
 * 
 * @author Elvis Ligu
 * @since 0.0.1
 * @version 0.0.1
 * 
 */
public interface VCSTag {

	/**
	 * Return the ID of this tag (a.k.a. tag name)
	 * 
	 * @return
	 */
	String getID();
	
	/**
	 * Get the commit this tag is attached to
	 * 
	 * @return
	 * @throws VCSRepositoryException 
	 */
	VCSCommit getCommit() throws VCSRepositoryException;

	/**
	 * Get tag name
	 * 
	 * @return
	 */
	public abstract String getTagName();
}
