/**
 * 
 */
package gr.uom.se.vcs.walker.filter.resource;

import gr.uom.se.vcs.VCSResource;

/**
 * A base class for all resource filter classes.
 * <p>
 * 
 * This class defines protected methods, that can be used within this package
 * and or from any sub class. These protected methods are used to utilize the
 * filters and their usage. I.e. we can not combine with AND a path filter and a
 * prefix filter, because the two filters are mutually exclusive.
 * <p>
 * It is highly recommended that implementations of {@link VCSResourceFilter}
 * subclass this class. In cases when a given implementation of a filter can not
 * be combined with another one then it should be returned true from
 * {@link #excludesAnd(AbstractResourceFilter)}.
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public abstract class AbstractResourceFilter<T extends VCSResource> implements
      VCSResourceFilter<T> {

   /**
    * Check if the given filter can not be combined in an AND clause with this
    * one.
    * <p>
    * 
    * When the given filter excludes this one (modified and not modified for
    * example) then this method should return true.
    * <p>
    * The default implementation returns always false.
    * 
    * @param filter
    *           to check if excludes in AND this one
    * @return true if filter excludes this
    */
   protected boolean excludesAnd(AbstractResourceFilter<T> filter) {
      return false;
   }

   /**
    * Negates the result of this filter.
    * <p>
    * In most situation a simple NOT should work when filtering the resources,
    * however the <code>enter()</code> method is not just a simple not. For
    * example a path prefix filter allow every resource that is a prefix or a
    * sub path of one of the given paths to enter, however, a not prefix filter
    * should not stop entering prefixes of the given paths.
    * 
    * @return a special case of this filter, that revert the results
    */
   protected abstract VCSResourceFilter<T> not();
}
