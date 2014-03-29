/**
 * 
 */
package se.uom.vcs.walker.filter;

import java.util.Date;

import se.uom.vcs.VCSCommit;

/**
 * Filter commits that are within specified date range.<p>
 * 
 * If <code>start</code> is specified it will filter commits that are after this date including it.
 * If <code>end</code> is specified it will filter commits that are before this date including it.
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public class CommitDateRangeFilter<T extends VCSCommit> implements VCSCommitFilter<T> {

    private Date start;
    private Date end;
    
    public CommitDateRangeFilter(Date start, Date end) {
	if(start == null && end == null) {
	    throw new IllegalArgumentException("start and end must not be both null");
	}
	if(start != null && end != null && start.after(end) && start.equals(end)) {
	    throw new IllegalArgumentException("start must be before end");
	}
	this.start = start;
	this.end = end;
    }
    
    public Date getStart() {
	return this.start;
    }
    
    public Date getEnd() {
	return this.end;
    }
    
    @Override
    public boolean include(T entity) {
	
	boolean include = false;
	Date cDate = entity.getDate();
	
	if(start != null) {
	    include = cDate.after(start) || cDate.equals(start);
	}
	if(end != null) {
	    include = cDate.before(end) || cDate.equals(end);
	}
	
	return include;
    }

}
