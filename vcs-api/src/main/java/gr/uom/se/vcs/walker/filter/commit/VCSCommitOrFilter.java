/**
 * 
 */
package gr.uom.se.vcs.walker.filter.commit;

import gr.uom.se.vcs.VCSCommit;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;


/**
 * A simple implementation of OR operator based on the given filters.
 * <p>
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public class VCSCommitOrFilter implements
      VCSCommitFilter {

   /**
    * The set of filters that will be applied OR.
    * <p>
    */
   private final Set<VCSCommitFilter> filters;

   /**
    * Create a new filter that will check if any of the given filters returns
    * true.
    * <p>
    * 
    * @param filters
    *           the filters to check if any of the filters returns true. Must
    *           not be null and must contain at least two filters.
    */
   public VCSCommitOrFilter(Collection<VCSCommitFilter> filters) {
      if (filters == null) {
         throw new IllegalArgumentException("filters must not be null");
      }
      if (filters.isEmpty()) {
         throw new IllegalArgumentException("filters must not be empty");
      }

      this.filters = new LinkedHashSet<VCSCommitFilter>();

      for (VCSCommitFilter f : filters) {
         if (f == null) {
            throw new IllegalArgumentException("filters must not contain null");
         }
         this.extract(f);
      }
      if (this.filters.size() < 2) {
         throw new IllegalArgumentException(
               "filters must contain at least two elements");
      }
   }

   /**
    * Extract recursively the given filters.
    * <p>
    * 
    * If there are nested OR then they will be extracted to this OR. That is, if
    * the given filter is instance of this class all its filters will be added
    * to this instance filters.
    * 
    * @param filter
    *           to extract any nested OR.
    */
   private void extract(VCSCommitFilter filter) {
      // If the specified filter is an OR filter, then it will
      // extract all the filters contained in this filter
      if (this.getClass().isAssignableFrom(filter.getClass())) {
         VCSCommitOrFilter orFilter = ((VCSCommitOrFilter) filter);
         for (VCSCommitFilter f : orFilter.filters) {
            extract(f);
         }
      } else {
         this.filters.add(filter);
      }
   }

   /**
    * @return an unmodifiable set of filters of this OR operator
    */
   public Set<VCSCommitFilter> getFilters() {
      return Collections.unmodifiableSet(this.filters);
   }

   /**
    * {@inheritDoc}
    * <p>
    * Returns true if any of the specified filters (during creation) returns
    * true.
    */
   @Override
   public boolean include(VCSCommit entity) {

      for (VCSCommitFilter f : filters) {
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
      VCSCommitOrFilter other = (VCSCommitOrFilter) obj;
      if (filters == null) {
         if (other.filters != null)
            return false;
      } else if (!filters.equals(other.filters))
         return false;
      return true;
   }
}
