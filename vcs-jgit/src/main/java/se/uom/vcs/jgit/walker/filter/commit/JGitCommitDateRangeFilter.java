/**
 * 
 */
package se.uom.vcs.jgit.walker.filter.commit;

import java.io.IOException;
import java.util.Date;

import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.errors.StopWalkException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.filter.RevFilter;

import se.uom.vcs.walker.filter.commit.CommitDateRangeFilter;

/**
 * A custom implementation of {@link CommitDateRangeFilter} to be used with JGit
 * library.
 * <p>
 * A commit date range filter will be parsed to an instance of this filter and
 * be applied directly to RevWalk.
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 * @see CommitDateRangeFilter
 */
public class JGitCommitDateRangeFilter extends RevFilter {

   /**
    * The start date, prior to which all commits are rejected.
    * <p>
    */
   private Date start;
   /**
    * The end date, after which all commits are rejected.
    * <p>
    */
   private Date end;

   /**
    * Creates a new date range filter.
    * <p>
    * 
    * @param start
    *           the date prior to which all commits will be rejected. Must be
    *           null (if end is not null).
    * @param end
    *           the date after which all commits will be rejected. Must be null
    *           (if start is not null).
    */
   public JGitCommitDateRangeFilter(Date start, Date end) {
      
      if (start == null && end == null) {
         throw new IllegalArgumentException(
               "start and end must not be both null");
      }
      if (start != null && end != null && start.after(end) && start.equals(end)) {
         throw new IllegalArgumentException("start must be before end");
      }
      this.start = start;
      this.end = end;
   }

   @Override
   public RevFilter clone() {
      return this;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean include(RevWalk walk, RevCommit cmit)
         throws StopWalkException, MissingObjectException,
         IncorrectObjectTypeException, IOException {

      boolean include = false;
      Date cDate = cmit.getCommitterIdent().getWhen();

      if (start != null) {
         include = cDate.after(start) || cDate.equals(start);
      }
      if (include && end != null) {
         include = cDate.before(end) || cDate.equals(end);
      }

      return include;
   }

}
