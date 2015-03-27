/**
 * 
 */
package gr.uom.se.util.mapper;

import gr.uom.se.util.validation.ArgsCheck;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A mapper factory keeps track of different available mappers.
 * <p>
 * 
 * To get a mapper for a given source to a given type use
 * {@link #getMapper(Class, Class)}. To register a mapper use
 * {@link #setMapper(Class, Class, Mapper)}. A better usage of this factory is
 * using the provider method to get the single instance, as a singleton. However
 * there are circumstances where a singleton pattern is not the preferred one,
 * hence the constructor is left public on purpose.
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public class MapperFactory {

   /**
    * The singleton instance.
    * <p>
    */
   private static MapperFactory factory = new MapperFactory();

   /**
    * The default mapper.
    * <p>
    */
   private static final PrimitiveToStringMapper defaultMapper = new PrimitiveToStringMapper();

   /**
    * The registered mappers.
    * <p>
    */
   private ConcurrentHashMap<Class<?>, Map<Class<?>, Mapper>> mappers = null;

   /**
    * Intentionally left public. DO NOT create instances with this
    * constructor. Instead use singleton method to get the instance.
    */
   public MapperFactory() {
      mappers = new ConcurrentHashMap<>();
   }

   /**
    * Get a mapper implementation that can convert a value of type {@code from}
    * to a value of type {@code to}.
    * <p>
    * If a mapper is not found for the given parameters then a type of
    * {@link PrimitiveToStringMapper} is returned.
    * 
    * @param from
    *           the type of source the mapper should map the new value
    * @param to
    *           the type of new value that should be mapped from source
    * @return a mapper
    */
   public <T, S> Mapper getMapper(Class<S> from, Class<T> to) {
      ArgsCheck.notNull("from", from);
      ArgsCheck.notNull("to", to);
      // We do not need synchronization here, the reason is that
      // we assume the mapper is checked for its presence at the
      // beginning so if the map fromConverters is not found this
      // is the same as the mapper is not found.
      Map<Class<?>, Mapper> fromConverters = mappers.get(from);
      Mapper mapper = null;
      if (fromConverters != null) {
         mapper = fromConverters.get(to);
      }
      if (mapper == null) {
         return defaultMapper;
      }
      return mapper;
   }

   /**
    * Register a mapper with the provided parameters to this factory.
    * <p>
    * 
    * @param from
    *           the type of source of the value to map from
    * @param to
    *           the type of value to be mapped
    * @param converter
    *           the converter that can map these types
    */
   public <T, S> void setMapper(Class<S> from, Class<T> to, Mapper converter) {
      Map<Class<?>, Mapper> fromConverters = mappers.get(from);
      ArgsCheck.notNull("from", from);
      ArgsCheck.notNull("to", to);
      if (fromConverters == null) {
         fromConverters = new ConcurrentHashMap<>();
         fromConverters = mappers.putIfAbsent(from, fromConverters);
      }
      fromConverters.put(to, converter);
   }

   /**
    * Get the singleton instance of this factory.
    * <p>
    * Use the available constructor if the singleton instance is not preferred.
    * 
    * @return
    */
   public static MapperFactory getInstance() {
      return factory;
   }
}
