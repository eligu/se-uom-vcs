/**
 * 
 */
package gr.uom.se.util.reflect;

import gr.uom.se.util.filter.Filter;
import gr.uom.se.util.validation.ArgsCheck;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Implementation of method arguments filter.
 * <p>
 * 
 * This filter allow all members that have a specific signature. To specify a
 * signature pass into the constructor a number of {@link MethodSignature}
 * objects that describe each method. The method signature is considered to be
 * only its name, parameter types and return type.
 * 
 * @author Elvis Ligu
 * 
 */
public class MethodArgsFilter implements Filter<Method> {

   private Set<MethodSignature> signatures = new HashSet<>();

   /**
    * Create a new instance base on the given signatures.
    * <p>
    * 
    * @param signatures
    *           may not be null not or empty
    */
   public MethodArgsFilter(MethodSignature... signatures) {
      ArgsCheck.notNull("signatures", signatures);
      ArgsCheck.notEmpty("signatures", signatures);
      for (MethodSignature s : signatures) {
         ArgsCheck.notNull("signature", s);
         this.signatures.add(s);
      }
   }

   @Override
   public boolean accept(Method t) {
      Class<?>[] params = t.getParameterTypes();
      for (MethodSignature s : this.signatures) {
         String sname = s.getName();
         if (sname == null) {
            sname = t.getName();
         }
         Class<?> sreturnType = s.getReturnType();
         if (sreturnType == null) {
            sreturnType = t.getReturnType();
         }
         Class<?>[] sparams = s.getParameterTypes();
         if (sparams == null) {
            sparams = params;
         }
         if (sname.equals(t.getName()) && Arrays.equals(params, sparams)
               && sreturnType.equals(t.getReturnType())) {
            return true;
         }
      }
      return false;
   }

   /* (non-Javadoc)
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result
            + ((signatures == null) ? 0 : signatures.hashCode());
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
      MethodArgsFilter other = (MethodArgsFilter) obj;
      if (signatures == null) {
         if (other.signatures != null)
            return false;
      } else if (!signatures.equals(other.signatures))
         return false;
      return true;
   }
}
