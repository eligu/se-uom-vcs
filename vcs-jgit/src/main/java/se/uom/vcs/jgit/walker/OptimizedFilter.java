/**
 * 
 */
package se.uom.vcs.jgit.walker;

import se.uom.vcs.walker.filter.VCSFilter;

/**
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public abstract class OptimizedFilter<T> implements VCSFilter<T> {

    @Override
    public boolean include(Object entity) {
	throw new UnsupportedOperationException("this filter is for use with classes under se.uom.vcs.jgit");
    }
}
