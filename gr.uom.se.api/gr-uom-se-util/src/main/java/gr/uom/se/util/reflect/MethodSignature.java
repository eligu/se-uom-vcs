/**
 * 
 */
package gr.uom.se.util.reflect;

import java.util.Arrays;

/**
 * A general utility class that contains info about a method signature.
 * <p>
 * A method signature is considered its name, its return type and its
 * parameters.
 * 
 * @author Elvis Ligu
 * 
 */
public class MethodSignature {

   private Class<?> returnType;
   private String name;
   private Class<?>[] parameterTypes;

   public MethodSignature(Class<?> returnType, String name,
         Class<?>[] parameterTypes) {
      this.returnType = returnType;
      this.name = name;
      this.parameterTypes = parameterTypes;
   }

   /**
    * @return the type of the method
    */
   public Class<?> getReturnType() {
      return returnType;
   }

   /**
    * @return the simple name of the method
    */
   public String getName() {
      return name;
   }

   /**
    * @return the parameter types of the method
    */
   public Class<?>[] getParameterTypes() {
      return parameterTypes;
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
      result = prime * result + ((name == null) ? 0 : name.hashCode());
      result = prime * result + Arrays.hashCode(parameterTypes);
      result = prime * result
            + ((returnType == null) ? 0 : returnType.hashCode());
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
      MethodSignature other = (MethodSignature) obj;
      if (name == null) {
         if (other.name != null)
            return false;
      } else if (!name.equals(other.name))
         return false;
      if (!Arrays.equals(parameterTypes, other.parameterTypes))
         return false;
      if (returnType == null) {
         if (other.returnType != null)
            return false;
      } else if (!returnType.equals(other.returnType))
         return false;
      return true;
   }
}
