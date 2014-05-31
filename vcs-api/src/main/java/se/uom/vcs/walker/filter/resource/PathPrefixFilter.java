/**
 * 
 */
package se.uom.vcs.walker.filter.resource;

import java.util.Collection;

import se.uom.vcs.VCSResource;

/**
 * Filter all paths that are contained within a given path.
 * <p>
 * 
 * When applied this filter will check a given {@link VCSResource} if its path
 * has a prefix same as one of the given path prefixes.
 * <p>
 * This filter can not be combined in an AND clause with the following:
 * <ul>
 * <li>{@link PathPrefixFilter}</li>
 * <li>{@link PathFilter}</li>
 * <li>{@link ChildFilter}</li>
 * </ul>
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public class PathPrefixFilter<T extends VCSResource> extends
      AbstractPathFilter<T> {

   /**
    * Creates a filter for the given path prefixes.
    * <p>
    * 
    * @param paths
    *           prefixes to check each path against
    */
   public PathPrefixFilter(Collection<String> paths) {
      super(paths);
   }
   
   /**
    * {@inheritDoc}
    * Check if entity's path has one of the specified paths as a prefix.
    * <p>
    */
   @Override
   public boolean include(T entity) {
      String path = entity.getPath();
      for (String prefix : paths) {
         if (isPrefix(prefix, path)) {
            return true;
         }
      }
      return false;
   }

   @Override
   protected boolean excludesAnd(AbstractResourceFilter<T> filter) {

      if (filter instanceof ChildFilter) {
         return true;
      }
      if (filter instanceof PathFilter) {
         return true;
      }
      if (filter instanceof PathPrefixFilter) {
         return true;
      }
      return false;
   }

   @Override
   public VCSResourceFilter<T> not() {
      return new PathPrefixFilter<T>(paths) {
         @Override
         public boolean include(T entity) {
            return !super.include(entity);
         }

         @Override
         public boolean enter(T resource) {
            return super.notEnter(resource.getPath());
         }

         @Override
         public VCSResourceFilter<T> not() {
            return new PathPrefixFilter<T>(paths);
         }
      };
   }
}
