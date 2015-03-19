/**
 * 
 */
package gr.uom.se.util.module;

import gr.uom.se.util.config.ConfigManager;
import gr.uom.se.util.validation.ArgsCheck;

import java.util.Map;

/**
 * A property locator that will maintain a map of properties from where to look
 * first for a property and then delegate to its superclass if the property was
 * not found.
 * <p>
 * Generally speaking a default property locator will look first for a property
 * within the configuration manager (passed as a parameter to its methods) and
 * then to a properties map. This implementation will try to look first at its
 * properties map (the one supplied when constructed) and then will delegate the
 * call to the super implementation if the property was not found, in order to
 * continue the normal execution.
 * 
 * @author Elvis Ligu
 */
public class DynamicModulePropertyLocator extends DefaultModulePropertyLocator {

   /**
    * These properties will be used by this locator to be queried first, for a
    * property to be found.
    */
   private final Map<String, Map<String, Object>> dynamicProperties;

   /**
    * Create an instance of property locator.
    */
   public DynamicModulePropertyLocator(
         Map<String, Map<String, Object>> properties) {
      ArgsCheck.notNull("properties", properties);
      this.dynamicProperties = properties;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public <T> T getProperty(String domain, String name, Class<T> type,
         ConfigManager config, Map<String, Map<String, Object>> properties) {
      T prop = super.getProperty(domain, name, type, null, dynamicProperties);
      if (prop == null) {
         prop = super.getProperty(domain, name, type, config, properties);
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
            dynamicProperties);
      if (object == null) {
         object = super.getConfigPropertyObject(name, type, objectType, config,
               properties);
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
            null, dynamicProperties);
      if (val == null) {
         val = super.getConfigPropertyObjectWithDefault(name, type, objectType,
               config, properties);
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
            name, classType, null, dynamicProperties);
      if (clazz == null) {
         clazz = super.getConfigPropertyClassForDomain(domain, name, classType,
               config, properties);
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
            classType, null, dynamicProperties);
      if (clazz == null) {
         clazz = super.getConfigPropertyClass(name, type, classType, config,
               properties);
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
            type, classType, defaultType, null, dynamicProperties);
      if (clazz == null || clazz == defaultType) {
         clazz = super.getConfigPropertyClassWithDefault(name, type, classType,
               defaultType, config, properties);
      }
      return clazz;
   }
}
