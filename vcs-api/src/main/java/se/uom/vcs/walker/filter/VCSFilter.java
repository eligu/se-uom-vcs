/**
 * 
 */
package se.uom.vcs.walker.filter;

/**
 * A base interface to filter results when walking either tree or commits.<p>
 * 
 * @author Elvis Ligu
 * @since 0.0.1
 * @version 0.0.1
 */
public interface VCSFilter<T> {

    /**
     * Test the current entity if it should be included in results.<p>
     * 
     * @param entity to test
     * @return true if entity should be included in results
     */
    public boolean include(T entity);
}
