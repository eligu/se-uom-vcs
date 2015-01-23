/**
 * 
 */
package gr.uom.se.util.module;

import gr.uom.se.util.manager.ConfigManager;
import gr.uom.se.util.mapper.Mapper;
import gr.uom.se.util.mapper.MapperFactory;
import gr.uom.se.util.module.annotations.Module;
import gr.uom.se.util.module.annotations.Property;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public class ModuleUtils {

   /**
    * Will try to resolve first a property from the configuration manager, if
    * was not found then will resolve it from the given properties.
    * <p>
    * The return value will be the same as the given type.
    * 
    * @param domain
    *           of the configuration where the property will be searched for
    * @param name
    *           the name of the property to resolve
    * @param type
    *           the type of the property
    * @param properties
    *           a fallback map of properties to look for a property if it is not
    *           under config manager
    * @param config
    *           the configuration manager to look for the property first
    * @return a value of the given property or null if it was not found
    */
   @SuppressWarnings("unchecked")
   public static <T> T getCompatibleProperty(String domain, String name,
         Class<T> type, Map<String, Map<String, Object>> properties,
         ConfigManager config) {

      T val = null;
      // The first check is at configuration manager
      if (config != null) {
         val = config.getProperty(domain, name);
      }

      // If not found at config manager then look at provided properties
      if (val == null && properties != null) {
         // Check first for a value within default config
         Map<String, Object> propDomain = properties.get(domain);
         if (propDomain != null) {
            Object pVal = propDomain.get(name);

            // If the value is within the default config,
            // check if it is a compatible type with T, if so
            // return the value
            if (pVal != null) {
               if (type.isAssignableFrom(pVal.getClass())) {
                  return (T) pVal;
               }
            }
         }
      }
      return val;
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
   public static Map<String, Map<String, Object>> getModuleConfig(Module module) {
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
    * Will try to look up for a provider of {@code type} instances that is of
    * type {@code provider}.
    * <p>
    * First will look for the domain calling at
    * {@link ModuleConstants#getDefaultConfigFor(Class)} where the Class is the
    * {@code type} parameter. And the property name will be retrieved by calling
    * {@link ModuleConstants#getProviderNameFor(Class)}. The property will be
    * retrieved using
    * {@link #getCompatibleProperty(String, String, Class, Map, ConfigManager)}.
    * If the provider was not found at this domain then it will look under the
    * default module domain {@link ModuleConstants#DEFAULT_MODULE_CONFIG_DOMAIN}.
    * 
    * @param type
    *           to look for its provider
    * @param provider
    *           the type of the provider to look for
    * @param properties
    *           look under this config if not found at configuration manager
    * @param config
    *           the configuration manager to look for the provider
    * @return a module provider for {@code type} or null if it was not found
    */
   public static <T> T getModuleProvider(Class<?> type, Class<T> provider,
         Map<String, Map<String, Object>> properties, ConfigManager config) {
      // Given a type we need a provider
      // 1 - Try to find the provider under type's default config domain
      String domain = ModuleConstants.getDefaultConfigFor(type);
      String name = ModuleConstants.getProviderNameFor(type);
      T pInstance = getCompatibleProperty(domain, name, provider, properties,
            config);

      // If the provider was not found then try to find it under
      // module's default domain
      if (pInstance == null) {
         domain = ModuleConstants.DEFAULT_MODULE_CONFIG_DOMAIN;
         pInstance = getCompatibleProperty(domain, name, provider, null, config);
      }
      return pInstance;
   }

   /**
    * Return a parameter provider to be used to resolve parameters of a given
    * method.
    * <p>
    * First will look for the domain calling at
    * {@link ModuleConstants#getDefaultConfigFor(Class)} where the Class is the
    * {@code type} parameter. And the property name will be retrieved by calling
    * {@link ModuleConstants#PARAMETER_PROVIDER_PROPERTY}. The property will be
    * retrieved using
    * {@link #getCompatibleProperty(String, String, Class, Map, ConfigManager)}.
    * If the provider was not found at this domain then it will look under the
    * default module domain {@link ModuleConstants#DEFAULT_MODULE_CONFIG_DOMAIN}
    * . Because a parameter provider is a general purpose instance it will be
    * under the property with name
    * {@link ModuleConstants#DEFAULT_PARAMETER_PROVIDER_PROPERTY}.
    * 
    * @param type
    *           of parameter to load
    * @param properties
    * @return
    */
   public static ParameterProvider getParameterProvider(Class<?> type,
         Map<String, Map<String, Object>> properties, ConfigManager config) {
      // Check first under the default domain of the given class
      // if there is any parameter provider available
      String loaderProperty = ModuleConstants.PARAMETER_PROVIDER_PROPERTY;
      String loaderDomain = ModuleConstants.getDefaultConfigFor(type);

      ParameterProvider provider = getCompatibleProperty(loaderDomain,
            loaderProperty, ParameterProvider.class, properties, config);

      // If a provider is not available then check under the default
      // module's domain if it is available there
      if (provider == null) {
         loaderProperty = ModuleConstants.DEFAULT_PARAMETER_PROVIDER_PROPERTY;
         loaderDomain = ModuleConstants.DEFAULT_MODULE_CONFIG_DOMAIN;
         provider = getCompatibleProperty(loaderDomain, loaderProperty,
               ParameterProvider.class, properties, config);
      }
      return provider;
   }

   /**
    * Ensure that this method will return a provider, even if the
    * {@link #getParameterProvider(Class, Map, ConfigManager)} doesn't return any provider.
    * <p>
    * The provider returned by this method is of type {@link DefaultParameterProvider}.
    * 
    * @param type
    *           the type to get the loader for
    * @param properties
    *           to look for a loader as an alternative if no config is specified
    * @param config
    *           to look primarily for the loader
    * @return a loader for the given type
    */
   public static ParameterProvider resolveParameterProvider(Class<?> type,
         Map<String, Map<String, Object>> properties, ConfigManager config) {

      ParameterProvider provider = getParameterProvider(type, properties,
            config);

      if (provider == null) {
         return new DefaultParameterProvider(config, null);
      }
      return provider;
   }

   /**
    * Return a cachedLoader for the given type by looking at the default places.
    * <p>
    * 
    * First will look for the domain calling at
    * {@link ModuleConstants#getDefaultConfigFor(Class)} where the Class is the
    * {@code type} parameter. And the property name will be retrieved by calling
    * {@link ModuleConstants#LOADER_PROPERTY}. The property will be retrieved
    * using
    * {@link #getCompatibleProperty(String, String, Class, Map, ConfigManager)}.
    * If the provider was not found at this domain then it will look under the
    * default module domain {@link ModuleConstants#DEFAULT_MODULE_CONFIG_DOMAIN}
    * . Because a cachedLoader is a general purpose instance it will be searched
    * under the property with name
    * {@link ModuleConstants#DEFAULT_MODULE_LOADER_PROPERTY}.
    * 
    * @param type
    *           of parameter to load
    * @param properties
    *           default config to look for if the manager has not the
    *           cachedLoader
    * @return a cachedLoader for the given type
    */
   public static ModuleLoader getLoader(Class<?> type,
         Map<String, Map<String, Object>> properties, ConfigManager config) {
      // Check first under the default domain of the given class
      // if there is any cachedLoader available
      String loaderProperty = ModuleConstants.LOADER_PROPERTY;
      String loaderDomain = ModuleConstants.getDefaultConfigFor(type);

      ModuleLoader loader = getCompatibleProperty(loaderDomain, loaderProperty,
            ModuleLoader.class, properties, config);

      // If a cachedLoader is not available then check under the default
      // module's domain if a cachedLoader is available
      if (loader == null) {
         loaderProperty = ModuleConstants.DEFAULT_MODULE_LOADER_PROPERTY;
         loaderDomain = ModuleConstants.DEFAULT_MODULE_CONFIG_DOMAIN;
         loader = getCompatibleProperty(loaderDomain, loaderProperty,
               ModuleLoader.class, properties, config);
      }
      return loader;
   }

   /**
    * Ensure that this method will return a loader, even if the
    * {@link #getLoader(Class, Map, ConfigManager)} doesn't return any loader.
    * <p>
    * The loader returned by this method is of type {@link DefaultModuleLoader}.
    * 
    * @param type
    *           the type to get the loader for
    * @param properties
    *           to look for a loader as an alternative if no config is specified
    * @param config
    *           to look primarily for the loader
    * @return a loader for the given type
    */
   public static ModuleLoader resolveLoader(Class<?> type,
         Map<String, Map<String, Object>> properties, ConfigManager config) {
      ModuleLoader loader = ModuleUtils.getLoader(type, properties, config);
      // If no cachedLoader was found then create a default module cachedLoader
      // if it is not created
      if (loader == null) {
         return new DefaultModuleLoader(config, null);
      }
      return loader;
   }

   /**
    * Get a mapper to map value from type {@code from} to type {@code to}.
    * <p>
    * 
    * First will look for the domain calling at
    * {@link ModuleConstants#getDefaultConfigFor(Class)} where the Class is the
    * {@code type} parameter. And the property name will be retrieved by calling
    * {@link ModuleConstants#getMapperNameFor(Class, Class)}. The property will
    * be retrieved using
    * {@link #getCompatibleProperty(String, String, Class, Map, ConfigManager)}.
    * If the mapper was not found at this domain then it will look under the
    * default module domain {@link ModuleConstants#DEFAULT_MODULE_CONFIG_DOMAIN}
    * a property called {@link ModuleConstants#DEFAULT_MAPPER_FACTORY_PROPERTY}
    * to get the default mapper factory. If the factory was not found then it
    * will get the singleton factory using {@link MapperFactory#getInstance()},
    * and load the mapper from there.
    * 
    * @param type
    * @param from
    * @param to
    * @param properties
    * @param config
    * @return
    */
   public static Mapper getMapperOfType(Class<?> type, Class<?> from,
         Class<?> to, Map<String, Map<String, Object>> properties,
         ConfigManager config) {

      String property = ModuleConstants.getMapperNameFor(from, to);
      String domain = ModuleConstants.getDefaultConfigFor(type);

      Mapper mapper = getCompatibleProperty(domain, property, Mapper.class,
            properties, config);

      if (mapper == null) {
         property = ModuleConstants.DEFAULT_MAPPER_FACTORY_PROPERTY;
         domain = ModuleConstants.DEFAULT_MODULE_CONFIG_DOMAIN;
         MapperFactory factory = getCompatibleProperty(domain, property,
               MapperFactory.class, properties, config);
         if (factory == null) {
            factory = MapperFactory.getInstance();
         }
         mapper = factory.getMapper(from, to);
      }
      return mapper;
   }
}
