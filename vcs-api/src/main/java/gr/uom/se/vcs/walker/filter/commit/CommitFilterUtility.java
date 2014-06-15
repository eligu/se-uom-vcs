/**
 * 
 */
package gr.uom.se.vcs.walker.filter.commit;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;


/**
 * A utility class containing static methods that helps in constructing commit
 * filters.
 * <p>
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public class CommitFilterUtility {

   /**
    * Creates a new author filter based on the given patterns.
    * <p>
    * 
    * @param patterns
    *           of authors
    * @return a new author filter
    * @see AuthorFilter
    */
   public static VCSCommitFilter author(
         Set<String> patterns) {
      return new AuthorFilter(patterns);
   }

   /**
    * Creates a new author filter based on the given patterns.
    * <p>
    * 
    * @param patterns
    *           of authors
    * @return a new author filter
    * @see AuthorFilter
    */
   public static VCSCommitFilter author(
         String... patterns) {
      Set<String> authors = new HashSet<String>();
      for (String a : patterns) {
         authors.add(a);
      }
      return author(authors);
   }

   /**
    * Creates a new committer filter based on the given patterns.
    * <p>
    * 
    * @param patterns
    *           of committers
    * @return a new committer filter
    * @see CommitterFilter
    */
   public static VCSCommitFilter committer(
         Set<String> patterns) {
      return new CommitterFilter(patterns);
   }

   /**
    * Creates a new committer filter based on the given patterns.
    * <p>
    * 
    * @param patterns
    *           of committers
    * @return a new committer filter
    * @see CommitterFilter
    */
   public static VCSCommitFilter committer(
         String... patterns) {
      Set<String> committers = new HashSet<String>();
      for (String a : patterns) {
         committers.add(a);
      }
      return committer(committers);
   }

   /**
    * Creates a new commit date range filter.
    * <p>
    * 
    * @param start
    *           the oldest date
    * @param end
    *           the newest date
    * @return a commit date range filter
    * @see CommitDateRangeFilter
    */
   public static VCSCommitFilter range(Date start,
         Date end) {
      return new CommitDateRangeFilter(start, end);
   }

   /**
    * Creates a new max counter filter.
    * <p>
    * 
    * @param count
    *           the number of the commits to iterate until the filter stops
    * @return a new max counter filter
    * @see MaxCounterFilter
    */
   public static VCSCommitFilter counter(int count) {
      return new MaxCounterFilter(count);
   }

   /**
    * Creates a new merge filter, to check if a commit is a merge.
    * <p>
    * 
    * @return a new merge filter
    * @see MergeFilter
    */
   public static VCSCommitFilter merge() {
      return MergeFilter.getInstance();
   }

   /**
    * Creates a new message filter based on the given patterns.
    * <p>
    * 
    * @param patterns
    *           of commits message
    * @return a new message filter
    * @see MessageFilter
    */
   public static VCSCommitFilter message(
         Set<String> patterns) {
      return new MessageFilter(patterns);
   }

   /**
    * Creates a new message filter based on the given patterns.
    * <p>
    * 
    * @param patterns
    *           of commits message
    * @return a new message filter
    * @see MessageFilter
    */
   public static VCSCommitFilter message(
         String... patterns) {
      Set<String> pats = new HashSet<String>();
      for (String a : patterns) {
         pats.add(a);
      }
      return message(pats);
   }

   /**
    * Creates a new skip filter.
    * <p>
    * 
    * @param skip
    *           the number of n first commits to until the commits will start to include
    * @return a new skip filter
    * @see SkipFilter
    */
   public static VCSCommitFilter skip(int skip) {
      return new SkipFilter(skip);
   }

   /**
    * Creates a new iteration filter.
    * <p>
    * 
    * @param skip
    *           the number of the commits to skip until the next is allowed
    * @return a new skip filter
    * @see SkipFilter
    */
   public static VCSCommitFilter iteration(int skip) {
      return new IterationFilter(skip);
   }

   /**
    * Creates a negative commit filter for the specified filter.
    * <p>
    * 
    * @param filter
    *           to negate with a NOT filter
    * @return a negated filter for the specified one
    * @see VCSCommitNotFilter
    */
   public static VCSCommitFilter not(
         VCSCommitFilter filter) {
      return new VCSCommitNotFilter(filter);
   }

   /**
    * Creates a filter that will return <code>true</code> if any of the given
    * filters returns <code>true</code>.
    * <p>
    * 
    * @param filters
    *           that will be returned in an <code>OR</code> filter
    * @return an <code>OR</code> filter for the specified filters
    * @see VCSCommitOrFilter
    */
   public static VCSCommitFilter or(VCSCommitFilter... filters) {

      return or(Arrays.asList(filters));
   }

   /**
    * Creates a filter that will return <code>true</code> if any of the given
    * filters returns <code>true</code>.
    * <p>
    * 
    * @param filters
    *           that will be returned in an <code>OR</code> filter
    * @return an <code>OR</code> filter for the specified filters
    * @see VCSCommitOrFilter
    */
   public static VCSCommitFilter or(
         Collection<VCSCommitFilter> filters) {
      return new VCSCommitOrFilter(filters);
   }

   /**
    * Creates a filter that will return <code>true</code> if all of the given
    * filters return <code>true</code>.
    * <p>
    * 
    * @param filters
    *           that will be returned in an <code>AND</code> filter
    * @return an <code>AND</code> filter for the specified filters
    * @see VCSCommitANDFilter
    */
   public static VCSCommitFilter and(VCSCommitFilter... filters) {
      return and(Arrays.asList(filters));
   }

   /**
    * Creates a filter that will return <code>true</code> if all of the given
    * filters return <code>true</code>.
    * <p>
    * 
    * @param filters
    *           that will be returned in an <code>AND</code> filter
    * @return an <code>AND</code> filter for the specified filters
    * @see VCSCommitANDFilter
    */
   public static VCSCommitFilter and(
         Collection<VCSCommitFilter> filters) {
      return new VCSCommitAndFilter(filters);
   }

}
