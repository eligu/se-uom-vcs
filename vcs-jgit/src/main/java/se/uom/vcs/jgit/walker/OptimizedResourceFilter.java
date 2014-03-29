package se.uom.vcs.jgit.walker;

import org.eclipse.jgit.treewalk.filter.TreeFilter;

import se.uom.vcs.VCSResource;
import se.uom.vcs.walker.filter.VCSResourceFilter;

public class OptimizedResourceFilter<T extends VCSResource> extends OptimizedFilter<T> implements VCSResourceFilter<T> {

    private TreeFilter current;
    
    public OptimizedResourceFilter(TreeFilter filter) {
	this.current = filter;
    }
    
    public TreeFilter getCurrent() {
	return current;
    }
    
    public void setCurrent(TreeFilter current) {
	this.current = current;
    }
}
