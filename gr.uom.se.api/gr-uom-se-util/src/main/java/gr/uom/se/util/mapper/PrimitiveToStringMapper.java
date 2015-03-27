/**
 * 
 */
package gr.uom.se.util.mapper;

import java.util.Date;
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
public class PrimitiveToStringMapper extends AbstractPrimitiveToStringMapper {

   
   @Override
   protected boolean isPrimitive(Class<?> type) {
      return primitivesToWrappers.containsKey(type)
            || primitivesToWrappers.containsValue(type);
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
   protected <T> T getPrimitiveValue(Class<T> type, String strval) {
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
   protected String getStringValue(Object val) {
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
      primitivesToWrappers.put(String.class, String.class);
      primitivesToWrappers.put(Date.class, Date.class);
   }
}
