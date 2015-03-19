/**
 * 
 */
package gr.uom.se.util.module;

import gr.uom.se.util.module.annotations.Module;
import gr.uom.se.util.module.annotations.NULLVal;
import gr.uom.se.util.module.annotations.Property;
import gr.uom.se.util.validation.ArgsCheck;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A class of utilities for the module API.
 * <p>
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public class ModuleUtils {

   /**
    * The cache for module configurations that are stored in annotations.
    * <p>
    * There is no need for thread safety here because even if two threads change
    * the same class at the same time, the result they put in cache will be the
    * same. Also cache is limitless, because in every system the number of
    * annotated classes that may be used will be very limited. So no need for
    * further memory management.
    */
   private static final Map<Class<?>, Map<String, Map<String, Object>>> moduleConfigCache = new ConcurrentHashMap<>();

   /**
    * Get the module config base on {@link Module} annotation.
    * <p>
    * It will check for it if it is available in a cache within this class
    * first.
    * 
    * @param module
    * @return
    */
   public static Map<String, Map<String, Object>> resolveModuleConfig(
         Class<?> module) {
      Map<String, Map<String, Object>> config = moduleConfigCache.get(module);
      if (config == null) {
         config = ModuleUtils.getModuleConfig(module);
         moduleConfigCache.put(module, config);
      }
      return config;
   }

   /**
    * Create a configuration from a {@link Module} annotation.
    * <p>
    * The key of the map is the domain, and the value is a map with key value
    * pairs of properties. Values are the default stringVal defined at
    * {@link Property} annotation. If the module annotation is null, it will
    * return a null config.
    * 
    * @param module
    *           the annotation with properties
    * @return a configuration based on module properties
    */
   private static Map<String, Map<String, Object>> getModuleConfig(Module module) {
      Map<String, Map<String, Object>> properties = new HashMap<>();
      if (module != null) {
         for (Property p : module.properties()) {
            Map<String, Object> domain = properties.get(p.domain());
            if (domain == null) {
               domain = new HashMap<>();
               properties.put(p.domain(), domain);
            }
            domain.put(p.name(), p.stringVal());
         }
      }

      return properties;
   }

   /**
    * Create a configuration from a {@link Module} annotation.
    * <p>
    * The key of the map is the domain, and the value is a map with key value
    * pairs of properties. Values are the default stringVal defined at
    * {@link Property} annotation. If the module annotation is null, it will
    * return a null config. Also the module provider will be stored at one of
    * the default locations in order to be queries later. If there is a
    * {@linkplain ModuleConstants#getDefaultConfigFor(Class) module config
    * domain} it will store the provider (if specified) there, if not, and there
    * is a {@linkplain ModuleConstants#DEFAULT_MODULE_CONFIG_DOMAIN default
    * module's config domain} it will store it there. If none of the above is
    * present, it will create a module config domain and store it there.
    * 
    * @param module
    *           the annotation with properties
    * @return a configuration based on module properties
    */
   private static Map<String, Map<String, Object>> getModuleConfig(
         Class<?> module) {
      ArgsCheck.notNull("module", module);
      Module annotation = module.getAnnotation(Module.class);
      Map<String, Map<String, Object>> properties = getModuleConfig(annotation);
      if (annotation == null) {
         return properties;
      }
      Class<?> providerClass = annotation.provider();
      if (providerClass != null
            && !providerClass.isAssignableFrom(NULLVal.class)) {
         // If a provider class is defined then set it
         // to the properties
         String domain = ModuleConstants.getDefaultConfigFor(module);
         String name = ModuleConstants.PROVIDER_CLASS_PROPERTY;
         Map<String, Object> map = properties.get(domain);
         if (map == null) {
            String defaultDomain = ModuleConstants.DEFAULT_MODULE_CONFIG_DOMAIN;
            map = properties.get(defaultDomain);
            if (map != null) {
               name = ModuleConstants.getProviderClassNameFor(module);
            } else {
               map = new HashMap<>(1);
               properties.put(domain, map);
            }
         }
         map.put(name, providerClass);
      }
      return properties;
   }

   /**
    * Set the given property in the domain:name like properties map.
    * <p>
    * 
    * @param domain
    *           the first key
    * @param name
    *           the second key
    * @param val
    *           the object value
    * @param properties
    *           the map
    */
   static void setProperty(String domain, String name, Object val,
         Map<String, Map<String, Object>> properties) {
      Map<String, Object> map = properties.get(domain);
      if (map == null) {
         if (val != null) {
            map = new HashMap<>();
            properties.put(domain, map);
         }
      }
      if (map != null) {
         if (val == null) {
            map.remove(name);
         } else {
            map.put(name, val);
         }
         if (map.isEmpty()) {
            properties.remove(domain);
         }
      }
   }

   /**
    * Override the domain with the given name, in domain:name like properties
    * map, with the one provided.
    * <p>
    * 
    * @param dname
    *           domain name
    * @param domain
    *           the domain values
    * @param properties
    *           the properties
    */
   static void overrideDomain(String dname, Map<String, Object> domain,
         Map<String, Map<String, Object>> properties) {
      if (domain == null || domain.isEmpty()) {
         properties.remove(dname);
      } else {
         for (String skey : domain.keySet()) {
            Object val = domain.get(skey);
            setProperty(dname, skey, val, properties);
         }
      }
   }

   /**
    * Override the properties from source to target in a domain:properties like
    * map.
    * <p>
    * 
    * @param source
    *           the source of properties
    * @param target
    *           the target of properties to be overridden
    * @return the overridden map 
    */
   static Map<String, Map<String, Object>> override(
         Map<String, Map<String, Object>> source,
         Map<String, Map<String, Object>> target) {
      if (target == null || target.isEmpty()) {
         target = source;
      }
      if (source == null || source.isEmpty()) {
         return target;
      } else if (source != target) {
         for (String dname : source.keySet()) {
            Map<String, Object> domain = source.get(dname);
            overrideDomain(dname, domain, target);
         }
      }
      return target;
   }

   /**
    * Return a property annotation, or null if annotations is null or empty.
    * <p>
    * This method will throw an exception if there are more than one property
    * annotations.
    * 
    * @param annotations
    *           to check for the property annotation
    * @return a property annotation or null if there is not any
    */
   static Property getPropertyAnnotation(Annotation... annotations) {
      if (annotations == null || annotations.length == 0) {
         return null;
      }
      // We should check the number of @Property annotations
      // and will be skipping other non related annotations
      Property propertyAnnotation = null;
      int count = 0;
      for (Annotation an : annotations) {
         if (an.annotationType().equals(Property.class)) {
            count++;
            propertyAnnotation = (Property) an;
         }
      }

      // Check for more than one property annotation
      if (count > 1) {
         throw new IllegalArgumentException(
               "found more than 1 annotation @Property for parameter");
      }
      return propertyAnnotation;
   }
}
