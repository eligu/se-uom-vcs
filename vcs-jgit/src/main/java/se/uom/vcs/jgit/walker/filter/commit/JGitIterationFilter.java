/**
 * 
 */
package se.uom.vcs.jgit.walker.filter.commit;

import java.io.IOException;

import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.errors.StopWalkException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.filter.RevFilter;

/**
 * A filter that include each nth commit, where n is specified in constructor.<p>
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public class JGitIterationFilter extends RevFilter {

   /**
    * The number of commits to skip.
    * <p>
    */
   private int block;
   /**
    * The current number of commits that are skipped.
    * <p>
    */
   private int counter = 0;

   /**
    * Create a new filter that will return true for each nth commit.
    * <p>
    * 
    * @param skip
    *           the n for which the commits will be skipped. Must be greater
    *           than 1.
    */
   public JGitIterationFilter(int skip) {
      if (skip < 2) {
         throw new IllegalArgumentException("skip must be grater than 1");
      }
      block = skip;
   }
   
   /* {@inheritDoc)
    * @see org.eclipse.jgit.revwalk.filter.RevFilter#include(org.eclipse.jgit.revwalk.RevWalk, org.eclipse.jgit.revwalk.RevCommit)
    */
   @Override
   public boolean include(RevWalk walker, RevCommit cmit)
         throws StopWalkException, MissingObjectException,
         IncorrectObjectTypeException, IOException {
      
      return (counter++ % block) == 0;
   }

   /* {@inheritDoc)
    * @see org.eclipse.jgit.revwalk.filter.RevFilter#clone()
    */
   @Override
   public synchronized RevFilter clone() {
      
      JGitIterationFilter filter = new JGitIterationFilter(block);
      filter.counter = counter;
      return filter;
   }

}
