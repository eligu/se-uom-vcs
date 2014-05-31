/**
 * 
 */
package se.uom.vcs.walker.filter.resource;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import se.uom.vcs.VCSResource;

/**
 * Abstract base class for path filtering of {@link VCSResource}s.
 * <p>
 * 
 * This class contains a list of String paths that are pre-formated when
 * created. That is, each path will not start or and with a '/' and the file
 * separator is '/'. However the specified paths may have a '\' as a file
 * separator and may start or end with '/' ('\').
 * <p>
 * It is recommended that all implementations of {@link VCSResourceFilter} that
 * deal with resource paths, subclass this as it would allow for making any
 * required optimizations, such as defining if two path filters in an AND clause
 * contains same filters.
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public abstract class AbstractPathFilter<T extends VCSResource> extends
      AbstractResourceFilter<T> {

   /**
    * Set of abstract paths.
    * <p>
    */
   protected Set<String> paths;

   /**
    * Creates a new filter based on the given paths.
    * <p>
    * 
    * @param paths
    *           to check each entity if it is contained within paths. Must not
    *           be null not or empty or contain a null path. Paths like '/' or
    *           '/+s/' or empty are not allowed.
    */
   public AbstractPathFilter(Collection<String> paths) {
      if (paths == null) {
         throw new IllegalArgumentException("paths must not be null");
      }
      if (paths.isEmpty()) {
         throw new IllegalArgumentException("paths must not be empty");
      }

      this.paths = new LinkedHashSet<String>();
      for (String path : paths) {
         this.paths.add(correctAndCheckPath(path));
      }
   }

   /**
    * Replace all '\' with '/' and remove '/' from start and end of the string.
    * Check path is a valid one. Paths like '/' or '/+s/' or empty are not
    * allowed.
    * <p>
    * 
    * @param path
    *           to check and correct
    * @return corrected path
    */
   public static String correctAndCheckPath(String path) {
      if (path == null) {
         throw new IllegalArgumentException("a path must not be null");
      }
      path = path.trim();
      if (path.isEmpty()) {
         throw new IllegalArgumentException("a path must not be empty");
      }

      path = path.replace('\\', '/');
      if (path.charAt(0) == '/') {
         if (path.length() == 1) {
            throw new IllegalArgumentException(
                  "/ or \\ is not allowed as a path");
         }
         path = path.substring(1).trim();
      }
      if (path.charAt(path.length() - 1) == '/') {
         if (path.length() == 1) {
            throw new IllegalArgumentException(
                  "/ or \\ is not allowed as a path");
         }
         path = path.substring(0, path.length() - 1).trim();
      }
      return path;
   }

   /**
    * Check whether <code>prefix</code> is a path prefix of <code>path</code>.
    * <p>
    * This method will assume each path will have as a line separator a '/'. The
    * path must be formatted as in {@link #correctAndCheckPath(String)}.
    * 
    * @param prefix
    * @param path
    * @return true if <code>prefix</code> is a path prefix of <code>path</code>
    */
   public static boolean isPrefix(String prefix, String path) {

      if (prefix.length() > path.length()) {
         return false;
      }

      if (prefix.length() == path.length()) {
         return prefix.equals(path);
      }

      char last = path.charAt(prefix.length());
      if (last != '/') {
         return false;
      }

      int i = prefix.length();
      while (i-- != 0) {
         if (prefix.charAt(i) != path.charAt(i)) {
            return false;
         }
      }

      return true;
   }

   /**
    * @return the paths of this filter
    */
   public Collection<String> getPaths() {
      return paths;
   }

   /**
    * {@inheritDoc}
    * <p>
    * This method will return true if only one of the supplied paths is a prefix
    * of this resource or a subpath. That will ensure that we don't accidentally
    * interrupt tree walking for a given resource. However subclasses are not
    * required to include this resource.
    */
   @Override
   public boolean enter(T resource) {
      return allowThisResource(resource);
   }

   /**
    * Care should be taken here to not accidentally prevent the tree walking
    * when a given resource is a prefix of one of the given paths. If the given
    * resource is a prefix of one of the supplied paths than it should be
    * allowed. If the given resource is a sub prefix of the supplied paths than
    * it should be allowed too.
    * <p>
    * Subclasses may override this in case another strategy should be
    * followed. This method is called by {@link #enter(VCSResource)}.
    * 
    * @param resource
    *           to check if this should be entered
    * @return true if the given resource is a prefix or a sub path of the given
    *         paths.
    */
   protected boolean allowThisResource(T resource) {

      String path = resource.getPath();
      if(this.paths.contains(path)) {
         return true;
      }
      for (String prefix : paths) {
         if (isPrefix(prefix, path) || isPrefix(path, prefix)) {
            return true;
         }
      }
      return false;
   }

   /**
    * In case when a not filter of a specified path filter should be created,
    * then the "enter" strategy is not just a NOT(enter).
    * <p>
    * 
    * Generally speaking any sub path of the given paths this filter contains
    * should not be entered. Subclasses may use this method if it is appropriate
    * (this method is provided as a general strategy for "not entering" and is
    * not used within this class) or may provide one that is more suited to
    * their case.
    * 
    * @param path
    *           the path to check
    * @return true if this not filter should enter the specified path
    */
   protected boolean notEnter(String path) {

      if(this.paths.contains(path)) {
         return true;
      }
      for (String prefix : paths) {
         if (isPrefix(prefix, path)) {
            return false;
         }
      }
      return true;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((paths == null) ? 0 : paths.hashCode());
      return result;
   }

   /**
    * Two filters are considered equals if they are of the same class, and
    * contains the same paths.
    * <p>
    */
   @Override
   public boolean equals(Object obj) {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      @SuppressWarnings("unchecked")
      AbstractPathFilter<T> other = (AbstractPathFilter<T>) obj;
      if (paths == null) {
         if (other.paths != null)
            return false;
      } else if (!paths.equals(other.paths))
         return false;
      return true;
   }
}
