/**
 * 
 */
package se.uom.vcs.walker.filter.commit;

import java.util.Collection;
import java.util.regex.Pattern;

import se.uom.vcs.VCSCommit;

/**
 * Test the current commit if it is from a specified list of authors.<p>
 * 
 * This implementation is based on Java patterns so any string passed at
 * the constructor should take care of the special characters used by
 * Java.
 * 
 * @author Elvis Ligu
 * @since 0.0.1
 * @version 0.0.1
 */
public class AuthorFilter<T extends VCSCommit> extends PatternsFilter<T> implements VCSCommitFilter<T> {
    
    /**
     * Create a new instance based on the given author patterns.<p>
     * 
     * @param authorPatterns
     * 		Java patterns that will be checked against the author of a given commit.
     */
    public AuthorFilter(Collection<String> authorPatterns) {
	super(authorPatterns);
    }
    
    /**
     * {@inheritDoc}
     * Returns true if the current entity match any of the author patterns contained in this filter.
     */
    @Override
    public boolean include(T entity) {
	
	String author = entity.getAuthor();
	for(Pattern p : patterns) {
	    if(p.matcher(author).matches()) {
		return true;
	    }
	}
	return false;
    }
}
