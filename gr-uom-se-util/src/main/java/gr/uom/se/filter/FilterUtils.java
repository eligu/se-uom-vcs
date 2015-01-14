/**
 * 
 */
package gr.uom.se.filter;

import java.util.Collection;

/**
 * @author Elvis Ligu
 *
 */
public class FilterUtils {
   
   private static final NullFilter<?> NULL_FILTER = new NullFilter<>();
   
   @SafeVarargs
   public static <T> Filter<T> and(Filter<T>... filters) {
      return new AndFilter<T>(filters);
   }
   
   public static <T> Filter<T> and(Collection<Filter<T>> filters) {
      return new AndFilter<T>(filters);
   }
   
   @SafeVarargs
   public static <T> Filter<T> or(Filter<T>... filters) {
      return new OrFilter<T>(filters);
   }
   
   public static <T> Filter<T> or(Collection<Filter<T>> filters) {
      return new OrFilter<T>(filters);
   }
   
   public static <T> Filter<T> not(Filter<T> filter) {
      return new NotFilter<T>(filter);
   }
   
   @SuppressWarnings("unchecked")
   public static <T> Filter<T> NULL() {
      return (Filter<T>) NULL_FILTER;
   }
}
