/**
 * 
 */
package se.uom.vcs.jgit.walker.filter.resource;

import java.io.IOException;
import java.util.Collection;

import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.TreeFilter;

import se.uom.vcs.walker.filter.resource.ChildFilter;

/**
 * A special case of {@link ChildFilter} optimized for JGit library.
 * <p>
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public class JGitChildFilter extends JGitAbstractPathFilter {

   /**
    * Creates a new instance.
    * <p>
    * 
    * @param paths
    *           the set of paths to limit their children.
    */
   public JGitChildFilter(Collection<String> paths) {
      super(paths);
   }

   /**
    * Check if the given path is a child of prefix.
    * <p>
    * 
    * @param prefix
    *           the parent of path
    * @param path
    *           the child of prefix
    * @return true if the given path is a child of prefix
    */
   public static boolean isChild(byte[] prefix, byte[] path) {

      // Prefix length should be less than path length
      if (prefix.length >= path.length) {
         return false;
      }

      // If the character in path at prefix length is not
      // a line separator that means this path has no
      // prefix the given one
      if (path[prefix.length] != LINE_SEP) {
         return false;
      }

      // Find last char of '/'
      int i = prefix.length + 1;
      while (i < path.length) {
         if (path[i] == LINE_SEP) {
            break;
         }
         i++;
      }

      // If i is equal to path length that means no other '/'
      // char was found so we only have to check the
      // characters starting from last prefix
      // char
      if (i < path.length) {
         return false;
      }

      i = prefix.length;
      while (i-- != 0) {
         if (prefix[i] != path[i]) {
            return false;
         }
      }

      return true;
   }

/**
     *  {@inheritDoc)
     * @see TreeFilter#include(TreeWalk)
     */
   @Override
   public boolean include(TreeWalk walker) throws MissingObjectException,
         IncorrectObjectTypeException, IOException {
      byte[] path = walker.getRawPath();
      for (byte[] prefix : paths) {
         if (prefix.length > 0) {
            if (isPrefix(path, prefix) || isChild(prefix, path)) {
               return true;
            }
         } else {
            if (isRootPath(path)) {
               return true;
            }
         }
      }
      return false;
   }
}
