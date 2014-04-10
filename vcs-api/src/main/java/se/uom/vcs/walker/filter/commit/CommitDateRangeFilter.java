/**
 * 
 */
package se.uom.vcs.walker.filter.commit;

import java.util.Date;

import se.uom.vcs.VCSCommit;

/**
 * Filter commits that are within specified date range.<p>
 * 
 * If <code>start</code> is specified it will filter commits that are after this date including it.
 * If <code>end</code> is specified it will filter commits that are before this date including it.
 * <p>
 * When applied if commit visiting produces a sorted result based on date a visitor must
 * return false as soon as a commit is outside of a given range, in order to stop commit
 * walking as it will not return any important commit (a commit that pass the filtering).
 * <p>
 * Be cautious when using this filter because the local date may differ from the remote
 * one.
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public class CommitDateRangeFilter<T extends VCSCommit> implements VCSCommitFilter<T> {

    /**
     * Include all commits that are after this date including this one.<p>
     * If null all commits before <code>end</code> date will be included.
     */
    private Date start;
    /**
     * Include all commits that are before this date including this one.<p>
     * If null all commits after <code>start</code> date will be included.
     */
    private Date end;
    
    /**
     * Create a new instance to filter the commits based on their dates.<p>
     * 
     * If <code>start</code> is null than all commits before <code>end</code>
     * including it will be included. If <code>end</code> is null all commits
     * after <code>start</code> including it, will be included.
     * <p>
     * At least one date must be specified, and <code>start</code> can not
     * be after <code>end</code>. If they are equal that means this will allow
     * only one commit that is exactly at that date.
     * 
     * @param start
     * 		excludes the commits that are older than. May be null.
     * @param end
     * 		excludes the commits that are newer than. May be null.
     */
    public CommitDateRangeFilter(Date start, Date end) {
	if(start == null && end == null) {
	    throw new IllegalArgumentException("start and end must not be both null");
	}
	if(start != null && end != null && start.after(end)) {
	    throw new IllegalArgumentException("start must be before end");
	}
	this.start = start;
	this.end = end;
    }
    
    /**
     * Get start date.<p>
     * 
     * @return
     * 		the start date. May be null.
     */
    public Date getStart() {
	return this.start;
    }
    
    /**
     * Get end date.<p>
     * 
     * @return
     * 		the end date. May be null.
     */
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
