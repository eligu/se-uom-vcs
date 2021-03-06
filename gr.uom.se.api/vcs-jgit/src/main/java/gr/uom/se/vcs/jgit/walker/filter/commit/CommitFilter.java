/**
 * 
 */
package gr.uom.se.vcs.jgit.walker.filter.commit;

import gr.uom.se.vcs.VCSCommit;
import gr.uom.se.vcs.jgit.walker.FilterParser;
import gr.uom.se.vcs.walker.filter.commit.AuthorFilter;
import gr.uom.se.vcs.walker.filter.commit.CommitDateRangeFilter;
import gr.uom.se.vcs.walker.filter.commit.CommitterFilter;
import gr.uom.se.vcs.walker.filter.commit.IterationFilter;
import gr.uom.se.vcs.walker.filter.commit.MaxCounterFilter;
import gr.uom.se.vcs.walker.filter.commit.MergeFilter;
import gr.uom.se.vcs.walker.filter.commit.MessageFilter;
import gr.uom.se.vcs.walker.filter.commit.SkipFilter;
import gr.uom.se.vcs.walker.filter.commit.VCSCommitAndFilter;
import gr.uom.se.vcs.walker.filter.commit.VCSCommitFilter;
import gr.uom.se.vcs.walker.filter.commit.VCSCommitNotFilter;
import gr.uom.se.vcs.walker.filter.commit.VCSCommitOrFilter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.eclipse.jgit.revwalk.filter.AndRevFilter;
import org.eclipse.jgit.revwalk.filter.AuthorRevFilter;
import org.eclipse.jgit.revwalk.filter.CommitterRevFilter;
import org.eclipse.jgit.revwalk.filter.MaxCountRevFilter;
import org.eclipse.jgit.revwalk.filter.MessageRevFilter;
import org.eclipse.jgit.revwalk.filter.OrRevFilter;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.eclipse.jgit.revwalk.filter.SkipRevFilter;

