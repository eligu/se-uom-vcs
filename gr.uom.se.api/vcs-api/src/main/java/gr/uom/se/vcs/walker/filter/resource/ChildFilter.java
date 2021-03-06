/**
 * 
 */
package gr.uom.se.vcs.walker.filter.resource;

import gr.uom.se.vcs.VCSResource;

import java.util.Collection;
import java.util.Set;


/**
 * A special case of prefix filter, that allows only the children of the
 * specified paths.
 * <p>
 * 
 * In case when only the children of same specified paths should be allowed then
 * use this filter. This filter can not be combined in an AND clause with the
 * following:
 * <ul>
 * <li>{@link PathPrefixFilter}</li>
 * <li>{@link PathFilter}</li>
 * <li>{@link ChildFilter}</li>
 * </ul>
 * Although, it is allowed to use this filter in an OR clause with a prefix
 * filter that contains same paths, it is discouraged. In case you have a set of
 * paths and want only their children specify these paths in a child filter. If
 * a prefix is specified in another prefix filter and is contained within a
 * child filter (within a clause AND) than the child filter will take
 * precedence.
 * <p>
 * The usage of NON child filter in combination with other path filters is
 * discouraged, as it will not enter any sub paths of the given paths, but it
 * will enter any other paths including those allowed by all other specified
 * path filters and those not specified, i.e.:
 * 
 * <pre>
 * VCSResourceFilter&lt;VCSResource&gt; filter = filters.or(
 * // allow all under java/test
 *       filters.and(filters.prefix(&quot;java/test&quot;),
 *             filters.type(VCSResource.Type.FILE)),
 *       // allow files under java/main that are .java
 *       filters.and(filters.prefix(&quot;java/main&quot;), filters.suffix(&quot;.java&quot;),
 *             filters.type(VCSResource.Type.FILE)),
 *       // or paths
 *       filters.path(&quot;src/java/test.java&quot;, &quot;src/main/java&quot;),
 *       // or children of java/path that ends with .txt
 *       filters.and(filters.childFilter(&quot;java/path&quot;), filters.suffix(&quot;.txt&quot;)));
 * 
 * // and not children of java/main
 * filter = filters.and(filter, filters.childFilter(&quot;java/main&quot;).not());
 * </pre>
 * 
 * The above filter will work as expected for any given resource, it should
 * allow all resources that ends with .java under 'java/main' prefix, excluding
 * the children of 'java/main'. However the enter method will allow everything
 * that is not under 'java/main' (because of NOT child filter) that means the
 * whole tree should be walked, because the AND clause enters a resource if any
 * of the specified filters allows it. Thus, we should avoid using NOT of this
 * filter in combination with other path filters.
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public class ChildFilter<T extends VCSResource> extends
      ExactAbstractPathFilter<T> {

   /**
    * Create a new instance with the specified paths.
    * <p>
    * 
    * @param paths
    *           the parent of the children we want to filter
    */
   public ChildFilter(Collection<String> paths) {
      super(paths);
   }

   /**
    * {@inheritDoc}
    * <p>
    * Check the path of the entity's parent if it is contained in paths set.
    */
   @Override
   public boolean include(T entity) {

      VCSResource parent = entity.getParent();

      if (parent == null) {
         int index = entity.getPath().lastIndexOf("/");
         if (index > 0) {
            return paths.contains(entity.getPath().substring(0, index));
         } else {
            return false;
         }
      } else {
         return paths.contains(parent.getPath());
      }
   }

   /**
    * {@inheritDoc}
    * <p>
    * Care should be taken when using a NOT filter of this one. It will enter
    * all resources of a given tree, however it should not include any children
    * of the specified paths.
    */
   @Override
   public VCSResourceFilter<T> not() {
      return new NotChild<T>(paths);
   }

   @Override
   public boolean enter(T resource) {
      String path = resource.getPath();

      if (path != null) {
         for (String prefix : paths) {
            if (isPrefix(path, prefix)) {
               return true;
            }
         }
      }
      return false;
   }

   /**
    * {@inheritDoc}
    * <p>
    * This filter can not be combined in an AND clause with the following:
    * <ul>
    * <li>{@link PathPrefixFilter}</li>
    * <li>{@link PathFilter}</li>
    * <li>{@link ChildFilter}</li>
    * </ul>
    */
   @Override
   protected boolean excludesAnd(AbstractResourceFilter<T> filter) {

      // The path prefix filter can not be combined with AND because a
      // resource can not be a sub
      // resource of a prefix and a children of another prefix. Unless all
      // paths of this filter
      // are the same as those of PathPrefixFilter, in which case a
      // ChildFilter should be used,
      // since the path prefix filter is redundant
      if (filter instanceof PathPrefixFilter) {
         return true;
      }

      // In this case the paths of path filter should be
      // children of this filter paths. However the path filter
      // take precedence so this child filter should be redundant 
      if (filter instanceof PathFilter) {
         return true;
      }
      
      // A path can not be a child of two different paths,
      // If all paths are the same or they contains same paths,
      // it is redundant to use two child filters 
      if (filter instanceof ChildFilter) {
         return true;
      }

      return false;
   }

   /**
    * A special case of a negate filter for a child filter.
    * <p>
    * 
    * @author Elvis Ligu
    * @version 0.0.1
    * @since 0.0.1
    * @param <T>
    */
   private static class NotChild<T extends VCSResource> extends ChildFilter<T> {

      public NotChild(Collection<String> paths) {
         super(paths);
      }

      @Override
      public boolean include(T entity) {
         return !super.include(entity);
      }

      @Override
      public boolean enter(T resource) {
         return true;
      }

      @Override
      public VCSResourceFilter<T> not() {
         return new ChildFilter<T>(paths);
      }

      @Override
      protected boolean excludesAnd(AbstractResourceFilter<T> filter) {
         if (filter instanceof ChildFilter) {
            Set<?> ps = ((ChildFilter<T>) filter).paths;
            for (String p : paths) {
               if (ps.contains(p)) {
                  return true;
               }
            }
         }
         if (filter instanceof PathFilter) {
            Set<String> ps = ((PathFilter<T>) filter).paths;
            for (String path : ps) {
               int index = path.lastIndexOf("/");
               if (index > 0) {
                  if (paths.contains(path.subSequence(0, index))) {
                     return true;
                  }
               }
            }
         }
         return this.equals(filter);
      }
   }
}
