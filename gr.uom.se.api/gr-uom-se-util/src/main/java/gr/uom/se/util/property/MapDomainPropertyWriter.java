/**
 * 
 */
package gr.uom.se.util.property;

import gr.uom.se.util.validation.ArgsCheck;

import java.util.HashMap;
import java.util.Map;

/**
 * An implementation of domain propety writer based on map.
 * <p>
 * 
 * @author Elvis Ligu
 */
public class MapDomainPropertyWriter implements DomainPropertyWriter {

   private final Map<String, Map<String, Object>> properties;

   /**
    * Create an instance based on the given mapped properties.
    * <p>
    * 
    * @param properties
    *           the mapped properties, must not be null.
    */
   public MapDomainPropertyWriter(Map<String, Map<String, Object>> properties) {
      ArgsCheck.notNull("properties", properties);
      this.properties = properties;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void setProperty(String domain, String name, Object value) {
      ArgsCheck.notNull("domain", domain);
      ArgsCheck.notNull("name", name);
      Map<String, Object> map = properties.get(domain);
      if (map == null) {
         map = new HashMap<>();
         properties.put(domain, map);
      }
      map.put(name, value);
   }

}
