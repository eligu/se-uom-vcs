/**
 * 
 */
package gr.uom.se.filter;

import gr.uom.se.util.validation.ArgsCheck;

/**
 * @author Elvis Ligu
 *
 */
public class NotFilter<T> implements Filter<T> {

   private Filter<T> filter;
   public NotFilter(Filter<T> filter) {
      ArgsCheck.notNull("filter", filter);
      this.filter = filter;
   }
   @Override
   public boolean accept(T t) {
      return !filter.accept(t);
   }
}
