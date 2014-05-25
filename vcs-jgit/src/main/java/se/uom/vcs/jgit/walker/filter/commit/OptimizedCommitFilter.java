package se.uom.vcs.jgit.walker.filter.commit;

import org.eclipse.jgit.revwalk.filter.RevFilter;

import se.uom.vcs.VCSCommit;
import se.uom.vcs.jgit.walker.OptimizedFilter;
import se.uom.vcs.walker.filter.commit.VCSCommitFilter;

public class OptimizedCommitFilter<T extends VCSCommit> extends OptimizedFilter<T> implements VCSCommitFilter<T> {

    private RevFilter current;
    
    public OptimizedCommitFilter(RevFilter filter) {
	this.current = filter;
    }
    
    public RevFilter getCurrent() {
	return current;
    }
    
    public void setCurrent(RevFilter current) {
	this.current = current;
    }
}
