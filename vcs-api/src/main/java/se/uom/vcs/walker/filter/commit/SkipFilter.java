/**
 * 
 */
package se.uom.vcs.walker.filter.commit;

import se.uom.vcs.VCSCommit;

/**
 * Skip block of commits based on a counter.<p>
 * 
 * When applied this filter will pick each nth commit while iterating, starting from the first one.
 * Generally speaking, querying information from every nth commit of a repository comes in
 * handy when someone needs to create reports based on each nth commit, and this makes
 * sense only if the commit walking is sorted. However when walking over commits the 
 * implementation is not required to return them ordered.
 * 
 * in any order, thus the use  
 * @author Elvis Ligu
 * @since 0.0.1
 * @version 0.0.1
 */
public class SkipFilter<T extends VCSCommit> implements VCSCommitFilter<T> {

    /**
     * The number of commits to skip.<p>
     */
    private int block;
    /**
     * The current number of commits that are skipped.<p>
     */
    private int counter = 0;
    
    /**
     * Create a new filter that will return true for each nth commit.<p>
     * 
     * @param skip
     * 		the n for which the commits will be skipped. Must be greater than 1.		
     */
    public SkipFilter(int skip) {
	if(skip < 2) {
	    throw new IllegalArgumentException("skip must be grater than 1");
	}
	block = skip;
    }
    
    /**
     * @return
     * 		the number of commits to skip
     */
    public int getBlock() {
	return block;
    }
    
    /**
     * @return
     * 		the number of current commits that are skipped.
     */
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
