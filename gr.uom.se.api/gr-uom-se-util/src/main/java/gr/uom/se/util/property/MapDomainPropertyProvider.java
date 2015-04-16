/**
 * 
 */
package gr.uom.se.util.property;

import gr.uom.se.util.mapper.Mapper;
import gr.uom.se.util.mapper.MapperFactory;
import gr.uom.se.util.validation.ArgsCheck;

import java.util.Map;

/**
 * A property provider implementation based on a map.
 * <p>
 * 
 * @author Elvis Ligu
 */
public class MapDomainPropertyProvider implements DomainPropertyProvider {

   private final Map<String, Map<String, Object>> properties;

   /**
    * Create an instance based on the given mapped properties.
    * <p>
    * 
    * @param properties
    *           the mapped properties, must not be null.
    */
   public MapDomainPropertyProvider(Map<String, Map<String, Object>> properties) {
      ArgsCheck.notNull("properties", properties);
      this.properties = properties;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Object getProperty(String domain, String name) {
      ArgsCheck.notNull("domain", domain);
      ArgsCheck.notNull("name", name);
      Map<String, Object> map = properties.get(domain);
      if(map != null) {
         return map.get(name);
      }
      return null;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public <T> T getProperty(String domain, String name, Class<T> type) {
      Object instance = getProperty(domain, name);
      if(instance != null) {
         MapperFactory factory = MapperFactory.getInstance();
         Class<?> clazz = instance.getClass();
         Mapper mapper = factory.getMapper(clazz, type);
         if(mapper != null) {
            return mapper.map(instance, type);
         }
      }
      
      return null;
   }
   
   

}
