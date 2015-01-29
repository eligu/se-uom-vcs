/**
 * 
 */
package gr.uom.se.vcs.jgit.walker;

import gr.uom.se.vcs.walker.filter.VCSFilter;

/**
 * A helper interface used when parsing the default filters.
 * <p>
 * 
 * The vcs-api project provides default implementations for several filters
 * (resource and commit filters) however, each time a default filter is used we
 * try to parse it to a corresponding JGit filter (be it commit or tree filter),
 * because that would allow JGit library to optimize the execution.
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public interface FilterParser<E, T extends VCSFilter<E>> {

   /**
    * Parse the given filter.<p>
    * 
    * @param filter to parse
    * @return a new filter that corresponds to the parsed one
    */
   T parse(VCSFilter<E> filter);
}
