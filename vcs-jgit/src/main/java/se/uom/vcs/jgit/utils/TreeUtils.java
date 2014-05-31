package se.uom.vcs.jgit.utils;

import java.io.IOException;

import org.eclipse.jgit.errors.CorruptObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.AndTreeFilter;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.treewalk.filter.PathFilterGroup;
import org.eclipse.jgit.treewalk.filter.TreeFilter;

import se.uom.vcs.exceptions.VCSRepositoryException;
import se.uom.vcs.exceptions.VCSResourceNotFoundException;
import se.uom.vcs.jgit.ArgsCheck;

/**
 * A tree utility class to be used only with JGit TreeWalk and other API
 * classes.
 * <p>
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public class TreeUtils {

   /**
    * Prepare a tree parser for the given commit to use within a diff command or
    * to show only the changes (modifications/additions).
    * <p>
    * 
    * @param repository
    *           for the {@link RevWalk} which will be used to prepare the tree
    * @param commit
    *           which contains the tree
    * @param paths
    *           an array of paths to limit the tree parser. It can be null, or
    *           empty can not contain null paths.
    * @return a tree iterator to use for diffing with another iterator from
    *         another commit
    * @throws IOException
    *            thrown from JGit API
    * @throws MissingObjectException
    *            thrown from JGIT API
    * @throws IncorrectObjectTypeException
    *            thrown from JGit API
    */
   public static AbstractTreeIterator prepareTreeParserForDiff(
         final Repository repository, RevCommit commit, final String[] paths)
         throws IOException, MissingObjectException,
         IncorrectObjectTypeException {

      ArgsCheck.notNull("repository", repository);
      ArgsCheck.notNull("commit", commit);

      // from the commit we can build the tree which allows us to construct
      // the TreeParser
      final RevWalk walk = new RevWalk(repository);

      // If there are specified paths then specify the corresponding filters
      if ((paths != null) && (paths.length > 0)) {

         ArgsCheck.containsNoNull("paths array", (Object[]) paths);

         walk.setTreeFilter(AndTreeFilter.create(
               PathFilterGroup.createFromStrings(paths), TreeFilter.ANY_DIFF));
      } else {
         walk.setTreeFilter(TreeFilter.ANY_DIFF);
      }

      RevTree tree = null;
      try {
         commit = walk.parseCommit(commit);
         tree = walk.parseTree(commit.getTree().getId());
      } finally {
         walk.release();
      }

      final CanonicalTreeParser parser = new CanonicalTreeParser();
      final ObjectReader reader = repository.newObjectReader();

      try {
         parser.reset(reader, tree.getId());
      } finally {
         reader.release();
      }
      return parser;
   }

   /**
    * Prepare a tree parser for the given commit.
    * <p>
    * 
    * @param repository
    *           for the {@link RevWalk} which will be used to prepare the tree
    * @param commit
    *           which contains the tree
    * @param paths
    *           an array of paths to limit the tree parser. It can be null, or
    *           empty can not contain null paths.
    * @return a tree iterator
    * @throws IOException
    *            thrown from JGit API
    * @throws MissingObjectException
    *            thrown from JGIT API
    * @throws IncorrectObjectTypeException
    *            thrown from JGit API
    */
   public static AbstractTreeIterator prepareTreeParserForWalk(
         final Repository repository, RevCommit commit, final String[] paths)
         throws IOException, MissingObjectException,
         IncorrectObjectTypeException {

      ArgsCheck.notNull("repository", repository);
      ArgsCheck.notNull("commit", commit);

      // from the commit we can build the tree which allows us to construct
      // the TreeParser
      final RevWalk walk = new RevWalk(repository);
      if ((paths != null) && (paths.length > 0)) {
         ArgsCheck.containsNoNull("paths array", (Object[]) paths);
         walk.setTreeFilter(PathFilterGroup.createFromStrings(paths));
      }

      RevTree tree = null;
      try {
         commit = walk.parseCommit(commit);
         tree = walk.parseTree(commit.getTree().getId());
      } finally {
         walk.release();
      }

      final CanonicalTreeParser parser = new CanonicalTreeParser();
      final ObjectReader reader = repository.newObjectReader();

      try {
         parser.reset(reader, tree.getId());
      } finally {
         reader.release();
      }
      return parser;
   }

   /**
    * Prepare a tree walk for the given commit.
    * <p>
    * 
    * @param repository
    *           for the {@link RevWalk} which will be used to prepare the tree
    * @param commit
    *           which contains the tree
    * @param recursive
    *           if false the directory entries will be returned too
    * 
    * @return a tree walk
    * @throws IOException
    *            thrown from JGit API
    */
   public static TreeWalk createTreeWalk(final Repository repository,
         final RevCommit commit, final boolean recursive) throws IOException {

      return createTreeWalk(repository, commit, recursive, (String[]) null);
   }

   /**
    * Prepare a tree walk for the given path in the given commit.
    * <p>
    * 
    * Check if this path is available and throw an {@link IllegalStateException}
    * if not. That means, after the creation of tree walk it will call
    * {@link TreeWalk#next()} to check if it returns null.
    * 
    * @param commit
    *           which contains the tree
    * @param repository
    *           for the {@link RevWalk} which will be used to prepare the tree
    * @param recursive
    *           if false the directory entries will be returned too
    * @param path
    *           to create the tree walk for
    * @return a tree walk
    * @throws IOException
    *            thrown from JGit API
    */
   public static TreeWalk createTreeWalkForPathAndCheckIfExist(
         final RevCommit commit, final Repository repository,
         final boolean recursive, final String path) throws IOException {

      ArgsCheck.notNull("repository", repository);
      ArgsCheck.notNull("commit", commit);
      ArgsCheck.notNull("path", path);

      // create a tree walker and set path filter to this path only
      final TreeWalk treeWalk = createTreeWalk(repository, commit, recursive,
            (String[]) null);
      treeWalk.setFilter(PathFilter.create(path));

      // If there is not any entry to walk that means the path was not found
      if (!treeWalk.next()) {
         treeWalk.release();
         throw new IllegalStateException("Did not find expected path: " + path);
      }

      return treeWalk;
   }

   /**
    * Creates a tree walk with the given commit, and limit only to the given
    * paths.
    * <p>
    * 
    * @param repository
    *           for the {@link RevWalk} which will be used to prepare the tree
    * @param commit
    *           which contains the tree
    * @param recursive
    *           if false the directory entries will be returned too
    * @param paths
    *           to limit the tree walk (if null all entries will be included,
    *           must not be empty not or contain any null path)
    * 
    * @return a tree walk
    * @throws IOException
    *            thrown from JGit API
    */
   public static TreeWalk createTreeWalk(final Repository repository,
         final RevCommit commit, final boolean recursive, final String... paths)
         throws IOException {

      ArgsCheck.notNull("repository", repository);
      ArgsCheck.notNull("commit", commit);

      // create a tree walker
      TreeWalk treeWalk = null;
      RevWalk revWalk = null;
      try {
         treeWalk = new TreeWalk(repository);
         revWalk = new RevWalk(repository);
         treeWalk.addTree(revWalk.parseCommit(commit).getTree());
         treeWalk.setRecursive(recursive);

         if ((paths != null) && (paths.length > 0)) {
            ArgsCheck.containsNoNull("paths", (Object[]) paths);
            treeWalk.setFilter(PathFilterGroup.createFromStrings(paths));
         }
      } finally {
         if (treeWalk != null) {
            treeWalk.release();
         }
         if (revWalk != null) {
            revWalk.release();
         }
      }

      return treeWalk;
   }

   /**
    * Creates a tree walk for the given path, and walk it until the current
    * entry is the required one.
    * <p>
    * 
    * <b>NOTE:</b> the returned walker will be not recursive, that is using this
    * walker is required to call {@link TreeWalk#enterSubtree())} in order to
    * get all paths recursively under the given path (in case this is DIR).
    * 
    * @param commit
    *           which contains the tree
    * @param repository
    *           for the {@link RevWalk} which will be used to prepare the tree
    * @param path
    *           to create the tree walk for
    * @return a tree walk
    * @throws VCSResourceNotFoundException
    *            if the path is not found or is an unknown resource (not FILE or
    *            DIR)
    * @throws VCSRepositoryException
    *            if any problem occurs while reading the repository objects
    */
   public static TreeWalk getTreeWalkForPath(final RevCommit commit,
         final Repository repository, String path)
         throws VCSResourceNotFoundException, VCSRepositoryException {

      // Check arguments
      ArgsCheck.notNull("repository", repository);
      ArgsCheck.notNull("commit", commit);
      ArgsCheck.notNull("path", path);

      // Check if path is not allowed throw an IllegalArgumentException
      // if not then correct it
      path = correctPath(path);

      // First we have to check if the resource is available. By setting
      // recursive false we start checking each path segment of the given
      // path, if exists.
      // We do this because the given path may be a directory.
      final String[] segments = path.split("/");
      TreeWalk walk = null;

      int i = 0;
      try {

         // Create the tree walk with recursive false so we can have TREE
         // entries from walk
         walk = createTreeWalk(repository, commit, false, path);

         // Check each walk entry with the current segment path, if they are
         // not equal there is a problem with the walker, he is returning
         // entries
         // that are not under the given path
         while (walk.next() && (i < segments.length)) {

            final String segment = walk.getNameString();

            // The current entry is not under the given path, a problem is
            // here
            if (!segment.equals(segments[i++])) {
               throw new IllegalStateException(
                     "returned paths from tree are not under " + path);
            }

            // We are at the last path segment. All the walk entries
            // were equals to path's segments. Path is found.
            if (i == segments.length) {

               // If this is a known path return the walk
               final FileMode mode = walk.getFileMode(0);

               // We only allow FILE or DIR paths, other paths such as
               // symlink or submodules
               // are not allowed
               if (RevUtils.isDirMode(mode)
                     || RevUtils.isFileMode(mode)) {
                  return walk;
               }

               throw new VCSResourceNotFoundException("uknown path " + path);
               // The current walk entry is a directory so enter it
            } else if (walk.isSubtree()) {
               walk.enterSubtree();
               // If this is not a directory and we are in the middle of
               // checking our path segments
               // that mean the walker returned an illegal entry because we
               // limited it only to our path
            } else {
               throw new IllegalStateException("returned path "
                     + walk.getPathString() + " is not under " + path);
            }
         }

         // If walk has not any entry that means the path doesn't exist
         throw new VCSResourceNotFoundException("uknown path " + path);

      } catch (final MissingObjectException e) {
         throw new VCSRepositoryException(e);
      } catch (final IncorrectObjectTypeException e) {
         throw new VCSRepositoryException(e);
      } catch (final CorruptObjectException e) {
         throw new VCSRepositoryException(e);
      } catch (final IOException e) {
         throw new VCSRepositoryException(e);
      } finally {
         if (walk != null) {
            walk.release();
         }
      }
   }

   /**
    * Check path if it is allowed, and correct it by removing any blank spaces
    * from the beginning and the end, and removing slashes (/) from the beginning
    * or the end.
    * <p>
    * 
    * @param path
    *           to correct
    * @return the corrected path
    * @throws IllegalArgumentException
    *            if the path is null, empty or can not be corrected
    */
   public static String correctPath(String path) {

      ArgsCheck.notNull("path", path);
      // Path must not be empty
      if ((path = path.trim()).isEmpty()) {
         throw new IllegalArgumentException("not allowed path: " + path);
      }

      // Replace all occurrences of '\' with '/'
      path = path.replace('\\', '/');

      // If path ends with / and has no other character it is not allowed,
      // otherwise correct the path
      if (path.endsWith("/")) {
         if (path.length() == 1) {
            throw new IllegalArgumentException("not allowed path: " + path);
         }
         path = path.substring(0, path.length() - 1).trim();
      }

      // If path starts with / and has no other character it is not allowed,
      // otherwise correct the path
      if (path.startsWith("/")) {
         if (path.length() == 1) {
            throw new IllegalArgumentException("not allowed path: " + path);
         }
         path = path.substring(1).trim();
      }

      // After path correction path must not be empty
      if (path.isEmpty()) {
         throw new IllegalArgumentException("not allowed path: " + path);
      }

      return path;
   }

   /**
    * Check if path exists.
    * <p>
    * 
    * This will check if the specified path exist in the given commit.
    * 
    * @param commit
    *           to check the path at
    * @param repository
    *           for the {@link RevWalk} which will be used to prepare the tree
    * @param path
    *           to check for existence
    * @return true if the given path exists at the given commit
    * @throws IOException
    *            thrown from JGit API
    */
   public static boolean existPath(final RevCommit commit,
         final Repository repository, final String path) throws IOException {

      // create a tree walker and set path filter to this path only
      final TreeWalk treeWalk = createTreeWalk(repository, commit, true);
      treeWalk.setFilter(PathFilter.create(path));

      try {
         return treeWalk.next();
      } finally {
         treeWalk.release();
      }
   }
}
