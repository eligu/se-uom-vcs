package gr.uom.se.vcs.jgit.walker;

import gr.uom.se.vcs.exceptions.VCSRepositoryException;
import gr.uom.se.vcs.jgit.utils.TreeUtils;
import gr.uom.se.vcs.walker.Collector;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jgit.api.DiffCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.filter.TreeFilter;


/**
 * A helper class used to collect diffs between two commits.
 * <p>
 * 
 * This class uses JGit {@link DiffCommand} to produce diffs between two
 * commits. The diffs are produced from old to new commit.
 * 
 * @author Elvis Ligu
 * @since 0.0.1
 * @version 0.0.1
 * @param <T>
 *           of type {@link DiffEntry}
 */
public class DiffCollector<T extends DiffEntry> implements Collector<T> {

   /**
    * The repository from which the two commits comes from.
    * <p>
    */
   private final Repository repo;

   /**
    * The old commit.
    * <p>
    */
   private RevCommit oldC;

   /**
    * The new commit.
    * <p>
    */
   private RevCommit newC;

   /**
    * The {@link DiffCommand} to be used when producing diffs.
    * <p>
    */
   private final DiffCommand command;

   /**
    * Creates a new {@link DiffCollector}.
    * <p>
    * 
    * <b>NOTE: the two commits must not be equal.</b>
    * 
    * @param repo
    *           the repository to create the diff command (null not allowed)
    * @param commit1
    *           the first commit (null not allowed)
    * @param commit2
    *           the second commit (null not allowed)
    */
   public DiffCollector(final Repository repo, final RevCommit commit1,
         final RevCommit commit2) {

      if ((commit1 == null) || (commit2 == null) || (repo == null)) {
         throw new IllegalArgumentException("check args for null");
      }

      if (AnyObjectId.equals(commit1, commit2)) {
         throw new IllegalArgumentException(
               "commit1 must be different from commit2");
      }

      // Set old and new commits accordingly
      if (commit1.getCommitTime() > commit2.getCommitTime()) {
         this.newC = commit1;
         this.oldC = commit2;
      } else {
         this.newC = commit2;
         this.oldC = commit1;
      }

      // Create a diff command
      this.command = new Git(repo).diff();
      this.repo = repo;
   }

   /**
    * @return old commit
    */
   public RevCommit getOldCommit() {
      return this.oldC;
   }

   /**
    * @return new commit
    */
   public RevCommit getNewCommit() {
      return this.newC;
   }

   /**
    * Limit diffs only to this paths.
    * <p>
    * 
    * 
    * @param paths
    *           to include results from (must not be null, not or empty)
    * @return this collector
    */
   public DiffCollector<T> setPathFilters(final TreeFilter filter) {
      this.command.setPathFilter(filter);
      return this;
   }

   /**
    * <b>NOTE:</b> This implementation will first call in {@link #collect()}
    * method and creates an iterator from the returned collection.
    * <p>
    */
   @Override
   public Iterator<T> iterator() {

      try {
         final Collection<T> list = this.collect();
         return list.iterator();

      } catch (final VCSRepositoryException e) {
         throw new IllegalStateException(e);
      }
   }

   /**
    * {@inheritDoc} Prepares two tree iterators from old and new commit and
    * calculates the difference of the trees.
    * 
    */
   @Override
   @SuppressWarnings("unchecked")
   public Collection<T> collect() throws VCSRepositoryException {

      // The diff works on TreeIterators, we prepare two for the two commits
      try {

         // Create tree iterators
         final AbstractTreeIterator oldTreeParser = TreeUtils
               .prepareTreeParserForWalk(this.repo, this.oldC, null);
         final AbstractTreeIterator newTreeParser = TreeUtils
               .prepareTreeParserForWalk(this.repo, this.newC, null);

         // then the porcelain diff-command returns a list of diff entries
         final List<DiffEntry> diff = this.command.setOldTree(oldTreeParser)
               .setNewTree(newTreeParser).call();

         return (Collection<T>) diff;

      } catch (final MissingObjectException e) {
         throw new VCSRepositoryException(e);
      } catch (final IncorrectObjectTypeException e) {
         throw new VCSRepositoryException(e);
      } catch (final IOException e) {
         throw new VCSRepositoryException(e);
      } catch (final GitAPIException e) {
         throw new VCSRepositoryException(e);
      }
   }

}
