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
public interface FilterParser<E, T extends VCSFilter<E>> {

    T parse(VCSFilter<E> filter); 
}
