/**
 * 
 */
package se.uom.vcs.walker.filter;

import java.util.Collection;

/**
 * General filter factory interface.<p>
 * 
 * This interface defines methods for filters creation, such as
 * NOT, OR, and AND.
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public interface FilterFactory {

    /**
     * Return a not filter for the specified filter.<p>
     * 
     * @param filter
     * 		to negate the result
     * @return
     * 		a new filter that will be the opposite of the specified one.
     */
    <T, F extends VCSFilter<T>> F not(F filter);
    
    /**
     * Return a OR filter for the specified filters.<p>
     * 
     * @param filters
     * 		to apply OR operator
     * @return
     * 		a new filter that will be an OR result of the specified filters.
     */
    <T, F extends VCSFilter<T>> F or(@SuppressWarnings("unchecked") F... filters);
    
    /**
     * Return a OR filter for the specified filters.<p>
     * 
     * @param filter
     * 		to apply OR operator
     * @return
     * 		a new filter that will be an OR result of the specified filters.
     */
    <T, F extends VCSFilter<T>> F or(Collection<F> filters);
    
    /**
     * Return a AND filter for the specified filters.<p>
     * 
     * @param filters
     * 		to apply AND operator
     * @return
     * 		a new filter that will be an AND result of the specified filters.
     */
    <T, F extends VCSFilter<T>> F and(@SuppressWarnings("unchecked") F... filters);
    
    /**
     * Return a AND filter for the specified filters.<p>
     * 
     * @param filters
     * 		to apply AND operator
     * @return
     * 		a new filter that will be an AND result of the specified filters.
     */
    <T, F extends VCSFilter<T>> F and(Collection<F> filters);
}
