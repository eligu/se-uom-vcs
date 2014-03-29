/**
 * 
 */
package se.uom.vcs.jgit.walker;

import java.io.IOException;
import java.util.Collection;

import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.TreeFilter;

/**
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public class DirNameFilter extends NameFilter {

    public DirNameFilter(Collection<String> patterns) {
	super(patterns);
    }

    /** 
     * {@inheritDoc)
     * @see TreeFilter#include(TreeWalk)
     */
    @Override
    public boolean include(TreeWalk walker) throws MissingObjectException,
	    IncorrectObjectTypeException, IOException {
	return walker.isSubtree() && super.include(walker);
    }
}
