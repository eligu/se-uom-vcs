/**
 * 
 */
package se.uom.vcs.walker.filter;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Check whether all filters include true to return the result.<p>
 * 
 * @author Elvis Ligu
 * @since 0.0.1
 * @version 0.0.1
 */
public class VCSAndFilter<T> implements VCSFilter<T> {

    private Set<VCSFilter<T>> filters;
    
    /**
     * Create a new filter that will check if all of the given filters returns true.<p>
     * 
     * @param filters
     * 		the filters to check if all of the filters returns true. Must not be null not or empty
     * 		or contain a null filter.
     */
    public VCSAndFilter(Collection<VCSFilter<T>> filters) {
	if(filters == null) {
	    throw new IllegalArgumentException("filters must not be null");
	}
	if(filters.isEmpty()) {
	    throw new IllegalArgumentException("filters must not be empty");
	}
	this.filters = new LinkedHashSet<VCSFilter<T>>();
	for(VCSFilter<T> f : filters) {
	    if(f == null) {
		throw new IllegalArgumentException("filters must not contain null");
	    }
	    this.filters.add(f);
	}
    }
    
    /**
     * {@inheritDoc}
     * Returns true if all of the specified filters (during creation) returns true.<p>
     */
    @Override
    public boolean include(T entity) {
	
	for(VCSFilter<T> f : filters) {
	    if(!f.include(entity)) {
		return false;
	    }
	}
	return true;
    }

}
