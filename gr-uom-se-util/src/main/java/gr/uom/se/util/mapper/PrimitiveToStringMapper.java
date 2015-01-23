/**
 * 
 */
package gr.uom.se.util.mapper;

import gr.uom.se.util.validation.ArgsCheck;

import java.util.HashMap;
import java.util.Map;

import net.minidev.json.JSONValue;
import net.minidev.json.parser.ParseException;

/**
 * A mapper from primitive to string and vice versa.
 * <p>
 * This mapper can map any given string representation of a primitive type (or a
 * corresponding Java type) to a primitive instance. Also any given array of
 * primitive types (multidimensional to) can be mapped with a string to where a
 * string representation would be [ [1 2 3], [1 2], [1] ] like.
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public class PrimitiveToStringMapper implements Mapper {

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
         boolean contains = primitivesToWrappers.containsKey(type)
               || primitivesToWrappers.containsValue(type);
         // If it is primitive (or an array of primitives)
         // then convert the string to required type
         if (contains) {
            return getPrimitiveValue(to, source.toString());
         }
      } else if (to.isAssignableFrom(String.class)) {
         // Case when required type is string and
         // the source type is a primitive (a wrapper or an array of)
         type = getComponent(from);
         boolean contains = primitivesToWrappers.containsKey(type)
               || primitivesToWrappers.containsValue(type);
         // Convert the source to string
         if (contains) {
            return (T) getStringValue(source);
         }
      }
      throw new IllegalArgumentException("can not convert " + from + " " + to);
   }

   /**
    * Use json-smart library to extract a primitive type from a string value.
    * <p>
    * 
    * @param type
    * @param strval
    * @return
    */
   @SuppressWarnings("unchecked")
   static <T> T getPrimitiveValue(Class<T> type, String strval) {
      try {

         return (T) JSONValue.parseWithException(strval, wrapType(type));
      } catch (ParseException ex) {
         throw new IllegalArgumentException(ex);
      }
   }

   /**
    * Use json-smart library to return a primitive value (or an array of those)
    * to a string.
    * <p>
    * 
    * @param val
    * @return
    */
   static String getStringValue(Object val) {
      return JSONValue.toJSONString(val);
   }

   /**
    * If the given type is a primitive type, wrap it to its corresponding Java
    * type.
    * <p>
    * 
    * @param type
    *           the type to wrap
    * @return the wrapped primitive
    */
   static Class<?> wrapType(Class<?> type) {
      Class<?> wrapper = primitivesToWrappers.get(type);
      if (wrapper != null) {
         return wrapper;
      }
      return type;
   }

   /**
    * Mapped primitives to their Java objects.
    * <p>
    */
   private final static Map<Class<?>, Class<?>> primitivesToWrappers = new HashMap<>();
   static {
      primitivesToWrappers.put(int.class, Integer.class);
      primitivesToWrappers.put(double.class, Double.class);
      primitivesToWrappers.put(float.class, Float.class);
      primitivesToWrappers.put(long.class, Long.class);
      primitivesToWrappers.put(short.class, Short.class);
      primitivesToWrappers.put(char.class, Character.class);
      primitivesToWrappers.put(boolean.class, Boolean.class);
      primitivesToWrappers.put(byte.class, Byte.class);
   }

   /**
    * Get the base component if the given type is a multidimensional array.
    * <p>
    * 
    * @param array
    * @return
    */
   private static Class<?> getComponent(Class<?> array) {
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
