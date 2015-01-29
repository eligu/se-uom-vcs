/**
 * 
 */
package gr.uom.se.util.reflect;

import gr.uom.se.util.filter.Filter;
import gr.uom.se.util.filter.NotFilter;
import gr.uom.se.util.validation.ArgsCheck;
import gr.uom.se.util.validation.ContainmentCheck;

import java.lang.reflect.Member;
import java.util.Collection;
import java.util.HashSet;

/**
 * A filter that will allow only methods of specific classes.
 * <p>
 * 
 * @author Elvis Ligu
 * 
 */
public class MemberOfFilter<T extends Member> implements Filter<T> {

   /**
    * Filter to allow only {@link Object} methods.
    * <p>
    * Use a {@link NotFilter} to reverse the effect of this filter.
    */
   @SuppressWarnings({ "unchecked", "rawtypes" })
   public static Filter<? extends Member> OBJECT_MEMBERS = new MemberOfFilter(
         Object.class);

   /**
    * The types to allow the methods of.
    * <p>
    */
   private Collection<Class<?>> types;

   /**
    * Create a new instance providing types to filter their methods.
    * <p>
    * 
    * @param types
    *           must not be null not or empty
    */
   public MemberOfFilter(Class<?>... types) {
      ArgsCheck.notNull("types", types);
      ArgsCheck.notEmpty("types", types);
      this.types = new HashSet<>();
      for (Class<?> t : types) {
         ArgsCheck.notNull("type", t);
         this.types.add(t);
      }
   }

   @Override
   public boolean accept(Member t) {
      for (Class<?> type : types) {
         type.getDeclaredMethods();
         if (ContainmentCheck.contains(type.getDeclaredMethods(), t)) {
            return true;
         }
      }
      return false;
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((types == null) ? 0 : types.hashCode());
      return result;
   }

   /*
    * (non-Javadoc)
    * 
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(Object obj) {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      MemberOfFilter<?> other = (MemberOfFilter<?>) obj;
      if (types == null) {
         if (other.types != null)
            return false;
      } else if (!types.equals(other.types))
         return false;
      return true;
   }
}
