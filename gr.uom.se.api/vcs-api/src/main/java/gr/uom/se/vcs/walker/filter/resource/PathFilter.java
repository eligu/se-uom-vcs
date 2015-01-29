/**
 * 
 */
package gr.uom.se.vcs.walker.filter.resource;

import gr.uom.se.vcs.VCSResource;

import java.util.Collection;


/**
 * Filters only the required paths.
 * <p>
 * 
 * This filter can be used to allow a certain path or a group of paths. Each
 * time a {@link VCSResource} is tested it will check if the path is contained
 * in the list of supplied paths when this filter was constructed. The paths
 * will be checked exactly, not matching or pattern processing will be done.
 * <p>
 * <b>WARNING:</b> This should not be used for checking if a given resource is a
 * sub resource in one of the supplied paths. Instead it will return true only
 * if the given resource's path is exactly the same as one of the supplied
 * paths. Use {@link PathPrefixFilter} filter in order to check if a given
 * resource is a sub resource of a given path.
 * 
 * This filter can be combined in an AND clause only with exact filters (those
 * that do not deal with paths).
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public class PathFilter<T extends VCSResource> extends
      ExactAbstractPathFilter<T> {

   /**
    * Creates a new filter based on given paths.
    * <p>
    * Using two or more filters it will include every resource that has a path
    * which is contained within this filter.
    * 
    * @param paths
    *           to check when a given resource will be included in the results.
    */
   public PathFilter(Collection<String> paths) {
      super(paths);
   }

   /**
    * {@inheritDoc}
    * <p>
    * Returns true only if the given resource's path is contained in the paths
    * specified when this filter was created.
    * <p>
    * The resource's path must have the same format as calling
    * {@link AbstractPathFilter#correctAndCheckPath(String)}.
    */
   @Override
   public boolean include(T entity) {

      String path = entity.getPath();
      return paths.contains(path);
   }

   /**
    * {@inheritDoc}
    * <p>
    * Will allow all paths to enter, even those that are contained in this
    * filter because we do not exclude sub resources of these paths, however
    * only paths that are not specified within this filter will be allowed to
    * visit.
    */
   @Override
   public VCSResourceFilter<T> not() {
      return new NotFilter<T>(paths);
   }

   /**
    * {@inheritDoc}
    * <p>
    * Can be combined only with an instance of exact filter.
    */
   @Override
   protected boolean excludesAnd(AbstractResourceFilter<T> filter) {
      if (filter instanceof ExactFilter) {
         return false;
      }
      return true;
   }

   private static class NotFilter<T extends VCSResource> extends PathFilter<T> {

      public NotFilter(Collection<String> paths) {
         super(paths);
      }

      @Override
      public boolean include(T entity) {
         return !super.include(entity);
      }

      @Override
      public boolean enter(T resource) {
         // We should allow to enter any path, even
         // those that are specified in this filter
         // because we do not exclude any sub resources
         // of those.
         return true;
      }

      @Override
      public VCSResourceFilter<T> not() {
         return new PathFilter<T>(paths);
      }
   }
}
