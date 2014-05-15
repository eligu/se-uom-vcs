package se.uom.vcs.jgit.walker.filter.resource;

import org.eclipse.jgit.treewalk.filter.TreeFilter;

import se.uom.vcs.VCSResource;
import se.uom.vcs.jgit.walker.OptimizedFilter;
import se.uom.vcs.walker.filter.resource.VCSResourceFilter;

/**
 * An optimized version of a {@link VCSResourceFilter}.<p>
 * 
 * This filter will contain a tree filter implementation of JGit library that corresponds,
 * to a filter type of {@link VCSResourceFilter}.
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 * @param <T>
 */
public class OptimizedResourceFilter<T extends VCSResource> extends OptimizedFilter<T> implements VCSResourceFilter<T> {

    /**
     * The tree filter.<p>
     */
    private TreeFilter current;
    
    /**
     * Creates a new instance of filter.<p>
     * @param filter
     */
    public OptimizedResourceFilter(TreeFilter filter) {
	this.current = filter;
    }
    
    /**
     * @return the JGit filter
     */
    public TreeFilter getCurrent() {
	return current;
    }
    
    /**
     * Set the JGit filter.<p>
     * 
     * @param current
     */
    public void setCurrent(TreeFilter current) {
	this.current = current;
    }

    /**
     * Not used method.<p>
     */
    @Override
    public boolean include(T entity) {
	return false;
    }

    /**
     * Not used method.<p>
     */
    @Override
    public boolean enter(T resource) {
	return false;
    }
}
