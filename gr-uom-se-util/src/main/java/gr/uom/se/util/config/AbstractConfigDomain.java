/**
 * 
 */
package gr.uom.se.util.config;

import gr.uom.se.util.validation.ArgsCheck;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Abstract implementation of a configuration domain.
 * <p>
 * Uses internally a concurrent hash map to obtain values.
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public abstract class AbstractConfigDomain implements ConfigDomain {

   /**
    * The domain name of this configuration.
    * <p>
    */
   protected final String name;

   /**
    * The properties of this domain.
    * <p>
    * The map is a concurrent map.
    */
   protected Map<String, Object> properties;

   /**
    * 
    */
   public AbstractConfigDomain(String name) {
      ArgsCheck.notEmpty("name", name);
      this.name = name;
      this.properties = new ConcurrentHashMap<>();
   }

   /**
    * {@inheritDoc)
    */
   @Override
   public String getName() {
      return name;
   }

   /**
    * {@inheritDoc)
    */
   @Override
   public Object getProperty(String name) {
      ArgsCheck.notEmpty("name", name);
      return properties.get(name);
   }

   /**
    * {@inheritDoc)
    */
   @Override
   public void setProperty(String name, Object val) {
      ArgsCheck.notEmpty("name", name);
      if (val == null) {
         properties.remove(name);
      } else {
         properties.put(name, val);
      }
   }
}
