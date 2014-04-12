/**
 * 
 */
package se.uom.vcs.walker.filter.resource;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import se.uom.vcs.VCSResource;

/**
 * Check whether any of filters include true to return the result.
 * <p>
 * 
 * @author Elvis Ligu
 * @since 0.0.1
 * @version 0.0.1
 */
public class VCSResourceOrFilter<T extends VCSResource> extends AbstractResourceFilter<T> implements VCSResourceFilter<T> {

    private Set<VCSResourceFilter<T>> filters;

    /**
     * Create a new filter that will check if any of the given filters returns
     * true.
     * <p>
     * 
     * @param filters
     *            the filters to check if any of the filters returns true. Must
     *            not be null not or empty or contain a null filter.
     */
    public VCSResourceOrFilter(Collection<VCSResourceFilter<T>> filters) {
	if (filters == null) {
	    throw new IllegalArgumentException("filters must not be null");
	}
	if (filters.isEmpty()) {
	    throw new IllegalArgumentException("filters must not be empty");
	}

	this.filters = new LinkedHashSet<VCSResourceFilter<T>>();

	for (VCSResourceFilter<T> f : filters) {
	    if (f == null) {
		throw new IllegalArgumentException(
			"filters must not contain null");
	    }
	    this.extract(f);
	}
	
	boolean exact = false;
	boolean path = false;
	for(VCSResourceFilter<T> f : filters) {
	    if(f instanceof ExactFilter) {
		exact = true;
		if(path) {
		    throw new IllegalArgumentException("exact filter " + f + " can not combine with a path filter");
		}
	    } else if (f instanceof AbstractPathFilter) {
		path = true;
		if(exact) {
		    throw new IllegalArgumentException("path filter " + f + " can not be combined with an exact filter");
		}
	    }
	}
    }

    private void extract(VCSResourceFilter<T> filter) {
	if (this.getClass().isAssignableFrom(filter.getClass())) {

	    for (VCSResourceFilter<T> f : ((VCSResourceOrFilter<T>) filter).filters) {
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
     * @return the filters
     */
    public Set<VCSResourceFilter<T>> getFilters() {
	return this.filters;
    }

    /**
     * {@inheritDoc} Returns true if any of the specified filters (during
     * creation) returns true.
     * <p>
     */
    @Override
    public boolean include(T entity) {

	for (VCSResourceFilter<T> f : filters) {
	    if (f.include(entity)) {
		return true;
	    }
	}
	return false;
    }
    
    @Override
    public boolean enter(T resource) {
        // Allow only if one of the supplied filters allow this
	for(VCSResourceFilter<T> f : filters) {
	    if(f.enter(resource)) {
		return true;
	    }
	}
        return false;
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
        // An or filter can be combined with all filters, but the same filter
        return this.equals(filter);
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((filters == null) ? 0 : filters.hashCode());
	return result;
    }

    @Override
    public boolean equals(Object obj) {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (getClass() != obj.getClass())
	    return false;
	@SuppressWarnings("unchecked")
	VCSResourceOrFilter<T> other = (VCSResourceOrFilter<T>) obj;
	if (filters == null) {
	    if (other.filters != null)
		return false;
	} else if (!filters.equals(other.filters))
	    return false;
	return true;
    }
    
}