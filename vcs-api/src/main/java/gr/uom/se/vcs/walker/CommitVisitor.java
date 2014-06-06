/**
 * 
 */
package gr.uom.se.vcs.walker;

import gr.uom.se.vcs.VCSCommit;
import gr.uom.se.vcs.VCSResource;
import gr.uom.se.vcs.walker.filter.VCSFilter;
import gr.uom.se.vcs.walker.filter.commit.VCSCommitFilter;
import gr.uom.se.vcs.walker.filter.resource.VCSResourceFilter;

/**
 * A visitor used when walking commits, that apply filters.
 * <p>
 * 
 * {@link #getFilter()} will return a commit filter. When walking commits it may
 * be needed to filter only those commits that contains a specified path, or
 * modify this path and so on. Use {@link #getResourceFilter()} to limit only to
 * those commits that has a resource that this filter allows.
 * <p>
 * USAGE: the following snippet shows how to use this visitor with filters.
 * 
 * <pre>
 * ResourceFilterFactory filters = new ResourceFilterFactory();
 *  final VCSResourceFilter&ltVCSResource&gt rfilter = 
 *  	filters.and(
 * 		 filters.prefix("src/main/java/com/package/gui", 
 * 		  	       "src/main/java/com/package/model"),
 * 		 filters.suffix(".java"),
 * 		 filters.modified());
 * 
 *  // Create a date range filter to allow only the commits
 *  // that are between the specified dates
 *  Date d1 = ...;
 *  Date d2 = ...;
 *  final VCSCommitFilter&ltVCSCommit&gt cfilter = new CommitDateRangeFilter&ltVCSCommit&gt(d1, d2);
 *  
 *  commit.walkCommitBack(new CommitVisitor&ltVCSCommit&gt(){
 *  
 *  		public VCSCommitFilter&ltVCSCommit&gt getFilter(){ return cfilter; }
 *  
 *  		public VCSResourceFilter&ltVCSResource&gt getResourceFilter() { return rfilter; }
 *  
 *  		public boolean visit(VCSCommit entity) {...}
 *  	});
 * </pre>
 * 
 * Starting from the current commit will return all previous commits that modify
 * a given .java file under the specified paths, and are between the specified
 * dates. Commits will be passed to {@link Visitor#visit(Object)} method.
 * <p>
 * NOTE: it is highly recommended that you filter the results with a filter (at
 * {@link VCSFilter#include(Object)} level) and not while visiting each entity.
 * This will allow the implementation to perform any optimization, if any, such
 * as early stopping the walk to tree entries that can not pass the filter
 * <code>include()</code>, or early stopping the iteration of commits that are
 * before the <code>start</code> date, or discarding any commit that is not
 * before <code>end</code> date.
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 * @see VCSCommit#walkCommits(CommitVisitor, boolean)
 */
public interface CommitVisitor<T extends VCSCommit> extends FilterVisitor<T> {

   /**
    * Return the resource filter.
    * <p>
    * 
    * @return the resource filter
    */
   <R extends VCSResource> VCSResourceFilter<R> getResourceFilter();

   /**
    * {@inheritDoc} Use a {@link VCSCommitFilter} if the returned commits should
    * be limited. For example:
    * 
    * <pre>
    *  // Create a date range filter to allow only the commits
    *  // that are between the specified dates
    *  Date d1 = ...;
    *  Date d2 = ...;
    *  VCSCommitFilter&ltVCSCommit&gt filter = new CommitDateRangeFilter&ltVCSCommit&gt(d1, d2);
    * </pre>
    * 
    * When used in conjunction with a resource filter then it will limit the
    * results only to those commits that has a resource that can pass
    * <code>include()</code>.
    * <p>
    * <b>WARNING:</b> Implementations has to ensure that this will return a
    * commit filter.
    * 
    * @see VCSCommitFilter
    */
   @SuppressWarnings("unchecked")
   VCSCommitFilter<T> getFilter();
}
