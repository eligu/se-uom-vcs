/**
 * 
 */
package se.uom.vcs.walker;

import se.uom.vcs.VCSCommit;
import se.uom.vcs.VCSResource;
import se.uom.vcs.walker.filter.VCSCommitFilter;
import se.uom.vcs.walker.filter.VCSResourceFilter;

/**
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public interface FilteredVisitor<T> extends Visitor<T> {

    public <C extends VCSCommit> VCSCommitFilter<C> getCommitFilter();
    
    public <R extends VCSResource> VCSResourceFilter<R> getResourceFilter();
}
