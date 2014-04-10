/**
 * 
 */
package se.uom.vcs.walker.filter;

/**
 * Revert the result of a {@link VCSFilter#include(Object)}.<p>
 * 
 * This is a simple implementation that works in most cases, and
 * will revert the filtering strategy. However there are situations
 * that the opposite of a filter include is not just a NOT operator.
 * 
 * @author Elvis Ligu
 * @since 0.0.1
 * @version 0.0.1
 */
public class VCSNotFilter<T> implements VCSFilter<T> {

    /**
     * The filter to negate the result of include.<p>
     */
    private VCSFilter<T> filter;
    
    /**
     * Create a new filter that will negate the include result of the given filter argument.<p>
     * 
     * @param filter 
     * 		the include result of which will be negated
     */
    public VCSNotFilter(VCSFilter<T> filter) {
	if(filter == null) {
	    throw new IllegalArgumentException("filter must not be null");
	}
	this.filter = filter;
    }
    
    /**
     * @return
     * 		the filter this will revert
     */
    public VCSFilter<T> getFilter() {
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
