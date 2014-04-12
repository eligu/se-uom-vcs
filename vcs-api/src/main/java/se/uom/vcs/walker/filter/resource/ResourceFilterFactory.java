/**
 * 
 */
package se.uom.vcs.walker.filter.resource;

import java.util.Arrays;
import java.util.Collection;

import se.uom.vcs.VCSResource;
import se.uom.vcs.VCSResource.Type;

/**
 * Utility for creating filters.<p>
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public class ResourceFilterFactory {

    /**
     * Creates a new path filter, given the specified paths.<p>
     * 
     * @param paths a collection of paths. 
     * 	      Must not be null, empty or contain any null path.
     * @return a new path filter
     * @see PathFilter
     */
    public <T extends VCSResource> VCSResourceFilter<T> path(
	    Collection<String> paths) {
	return new PathFilter<T>(paths);
    }

    /**
     * Creates a new path filter given the specified paths.<p>
     * 
     * @param paths an array of paths.
     * 	      Must not be null, empty or contain any null path.
     * @return a new path filter
     * @see PathFilter
     */
    public <T extends VCSResource> VCSResourceFilter<T> path(String... paths) {
	return new PathFilter<T>(Arrays.asList(paths));
    }

    /**
     * Creates a new suffix filter given the specified paths.<p>
     * 
     * @param paths a collection of suffixes.
     *        Must not be null, empty or contain any null element.
     * @return a new suffix filter
     * @see SuffixFilter
     */
    public <T extends VCSResource> VCSResourceFilter<T> suffix(
	    Collection<String> paths) {
	return new SuffixFilter<T>(paths);
    }

    /**
     * Creates a new suffix filter given the specified paths.<p>
     * 
     * @param paths an array of suffixes.
     *        Must not be null, empty or contain any null element.
     * @return a new suffix filter
     * @see SuffixFilter
     */
    public <T extends VCSResource> VCSResourceFilter<T> suffix(String... paths) {
	return new SuffixFilter<T>(Arrays.asList(paths));
    }

    /**
     * Creates a new child filter given the specified paths.<p>
     * 
     * @param paths a collection of paths.
     *        Must not be null, empty or contain any null element.
     * @return a new child filter
     * @see ChildFilter
     */
    public <T extends VCSResource> VCSResourceFilter<T> childFilter(Collection<String> paths) {
	return new ChildFilter<T>(paths);
    }
    
    /**
     * Creates a new child filter given the specified paths.<p>
     * 
     * @param paths an array of paths.
     *        Must not be null, empty or contain any null element.
     * @return a new child filter
     * @see ChildFilter
     */
    public <T extends VCSResource> VCSResourceFilter<T> childFilter(String... paths) {
	return childFilter(Arrays.asList(paths));
    }
    
    /**
     * Creates a new prefix filter given the specified paths.<p>
     * 
     * @param paths a collection of paths.
     *        Must not be null, empty or contain any null element.
     * @return a new prefix filter
     * @see PathPrefixFilter
     */
    public <T extends VCSResource> VCSResourceFilter<T> prefix(
	    Collection<String> paths) {
	return new PathPrefixFilter<T>(paths);
    }

    /**
     * Creates a new prefix filter given the specified paths.<p>
     * 
     * @param paths an array of paths.
     *        Must not be null, empty or contain any null element.
     * @return a new prefix filter
     * @see PathPrefixFilter
     */
    public <T extends VCSResource> VCSResourceFilter<T> prefix(String... paths) {
	return new PathPrefixFilter<T>(Arrays.asList(paths));
    }

    /**
     * Creates a new AND filter given the specified filters.<p>
     * 
     * @param filters a collection of filters.
     *        Must not be null, empty or contain any null element.
     * @return a new AND filter
     * @see VCSResourceAndFilter
     */
    public <T extends VCSResource> VCSResourceFilter<T> and(
	    Collection<VCSResourceFilter<T>> filters) {
	
	for (VCSResourceFilter<T> f : filters) {
	    if(f == null) {
		throw new IllegalArgumentException("filters must not contain null");
	    }
	}
	if(filters.size() == 1) {
	    return filters.iterator().next();
	}
	
	return new VCSResourceAndFilter<T>(filters);
    }

    /**
     * Creates a new AND filter given the specified filters.<p>
     * 
     * @param filters an array of filters.
     *        Must not be null, empty or contain any null element.
     * @return a new AND filter
     * @see VCSResourceAndFilter
     */
    public <T extends VCSResource> VCSResourceFilter<T> and(
	    @SuppressWarnings("unchecked") VCSResourceFilter<T>... filters) {
	return this.and(Arrays.asList(filters));
    }

    /**
     * Creates a new OR filter given the specified filters.<p>
     * 
     * @param filters a collection of filters.
     *        Must not be null, empty or contain any null element.
     * @return a new OR filter
     * @see VCSResourceOrFilter
     */
    public <T extends VCSResource> VCSResourceFilter<T> or(
	    Collection<VCSResourceFilter<T>> filters) {
	for(Object o : filters) {
	    if(o == null) {
		throw new IllegalArgumentException("filters must not contain null");
	    }
	}
	if(filters.size() == 1) {
	    return filters.iterator().next();
	}
	return new VCSResourceOrFilter<T>(filters);
    }

    /**
     * Creates a new OR filter given the specified filters.<p>
     * 
     * @param filters an array of filters.
     *        Must not be null, empty or contain any null element.
     * @return a new OR filter
     * @see VCSResourceOrFilter
     */
    public <T extends VCSResource> VCSResourceFilter<T> or(
	    @SuppressWarnings("unchecked") VCSResourceFilter<T>... filters) {
	return new VCSResourceOrFilter<T>(Arrays.asList(filters));
    }

    /**
     * Creates a new filter that checks if a resource is modified.<p>
     * 
     * @return
     * 	 	new ModifiedFilter
     * @see ModifiedFilter
     */
    public VCSResourceFilter<VCSResource> modified() {
	return new ModifiedFilter<VCSResource>();
    }

    /**
     * Creates a new type filter given the type.<p>
     * 
     * @param type must not be null
     * @return a new type filter
     * @see TypeFilter
     */
    public VCSResourceFilter<VCSResource> type(Type type) {
	return new TypeFilter<VCSResource>(type);
    }
}
