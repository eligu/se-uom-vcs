/**
 * 
 */
package gr.uom.se.util.module;

import gr.uom.se.util.config.ConfigManager;
import gr.uom.se.util.mapper.Mapper;
import gr.uom.se.util.mapper.MapperFactory;
import gr.uom.se.util.module.annotations.Module;
import gr.uom.se.util.module.annotations.Property;
import gr.uom.se.util.validation.ArgsCheck;

import java.lang.annotation.Annotation;
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
    * The return value will be the same as the given type. This is the main
    * method
    * 
    * @param domain
    *           of the configuration where the property will be searched for
    * @param name
    *           the name of the property to resolve
    * @param type
    *           the type of the property
    * @param config
    *           the configuration manager to look for the property first
    * @param properties
    *           a fallback map of properties to look for a property if it is not
    *           under config manager
    * @return a value of the given property or null if it was not found
    */
   @SuppressWarnings("unchecked")
   public static <T> T getCompatibleProperty(String domain, String name,
         Class<T> type, ConfigManager config,
         Map<String, Map<String, Object>> properties) {

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
   public static Map<String, Map<String, Object>> getModuleConfig(
         Class<?> module) {
      ArgsCheck.notNull("module", module);
      Module annotation = module.getAnnotation(Module.class);
      return getModuleConfig(annotation);
   }

   /**
    * Get a config property instance.
    * <p>
    * For each given {@code type} there is a default config domain which can be
    * obtained by calling {@link ModuleConstants#getDefaultConfigFor(Class)}.
    * Different configurations that can affect the operation of modules are
    * stored within this default type config. This method will look under this
    * domain for an instance of the given type {@code objectType}. If an
    * instance was not found there, it will look under the default domain
    * {@link ModuleConstants#DEFAULT_MODULE_CONFIG_DOMAIN}. If no instance was
    * found a null will be returned.
    * 
    * @param name
    *           the name of the property where to find instance
    * @param type
    *           the type to find its domain for the instance
    * @param objectType
    *           the type of the instance we are looking
    * @param properties
    *           if a property can not be found in the config manager it will
    *           look under this map
    * @param config
    *           to look first for the instance
    * @return an instance of type {@code objectType}
    */
   static <T> T getConfigPropertyObject(String name, Class<?> type,
         Class<T> objectType, Map<String, Map<String, Object>> properties,
         ConfigManager config) {

      // Check first under the default domain of the given class
      // if there is any parameter provider available
      String loaderDomain = ModuleConstants.getDefaultConfigFor(type);

      T configObject = getCompatibleProperty(loaderDomain, name, objectType,
            config, properties);

      // If a provider is not available then check under the default
      // module's domain if it is available there
      if (configObject == null) {
         loaderDomain = ModuleConstants.DEFAULT_MODULE_CONFIG_DOMAIN;
         configObject = getCompatibleProperty(loaderDomain, name, objectType,
               config, properties);
      }
      return configObject;
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
    * {@link #getCompatibleProperty(String, String, Class, ConfigManager, Map)}.
    * If the provider was not found at this domain then it will look under the
    * default module domain {@link ModuleConstants#DEFAULT_MODULE_CONFIG_DOMAIN}.
    * <p>
    * This method works the same as
    * {@link #getConfigPropertyObject(String, Class, Class, Map, ConfigManager)}
    * with the difference that when looking at default config domain the
    * property name is different from the name when looking at module domain.
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
      String name = ModuleConstants.PROVIDER_PROPERTY;
      T pInstance = getCompatibleProperty(domain, name, provider, config,
            properties);

      // If the provider was not found then try to find it under
      // module's default domain
      if (pInstance == null) {
         domain = ModuleConstants.DEFAULT_MODULE_CONFIG_DOMAIN;
         name = ModuleConstants.getProviderNameFor(type);
         pInstance = getCompatibleProperty(domain, name, provider, config,
               properties);
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
    * {@link #getCompatibleProperty(String, String, Class, ConfigManager, Map)}.
    * If the provider was not found at this domain then it will look under the
    * default module domain {@link ModuleConstants#DEFAULT_MODULE_CONFIG_DOMAIN}
    * . Because a parameter provider is a general purpose instance it will be
    * under the property with name
    * {@link ModuleConstants#PARAMETER_PROVIDER_PROPERTY}.
    * 
    * @param type
    *           of parameter to load
    * @param properties
    * @return
    */
   public static ParameterProvider getParameterProvider(Class<?> type,
         Map<String, Map<String, Object>> properties, ConfigManager config) {

      return getConfigPropertyObject(
            ModuleConstants.PARAMETER_PROVIDER_PROPERTY, type,
            ParameterProvider.class, properties, config);
   }

   /**
    * Ensure that this method will return a provider, even if the
    * {@link #getParameterProvider(Class, Map, ConfigManager)} doesn't return
    * any provider.
    * <p>
    * The provider returned by this method is of type
    * {@link DefaultParameterProvider}.
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

      // If no provider was found then try to find a provider class
      // for the given type
      if (provider == null) {
         Class<? extends ParameterProvider> providerClass = getParameterProviderClassFor(
               type, config, properties);
         // No provider class was found for this type
         // we should provide the default provider
         if (providerClass.equals(DefaultParameterProvider.class)) {
            provider = new DefaultParameterProvider(config, null);
         } else {
            // The provider class for the given type is
            // not the default provider so we must load
            // this custom provider by resolving a loader for
            // it
            provider = resolveLoader(providerClass, properties, config).load(
                  providerClass);
         }
      }
      return provider;
   }

   /**
    * Get the parameter provider class for the given type.
    * <p>
    * Check only the {@code clazz} domain if there is a property with the given
    * {@link ModuleConstants#PARAMETER_PROVIDER_PROPERTY} and the type
    * {@link ParameterProvider} if so then return its type, if not then check
    * for the class of this property calling
    * {@link ModuleUtils#getConfigPropertyClassForDomain(String, String, Class, ConfigManager, Map)}
    * . If a class was not found then look under the default domain calling
    * {@link #getDefaulParametertProviderClass(ConfigManager, Map)}.
    * 
    * @param clazz
    *           of which the parameter provider will provide
    * @param config
    *           to look for it first
    * @param properties
    *           to look for it if it was not found under config
    * @return a parameter provider class
    */
   public static Class<? extends ParameterProvider> getParameterProviderClassFor(
         Class<?> clazz, ConfigManager config,
         Map<String, Map<String, Object>> properties) {

      // Check first for the loader class within the module domain
      String domain = ModuleConstants.getDefaultConfigFor(clazz);

      Class<? extends ParameterProvider> pClass = getConfigPropertyClassForDomain(
            domain, ModuleConstants.PARAMETER_PROVIDER_PROPERTY,
            ParameterProvider.class, config, properties);

      if (pClass == null) {
         return getDefaulParametertProviderClass(config, properties);
      } else {
         return pClass;
      }
   }

   /**
    * Check at the default config domain for a parameter provider class.
    * <p>
    * If no property class was found it will return a
    * {@link DefaultParameterProvider} class.
    * 
    * @param config
    *           to look for the property first
    * @param properties
    *           to look for the property if it waqs not found within config
    * @return a parameter provider class
    */
   public static Class<? extends ParameterProvider> getDefaulParametertProviderClass(
         ConfigManager config, Map<String, Map<String, Object>> properties) {
      String domain = ModuleConstants.DEFAULT_MODULE_CONFIG_DOMAIN;

      Class<? extends ParameterProvider> pClass = getConfigPropertyClassForDomain(
            domain, ModuleConstants.PARAMETER_PROVIDER_PROPERTY,
            ParameterProvider.class, config, properties);

      if (pClass == null) {
         return DefaultParameterProvider.class;
      } else {
         return pClass;
      }
   }

   /**
    * Given a domain and a property name, try to locate a class of type
    * {@code classType}.
    * <p>
    * It will first look up an instance within the given domain with the given
    * name, if it is of type {@code classType}. If an instance was not found it
    * will look under the same domain with a name
    * {@link ModuleConstants#getPropertyNameForConfigClass(String)}, to find a
    * class name. If it was found it will load the class with the given name and
    * check if the class is of the required type. If the class is not of the
    * required type it will throw an exception.
    * 
    * @param domain
    *           to look for property
    * @param name
    *           of the property
    * @param classType
    *           the type of the class to look for
    * @param config
    *           where it will look first for the class
    * @param properties
    *           where it will look if no class was found at config
    * @return a class for the given type
    */
   @SuppressWarnings("unchecked")
   static <T> Class<? extends T> getConfigPropertyClassForDomain(String domain,
         String name, Class<T> classType, ConfigManager config,
         Map<String, Map<String, Object>> properties) {
      if (config == null && properties == null) {
         return null;
      }

      // Try to find a type within config
      T tObject = getCompatibleProperty(domain, name, classType, config,
            properties);

      Class<? extends T> tClass = null;
      // There is not a type for the given domain
      if (tObject == null) {
         // We must check for a classType
         name = ModuleConstants.getPropertyNameForConfigClass(name);
         String className = getCompatibleProperty(domain, name, String.class,
               config, properties);

         // We found the class name
         // now load the class and check if it is a module loader
         if (className != null) {
            try {
               Class<?> pc = Class.forName(className);
               // Class is not a module loader
               if (!classType.isAssignableFrom(pc)) {
                  throw new RuntimeException("class " + pc
                        + " should be a sub type of " + classType);
               }
               tClass = (Class<? extends T>) pc;
            } catch (ClassNotFoundException e) {
               throw new RuntimeException(e);
            }
         }
      } else {
         return (Class<? extends T>) tObject.getClass();
      }
      return tClass;
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
    * {@link #getCompatibleProperty(String, String, Class, ConfigManager, Map)}.
    * If the provider was not found at this domain then it will look under the
    * default module domain {@link ModuleConstants#DEFAULT_MODULE_CONFIG_DOMAIN}
    * . Because a cachedLoader is a general purpose instance it will be searched
    * under the property with name {@link ModuleConstants#LOADER_PROPERTY}.
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
      return getConfigPropertyObject(ModuleConstants.LOADER_PROPERTY, type,
            ModuleLoader.class, properties, config);
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
      // If no loader was found then try to find a loader class
      // for the given type
      if (loader == null) {
         Class<? extends ModuleLoader> loaderClass = getLoaderClassFor(type,
               config, properties);
         // No loader class was found for this type
         // we should provide the default loader
         if (loaderClass.equals(DefaultModuleLoader.class)) {
            loader = new DefaultModuleLoader(config, null);
         } else {
            // The loader class for the given type is
            // not the default loader so we must load
            // this custom loader by resolving a loader for
            // it
            loader = resolveLoader(loaderClass, properties, config).load(
                  loaderClass);
         }
      }
      return loader;
   }

   /**
    * Get the loader class for the given type.
    * <p>
    * Check only the {@code clazz} domain if there is a property with the given
    * {@link ModuleConstants#LOADER_PROPERTY} and the type {@link ModuleLoader}
    * if so then return its type, if not then check for the class of this
    * property calling
    * {@link ModuleUtils#getConfigPropertyClassForDomain(String, String, Class, ConfigManager, Map)}
    * . If a class was not found then look under the default domain calling
    * {@link #getDefaulLoaderClass(ConfigManager, Map)}.
    * 
    * @param clazz
    *           of which the parameter provider will provide
    * @param config
    *           to look for it first
    * @param properties
    *           to look for it if it was not found under config
    * @return a parameter provider class
    */
   public static Class<? extends ModuleLoader> getLoaderClassFor(
         Class<?> clazz, ConfigManager config,
         Map<String, Map<String, Object>> properties) {

      // Check first for the loader class within the module domain
      String domain = ModuleConstants.getDefaultConfigFor(clazz);

      Class<? extends ModuleLoader> loaderClass = getConfigPropertyClassForDomain(
            domain, ModuleConstants.LOADER_PROPERTY, ModuleLoader.class,
            config, properties);

      if (loaderClass == null) {
         return getDefaultLoaderClass(config, properties);
      } else {
         return loaderClass;
      }
   }

   /**
    * Check at the default config domain for a loader class.
    * <p>
    * If no property class was found it will return a
    * {@link DefaultParameterProvider} class.
    * 
    * @param config
    *           to look for the property first
    * @param properties
    *           to look for the property if it was not found within config
    * @return a parameter provider class
    */
   public static Class<? extends ModuleLoader> getDefaultLoaderClass(
         ConfigManager config, Map<String, Map<String, Object>> properties) {

      String domain = ModuleConstants.DEFAULT_MODULE_CONFIG_DOMAIN;

      Class<? extends ModuleLoader> loaderClass = getConfigPropertyClassForDomain(
            domain, ModuleConstants.LOADER_PROPERTY, ModuleLoader.class,
            config, properties);

      if (loaderClass == null) {
         return DefaultModuleLoader.class;
      } else {
         return loaderClass;
      }
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
    * {@link #getCompatibleProperty(String, String, Class, ConfigManager, Map)}.
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
            config, properties);

      if (mapper == null) {
         property = ModuleConstants.DEFAULT_MAPPER_FACTORY_PROPERTY;
         domain = ModuleConstants.DEFAULT_MODULE_CONFIG_DOMAIN;
         MapperFactory factory = getCompatibleProperty(domain, property,
               MapperFactory.class, config, properties);
         if (factory == null) {
            factory = MapperFactory.getInstance();
         }
         mapper = factory.getMapper(from, to);
      }
      return mapper;
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
