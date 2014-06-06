/**
 * 
 */
package gr.uom.se.vcs.jgit.walker;

import gr.uom.se.vcs.jgit.walker.filter.commit.OptimizedCommitFilter;
import gr.uom.se.vcs.jgit.walker.filter.resource.OptimizedResourceFilter;
import gr.uom.se.vcs.walker.filter.VCSFilter;
import gr.uom.se.vcs.walker.filter.resource.PathPrefixFilter;

/**
 * A helper filter that represents an optimized filter ready to be used in JGit
 * API.
 * <p>
 * 
 * The vcs-api project provides default implementations for several filters
 * (resource and commit filters), however each time a default filter is used we
 * try to parse it to a corresponding JGit filter (be it commit or tree filter),
 * because that would allow JGit library to optimize the execution. For example
 * if the provided filter is {@link PathPrefixFilter} we can parse it to a
 * prefix tree filter of JGit (there is an implementation provided by library).
 * <p>
 * The optimized filter is used in case when a provided visitor's filter has
 * been parsed and is converted to an optimized filter.
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 * @see OptimizedCommitFilter
 * @see OptimizedResourceFilter
 */
public abstract class OptimizedFilter<T> implements VCSFilter<T> {

   @Override
   public boolean include(T entity) {
      throw new UnsupportedOperationException(
            "this filter is for use with classes under gr.uom.se.vcs.jgit");
   }
}
