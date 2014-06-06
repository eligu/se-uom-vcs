package gr.uom.se.vcs.jgit.walker.filter.commit;

import gr.uom.se.vcs.VCSCommit;
import gr.uom.se.vcs.jgit.walker.OptimizedFilter;
import gr.uom.se.vcs.walker.filter.commit.VCSCommitFilter;

import org.eclipse.jgit.revwalk.filter.RevFilter;


/**
 * A helper filter that represents an optimized filter for commit filters, ready
 * to be used in JGit API.
 * <p>
 * 
 * For every default commit filter (provided by vcs-api) that is used in a
 * visitor, it will be parsed and converted to a corresponding JGit filter
 * (provided by JGit library or implemented by this implementation). This class
 * represents a parsed and converted default commit filter, ready to be used
 * with JGit library. (Actually this is a place holder for the filter.
 * see {@link #getCurrent()})
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 * @param <T>
 */
public class OptimizedCommitFilter<T extends VCSCommit> extends
      OptimizedFilter<T> implements VCSCommitFilter<T> {

   /**
    * The current JGit filter that corresponds to the commit filter.
    * <p>
    */
   private RevFilter current;

   public OptimizedCommitFilter(RevFilter filter) {
      this.current = filter;
   }

   /**
    * @return the current JGit filter
    */
   public RevFilter getCurrent() {
      return current;
   }

   /**
    * Set the current JGit filter.
    * <p>
    * 
    * @param current the new filter
    */
   public void setCurrent(RevFilter current) {
      this.current = current;
   }
}
