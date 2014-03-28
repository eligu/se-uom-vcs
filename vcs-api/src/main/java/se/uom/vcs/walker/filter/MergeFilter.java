/**
 * 
 */
package se.uom.vcs.walker.filter;

import se.uom.vcs.VCSCommit;

/**
 * Filter commits that comes from a merge result.<p>
 * 
 * Usually commits that are merge results have two or more parents.
 * However this depends on implementation of {@link VCSCommit#isMergeCommit()}.
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public class MergeFilter<T extends VCSCommit> implements VCSCommitFilter<T> {

    @Override
    public boolean include(T entity) {
	return entity.isMergeCommit();
    }
}
