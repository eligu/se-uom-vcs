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

import se.uom.vcs.VCSResource;
import se.uom.vcs.walker.filter.PathFilter;

/**
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public class JGitPathFilter extends TreeFilter {

    private PathFilter<VCSResource> filter;
    
    public JGitPathFilter(Collection<String> paths) {
	this.filter = new PathFilter<VCSResource>(paths);
    }
    
    @Override
    public boolean include(TreeWalk walker) throws MissingObjectException,
	    IncorrectObjectTypeException, IOException {
	return this.filter.containsPath(walker.getPathString());
    }

    @Override
    public boolean shouldBeRecursive() {
	return false;
    }

    @Override
    public TreeFilter clone() {
	return this;
    }
}
