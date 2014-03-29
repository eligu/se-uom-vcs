/**
 * 
 */
package se.uom.vcs.walker.filter;

import se.uom.vcs.VCSCommit;

/**
 * Skip block of commits based on a counter.<p>
 * 
 * When a applied this filter will pick each nth commit while iterating, starting from the first one.
 *  
 * @author Elvis Ligu
 * @since 0.0.1
 * @version 0.0.1
 */
public class SkipFilter<T extends VCSCommit> implements VCSCommitFilter<T> {

    private int block;
    private int counter = 0;
    
    /**
     * Create a new filter that will return true for each nth commit.<p>
     * 
     * @param skip
     * 		the n for which the commits will be skipped		
     */
    public SkipFilter(int skip) {
	if(skip <= 1) {
	    throw new IllegalArgumentException("skip must be grater than 1");
	}
	block = skip;
    }
    
    public int getBlock() {
	return block;
    }
    
    public int getCounter() {
	return counter;
    }
    
    /**
     * {@inheritDoc}
     * For each nth commit return true, where n is the argument specified when this filter was created.
     */
    @Override
    public boolean include(T entity) {
	
	return (counter++ % block) == 0;
    }

    
}
