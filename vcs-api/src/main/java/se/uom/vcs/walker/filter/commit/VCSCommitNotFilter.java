/**
 * 
 */
package se.uom.vcs.walker.filter.commit;

import se.uom.vcs.VCSCommit;

/**
 * A simple implementation of commit filter that revert the result of a filter.<p>
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public class VCSCommitNotFilter<T extends VCSCommit> implements VCSCommitFilter<T> {

    /**
     * The filter to negate its result.<p>
     */
    private VCSCommitFilter<T> filter;
    
    /**
     * Create a new filter that will negate the include result of the given filter argument.<p>
     * 
     * @param filter 
     * 		the include result of which will be negated
     */
    public VCSCommitNotFilter(VCSCommitFilter<T> filter) {
	if(filter == null) {
	    throw new IllegalArgumentException("filter must not be null");
	}
	this.filter = filter;
    }
    
    /**
     * @return
     * 		the filter this will revert
     */
    public VCSCommitFilter<T> getFilter() {
	return this.filter;
    }
    
    /**
     * {@inheritDoc}
     * This will revert the specified filter (during the creation) returned value. 
     */
    @Override
    public boolean include(T entity) {
	return !filter.include(entity);
    }
}
