/**
 * 
 */
package se.uom.vcs.jgit.walker.filter.commit;

import java.io.IOException;

import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.errors.StopWalkException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.filter.RevFilter;

/**
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public class JGitMergeFilter extends RevFilter {

    /**
     * {@inheritDoc)
     * @see RevFilter#include(RevWalk, RevCommit)
     */
    @Override
    public boolean include(RevWalk walker, RevCommit cmit)
	    throws StopWalkException, MissingObjectException,
	    IncorrectObjectTypeException, IOException {
	
	return cmit.getParentCount() > 1;
    }

    /** 
     * {@inheritDoc)
     * @see org.eclipse.jgit.revwalk.filter.RevFilter#clone()
     */
    @Override
    public RevFilter clone() {
	return this;
    }

}
