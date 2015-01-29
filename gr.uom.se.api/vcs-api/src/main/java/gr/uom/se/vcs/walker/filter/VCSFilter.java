/**
 * 
 */
package gr.uom.se.vcs.walker.filter;

import gr.uom.se.vcs.walker.Visitor;
import gr.uom.se.vcs.walker.filter.commit.VCSCommitFilter;
import gr.uom.se.vcs.walker.filter.resource.VCSResourceFilter;

/**
 * A base interface to filter results when using a visitor.
 * <p>
 * 
 * There are two extensions to this interface {@link VCSResourceFilter} and
 * {@link VCSCommitFilter}. When only a subset of results is required it is
 * highly recommended to use a filter to limit the results and not rely on
 * {@link Visitor#visit(Object)} method, because that should allow
 * implementations to make any optimization, such as discarding any tree entries
 * that are unnecessary when walking resources or discarding commits that can
 * not pass <code>include()</code> method.
 * 
 * @author Elvis Ligu
 * @since 0.0.1
 * @version 0.0.1
 * @see VCSCommitFilter
 * @see VCSResourceFilter
 */
public interface VCSFilter<T> {

   /**
    * Test the current entity if it should be included in results.
    * <p>
    * 
    * @param entity
    *           to test
    * @return true if entity should be included in results
    */
   public boolean include(T entity);
}
