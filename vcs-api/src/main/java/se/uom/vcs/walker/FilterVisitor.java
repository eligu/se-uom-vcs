/**
 * 
 */
package se.uom.vcs.walker;

import se.uom.vcs.walker.filter.VCSFilter;

/**
 * A visitor that will filter its results based on a {@link VCSFilter}.<p>
 * 
 * This is a base interface for all types of visitors that are used
 * in different places of this API.
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 * @see ChangeVisitor
 * @see CommitVisitor
 * @see ResourceVisitor
 */
public interface FilterVisitor<T> extends Visitor<T> {

    /**
     * Get the filter of this visitor.<p>
     * 
     * This filter will be applied to each entity before {@link #visit(T)}
     * will be included.
     * 
     * @return
     * 		the filter of this visitor
     */
    <F extends VCSFilter<T>> F getFilter();
}
