/**
 * 
 */
package se.uom.vcs.walker;

import se.uom.vcs.VCSChange;
import se.uom.vcs.VCSCommit;
import se.uom.vcs.VCSResource;
import se.uom.vcs.walker.filter.VCSFilter;
import se.uom.vcs.walker.filter.resource.VCSResourceFilter;

/**
 * A filter visitor used when walking changes between two commits.<p>
 * 
 * {@link #getFilter()} will be the filter for changes.
 * Use {@link #getResourceFilter()} if changes should be limited to some
 * specific resources.
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 * @see VCSCommit#walkChanges(VCSCommit, ChangeVisitor)
 */
public interface ChangeVisitor<T extends VCSChange<?>> extends FilterVisitor<T> {

    /**
     * Return the resource filter.<p>
     * 
     * When walking changes you can filter them to only same specific resources.
     * Use this filter to limit the changes only to the resources this filter allows.
     * <p>
     * For example:
     * <pre>
     *  final PathFilter<VCSResource> filter = 
     * 		new PathFilter<VCSResource>(
     * 			Arrays.asList("path/to/java/MyClass.java"));
     *  commit1.walkChanges(commit2, new ChangeVisitor<VCSChange>(){
     *  
     *  		public VCSFilter<VCSChange> getFilter(){ return null; // no filter here}
     *  
     *  		public VCSResourceFilter<VCSResource> getResourceFilter() { return filter; }
     *  
     *  		public boolean visit(VCSChange entity) {...}
     *  	});
     * </pre>
     * Will return the changes from older commit to new one (it will automatically pick up the old and new one)
     * for the path <code>path/to/java/MyClass.java</code> and each change will be passed to 
     * {@link Visitor#visit(Object)} method.
     * <p>
     * NOTE: it is highly recommended that you filter the results with a filter 
     * (at {@link VCSFilter#include(Object)} level) and not while visiting each entity. That will allow
     * any implementation to perform any optimization, if any, such as discarding all tree entries
     * that can not pass the filter <code>include()</code>. If there is no filter specified and only
     * a certain path is required, then all changes will be calculated.
     * 
     * @return
     * 		the resource filter
     */
    <R extends VCSResource> VCSResourceFilter<R> getResourceFilter();
}
