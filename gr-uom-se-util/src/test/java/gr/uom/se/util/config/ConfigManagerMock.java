/**
 * 
 */
package gr.uom.se.util.config;

import gr.uom.se.util.config.ConfigDomain;
import gr.uom.se.util.config.ConfigManager;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public class ConfigManagerMock implements ConfigManager {

   private Map<String, Map<String, Object>> properties = new HashMap<>();
   private static final String DEFAULT_CONFIG = "default";

   /**
    * {@inheritDoc)
    * 
    * @see gr.uom.se.util.config.ConfigManager#getProperty(java.lang.String)
    */
   @Override
   public <T> T getProperty(String name) {
      return this.getProperty(DEFAULT_CONFIG, name);

   }

   /**
    * {@inheritDoc)
    * 
    * @see gr.uom.se.util.config.ConfigManager#setProperty(java.lang.String,
    * java.lang.Object)
    */
   @Override
   public void setProperty(String name, Object value) {
      this.setProperty(DEFAULT_CONFIG, name, value);
   }

   /**
    * {@inheritDoc)
    * 
    * @see gr.uom.se.util.config.ConfigManager#getProperty(java.lang.String,
    * java.lang.String)
    */
   @SuppressWarnings("unchecked")
   @Override
   public <T> T getProperty(String domain, String name) {
      Map<String, Object> props = properties.get(domain);
      if (props != null) {
         return (T) props.get(name);
      }
      return null;
   }

   /**
    * {@inheritDoc)
    * 
    * @see gr.uom.se.util.config.ConfigManager#setProperty(java.lang.String,
    * java.lang.String, java.lang.Object)
    */
   @Override
   public void setProperty(String domain, String name, Object value) {
      Map<String, Object> props = properties.get(domain);
      if (props == null) {
         props = new HashMap<>();
         properties.put(domain, props);
      }
      props.put(name, value);
   }

   /**
    * {@inheritDoc)
    * 
    * @see gr.uom.se.util.config.ConfigManager#loadDomain(java.lang.String)
    */
   @Override
   public void loadDomain(String domain) {
      // TODO Auto-generated method stub

   }

   /**
    * {@inheritDoc)
    * 
    * @see gr.uom.se.util.config.ConfigManager#loadDomain(java.lang.Class)
    */
   @Override
   public <T extends ConfigDomain> T loadDomain(Class<T> domain) {
      return null;
   }

   @Override
   public ConfigDomain getDomain(String domain) {
      // TODO Auto-generated method stub
      return null;
   }

}
