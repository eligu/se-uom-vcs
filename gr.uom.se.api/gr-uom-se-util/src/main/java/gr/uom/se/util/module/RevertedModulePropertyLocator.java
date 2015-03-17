/**
 * 
 */
package gr.uom.se.util.module;

import gr.uom.se.util.config.ConfigManager;

import java.util.Map;

/**
 * A property locator that will revert the calls of default property locator
 * implementation.
 * <p>
 * Generally speaking a default property locator will look first for a property
 * within the configuration manager (passed as a parameter to its methods) and
 * then to a properties map. This implementation will revert this strategy so
 * the properties should be first looked at provided map and then at config
 * manager.
 * 
 * @author Elvis Ligu
 */
public class RevertedModulePropertyLocator extends DefaultModulePropertyLocator {

   /**
    * Create an instance of property locator.
    */
   public RevertedModulePropertyLocator() {
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public <T> T getProperty(String domain, String name, Class<T> type,
         ConfigManager config, Map<String, Map<String, Object>> properties) {
      T prop = super.getProperty(domain, name, type, null, properties);
      if (prop == null) {
         prop = super.getProperty(domain, name, type, config, null);
      }
      return prop;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public <T> T getConfigPropertyObject(String name, Class<?> type,
         Class<T> objectType, ConfigManager config,
         Map<String, Map<String, Object>> properties) {
      T object = super.getConfigPropertyObject(name, type, objectType, null,
            properties);
      if (object == null) {
         object = super.getConfigPropertyObject(name, type, objectType, config,
               null);
      }
      return object;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public <T> T getConfigPropertyObjectWithDefault(String name, Class<?> type,
         Class<T> objectType, ConfigManager config,
         Map<String, Map<String, Object>> properties) {

      T val = super.getConfigPropertyObjectWithDefault(name, type, objectType,
            null, properties);
      if (val == null) {
         val = super.getConfigPropertyObjectWithDefault(name, type, objectType,
               config, null);
      }
      return val;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public <T> Class<? extends T> getConfigPropertyClassForDomain(String domain,
         String name, Class<T> classType, ConfigManager config,
         Map<String, Map<String, Object>> properties) {
      Class<? extends T> clazz = super.getConfigPropertyClassForDomain(domain,
            name, classType, null, properties);
      if (clazz == null) {
         clazz = super.getConfigPropertyClassForDomain(domain, name, classType,
               config, null);
      }
      return clazz;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public <T> Class<? extends T> getConfigPropertyClass(String name,
         Class<?> type, Class<T> classType, ConfigManager config,
         Map<String, Map<String, Object>> properties) {
      Class<? extends T> clazz = super.getConfigPropertyClass(name, type,
            classType, null, properties);
      if (clazz == null) {
         clazz = super.getConfigPropertyClass(name, type, classType, config,
               null);
      }
      return clazz;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public <T> Class<? extends T> getConfigPropertyClassWithDefault(String name,
         Class<?> type, Class<T> classType, Class<? extends T> defaultType,
         ConfigManager config, Map<String, Map<String, Object>> properties) {

      Class<? extends T> clazz = super.getConfigPropertyClassWithDefault(name,
            type, classType, defaultType, null, properties);
      if (clazz == null || clazz == defaultType) {
         clazz = super.getConfigPropertyClassWithDefault(name, type, classType,
               defaultType, config, null);
      }
      return clazz;
   }
}
