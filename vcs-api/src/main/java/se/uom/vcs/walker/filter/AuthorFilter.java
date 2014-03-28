/**
 * 
 */
package se.uom.vcs.walker.filter;

import se.uom.vcs.VCSCommit;

/**
 * Test the current commit if it is from a specified list of authors.<p>
 * 
 * @author Elvis Ligu
 * @since 0.0.1
 * @version 0.0.1
 */
public interface AuthorFilter<T extends VCSCommit> extends VCSCommitFilter<T> {
    
}
