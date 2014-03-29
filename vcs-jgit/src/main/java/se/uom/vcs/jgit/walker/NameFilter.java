/**
 * 
 */
package se.uom.vcs.jgit.walker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.AndTreeFilter;
import org.eclipse.jgit.treewalk.filter.PathSuffixFilter;
import org.eclipse.jgit.treewalk.filter.TreeFilter;

/**
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public abstract class NameFilter extends TreeFilter {

    private TreeFilter filter;

    public NameFilter(Collection<String> patterns) {

	if (patterns == null) {
	    throw new IllegalArgumentException("patterns must not be null");
	}
	if (patterns.isEmpty()) {
	    throw new IllegalArgumentException("patterns must not be empty");
	}
	for (String str : patterns) {
	    if (str == null) {
		throw new IllegalArgumentException(
			"patterns must not contain null");
	    } else if (str.trim().isEmpty()) {
		throw new IllegalArgumentException(
			"patterns must not contain empty string");
	    }
	}

	List<TreeFilter> filters = new ArrayList<TreeFilter>();
	for (String p : patterns) {
	    filters.add(PathSuffixFilter.create(p));
	}
	if(filters.size() == 1) {
	    filter = filters.get(0);
	} else {
	    this.filter = AndTreeFilter.create(filters);
	}
    }
    
    /** 
     * {@inheritDoc)
     * @see TreeFilter#include(TreeWalk)
     */
    @Override
    public boolean include(TreeWalk walker) throws MissingObjectException,
	    IncorrectObjectTypeException, IOException {
	return filter.include(walker);
    }

    /**
     * {@inheritDoc)
     * @see TreeFilter#shouldBeRecursive()
     */
    @Override
    public boolean shouldBeRecursive() {
	return filter.shouldBeRecursive();
    }

    /**
     * {@inheritDoc)
     * @see TreeFilter#clone()
     */
    @Override
    public TreeFilter clone() {
	return filter.clone();
    }

}
