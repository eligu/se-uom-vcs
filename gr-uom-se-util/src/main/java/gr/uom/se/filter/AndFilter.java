/**
 * 
 */
package gr.uom.se.filter;

import gr.uom.se.util.validation.ArgsCheck;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Elvis
 * @param <T>
 * 
 */
public class AndFilter<T> implements Filter<T> {

   private Set<Filter<T>> filters = new HashSet<>();

   @SafeVarargs
   public AndFilter(Filter<T>... filters) {
      if (filters != null) {
         for (Filter<T> f : filters) {
            ArgsCheck.notNull("filter", f);
            this.filters.add(f);
         }
      }
   }

   public AndFilter(Collection<Filter<T>> filters) {
      if (filters != null) {
         for (Filter<T> f : filters) {
            ArgsCheck.notNull("filter", f);
            this.filters.add(f);
         }
      }
   }
   
   @Override
   public boolean accept(T t) {
      for(Filter<T> f : filters) {
         if(!f.accept(t)) {
            return false;
         }
      }
      return true;
   }

}
