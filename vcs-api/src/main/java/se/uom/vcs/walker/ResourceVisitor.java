/**
 * 
 */
package se.uom.vcs.walker;

import se.uom.vcs.VCSResource;
import se.uom.vcs.walker.filter.resource.VCSResourceFilter;

/**
 * A visitor used when walking resources, that apply resource filters.<p>
 * 
 * {@link #getFilter()} will return a {@link VCSResourceFilter} that will be used
 * to filter the results.
 * <p>
 * When walking resources keep in mind that the process requires a lot of
 * processing power, and it is highly recommended that you use a resource
 * filter if only a certain resources are required. Do not filter the
 * results at {@link Visitor#visit(Object)} level, because it will not 
 * allow the implementation to make any optimization, otherwise for a certain
 * types of resources that are under some specified paths the whole tree
 * will be parsed.
 *  
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public interface ResourceVisitor<T extends VCSResource> extends FilterVisitor<T> {

    /**
     * {@inheritDoc}
     * <p>
     * <b>WARNING:</b> Implementations has to ensure that this will return a resource filter.
     * 
     * @see VCSResourceFilter
     */
    @SuppressWarnings("unchecked")
    VCSResourceFilter<T> getFilter();
}
