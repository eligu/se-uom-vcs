/**
 * 
 */
package gr.uom.se.vcs.walker.filter.resource;

import gr.uom.se.vcs.VCSResource;
import gr.uom.se.vcs.walker.filter.VCSFilter;

/**
 * Base interface used to filter the resources returned when tree walking is
 * required.
 * <p>
 * 
 * This is a marker interface and also limits parameter types to
 * {@link VCSResource}. All resource filters must implement this interface in
 * order to be used by a visitor.
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public interface VCSResourceFilter<T extends VCSResource> extends VCSFilter<T> {

   /**
    * A special case method that optimizes checking for a resource.
    * <p>
    * 
    * Implementations should use this method to allow the entering in a given
    * path (usually in a directory). This method should not be used for
    * filtering purposes (instead use {@link #include(Object)}. In case when a
    * given filter is a path filter, then it should enter only the paths that
    * are prefixes of the given filter and those that are sub paths of them.
    * i.e. having a path prefix for src/path this method should return true for
    * all prefixes (src,src/path) and for all sub paths (src/path/folder...).
    * This will allow the implementation of tree walk to make optimizations such
    * as not enter any other path (suppose /test/path) that is not necessary.
    * However, any other filter that does not deal with paths should always
    * return true.
    * 
    * @param resource
    *           to check if this is eligible for filtering
    * @return true if the given resource is eligible for filtering
    */
   public boolean enter(T resource);
}
