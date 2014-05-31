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
 * A filter visitor used when walking changes between two commits.
 * <p>
 * 
 * {@link #getFilter()} will be the filter for changes. Use
 * {@link #getResourceFilter()} if changes should be limited to some specific
 * resources.
 * <p>
 * It is recommended to use resource filter wherever is possible in order to
 * limit the results returned. If the changes for some specific files (be they
 * some .java files under a package or a concrete file) are required then, you
 * must specify a resource filter. That will allow the current implementation to
 * skip diffing for two files that should not be included in the results. Keep
 * in mind that diffing its a very resource hungry process.
 * <p>
 * It is advisable to use very 'clean' resource filter's combinations as this
 * will allow the implementations to make optimizations. For example using a
 * filter that allow all files under a specific path (prefix filter) that have a
 * specific suffix (suffix filter) is a clean filter.
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 * @see VCSCommit#walkChanges(VCSCommit, ChangeVisitor)
 */
public interface ChangeVisitor<T extends VCSChange<?>> extends FilterVisitor<T> {

   /**
    * Return the resource filter.
    * <p>
    * 
    * When walking changes you can filter them to only same specific resources.
    * Use this filter to limit the changes only to the resources this filter
    * allows.
    * <p>
    * For example:
    * 
    * <pre>
    *  final PathFilter&ltVCSResource&gt filter = 
    * 		new PathFilter&ltVCSResource&gt(
    * 			Arrays.asList("path/to/java/MyClass.java"));
    *  commit1.walkChanges(commit2, new ChangeVisitor&ltVCSChange&gt(){
    *  
    *  		public VCSFilter&ltVCSChange&gt getFilter(){ return null; // no filter here}
    *  
    *  		public VCSResourceFilter&ltVCSResource&gt getResourceFilter() { return filter; }
    *  
    *  		public boolean visit(VCSChange entity) {...}
    *  	});
    * </pre>
    * 
    * Will return the changes from older commit to new one (it will
    * automatically pick up the old and new one) for the path
    * <code>path/to/java/MyClass.java</code> and each change will be passed to
    * {@link Visitor#visit(Object)} method.
    * <p>
    * NOTE: it is highly recommended that you filter the results with a filter
    * (at {@link VCSFilter#include(Object)} level) and not while visiting each
    * entity. That will allow any implementation to perform any optimization, if
    * any, such as discarding all tree entries that can not pass the filter
    * <code>include()</code>. If there is no filter specified and only a certain
    * path is required, then all changes will be calculated.
    * 
    * @return the resource filter
    */
   <R extends VCSResource> VCSResourceFilter<R> getResourceFilter();
}
