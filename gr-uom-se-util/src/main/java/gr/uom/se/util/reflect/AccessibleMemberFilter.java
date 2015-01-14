package gr.uom.se.util.reflect;

import java.lang.reflect.Member;

import gr.uom.se.filter.Filter;
import gr.uom.se.util.validation.ArgsCheck;

/**
 * A filter that allows only members of a given class that are accessible by
 * another class.
 * <p>
 * 
 * @author Elvis Ligu
 * 
 * @param <T>
 */
public class AccessibleMemberFilter<T extends Member> implements Filter<T> {

   /**
    * The class that contains the member to check, or inherit it from one if its
    * super classes.
    * <p>
    */
   private Class<?> target;

   /**
    * The class that needs to access the underlying member, by using a reference
    * of (instance or) taarget class.
    * <p>
    */
   private Class<?> accessing;

   /**
    * Create a new filter given its parameters.
    * <p>
    * 
    * @param target
    *           the class to whom the member belongs
    * @param accessing
    *           the class who need to access a member of target
    */
   public AccessibleMemberFilter(Class<?> target, Class<?> accessing) {
      ArgsCheck.notNull("target", target);
      ArgsCheck.notNull("accessing", accessing);
      this.target = target;
      this.accessing = accessing;
   }

   @Override
   public boolean accept(T t) {
      return ReflectionUtils.isAccessible(t, target, accessing);
   }

   /* (non-Javadoc)
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result
            + ((accessing == null) ? 0 : accessing.hashCode());
      result = prime * result + ((target == null) ? 0 : target.hashCode());
      return result;
   }

   /* (non-Javadoc)
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
      AccessibleMemberFilter<?> other = (AccessibleMemberFilter<?>) obj;
      if (accessing == null) {
         if (other.accessing != null)
            return false;
      } else if (!accessing.equals(other.accessing))
         return false;
      if (target == null) {
         if (other.target != null)
            return false;
      } else if (!target.equals(other.target))
         return false;
      return true;
   }
}
