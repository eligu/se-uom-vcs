/**
 * 
 */
package se.uom.vcs.jgit.walker;

import java.io.IOException;
import java.util.Date;

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
public class JGitCommitDateRangeFilter extends RevFilter {

    private Date start;
    private Date end;
    
    public JGitCommitDateRangeFilter(Date start, Date end) {
	if(start == null && end == null) {
	    throw new IllegalArgumentException("start and end must not be both null");
	}
	if(start != null && end != null && start.after(end) && start.equals(end)) {
	    throw new IllegalArgumentException("start must be before end");
	}
	this.start = start;
	this.end = end;
    }
    
    @Override
    public RevFilter clone() {
	return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean include(RevWalk walk, RevCommit cmit)
	    throws StopWalkException, MissingObjectException,
	    IncorrectObjectTypeException, IOException {
	
	boolean include = false;
	Date cDate = cmit.getCommitterIdent().getWhen();
	
	if(start != null) {
	    include = cDate.after(start) || cDate.equals(start);
	}
	if(end != null) {
	    include = cDate.before(end) || cDate.equals(end);
	}
	
	return include;
    }

}
