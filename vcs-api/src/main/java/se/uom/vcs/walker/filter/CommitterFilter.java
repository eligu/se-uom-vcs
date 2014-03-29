/**
 * 
 */
package se.uom.vcs.walker.filter;

import java.util.Collection;
import java.util.Set;
import java.util.regex.Pattern;

import se.uom.vcs.VCSCommit;

/**
 * Test the current commit if it is from a specified list of committers.<p>
 * 
 * @author Elvis Ligu
 * @since 0.0.1
 * @version 0.0.1
 */
public class CommitterFilter<T extends VCSCommit> extends PatternsFilter<T> implements VCSCommitFilter<T>{

    public CommitterFilter(Collection<String> committersPatterns) {
	super(committersPatterns);
    }
    
    /**
     * {@inheritDoc}
     * Returns true if the current entity match any of the committer patterns contained in this filter.
     */
    @Override
    public boolean include(T entity) {
	
	String commiter = entity.getCommiter();
	for(Pattern p : patterns) {
	    if(p.matcher(commiter).matches()) {
		return true;
	    }
	}
	return false;
    }

    
}
