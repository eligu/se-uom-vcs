/**
 * 
 */
package gr.uom.se.util.mapper;

import gr.uom.se.util.validation.ArgsCheck;

/**
 * @author Elvis Ligu
 */
public abstract class AbstractPrimitiveToStringMapper implements Mapper {

   /**
    * Return true if the given type is a primitive type known by this
    * implementation.
    * <p>
    * 
    * @param type
    * @return
    */
   protected abstract boolean isPrimitive(Class<?> type);

   /**
    * Given the required type and its string representation convert the string
    * to the given type.
    * <p>
    * 
    * @param type
    *           to be converted from string
    * @param strval
    *           the string representation of type
    * @return a value of the given type parsed by string
    */
   protected abstract <T> T getPrimitiveValue(Class<T> type, String strval);

   /**
    * Given the object, convert it to a string value.
    * <p>
    * 
    * @param val
    *           the object to be converted
    * @return the string representation of the given val object
    */
   protected abstract String getStringValue(Object val);

   /**
    * {@inheritDoc)
    */
   @SuppressWarnings("unchecked")
   @Override
   public <T, S> T map(S source, Class<T> to) {
      // If the requested source is null then return a null
      if (source == null) {
         return null;
      }
      ArgsCheck.notNull("to", to);

      // If the requested type is the same as
      // the source type, then return the source
      if (to.isAssignableFrom(source.getClass())) {
         return (T) source;
      }

      Class<?> from = source.getClass();
      Class<?> type = getComponent(to);

      // Case when the source is string
      if (String.class.isAssignableFrom(from)) {
         // Check if the required type is primitive or
         // a corresponding Java type.
         boolean contains = isPrimitive(type);
         // If it is primitive (or an array of primitives)
         // then convert the string to required type
         if (contains) {
            return getPrimitiveValue(to, source.toString());
         }
      } else if (to.isAssignableFrom(String.class)) {
         // Case when required type is string and
         // the source type is a primitive (a wrapper or an array of)
         type = getComponent(from);
         boolean contains = isPrimitive(type);
         // Convert the source to string
         if (contains) {
            return (T) getStringValue(source);
         }
      }

      return (T) source;
   }

   /**
    * Get the base component if the given type is a multidimensional array.
    * <p>
    * 
    * @param array
    * @return
    */
   public static Class<?> getComponent(Class<?> array) {
      if (!array.isArray()) {
         return array;
      }
      Class<?> component = array.getComponentType();
      while (true) {
         Class<?> type = component.getComponentType();
         if (type == null) {
            return component;
         }
         component = type;
      }
   }
}
