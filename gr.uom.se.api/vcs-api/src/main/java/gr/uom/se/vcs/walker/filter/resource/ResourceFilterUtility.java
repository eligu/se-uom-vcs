/**
 * 
 */
package gr.uom.se.vcs.walker.filter.resource;

import gr.uom.se.vcs.VCSResource;
import gr.uom.se.vcs.VCSResource.Type;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * A utility class containing static methods that helps in constructing resource
 * filters.
 * <p>
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public class ResourceFilterUtility {

   /**
    * Creates a new path filter, given the specified paths.
    * <p>
    * 
    * @param paths
    *           a collection of paths. Must not be null, empty or contain any
    *           null path.
    * @return a new path filter
    * @see PathFilter
    */
   public static <T extends VCSResource> VCSResourceFilter<T> path(
         Collection<String> paths) {
      return new PathFilter<T>(paths);
   }

   /**
    * Creates a new path filter given the specified paths.
    * <p>
    * 
    * @param paths
    *           an array of paths. Must not be null, empty or contain any null
    *           path.
    * @return a new path filter
    * @see PathFilter
    */
   public static <T extends VCSResource> VCSResourceFilter<T> path(
         String... paths) {
      return new PathFilter<T>(Arrays.asList(paths));
   }

   /**
    * Creates a new suffix filter given the specified paths.
    * <p>
    * 
    * @param paths
    *           a collection of suffixes. Must not be null, empty or contain any
    *           null element.
    * @return a new suffix filter
    * @see SuffixFilter
    */
   public static <T extends VCSResource> VCSResourceFilter<T> suffix(
         Collection<String> paths) {
      return new SuffixFilter<T>(paths);
   }

   /**
    * Creates a new suffix filter given the specified paths.
    * <p>
    * 
    * @param paths
    *           an array of suffixes. Must not be null, empty or contain any
    *           null element.
    * @return a new suffix filter
    * @see SuffixFilter
    */
   public static <T extends VCSResource> VCSResourceFilter<T> suffix(
         String... paths) {
      return new SuffixFilter<T>(Arrays.asList(paths));
   }

   /**
    * Creates a new child filter given the specified paths.
    * <p>
    * 
    * @param paths
    *           a collection of paths. Must not be null, empty or contain any
    *           null element.
    * @return a new child filter
    * @see ChildFilter
    */
   public static <T extends VCSResource> VCSResourceFilter<T> child(
         Collection<String> paths) {
      return new ChildFilter<T>(paths);
   }

   /**
    * Creates a new child filter given the specified paths.
    * <p>
    * 
    * @param paths
    *           an array of paths. Must not be null, empty or contain any null
    *           element.
    * @return a new child filter
    * @see ChildFilter
    */
   public static <T extends VCSResource> VCSResourceFilter<T> child(
         String... paths) {
      return child(Arrays.asList(paths));
   }

   /**
    * Creates a new prefix filter given the specified paths.
    * <p>
    * 
    * @param paths
    *           a collection of paths. Must not be null, empty or contain any
    *           null element.
    * @return a new prefix filter
    * @see PathPrefixFilter
    */
   public static <T extends VCSResource> VCSResourceFilter<T> prefix(
         Collection<String> paths) {
      return new PathPrefixFilter<T>(paths);
   }

   /**
    * Creates a new prefix filter given the specified paths.
    * <p>
    * 
    * @param paths
    *           an array of paths. Must not be null, empty or contain any null
    *           element.
    * @return a new prefix filter
    * @see PathPrefixFilter
    */
   public static <T extends VCSResource> VCSResourceFilter<T> prefix(
         String... paths) {
      return new PathPrefixFilter<T>(Arrays.asList(paths));
   }

   /**
    * Creates a new AND filter given the specified filters.
    * <p>
    * 
    * @param filters
    *           a collection of filters. Must not be null, empty or contain any
    *           null element.
    * @return a new AND filter
    * @see VCSResourceAndFilter
    */
   public static <T extends VCSResource> VCSResourceFilter<T> and(
         Collection<VCSResourceFilter<T>> filters) {

      Set<VCSResourceFilter<T>> set = new HashSet<VCSResourceFilter<T>>();
      for (VCSResourceFilter<T> f : filters) {
         if (f == null) {
            throw new IllegalArgumentException("filters must not contain null");
         }
         set.add(f);
      }
      if (set.size() == 1) {
         return filters.iterator().next();
      }

      return new VCSResourceAndFilter<T>(set);
   }

   /**
    * Creates a new AND filter given the specified filters.
    * <p>
    * 
    * @param filters
    *           an array of filters. Must not be null, empty or contain any null
    *           element.
    * @return a new AND filter
    * @see VCSResourceAndFilter
    */
   @SafeVarargs
   public static <T extends VCSResource> VCSResourceFilter<T> and(
         VCSResourceFilter<T>... filters) {
      return and(Arrays.asList(filters));
   }

   /**
    * Creates a new OR filter given the specified filters.
    * <p>
    * 
    * @param filters
    *           a collection of filters. Must not be null, empty or contain any
    *           null element.
    * @return a new OR filter
    * @see VCSResourceOrFilter
    */
   public static <T extends VCSResource> VCSResourceFilter<T> or(
         Collection<VCSResourceFilter<T>> filters) {
      Set<VCSResourceFilter<T>> set = new HashSet<VCSResourceFilter<T>>();
      for (VCSResourceFilter<T> o : filters) {
         if (o == null) {
            throw new IllegalArgumentException("filters must not contain null");
         }
         set.add(o);
      }
      if (set.size() == 1) {
         return set.iterator().next();
      }
      return new VCSResourceOrFilter<T>(set);
   }

   /**
    * Creates a new OR filter given the specified filters.
    * <p>
    * 
    * @param filters
    *           an array of filters. Must not be null, empty or contain any null
    *           element.
    * @return a new OR filter
    * @see VCSResourceOrFilter
    */
   @SafeVarargs
   public static <T extends VCSResource> VCSResourceFilter<T> or(
         VCSResourceFilter<T>... filters) {
      return new VCSResourceOrFilter<T>(Arrays.asList(filters));
   }

   /**
    * Creates a new filter that checks if a resource is modified.
    * <p>
    * 
    * @return new ModifiedFilter
    * @see ModifiedFilter
    */
   public static VCSResourceFilter<VCSResource> modified() {
      return ModifiedFilter.getInstance();
   }

   /**
    * Creates a new type filter given the type.
    * <p>
    * 
    * @param type
    *           must not be null
    * @return a new type filter
    * @see TypeFilter
    */
   public VCSResourceFilter<VCSResource> type(Type type) {
      return new TypeFilter<VCSResource>(type);
   }
}
