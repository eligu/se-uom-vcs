/**
 * 
 */
package se.uom.vcs.walker.filter.resource;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import se.uom.vcs.VCSResource;

/**
 * Check wether all the specified filters return true.
 * <p>
 * 
 * @author Elvis Ligu
 * @since 0.0.1
 * @version 0.0.1
 */
public class VCSResourceAndFilter<T extends VCSResource> extends AbstractResourceFilter<T> implements VCSResourceFilter<T> {

    private Set<VCSResourceFilter<T>> filters;

    /**
     * Create a new filter that will check if all of the given filters returns
     * true.
     * <p>
     * 
     * @param filters
     *            the filters to check if all of the filters returns true. Must
     *            not be null not or empty or contain a null filter.
     */
    public VCSResourceAndFilter(Collection<VCSResourceFilter<T>> filters) {
	if (filters == null) {
	    throw new IllegalArgumentException("filters must not be null");
	}
	if (filters.size() < 2) {
	    throw new IllegalArgumentException(
		    "filters mut contain at least two elements");
	}
	this.filters = new LinkedHashSet<VCSResourceFilter<T>>();
	for (VCSResourceFilter<T> f : filters) {
	    if (f == null) {
		throw new IllegalArgumentException(
			"filters must not contain null");
	    }
	    this.extract(f);
	}
	Set<VCSResourceFilter<T>> tfilters = new LinkedHashSet<VCSResourceFilter<T>>(filters);
	
	// Check filters if they excludes each other
	for(VCSResourceFilter<T> f : filters) {
	    
	    if(f instanceof AbstractResourceFilter) {
		tfilters.remove(f);
		AbstractResourceFilter<T> af = ((AbstractResourceFilter<T>) f);
		for(VCSResourceFilter<T> f1 : tfilters) {
		    if(f1 instanceof AbstractResourceFilter) {
			if(af.excludesAnd((AbstractResourceFilter<T>) f1)) {
			    throw new IllegalArgumentException("filter " + af + " excludes " + f1);
			}
		    }
		}
	    }
	    
	    
	}
    }

    private void extract(VCSResourceFilter<T> filter) {
	if (this.getClass().isAssignableFrom(filter.getClass())) {

	    for (VCSResourceFilter<T> f : ((VCSResourceAndFilter<T>) filter).filters) {
		if (this.getClass().isAssignableFrom(f.getClass())) {
		    extract(f);
		} else {
		    this.filters.add(f);
		}
	    }
	} else {
	    this.filters.add(filter);
	}
    }

    /**
     * 
     * @return the filters
     */
    public Set<VCSResourceFilter<T>> getFilters() {
	return this.filters;
    }

    /**
     * {@inheritDoc} Returns true if all of the specified filters (during
     * creation) returns true.
     * <p>
     */
    @Override
    public boolean include(T entity) {

	for (VCSResourceFilter<T> f : filters) {
	    if (!f.include(entity)) {
		return false;
	    }
	}
	return true;
    }

    @Override
    public boolean enter(T resource) {
	// Will allow only if any of the filters allow this entity
	for (VCSResourceFilter<T> f : filters) {
	    if (!f.enter(resource)) {
		return false;
	    }
	}
	return true;
    }

    @Override
    public VCSResourceFilter<T> not() {

	Set<VCSResourceFilter<T>> not = new LinkedHashSet<VCSResourceFilter<T>>(
		filters.size());

	for (VCSResourceFilter<T> f : filters) {
	    not.add(VCSResourceNotFilter.create(f));
	}
	return new VCSResourceAndFilter<T>(not);
    }

    @Override
    protected boolean excludesAnd(AbstractResourceFilter<T> filter) {
        for(VCSResourceFilter<T> f : filters) {
            if(f instanceof AbstractResourceFilter) {
        	if(filter.excludesAnd((AbstractResourceFilter<T>) f)) {
        	    return true;
        	}
            }
        }
        return false;
    }
}