/**
 * A utility class to deal with JGit revision filters.
 * <p>
 * 
 * The vcs-api provides default implementations for several filters (see package
 * gr.uom.se.vcs.walker.filter.commit), but the problem using these filters is
 * that for each RevCommit object of JGit API we must create a VCSCommitImp
 * wrapper and pass it to the filter, and this would waste time for commits that
 * the filter rejects. Therefore, each time a visitor's filter is specified, we
 * try to parse and convert it to a JGit filter. We have a common class that
 * represents all parsed filters ({@link OptimizedCommitFilter} that will be
 * applied directly to RevCommit, and reject commits without the need of the
 * creation of VCSCommitImp wrapper.
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public class CommitFilter {

   /**
    * Creates a new committer filter to be used with JGit library based on given
    * patterns.
    * <p>
    * 
    * The new filter will be an {@link OrRevFilter} that contains a list of
    * {@link CommitterRevFilter}.
    * 
    * @param committersPatterns
    *           the patterns of committers
    * @return a new committer filter
    * @see CommitterRevFilter
    * @see CommitterRevFilter#create(String)
    */
   public static OptimizedCommitFilter committer(
         Collection<String> committersPatterns) {

      Set<String> patterns = new CommitterFilter(committersPatterns)
            .getPatterns();
      List<RevFilter> filters = new ArrayList<RevFilter>();

      for (String p : patterns) {
         filters.add(CommitterRevFilter.create(p));
      }
      if (filters.size() == 1) {
         return new OptimizedCommitFilter(filters.get(0));
      }
      return new OptimizedCommitFilter(OrRevFilter.create(filters));
   }

   /**
    * Creates a new author filter to be used with JGit library based on given
    * patterns.
    * <p>
    * 
    * The new filter will be an {@link OrRevFilter} that contains a list of
    * {@link CommitterRevFilter}.
    * 
    * @param authorPatterns
    *           the patterns of authors
    * @return a new author filter
    * @see AuthorRevFilter
    * @see AuthorRevFilter#create(String)
    */
   public static OptimizedCommitFilter author(Collection<String> authorPatterns) {

      Set<String> patterns = new CommitterFilter(authorPatterns).getPatterns();
      List<RevFilter> filters = new ArrayList<RevFilter>();

      for (String p : patterns) {
         filters.add(AuthorRevFilter.create(p));
      }
      if (filters.size() == 1) {
         return new OptimizedCommitFilter(filters.get(0));
      }
      return new OptimizedCommitFilter(OrRevFilter.create(filters));
   }

   /**
    * Creates a new date range filter to be used with JGit library based on
    * given dates.
    * <p>
    * 
    * A range filter will check each if a commit is between the specified dates.
    * See {@link JGitCommitDateRangeFilter} for more info.
    * 
    * @param start
    *           the earlier date
    * @param end
    *           the last date
    * @return a new date range filter
    */
   public static OptimizedCommitFilter range(Date start, Date end) {
      return new OptimizedCommitFilter(
            new JGitCommitDateRangeFilter(start, end));
   }

   /**
    * Creates a new filter that will limit the number of commits returned.
    * <p>
    * 
    * @param count
    *           the max number of commits returned.
    *           <p>
    * @return a new max count filter
    */
   public static OptimizedCommitFilter count(int count) {
      return new OptimizedCommitFilter(MaxCountRevFilter.create(count));
   }

   /**
    * Creates a new filter that check if a commit has more than one parent.
    * <p>
    * 
    * @return a new merge filter
    */
   public static OptimizedCommitFilter merge() {
      return new OptimizedCommitFilter(new JGitMergeFilter());
   }

   /**
    * Creates a new message filter to be used with JGit library based on given
    * patterns.
    * <p>
    * 
    * The new filter will be an {@link OrRevFilter} that contains a list of
    * {@link MessageRevFilter}.
    * 
    * @param patterns
    *           the patterns of commit message
    * @return a new message filter
    * @see MessageRevFilter
    * @see MessageRevFilter#create(String)
    */
   public static OptimizedCommitFilter message(Collection<String> patterns) {
      Set<String> set = new MessageFilter(patterns).getPatterns();
      List<RevFilter> filters = new ArrayList<RevFilter>();

      for (String p : set) {
         filters.add(MessageRevFilter.create(p));
      }
      if (filters.size() == 1) {
         return new OptimizedCommitFilter(filters.get(0));
      }
      return new OptimizedCommitFilter(OrRevFilter.create(filters));
   }

   /**
    * Creates a new skip commit filter, that will include each nth commit.
    * <p>
    * 
    * @param block
    *           the number of commits to skip for each one that should be
    *           included
    * @return a new skip commit
    * @see SkipRevFilter
    */
   public static OptimizedCommitFilter skip(int block) {
      return new OptimizedCommitFilter(SkipRevFilter.create(block));
   }

   /**
    * Creates a new iteration filter.
    * <p>
    * 
    * @param block
    *           how many iteration should the filter make to allow each commit
    * @return a new optimized iteration filter to be used with JGit
    */
   public static OptimizedCommitFilter iteration(int block) {
      return new OptimizedCommitFilter(new JGitIterationFilter(block));
   }

   /**
    * Creates a new AND filter.
    * <p>
    * 
    * @param filters
    *           to apply the AND operator
    * @return a new filter
    */
   public static OptimizedCommitFilter and(
         Collection<OptimizedCommitFilter> filters) {
      List<RevFilter> revFilters = new ArrayList<RevFilter>();
      for (OptimizedCommitFilter f : filters) {
         revFilters.add(f.getCurrent());
      }
      return new OptimizedCommitFilter(AndRevFilter.create(revFilters));
   }

   /**
    * Creates a new OR filter.
    * <p>
    * 
    * @param filters
    *           to apply the OR operator
    * @return a new filter
    */
   public static OptimizedCommitFilter or(
         Collection<OptimizedCommitFilter> filters) {
      List<RevFilter> revFilters = new ArrayList<RevFilter>();
      for (OptimizedCommitFilter f : filters) {
         revFilters.add(f.getCurrent());
      }
      return new OptimizedCommitFilter(OrRevFilter.create(revFilters));
   }

   /**
    * Creates a new NOT filter.
    * <p>
    * 
    * @param filter
    *           to apply the NOT operator
    * @return a new filter
    */
   public static OptimizedCommitFilter not(OptimizedCommitFilter filter) {
      return new OptimizedCommitFilter(filter.getCurrent().negate());
   }

   /**
    * Parse a commit filter and return a new optimized filter to be used with
    * JGit library.
    * <p>
    * 
    * The new filter will contain an already implemented filter of JGit library
    * or a custom implementation.
    * 
    * @param filter
    *           to parse
    * @param newParam
    * @return a new optimized filter to be used with JGit library
    */
   public static OptimizedCommitFilter parse(VCSCommitFilter filter,
         FilterParser<VCSCommit, OptimizedCommitFilter> parser) {

      // Check the given parser if he can parse the filter
      // If not try to parse it with the default method
      if (parser != null) {
         OptimizedCommitFilter f = parser.parse(filter);
         if (f != null) {
            return f;
         }
      }

      // Case when this filter is of type OptimizedCommitFilter
      if (OptimizedCommitFilter.class.isAssignableFrom(filter.getClass())) {
         return (OptimizedCommitFilter) filter;
      }

      // Case when this filter is of type CommitDateRangeFilter
      if (CommitDateRangeFilter.class.isAssignableFrom(filter.getClass())) {
         return parseRange((CommitDateRangeFilter) filter);
      }

      // Case when this filter is of type CommitterFilter
      if (CommitterFilter.class.isAssignableFrom(filter.getClass())) {
         return parseCommitter((CommitterFilter) filter);
      }

      if (AuthorFilter.class.isAssignableFrom(filter.getClass())) {
         return parseAuthor((AuthorFilter) filter);
      }
      // Case when this filter is of type MaxCounterFilter
      if (MaxCounterFilter.class.isAssignableFrom(filter.getClass())) {
         return parseCount((MaxCounterFilter) filter);
      }

      // Case when this filter is of type MergeFilter
      if (MergeFilter.class.isAssignableFrom(filter.getClass())) {
         return parseMerge((MergeFilter) filter);
      }

      // Case when this filter is of type MessageFilter
      if (MessageFilter.class.isAssignableFrom(filter.getClass())) {
         return parseMessage((MessageFilter) filter);
      }

      // Case when this filter is of type SkipFilter
      if (SkipFilter.class.isAssignableFrom(filter.getClass())) {
         return parseSkip((SkipFilter) filter);
      }

      // Case when this filter is of type VCSCommitNotFilter
      if (VCSCommitNotFilter.class.isAssignableFrom(filter.getClass())) {
         return parseNot((VCSCommitNotFilter) filter, null);
      }

      // Case when this filter is of type VCSCommitAndFilter
      if (VCSCommitAndFilter.class.isAssignableFrom(filter.getClass())) {
         return parseAnd((VCSCommitAndFilter) filter, null);
      }

      // Case when this filter is of type VCSCommitOrFilter
      if (VCSCommitOrFilter.class.isAssignableFrom(filter.getClass())) {
         return parseOr((VCSCommitOrFilter) filter, null);
      }

      // Case when this filter is of type IterationFilter
      if (IterationFilter.class.isAssignableFrom(filter.getClass())) {
         return parseIteration((IterationFilter) filter);
      }
      return null;
   }

   /**
    * Parse an iteration filter.
    * <p>
    * 
    * @param filter
    *           to parse
    * @return a corresponding optimized filter to be used with JGit
    */
   public static OptimizedCommitFilter parseIteration(IterationFilter filter) {
      return iteration(filter.getBlock());
   }

   /**
    * Parse an author filter.
    * <p>
    * 
    * @param filter
    *           to parse
    * @return a corresponding optimized filter to be used with JGit
    */
   public static OptimizedCommitFilter parseAuthor(AuthorFilter filter) {
      return author(filter.getPatterns());
   }

   /**
    * Parse a range filter.
    * <p>
    * 
    * @param filter
    *           to parse
    * @return a corresponding optimized filter to be used with JGit
    */
   public static OptimizedCommitFilter parseRange(CommitDateRangeFilter filter) {
      return range(filter.getStart(), filter.getEnd());
   }

   /**
    * Parse a committer filter.
    * <p>
    * 
    * @param filter
    *           to parse
    * @return a corresponding optimized filter to be used with JGit
    */
   public static OptimizedCommitFilter parseCommitter(CommitterFilter filter) {
      return committer(filter.getPatterns());
   }

   /**
    * Parse a counter filter.
    * <p>
    * 
    * @param filter
    *           to parse
    * @return a corresponding optimized filter to be used with JGit
    */
   public static OptimizedCommitFilter parseCount(MaxCounterFilter filter) {
      return count(filter.getSize());
   }

   /**
    * Parse a merge filter.
    * <p>
    * 
    * @param filter
    *           to parse
    * @return a corresponding optimized filter to be used with JGit
    */
   public static OptimizedCommitFilter parseMerge(MergeFilter filter) {
      return merge();
   }

   /**
    * Parse a message filter.
    * <p>
    * 
    * @param filter
    *           to parse
    * @return a corresponding optimized filter to be used with JGit
    */
   public static OptimizedCommitFilter parseMessage(MessageFilter filter) {
      return message(filter.getPatterns());
   }

   /**
    * Parse a skip filter.
    * <p>
    * 
    * @param filter
    *           to parse
    * @return a corresponding optimized filter to be used with JGit
    */
   public static OptimizedCommitFilter parseSkip(SkipFilter filter) {
      return skip(filter.getBlock());
   }

   /**
    * Parse an AND filter.
    * <p>
    * 
    * @param filter
    *           to parse
    * @param parser
    * 
    * @return a corresponding optimized filter to be used with JGit
    */
   public static OptimizedCommitFilter parseAnd(VCSCommitAndFilter filter,
         FilterParser<VCSCommit, OptimizedCommitFilter> parser) {

      Set<VCSCommitFilter> filters = filter.getFilters();
      List<OptimizedCommitFilter> oFilters = new ArrayList<OptimizedCommitFilter>();

      for (VCSCommitFilter f : filters) {
         OptimizedCommitFilter of = parse(f, parser);
         if (of != null) {
            oFilters.add(of);
         } else {
            return null;
         }
      }

      if (oFilters.isEmpty()) {
         return null;
      } else if (oFilters.size() == 1) {
         return oFilters.get(0);
      } else {
         return and(oFilters);
      }
   }

   /**
    * Parse an OR filter.
    * <p>
    * 
    * @param filter
    *           to parse
    * @param parser
    * 
    * @return a corresponding optimized filter to be used with JGit
    */
   public static OptimizedCommitFilter parseOr(VCSCommitOrFilter filter,
         FilterParser<VCSCommit, OptimizedCommitFilter> parser) {

      Set<VCSCommitFilter> filters = filter.getFilters();
      List<OptimizedCommitFilter> oFilters = new ArrayList<OptimizedCommitFilter>();

      for (VCSCommitFilter f : filters) {
         OptimizedCommitFilter of = parse(f, parser);
         if (of != null) {
            oFilters.add(of);
         } else {
            return null;
         }
      }

      if (oFilters.isEmpty()) {
         return null;
      } else if (oFilters.size() == 1) {
         return oFilters.get(0);
      } else {
         return or(oFilters);
      }
   }

   /**
    * Parse a NOT filter.
    * <p>
    * 
    * @param filter
    *           to parse
    * @param parser
    * 
    * @return a corresponding optimized filter to be used with JGit
    */
   public static OptimizedCommitFilter parseNot(VCSCommitNotFilter filter,
         FilterParser<VCSCommit, OptimizedCommitFilter> parser) {
      OptimizedCommitFilter f = parse(filter.getFilter(), parser);
      if (f != null) {
         return not(f);
      }
      return null;
   }
}
