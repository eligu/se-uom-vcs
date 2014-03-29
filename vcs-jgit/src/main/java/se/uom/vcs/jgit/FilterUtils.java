/**
 * 
 */
package se.uom.vcs.jgit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.eclipse.jgit.revwalk.filter.AndRevFilter;
import org.eclipse.jgit.revwalk.filter.CommitterRevFilter;
import org.eclipse.jgit.revwalk.filter.MaxCountRevFilter;
import org.eclipse.jgit.revwalk.filter.MessageRevFilter;
import org.eclipse.jgit.revwalk.filter.OrRevFilter;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.eclipse.jgit.revwalk.filter.SkipRevFilter;
import org.eclipse.jgit.treewalk.filter.AndTreeFilter;
import org.eclipse.jgit.treewalk.filter.OrTreeFilter;
import org.eclipse.jgit.treewalk.filter.PathFilterGroup;
import org.eclipse.jgit.treewalk.filter.TreeFilter;

import se.uom.vcs.VCSCommit;
import se.uom.vcs.VCSResource;
import se.uom.vcs.jgit.walker.DirNameFilter;
import se.uom.vcs.jgit.walker.FileNameFilter;
import se.uom.vcs.jgit.walker.JGitCommitDateRangeFilter;
import se.uom.vcs.jgit.walker.JGitMergeFilter;
import se.uom.vcs.jgit.walker.JGitPathFilter;
import se.uom.vcs.jgit.walker.OptimizedCommitFilter;
import se.uom.vcs.jgit.walker.OptimizedResourceFilter;
import se.uom.vcs.walker.filter.CommitterFilter;
import se.uom.vcs.walker.filter.MessageFilter;

/**
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public class FilterUtils {

    public OptimizedCommitFilter<?> committerFilter(
	    Collection<String> committersPatterns) {
	
	Set<String> patterns = new CommitterFilter<VCSCommit>(committersPatterns).getPatterns();
	List<RevFilter> filters = new ArrayList<RevFilter>();

	for (String p : patterns) {
	    filters.add(CommitterRevFilter.create(p));
	}
	return new OptimizedCommitFilter<VCSCommit>(OrRevFilter.create(filters));
    }

    public OptimizedCommitFilter<?> commitDateRangeFilter(Date start, Date end) {
	return new OptimizedCommitFilter<VCSCommit>(new JGitCommitDateRangeFilter(start, end));
    }
    
    public OptimizedCommitFilter<?> commitMaxCountFilter(int count) {
	return new OptimizedCommitFilter<VCSCommit>(MaxCountRevFilter.create(count));
    }
    
    public OptimizedCommitFilter<?> commitMergeFilter() {
	return new OptimizedCommitFilter<VCSCommit>(new JGitMergeFilter());
    }
    
    public OptimizedCommitFilter<?> commitMessageFilter(Collection<String> patterns) {
	Set<String> set = new MessageFilter<VCSCommit>(patterns).getPatterns();
	List<RevFilter> filters = new ArrayList<RevFilter>();

	    for (String p : set) {
		filters.add(MessageRevFilter.create(p));
	    }
	    return new OptimizedCommitFilter<VCSCommit>(OrRevFilter.create(filters));
	
    }
    
    public OptimizedCommitFilter<?> commitSkipFilter(int block) {
	return new OptimizedCommitFilter<VCSCommit>(SkipRevFilter.create(block));
    }
    
    public OptimizedCommitFilter<?> commitAndFilter(Collection<OptimizedCommitFilter<?>> filters) {
	List<RevFilter> revFilters = new ArrayList<RevFilter>();
	for(OptimizedCommitFilter<?> f : filters) {
	    revFilters.add(f.getCurrent());
	}
	return new OptimizedCommitFilter<VCSCommit>(AndRevFilter.create(revFilters));
    }
    
    public OptimizedCommitFilter<?> commitOrFilter(Collection<OptimizedCommitFilter<?>> filters) {
	List<RevFilter> revFilters = new ArrayList<RevFilter>();
	for(OptimizedCommitFilter<?> f : filters) {
	    revFilters.add(f.getCurrent());
	}
	return new OptimizedCommitFilter<VCSCommit>(OrRevFilter.create(revFilters));
    }
    
    public OptimizedCommitFilter<?> commitNotFilter(OptimizedCommitFilter<?> filter) {
	return new OptimizedCommitFilter<VCSCommit>(filter.getCurrent().negate());
    }
    
    public OptimizedResourceFilter<?> resourceAndFilter(Collection<OptimizedResourceFilter<?>> filters) {
	List<TreeFilter> revFilters = new ArrayList<TreeFilter>();
	for(OptimizedResourceFilter<?> f : filters) {
	    revFilters.add(f.getCurrent());
	}
	return new OptimizedResourceFilter<VCSResource>(AndTreeFilter.create(revFilters));
    }
    
    public OptimizedResourceFilter<?> resourceNotFilter(OptimizedResourceFilter<?> filter) {
	return new OptimizedResourceFilter<VCSResource>(filter.getCurrent().negate());
    }
    
    public OptimizedResourceFilter<?> resourceOrFilter(Collection<OptimizedResourceFilter<?>> filters) {
	List<TreeFilter> revFilters = new ArrayList<TreeFilter>();
	for(OptimizedResourceFilter<?> f : filters) {
	    revFilters.add(f.getCurrent());
	}
	return new OptimizedResourceFilter<VCSResource>(OrTreeFilter.create(revFilters));
    }
    
    public OptimizedResourceFilter<?> resourcePathFilter(Collection<String> paths) {
	return new OptimizedResourceFilter<VCSResource>(new JGitPathFilter(paths));
    }
    
    public OptimizedResourceFilter<?> resourcePathPrefixFilter(Collection<String> patterns) {
	return new OptimizedResourceFilter<VCSResource>(PathFilterGroup.createFromStrings(patterns));
    }
    
    public OptimizedResourceFilter<?> resourceDirNameFilter(Collection<String> patterns) {
	return new OptimizedResourceFilter<VCSResource>(new DirNameFilter(patterns));
    }
    
    public OptimizedResourceFilter<?> resourceFileNameFilter(Collection<String> patterns) {
   	return new OptimizedResourceFilter<VCSResource>(new FileNameFilter(patterns));
    }
    
    public OptimizedResourceFilter<?> resourceModificationFilter() {
	return new OptimizedResourceFilter<VCSResource>(TreeFilter.ANY_DIFF);
    }
}
