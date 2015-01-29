/**
 * 
 */
package gr.uom.se.vcs.walker.filter;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Check whether any of filters include true to return the result.
 * <p>
 * 
 * This is a simple implementation of an OR operator that works in most
 * situations, and returns true if any of the specified filters returns true.
 * However, there are situations that an OR result is not defined simply as an
 * OR such as this class implements.
 * 
 * @author Elvis Ligu
 * @since 0.0.1
 * @version 0.0.1
 */
public class VCSOrFilter<T> implements VCSFilter<T> {

   /**
    * The filters to apply OR operator.
    * <p>
    */
   private final Set<VCSFilter<T>> filters;

   /**
    * Create a new filter that will check if any of the given filters returns
    * true.
    * <p>
    * 
    * @param filters
    *           the filters to check if any of the filters returns true. Must
    *           not be null not or empty or contain a null filter. Must contain
    *           at least two filters.
    */
   public VCSOrFilter(Collection<VCSFilter<T>> filters) {
      if (filters == null) {
         throw new IllegalArgumentException("filters must not be null");
      }
      if (filters.isEmpty()) {
         throw new IllegalArgumentException("filters must not be empty");
      }
      this.filters = new LinkedHashSet<VCSFilter<T>>();
      for (VCSFilter<T> f : filters) {
         if (f == null) {
            throw new IllegalArgumentException("filters must not contain null");
         }
         this.filters.add(f);
      }
      if (filters.size() < 2) {
         throw new IllegalArgumentException(
               "filters must contain at least  elements");
      }
   }

   /**
    * Return an unmodifiable set of these filters.
    * <p>
    * 
    * @return the filters
    */
   public Set<VCSFilter<T>> getFilters() {
      return Collections.unmodifiableSet(this.filters);
   }

   /**
    * {@inheritDoc} Returns true if any of the specified filters (during
    * creation) returns true.
    * <p>
    */
   @Override
   public boolean include(T entity) {

      for (VCSFilter<T> f : filters) {
         if (f.include(entity)) {
            return true;
         }
      }
      return false;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((filters == null) ? 0 : filters.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      VCSOrFilter<?> other = (VCSOrFilter<?>) obj;
      if (filters == null) {
         if (other.filters != null)
            return false;
      } else if (!filters.equals(other.filters))
         return false;
      return true;
   }
}
