/**
 * 
 */
package se.uom.vcs.jgit.walker;

import java.io.IOException;
import java.util.Collection;

import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.treewalk.TreeWalk;

/**
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public class FileNameFilter extends NameFilter {

    public FileNameFilter(Collection<String> patterns) {
	super(patterns);
    }

    @Override
    public boolean include(TreeWalk walker) throws MissingObjectException,
            IncorrectObjectTypeException, IOException {
        return !walker.isSubtree() && super.include(walker);
    }
}
