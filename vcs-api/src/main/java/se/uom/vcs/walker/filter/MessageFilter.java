/**
 * 
 */
package se.uom.vcs.walker.filter;

import java.util.Collection;
import java.util.regex.Pattern;

import se.uom.vcs.VCSCommit;

/**
 * Check the message of the current commit if it matches any of the patterns contained in this filter.<p>
 * 
 * @author Elvis Ligu
 * @since 0.0.1
 * @version 0.0.1
 */
public class MessageFilter<T extends VCSCommit> extends PatternsFilter<T> implements VCSCommitFilter<T> {
    
    public MessageFilter(Collection<String> patterns) {
	super(patterns);
    }

    /**
     * {@inheritDoc}
     * Returns true if the current entity match any of the message patterns contained in this filter.
     */
    @Override
    public boolean include(T entity) {
	
	String msg = entity.getMessage();
	for(Pattern p : patterns) {
	    if(p.matcher(msg).matches()) {
		return true;
	    }
	}
	return false;
    }
}
