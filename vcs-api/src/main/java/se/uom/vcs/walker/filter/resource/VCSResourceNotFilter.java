/**
 * 
 */
package se.uom.vcs.walker.filter.resource;

import se.uom.vcs.VCSResource;

/**
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public class VCSResourceNotFilter<T extends VCSResource> extends AbstractResourceFilter<T> {

    protected VCSResourceFilter<T> filter;
    
    private VCSResourceNotFilter() {
	
    }
    
    public static <T extends VCSResource> VCSResourceFilter<T> create(VCSResourceFilter<T> filter) {
	if(filter == null) {
	    throw new IllegalArgumentException("Filter must not be null");
	}
	VCSResourceNotFilter<T> notFilter = null;
	if(filter instanceof AbstractResourceFilter) {
	    
	    notFilter = new NegatedNot<T>();
	    notFilter.filter = ((AbstractResourceFilter<T>) filter).not();
	} else {
	    notFilter = new VCSResourceNotFilter<T>();
	    notFilter.filter = filter;
	}
	return notFilter;
    }
    @Override
    public boolean enter(T resource) {
	return ! filter.enter(resource);
    }

    @Override
    public boolean include(T entity) {
	return ! filter.include(entity);
    }

    @Override
    protected VCSResourceFilter<T> not() {
	return filter;
    }

    private static class NegatedNot<T extends VCSResource> extends VCSResourceNotFilter<T> {
	
	@Override
	public boolean enter(T resource) {
	    return filter.enter(resource);
	}
	
	@Override
	public boolean include(T entity) {
	    return filter.include(entity);
	}
	
	@Override
	protected VCSResourceFilter<T> not() {
	   return ((AbstractResourceFilter<T>)filter).not();
	}
    }

    public VCSResourceFilter<T> getFilter() {
	return this.filter;
    }
}
