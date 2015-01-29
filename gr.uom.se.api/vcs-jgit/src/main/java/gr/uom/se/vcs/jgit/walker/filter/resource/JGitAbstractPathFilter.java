package gr.uom.se.vcs.jgit.walker.filter.resource;

import gr.uom.se.util.validation.ArgsCheck;
import gr.uom.se.vcs.walker.filter.resource.AbstractPathFilter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.TreeFilter;


/**
 * An abstract class for all custom path filters used by JGit library.
 * <p>
 * 
 * This class will check each path by comparing their byte form.
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public abstract class JGitAbstractPathFilter extends TreeFilter {

   /**
    * The paths to check for children in bytes.
    * <p>
    */
   protected byte[][] paths;
   /**
    * The unix like line separator '/'.
    * <p>
    */
   public static final byte LINE_SEP = 0x2f; // '/' char
   /**
    * The encoding of paths.
    * <p>
    */
   public static final String ENCODING = "UTF-8";

   /**
    * If the walk will be recursive or not.
    * <p>
    */
   protected boolean recursive = true;

   /**
    * Creates a new instance.
    * <p>
    * 
    * @param paths
    *           the set of paths to limit their children.
    */
   public JGitAbstractPathFilter(Collection<String> paths) {
      
      ArgsCheck.notNull("paths", paths);
      ArgsCheck.notEmpty("paths", paths);
      
      Set<String> temp = new LinkedHashSet<String>();
      for (String p : paths) {
         ArgsCheck.notNull("path", p);
         temp.add(AbstractPathFilter.correctAndCheckPath(p));
      }

      this.paths = new byte[temp.size()][];
      int i = 0;
      for (String path : temp) {
         try {
            this.paths[i++] = path.getBytes(ENCODING);
         } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(ENCODING + " is not supported");
         }
      }
   }
   
   /**
    * Check if the given path is a root path.
    * <p>
    * 
    * A root path has no path separator in it. We assume the path doesn't start
    * with a separator.
    * 
    * @param path
    *           to check if it's root
    * @return true if the given path is root
    */
   public static boolean isRootPath(byte[] path) {
      // If we find any '/' that means
      // this is not a root path
      int i = 0;
      while (i < path.length) {
         if (path[i] == LINE_SEP) {
            return false;
         }
         i++;
      }
      return true;
   }

   /**
    * Check if the given prefix is a path prefix of the given path.
    * <p>
    * 
    * A path prefix may be a prefix of a path even if the two are equals.
    * 
    * @param prefix
    *           to check the path against
    * @param path
    *           to check for prefix
    * @return true if the first argument is prefix of the last
    */
   public static boolean isPrefix(byte[] prefix, byte[] path) {

      // Prefix length should be less than path length
      if (prefix.length > path.length) {
         return false;
      } else if (prefix.length == path.length) {
         return Arrays.equals(prefix, path);
      }

      // If the character in path at prefix length is not
      // a line separator that means this path has no
      // prefix the given one
      if (path[prefix.length] != LINE_SEP) {
         return false;
      }

      // If i is equal to path that means no other '/'
      // char was found so we only have to check the
      // characters starting from last prefix
      // char
      int i = prefix.length;
      while (i-- != 0) {
         if (prefix[i] != path[i]) {
            return false;
         }
      }
      return true;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public abstract boolean include(TreeWalk walker)
         throws MissingObjectException, IncorrectObjectTypeException,
         IOException;

   @Override
   public boolean shouldBeRecursive() {
      return recursive;
   }

   public void setRecursive(boolean recursive) {
      this.recursive = recursive;
   }

   @Override
   public TreeFilter clone() {
      return this;
   }
}
