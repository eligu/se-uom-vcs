/**
 * 
 */
package se.uom.vcs.jgit.utils;

import java.io.IOException;

import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

import se.uom.vcs.VCSChange;
import se.uom.vcs.VCSResource;
import se.uom.vcs.exceptions.VCSRepositoryException;

/**
 * A utility class for JGit rev commits, and more.<p>
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public class RevUtils {

   /**
    * Check parents of the child if one of them is equal to specified parent
    * return true.
    * <p>
    * 
    * @param parent
    *           to check if its a parent of child
    * @param child
    *           to check if parent is its parent
    * @return true if <code>parent</code> is a parent of <code>child</code>
    */
   public static boolean isParent(final RevCommit parent, final RevCommit child) {

      for (final RevCommit p : child.getParents()) {
         if (AnyObjectId.equals(parent, p)) {
            return true;
         }
      }
      return false;
   }

   /**
    * Check commit1 is ancestor of commit2.
    * <p>
    * 
    * @param commit1
    *           the ancestor to check
    * @param commit2
    *           the descendant of commit2
    * @param repo
    *           from where these commits comes from
    * @return true if commit1 is ancestor of commit2
    * @throws VCSRepositoryException
    */
   public static boolean isAncestor(final RevCommit commit1,
         final RevCommit commit2, final Repository repo)
         throws VCSRepositoryException {

      // Create a revision walk and check if commit1
      // is reachable from commit2
      final RevWalk walk = new RevWalk(repo);

      try {

         final RevCommit bHEAD = walk.parseCommit(commit2.getId());
         final RevCommit rCommit = walk.parseCommit(commit1);

         // Check first if this commit is equal to head
         if (AnyObjectId.equals(rCommit, bHEAD)) {
            return true;
         }

         if (rCommit.getCommitTime() > bHEAD.getCommitTime()) {
            return false;
         }

         // Start the walk from the head
         walk.markStart(bHEAD);
         walk.setRetainBody(false);

         // Walk the commits until we find the given commit
         RevCommit current = null;
         while ((current = walk.next()) != null) {

            if (AnyObjectId.equals(rCommit, current)) {
               return true;
            }
         }

         // We have walked all commits and found nothing that has the same id
         // as the given one
         return false;

      } catch (final MissingObjectException e) {
         throw new VCSRepositoryException(e);
      } catch (final IncorrectObjectTypeException e) {
         throw new VCSRepositoryException(e);
      } catch (final IOException e) {
         throw new VCSRepositoryException(e);
      } finally {
         walk.release();
      }
   }

   /**
    * Get the parents of this commit.
    * <p>
    * 
    * @param child
    *           the commit to get the parents
    * @param repo
    *           from where this commit comes from
    * @return an array of <code>child</code>'s parents
    * @throws MissingObjectException
    * @throws IncorrectObjectTypeException
    * @throws IOException
    */
   public static RevCommit[] parentOf(final RevCommit child,
         final Repository repo) throws MissingObjectException,
         IncorrectObjectTypeException, IOException {
      final RevWalk walk = new RevWalk(repo);

      try {
         final RevCommit commit = walk.parseCommit(child.getId());
         return commit.getParents();
      } finally {
         walk.release();
      }
   }

   /**
    * Convert to {@link VCSChange#Type}.
    * <p>
    * 
    * @param type
    *           of change
    * @return a {@link VCSChange.Type}
    */
   public static VCSChange.Type changeType(final ChangeType type) {

      switch (type) {
      case ADD:
         return VCSChange.Type.ADDED;
      case DELETE:
         return VCSChange.Type.DELETED;
      case COPY:
         return VCSChange.Type.COPIED;
      case MODIFY:
         return VCSChange.Type.MODIFIED;
      case RENAME:
         return VCSChange.Type.RENAMED;
      default:
         return VCSChange.Type.NONE;
      }
   }

   /**
    * Convert to {@link VCSResource#Type}.
    * <p>
    * 
    * @param mode
    *           of file
    * @return the type of resource
    */
   public static VCSResource.Type resourceType(final FileMode mode) {

      if (mode.equals(FileMode.REGULAR_FILE.getBits())
            || mode.equals(FileMode.EXECUTABLE_FILE.getBits())) {
         return VCSResource.Type.FILE;
      } else if (mode.equals(FileMode.TREE.getBits())) {
         return VCSResource.Type.DIR;
      }

      return VCSResource.Type.NONE;
   }

   /**
    * Return true only if this is a {@link FileMode#TREE} mode.
    * <p>
    * 
    * @param mode
    *           to check if this is a directory
    * @return true if this mode is a directory
    */
   public static boolean isDirMode(final FileMode mode) {
      return mode.equals(FileMode.TREE.getBits());
   }

   /**
    * If entry's mode is {@link FileMode#EXECUTABLE_FILE} or
    * {@link FileMode#REGULAR_FILE} then this will return true.
    * <p>
    * 
    * @param mode
    *           to check if it is a file
    * @return true if this mode is a file
    */
   public static boolean isFileMode(final FileMode mode) {
      return mode.equals(FileMode.EXECUTABLE_FILE.getBits())
            || mode.equals(FileMode.REGULAR_FILE.getBits());
   }

}
