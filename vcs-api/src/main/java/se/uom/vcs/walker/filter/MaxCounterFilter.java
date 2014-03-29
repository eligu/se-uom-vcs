/**
 * 
 */
package se.uom.vcs.walker.filter;

import se.uom.vcs.VCSCommit;

/**
 * Select the first nth commits.<p>
 * 
 * When applied this filter will return true only for the first n commits and false for others.
 * n argument is specified in constructor.
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public class MaxCounterFilter<T extends VCSCommit> implements VCSCommitFilter<T> {

    private int size;
    private int counter = 0;
    
    /**
     * Creates a new filter that will filter the first nth commits.<p>
     * 
     * @param size
     * 		the first commits to filter
     */
    public MaxCounterFilter(int size) {
	if(size <= 0) {
	    throw new IllegalArgumentException("size must be greater than 0");
	}
	this.size = size;
    }
    
    public int getSize() {
	return size;
    }
    
    /**
     * {@inheritDoc}
     * Return true for the first nth commits.<p>
     */
    @Override
    public boolean include(T entity) {
	
	return counter++ < size;
    }

}
