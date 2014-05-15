/**
 * 
 */
package se.uom.vcs.jgit.walker.filter.resource;

import java.io.IOException;
import java.util.Collection;

import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.treewalk.TreeWalk;

import se.uom.vcs.walker.filter.resource.PathFilter;

/**
 * A special case of {@link PathFilter} optimized for JGit library.<p>
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public class JGitPathFilter extends JGitAbstractPathFilter {

    /**
     * Creates a new instance based on the given paths.<p>
     * 
     * @param paths
     */
    public JGitPathFilter(Collection<String> paths) {
	super(paths);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean include(TreeWalk walker) throws MissingObjectException,
	    IncorrectObjectTypeException, IOException {
	byte[] path = walker.getRawPath();
	for(byte[] prefix : paths) {
	    if(prefix.length > 0) {
		if(isPrefix(path, prefix)) {
		    return true;
		}
	    } else {
		if(isRootPath(path)) {
		    return true;
		}
	    }
	}
	return false;
    }
}
