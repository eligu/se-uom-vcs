/**
 * 
 */
package se.uom.vcs.jgit.walker.filter.resource;

import java.io.IOException;

import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.TreeFilter;

import se.uom.vcs.VCSResource;
import se.uom.vcs.jgit.utils.RevUtils;

/**
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public class JGitTypeFilter extends TreeFilter {

    protected VCSResource.Type type;
    
    /**
     * 
     */
    public JGitTypeFilter(VCSResource.Type type) {
	if(type == null) {
	    throw new IllegalArgumentException("type must not be null");
	}
	this.type = type;
    }

    /**
     *  {@inheritDoc)
     * @see TreeFilter#include(TreeWalk)
     */
    @Override
    public boolean include(TreeWalk walker) throws MissingObjectException,
	    IncorrectObjectTypeException, IOException {
	return RevUtils.resourceType(walker.getFileMode(0)).equals(type);
    }

    /**
     *  {@inheritDoc)
     * @see TreeFilter#shouldBeRecursive()
     */
    @Override
    public boolean shouldBeRecursive() {
	return false;
    }

    /**
     *  {@inheritDoc)
     * @see TreeFilter#clone()
     */
    @Override
    public TreeFilter clone() {
	return this;
    }

}
