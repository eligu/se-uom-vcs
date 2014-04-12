/**
 * 
 */
package se.uom.vcs.jgit.walker;

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

import se.uom.vcs.VCSCommit;
import se.uom.vcs.walker.filter.commit.CommitDateRangeFilter;
import se.uom.vcs.walker.filter.commit.CommitterFilter;
import se.uom.vcs.walker.filter.commit.MaxCounterFilter;
import se.uom.vcs.walker.filter.commit.MergeFilter;
import se.uom.vcs.walker.filter.commit.MessageFilter;
import se.uom.vcs.walker.filter.commit.SkipFilter;
import se.uom.vcs.walker.filter.commit.VCSCommitAndFilter;
import se.uom.vcs.walker.filter.commit.VCSCommitFilter;
import se.uom.vcs.walker.filter.commit.VCSCommitNotFilter;
import se.uom.vcs.walker.filter.commit.VCSCommitOrFilter;

/**
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public class CommitFilter {

    public OptimizedCommitFilter<VCSCommit> committer(
	    Collection<String> committersPatterns) {

	Set<String> patterns = new CommitterFilter<VCSCommit>(
		committersPatterns).getPatterns();
	List<RevFilter> filters = new ArrayList<RevFilter>();

	for (String p : patterns) {
	    filters.add(CommitterRevFilter.create(p));
	}
	return new OptimizedCommitFilter<VCSCommit>(OrRevFilter.create(filters));
    }

    public OptimizedCommitFilter<VCSCommit> range(Date start, Date end) {
	return new OptimizedCommitFilter<VCSCommit>(
		new JGitCommitDateRangeFilter(start, end));
    }

    public OptimizedCommitFilter<VCSCommit> count(int count) {
	return new OptimizedCommitFilter<VCSCommit>(
		MaxCountRevFilter.create(count));
    }

    public OptimizedCommitFilter<VCSCommit> merge() {
	return new OptimizedCommitFilter<VCSCommit>(new JGitMergeFilter());
    }

    public OptimizedCommitFilter<VCSCommit> message(Collection<String> patterns) {
	Set<String> set = new MessageFilter<VCSCommit>(patterns).getPatterns();
	List<RevFilter> filters = new ArrayList<RevFilter>();

	for (String p : set) {
	    filters.add(MessageRevFilter.create(p));
	}
	return new OptimizedCommitFilter<VCSCommit>(OrRevFilter.create(filters));

    }

    public OptimizedCommitFilter<VCSCommit> skip(int block) {
	return new OptimizedCommitFilter<VCSCommit>(SkipRevFilter.create(block));
    }

    public OptimizedCommitFilter<VCSCommit> and(
	    Collection<OptimizedCommitFilter<VCSCommit>> filters) {
	List<RevFilter> revFilters = new ArrayList<RevFilter>();
	for (OptimizedCommitFilter<?> f : filters) {
	    revFilters.add(f.getCurrent());
	}
	return new OptimizedCommitFilter<VCSCommit>(
		AndRevFilter.create(revFilters));
    }

    public OptimizedCommitFilter<VCSCommit> or(
	    Collection<OptimizedCommitFilter<VCSCommit>> filters) {
	List<RevFilter> revFilters = new ArrayList<RevFilter>();
	for (OptimizedCommitFilter<?> f : filters) {
	    revFilters.add(f.getCurrent());
	}
	return new OptimizedCommitFilter<VCSCommit>(
		OrRevFilter.create(revFilters));
    }

    public OptimizedCommitFilter<VCSCommit> not(OptimizedCommitFilter<?> filter) {
	return new OptimizedCommitFilter<VCSCommit>(filter.getCurrent()
		.negate());
    }

    public OptimizedCommitFilter<VCSCommit> parse(VCSCommitFilter<VCSCommit> filter) {
	
	if(OptimizedCommitFilter.class.isAssignableFrom(filter.getClass())) {
	    return (OptimizedCommitFilter<VCSCommit>) filter;
	}
	
	if(CommitDateRangeFilter.class.isAssignableFrom(filter.getClass())) {
	    return this.parseRange((CommitDateRangeFilter<VCSCommit>) filter);
	}
	
	if(CommitterFilter.class.isAssignableFrom(filter.getClass())) {
	    return this.parseCommitter((CommitterFilter<VCSCommit>) filter);
	}
	
	if(MaxCounterFilter.class.isAssignableFrom(filter.getClass())) {
	    return this.parseCount((MaxCounterFilter<VCSCommit>) filter);
	}
	
	if(MergeFilter.class.isAssignableFrom(filter.getClass())) {
	    return this.parseMerge((MergeFilter<VCSCommit>) filter);
	}
	
	if(MessageFilter.class.isAssignableFrom(filter.getClass())) {
	    return this.parseMessage((MessageFilter<VCSCommit>) filter);
	}
	
	if(SkipFilter.class.isAssignableFrom(filter.getClass())) {
	    return this.parseSkip((SkipFilter<VCSCommit>) filter);
	}
	
	if(VCSCommitNotFilter.class.isAssignableFrom(filter.getClass())) {
	    return this.parseNot((VCSCommitNotFilter<VCSCommit>) filter);
	}
	
	if(VCSCommitAndFilter.class.isAssignableFrom(filter.getClass())) {
	    return this.parseAnd((VCSCommitAndFilter<VCSCommit>) filter);
	}
	
	if(VCSCommitOrFilter.class.isAssignableFrom(filter.getClass())) {
	    return this.parseOr((VCSCommitOrFilter<VCSCommit>) filter);
	}
	
	return null;
    }
    
    public OptimizedCommitFilter<VCSCommit> parseRange(CommitDateRangeFilter<VCSCommit> filter) {
	return this.range(filter.getStart(), filter.getEnd());
    }
    
    public OptimizedCommitFilter<VCSCommit> parseCommitter(CommitterFilter<VCSCommit> filter) {
	return this.committer(filter.getPatterns());
    }
    
    public OptimizedCommitFilter<VCSCommit> parseCount(MaxCounterFilter<VCSCommit> filter) {
	return this.count(filter.getSize());
    }
    
    public OptimizedCommitFilter<VCSCommit> parseMerge(MergeFilter<VCSCommit> filter) {
	return this.merge();
    }
    
    public OptimizedCommitFilter<VCSCommit> parseMessage(MessageFilter<VCSCommit> filter) {
	return this.message(filter.getPatterns());
    }
    
    public OptimizedCommitFilter<VCSCommit> parseSkip(SkipFilter<VCSCommit> filter) {
	return this.skip(filter.getBlock());
    }

    public OptimizedCommitFilter<VCSCommit> parseAnd(
	    VCSCommitAndFilter<VCSCommit> filter) {

	Set<VCSCommitFilter<VCSCommit>> filters = filter.getFilters();
	List<OptimizedCommitFilter<VCSCommit>> oFilters = new ArrayList<OptimizedCommitFilter<VCSCommit>>();

	for (VCSCommitFilter<VCSCommit> f : filters) {
	    OptimizedCommitFilter<VCSCommit> of = this.parse(f);
	    if (of != null) {
		oFilters.add(of);
	    } else {
		return null;
	    }
	}

	if (oFilters.isEmpty()) {
	    return null;
	} else if (oFilters.size() == 1) {
	    return oFilters.get(0);
	} else {
	    return this.and(oFilters);
	}
    }

    public OptimizedCommitFilter<VCSCommit> parseOr(
	    VCSCommitOrFilter<VCSCommit> filter) {

	Set<VCSCommitFilter<VCSCommit>> filters = filter.getFilters();
	List<OptimizedCommitFilter<VCSCommit>> oFilters = new ArrayList<OptimizedCommitFilter<VCSCommit>>();

	for (VCSCommitFilter<VCSCommit> f : filters) {
	    OptimizedCommitFilter<VCSCommit> of = this.parse(f);
	    if (of != null) {
		oFilters.add(of);
	    } else {
		return null;
	    }
	}

	if (oFilters.isEmpty()) {
	    return null;
	} else if (oFilters.size() == 1) {
	    return oFilters.get(0);
	} else {
	    return this.or(oFilters);
	}
    }

    public OptimizedCommitFilter<VCSCommit> parseNot(VCSCommitNotFilter<VCSCommit> filter) {
	OptimizedCommitFilter<VCSCommit> f = this.parse(filter.getFilter());
	if(f != null) {
	    return this.not(f);
	}
	return null;
    }
}
