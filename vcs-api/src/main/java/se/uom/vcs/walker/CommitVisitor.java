/**
 * 
 */
package se.uom.vcs.walker;

import se.uom.vcs.VCSCommit;
import se.uom.vcs.VCSResource;
import se.uom.vcs.walker.filter.VCSFilter;
import se.uom.vcs.walker.filter.commit.VCSCommitFilter;
import se.uom.vcs.walker.filter.resource.VCSResourceFilter;

/**
 * A visitor used when walking commits, that apply filters.<p>
 * 
 * {@link #getFilter()} will return a commit filter. When walking commits
 * it may be needed to filter only those commits that contains a specified
 * path, or modify this path and so on. Use {@link #getResourceFilter()}
 * to limit only to those commits that has a resource that this filter allows.
 * <p>
 * USAGE: the following snippet shows how to use this visitor with filters.
 * <pre>
 * ResourceFilterFactory filters = new ResourceFilterFactory();
 *  final VCSResourceFilter<VCSResource> rfilter = 
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
 *  final VCSCommitFilter<VCSCommit> cfilter = new CommitDateRangeFilter<VCSCommit>(d1, d2);
 *  
 *  commit.walkCommitBack(new CommitVisitor<VCSCommit>(){
 *  
 *  		public VCSCommitFilter<VCSCommit> getFilter(){ return cfilter; // no filter here}
 *  
 *  		public VCSResourceFilter<VCSResource> getResourceFilter() { return rfilter; }
 *  
 *  		public boolean visit(VCSCommit entity) {...}
 *  	});
 * </pre>
 * Starting from the current commit will return all previous commits that modify a given .java
 * file under the specified paths, and are between the specified dates. 
 * Commits will be passed to {@link Visitor#visit(Object)} method.
 * <p>
 * NOTE: it is highly recommended that you filter the results with a filter 
 * (at {@link VCSFilter#include(Object)} level) and not while visiting each entity. That will allow
 * any implementation to perform any optimization, if any, such as early stopping the walk to tree entries
 * that can not pass the filter <code>include()</code>, or early stopping the iteration of commits that are
 * before the <code>start</code> date, or discarding any commit that is not before <code>end</code> date.
 *
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 * @see VCSCommit#walkCommitBack(CommitVisitor)
 */
public interface CommitVisitor<T extends VCSCommit> extends FilterVisitor<T> {

    /**
     * Return the resource filter.<p>
     * 
     * When walking commits a visitor may be interesting to those that modify,
     * or contain a certain path. Use this filter so if the current commit
     * contains a resource that is allowed by this filter it will be included in results. 
     * 
     * For example:
     * <pre>
     *  ResourceFilterFactory filters = new ResourceFilterFactory();
     *  final VCSResourceFilter<VCSResource> filter = 
     *  	filters.and(
     * 		 filters.prefix("src/main/java/com/package/gui", 
     * 		  	       "src/main/java/com/package/model"),
     * 		 filters.suffix(".java"),
     * 		 filters.modified());
     *  
     *  commit.walkCommitBack(new CommitVisitor<VCSCommit>(){
     *  
     *  		public VCSCommitFilter<VCSCommit> getFilter(){ return null; // no filter here}
     *  
     *  		public VCSResourceFilter<VCSResource> getResourceFilter() { return rfilter; }
     *  
     *  		public boolean visit(VCSCommit entity) {...}
     *  	});
     * </pre>
     * Starting from the current commit will return all previous commits that modify a given .java
     * file under the specified paths. Commits will be passed to {@link Visitor#visit(Object)} method.
     * <p>
     * NOTE: it is highly recommended that you filter the results with a filter 
     * (at {@link VCSFilter#include(Object)} level) and not while visiting each entity. That will allow
     * any implementation to perform any optimization, if any, such as discarding all tree entries
     * that can not pass the filter <code>include()</code>. If there is no filter specified and only
     * a certain path is required, then all commits will be calculated.
     * 
     * @return
     * 		the resource filter
     */
    <R extends VCSResource> VCSResourceFilter<R> getResourceFilter();
    
    /**
     * {@inheritDoc}
     * Use a {@link VCSCommitFilter} if the returned commits should be limited.
     * For example:
     * <pre>
     *  // Create a date range filter to allow only the commits
     *  // that are between the specified dates
     *  Date d1 = ...;
     *  Date d2 = ...;
     *  VCSCommitFilter<VCSCommit> filter = new CommitDateRangeFilter<VCSCommit>(d1, d2);
     * </pre>
     * When used in conjunction with a resource filter then it will limit the results only
     * to those commits that has a resource that can pass <code>include()</code>.
     * <p>
     * <b>WARNING:</b> Implementations has to ensure that this will return a commit filter.
     * 
     * @see VCSCommitFilter
     */
    @SuppressWarnings("unchecked")
    VCSCommitFilter<T> getFilter();
}
