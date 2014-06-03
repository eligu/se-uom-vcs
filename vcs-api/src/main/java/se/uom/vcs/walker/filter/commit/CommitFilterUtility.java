/**
 * 
 */
package se.uom.vcs.walker.filter.commit;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import se.uom.vcs.VCSCommit;

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
   public static <T extends VCSCommit> VCSCommitFilter<T> author(
         Set<String> patterns) {
      return new AuthorFilter<T>(patterns);
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
   public static <T extends VCSCommit> VCSCommitFilter<T> author(
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
   public static <T extends VCSCommit> VCSCommitFilter<T> committer(
         Set<String> patterns) {
      return new CommitterFilter<T>(patterns);
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
   public static <T extends VCSCommit> VCSCommitFilter<T> committer(
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
   public static <T extends VCSCommit> VCSCommitFilter<T> range(Date start,
         Date end) {
      return new CommitDateRangeFilter<T>(start, end);
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
   public static <T extends VCSCommit> VCSCommitFilter<T> counter(int count) {
      return new MaxCounterFilter<T>(count);
   }

   /**
    * Creates a new merge filter, to check if a commit is a merge.
    * <p>
    * 
    * @return a new merge filter
    * @see MergeFilter
    */
   public static <T extends VCSCommit> VCSCommitFilter<T> merge() {
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
   public static <T extends VCSCommit> VCSCommitFilter<T> message(
         Set<String> patterns) {
      return new MessageFilter<T>(patterns);
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
   public static <T extends VCSCommit> VCSCommitFilter<T> message(
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
   public static <T extends VCSCommit> VCSCommitFilter<T> skip(int skip) {
      return new SkipFilter<T>(skip);
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
   public static <T extends VCSCommit> VCSCommitFilter<T> iteration(int skip) {
      return new IterationFilter<T>(skip);
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
   public static <T extends VCSCommit> VCSCommitFilter<T> not(
         VCSCommitFilter<T> filter) {
      return new VCSCommitNotFilter<T>(filter);
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
   public static <T extends VCSCommit> VCSCommitFilter<T> or(
         @SuppressWarnings("unchecked") VCSCommitFilter<T>... filters) {

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
   public static <T extends VCSCommit> VCSCommitFilter<T> or(
         Collection<VCSCommitFilter<T>> filters) {
      return new VCSCommitOrFilter<T>(filters);
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
   public static <T extends VCSCommit> VCSCommitFilter<T> and(
         @SuppressWarnings("unchecked") VCSCommitFilter<T>... filters) {
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
   public static <T extends VCSCommit> VCSCommitFilter<T> and(
         Collection<VCSCommitFilter<T>> filters) {
      return new VCSCommitAndFilter<T>(filters);
   }

}
