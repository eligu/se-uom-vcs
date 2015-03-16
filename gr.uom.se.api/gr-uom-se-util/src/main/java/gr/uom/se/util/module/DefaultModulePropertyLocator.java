/**
 * 
 */
package gr.uom.se.util.module;

import gr.uom.se.util.config.ConfigManager;
import gr.uom.se.util.mapper.Mapper;
import gr.uom.se.util.mapper.MapperFactory;
import gr.uom.se.util.validation.ArgsCheck;

import java.util.Map;

/**
 * Default implementation of {@link ModulePropertyLocator}.
 * <p>
 * This implementation is fully compatible with locator's interface and the
 * requirements it has for its methods. Although, when using one one of these
 * method implementations, keen in mind that it will first look up properties in
 * given configuration manager, and then in properties map. The reason is that
 * it assumes the properties map is a result of default configuration collected
 * from a module's annotation {@link Module}. If the client wants to have a
 * different look up strategy he must provide the config manager and properties
 * map accordingly. The two parameters (usually required) from methods of this
 * implementation are called sources, and they are allowed to be null. That is
 * if for example the client wants to look first under properties map, and then
 * under configuration manager, he can call the same method twice, providing at
 * first call the properties map, and then the configuration manager.
 * 
 * @author Elvis Ligu
 */
public class DefaultModulePropertyLocator implements ModulePropertyLocator {

   /**
    * Default constructor which may be used by module loader to create instances
    * of this locator.
    */
   public DefaultModulePropertyLocator() {
   }

   /**
    * Will try to resolve first a property from the configuration manager, if
    * was not found then will resolve it from the given properties.
    * <p>
    * The returned value will be the same as the given type. If the client wants
    * to check only one of the two sources (config manager or properties) he
    * should provide only one of them and leave the other null. If both sources
    * are null it will return null.
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
   @Override
   @SuppressWarnings("unchecked")
   public <T> T getProperty(String domain, String name, Class<T> type,
         ConfigManager config, Map<String, Map<String, Object>> properties) {

      T val = null;
      // The first check is at configuration manager
      if (config != null) {
         val = config.getProperty(domain, name, type);
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
    * Get a config property instance.
    * <p>
    * For each given {@code type} there is a default config domain which can be
    * obtained by calling {@link ModuleConstants#getDefaultConfigFor(Class)}.
    * Different configurations that can affect the operation of modules are
    * stored within this default type config. This domain is called the module
    * domain. There is also an other domain which is common for all modules, and
    * that is the default modules domain (
    * {@link ModuleConstants#DEFAULT_MODULE_CONFIG_DOMAIN}). When a module needs
    * to store a property in its domain he can do so by specifying the property
    * name, as long as there is no conflict with other properties (i.e.
    * {@code moduleProvider} is the name of module provider property). However
    * when a module stores a property in defaul modules domain, the property
    * should be prefixed with a proper prefix unique for the module. The
    * prefixed property can be obtained calling
    * {@link ModuleConstants#getPropertyNameForConfig(Class, String)}.
    * <p>
    * This method will first look for the object (be it a java object or a
    * primitive type) in module domain. If the object was not found it will look
    * under the default modules domain. And if not found it will return null.
    * <p>
    * Note: This is the same as calling
    * {@linkplain #getProperty(String, String, Class, ConfigManager, Map) get
    * property} method, that means that it will look first under configuration
    * manager and then under properties map.
    * 
    * @param name
    *           the name of the property where to find instance
    * @param type
    *           the type to find its domain for the instance
    * @param objectType
    *           the type of the instance we are looking
    * @param config
    *           to look first for the instance
    * @param properties
    *           if a property can not be found in the config manager it will
    *           look under this map
    * @return an instance of type {@code objectType} or null if the instance was
    *         not provided or sources are null.
    */
   @Override
   public <T> T getConfigPropertyObject(String name, Class<?> type,
         Class<T> objectType, ConfigManager config,
         Map<String, Map<String, Object>> properties) {

      // We should repeat the algorithm two times,
      // one for config and one for properties

      // ////////////////////////////////////////////////////////////////////
      // Check first the configuration manager
      String typeDomain = ModuleConstants.getDefaultConfigFor(type);
      String typeName = null;
      String defaultDomain = ModuleConstants.DEFAULT_MODULE_CONFIG_DOMAIN;
      String defaultName = name;
      T configObject = null;
      if (config != null) {
         // Check first under the default domain of the given class
         configObject = getProperty(typeDomain, defaultName, objectType,
               config, null);

         // If a property is not available then check under the default
         // module's domain if it is available there
         if (configObject == null) {
            typeName = ModuleConstants.getPropertyNameForConfig(type, name);
            configObject = getProperty(defaultDomain, typeName, objectType,
                  config, null);
         }
      }

      // //////////////////////////////////////////////////////////////////////
      // Check the properties map
      if (configObject == null && properties != null) {
         // Check first under the default domain of the given class
         configObject = getProperty(typeDomain, defaultName, objectType, null,
               properties);

         // If a property is not available then check under the default
         // module's domain if it is available there
         if (configObject == null) {
            // Here the typeName property has not received a value
            // so set a value
            if (typeName == null) {
               typeName = ModuleConstants.getPropertyNameForConfig(type, name);
            }
            configObject = getProperty(defaultDomain, typeName, objectType,
                  null, properties);
         }
      }
      return configObject;
   }

   /**
    * Get a config property instance with a default lookup.
    * <p>
    * For each given {@code type} there is a default config domain which can be
    * obtained by calling {@link ModuleConstants#getDefaultConfigFor(Class)}.
    * Different configurations that can affect the operation of modules are
    * stored within this default type config. This method will look under this
    * domain for an instance of the given type {@code objectType}. If an
    * instance was not found there, it will look under the default domain
    * {@linkplain ModuleConstants#DEFAULT_MODULE_CONFIG_DOMAIN default modules
    * domain}, however the property name will be
    * {@link ModuleConstants#getPropertyNameForConfig(Class, String)}. If no
    * instance was found a null will be returned.
    * <p>
    * Note: This is the same as calling
    * {@linkplain #getProperty(String, String, Class, ConfigManager, Map) get
    * property} method, the only difference is that, it will look for the
    * property in two different domains, the first domain will be the calls
    * domain and the second domain will be the default modules config domain.
    * That is, if the client wants to look only one of the sources he should
    * specify only that source (config manager or properties).
    * <p>
    * If the instance is not found in both locations, it will be looked at
    * default modules domain with the default name.
    * 
    * @param name
    *           the name of the property where to find instance
    * @param type
    *           the type to find its domain for the instance
    * @param objectType
    *           the type of the instance we are looking
    * @param config
    *           to look first for the instance
    * @param properties
    *           if a property can not be found in the config manager it will
    *           look under this map
    * @return an instance of type {@code objectType} or null if the instance was
    *         not provided or sources are null.
    */
   public <T> T getConfigPropertyObjectWithDefault(String name, Class<?> type,
         Class<T> objectType, ConfigManager config,
         Map<String, Map<String, Object>> properties) {

      T configObject = null;
      // We should repeat the algorithm two times,
      // one for config and one for properties

      // ////////////////////////////////////////////////////////////////////
      // Check first the configuration manager
      if (config != null) {
         configObject = getConfigPropertyObject(name, type, objectType, config,
               null);

         if (configObject == null) {
            String domain = ModuleConstants.DEFAULT_MODULE_CONFIG_DOMAIN;
            configObject = getProperty(domain, name, objectType, config, null);
         }
      }

      // //////////////////////////////////////////////////////////////////////
      // Check the properties map
      if (configObject == null && properties != null) {
         configObject = getConfigPropertyObject(name, type, objectType, null,
               properties);

         if (configObject == null) {
            String domain = ModuleConstants.DEFAULT_MODULE_CONFIG_DOMAIN;
            configObject = getProperty(domain, name, objectType, null,
                  properties);
         }
      }
      return configObject;
   }

   /**
    * Given a domain and a property name, try to locate a class of type
    * {@code classType}.
    * <p>
    * It will first look up an instance within the given domain with the given
    * name, if it is of type {@code classType}. If an instance was not found it
    * will look under the same domain with a name
    * {@link ModuleConstants#getPropertyNameForConfigClass(String)}, to find a
    * class name. The property found at this location may be a class or a
    * string, if the property is class it will check if the class is of the
    * required type, if not it will throw an exception, if the property is
    * string it will load the class with the given name and check if the class
    * is of the required type. If the class is not of the required type it will
    * throw an exception. Note that, this may throw an exception if the property
    * is string but it is not fully qualified name of a class, so the class can
    * not be found.
    * <p>
    * Note that the strategy of looking up the property is defined at get
    * property, which looks first the config manager, and then the properties
    * provided.
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
   @Override
   @SuppressWarnings("unchecked")
   public <T> Class<? extends T> getConfigPropertyClassForDomain(String domain,
         String name, Class<T> classType, ConfigManager config,
         Map<String, Map<String, Object>> properties) {

      // We should repeat the algorithm two times,
      // one for config and one for properties

      // ////////////////////////////////////////////////////////////////////
      // Check first the configuration manager
      Class<?> clazz = null;
      String classNameProperty = null;

      if (config != null) {
         // Try to find a type within config
         T tObject = getProperty(domain, name, classType, config, null);

         if (tObject != null) {
            clazz = tObject.getClass();
         } else {
            // There is not a type for the given domain
            // We must check for a classType
            classNameProperty = ModuleConstants
                  .getPropertyNameForConfigClass(name);
            Object propertyObject = getProperty(domain, classNameProperty,
                  Object.class, config, null);

            // We found the class object (be it a String name or a class)
            if (propertyObject != null) {
               // We expect the object to be a string (case when the name of the
               // class
               // is provided as a string) or a class type.
               // In case the object we found is a string
               // we assume this string is the class name
               if (String.class.isAssignableFrom(propertyObject.getClass())) {
                  String className = (String) propertyObject;
                  // now load the class and check if it is the same as the given
                  // type
                  try {
                     clazz = Class.forName(className);
                  } catch (ClassNotFoundException e) {
                     throw new RuntimeException(e);
                  }
                  // Case when the property object is a class
               } else if (propertyObject instanceof Class) {
                  clazz = (Class<?>) propertyObject;
               } else {
                  throw new IllegalArgumentException("property {" + domain
                        + ":" + name
                        + "} must be a class or a class name (String)");
               }
            }
         }
      }
      // //////////////////////////////////////////////////////////////////////
      // Check the properties map
      if (clazz == null && properties != null) {
         // Try to find a type within config
         T tObject = getProperty(domain, name, classType, null, properties);

         if (tObject != null) {
            clazz = tObject.getClass();
         } else {
            // There is not a type for the given domain
            // We must check for a classType
            if (classNameProperty == null) {
               // The class name here will ensure it is not null
               classNameProperty = ModuleConstants
                     .getPropertyNameForConfigClass(name);
            }
            Object propertyObject = getProperty(domain, classNameProperty,
                  Object.class, null, properties);

            // We found the class object (be it a String name or a class)
            if (propertyObject != null) {
               // We expect the object to be a string (case when the name of the
               // class
               // is provided as a string) or a class type.
               // In case the object we found is a string
               // we assume this string is the class name
               if (String.class.isAssignableFrom(propertyObject.getClass())) {
                  String className = (String) propertyObject;
                  // now load the class and check if it is the same as the given
                  // type
                  try {
                     clazz = Class.forName(className);
                  } catch (ClassNotFoundException e) {
                     throw new RuntimeException(e);
                  }
                  // Case when the property object is a class
               } else if (propertyObject instanceof Class) {
                  clazz = (Class<?>) propertyObject;
               } else {
                  throw new IllegalArgumentException("property {" + domain
                        + ":" + name
                        + "} must be a class or a class name (String)");
               }
            }
         }
      }

      // Make a check to ensure the clazz we found is
      // the same type as the required one
      if (clazz != null) {
         ArgsCheck.isSubtype("property {" + domain + ":" + name + "} ",
               classType, clazz);
      }

      return (Class<? extends T>) clazz;
   }

   /**
    * Given a domain and a property name, try to locate a class of type
    * {@code classType}.
    * <p>
    * It will first look up an instance within the given domain with the given
    * name, if it is of type {@code classType}. If an instance was not found it
    * will look under the same domain with a name
    * {@link ModuleConstants#getPropertyNameForConfigClass(String)}, to find a
    * class name. The property found at this location may be a class or a
    * string, if the property is class it will check if the class is of the
    * required type, if not it will throw an exception, if the property is
    * string it will load the class with the given name and check if the class
    * is of the required type. If the class is not of the required type it will
    * throw an exception. Note that, this may throw an exception if the property
    * is string but it is not fully qualified name of a class, so the class can
    * not be found.
    * <p>
    * Note that the strategy of looking up the property is defined at get
    * property, which looks first the config manager, and then the properties
    * provided.
    * <p>
    * If the class was not found it will return the defaultType.
    * 
    * @param domain
    *           to look for property
    * @param name
    *           of the property
    * @param classType
    *           the type of the class to look for
    * @param defaultType
    *           the default type to be returned if not type was found
    * @param config
    *           where it will look first for the class
    * @param properties
    *           where it will look if no class was found at config
    * @return a class for the given type
    */
   public <T> Class<? extends T> getConfigPropertyClassWithDefaultForDomain(
         String domain, String name, Class<T> classType,
         Class<? extends T> defaultType, ConfigManager config,
         Map<String, Map<String, Object>> properties) {

      // Check for the property under the default domain
      Class<? extends T> pClass = getConfigPropertyClassForDomain(domain, name,
            classType, config, properties);

      if (pClass == null) {
         return defaultType;
      } else {
         return pClass;
      }
   }

   /**
    * Given a type and a property name, try to locate a class of type
    * {@code classType} under the type's domain or under the default domain.
    * <p>
    * This is the equivalent of
    * {@link #getConfigPropertyObject(String, Class, Class, ConfigManager, Map)}
    * however it will call
    * {@link #getConfigPropertyClassForDomain(String, String, Class, ConfigManager, Map)}.
    * 
    * @param name
    *           property name
    * @param type
    *           the type to look for the classType
    * @param classType
    *           the type of the class to look for
    * @param config
    *           to look for class
    * @param properties
    *           to look for class
    * @return a class for the given type
    */
   @Override
   public <T> Class<? extends T> getConfigPropertyClass(String name,
         Class<?> type, Class<T> classType, ConfigManager config,
         Map<String, Map<String, Object>> properties) {

      // Check first under the default domain of the given class
      String typeDomain = ModuleConstants.getDefaultConfigFor(type);
      String typeName = null;
      String defaultDomain = ModuleConstants.DEFAULT_MODULE_CONFIG_DOMAIN;
      String defaultName = name;
      Class<? extends T> configClass = null;

      // We should repeat the algorithm two times,
      // one for config and one for properties

      // ////////////////////////////////////////////////////////////////////
      // Check first the configuration manager
      if (config != null) {
         configClass = getConfigPropertyClassForDomain(typeDomain, defaultName,
               classType, config, null);

         // If a property is not available then check under the default
         // module's domain if it is available there
         if (configClass == null) {
            typeName = ModuleConstants.getPropertyNameForConfig(type, name);
            configClass = getConfigPropertyClassForDomain(defaultDomain,
                  typeName, classType, config, null);
         }
      }

      // //////////////////////////////////////////////////////////////////////
      // Check the properties map
      if (configClass == null && properties != null) {
         configClass = getConfigPropertyClassForDomain(typeDomain, defaultName,
               classType, null, properties);

         // If a property is not available then check under the default
         // module's domain if it is available there
         if (configClass == null) {
            if (typeName == null) {
               typeName = ModuleConstants.getPropertyNameForConfig(type, name);
            }
            configClass = getConfigPropertyClassForDomain(defaultDomain,
                  typeName, classType, null, properties);
         }
      }
      return configClass;
   }

   /**
    * Given a type and a property name, try to locate a class of type
    * {@code classType} under the type's domain or under the default domain.
    * <p>
    * This is the equivalent of
    * {@link #getConfigPropertyObject(String, Class, Class, ConfigManager, Map)}
    * however it will call
    * {@link #getConfigPropertyClassForDomain(String, String, Class, ConfigManager, Map)}
    * . If the class was not found then return the default type.
    * 
    * @param name
    *           property name
    * @param type
    *           the type to look for the classType
    * @param classType
    *           the type of the class to look for
    * @param config
    *           to look for class
    * @param properties
    *           to look for class
    * @return a class for the given type
    */
   public <T> Class<? extends T> getConfigPropertyClassWithDefault(String name,
         Class<?> type, Class<T> classType, Class<? extends T> defaultType,
         ConfigManager config, Map<String, Map<String, Object>> properties) {

      Class<? extends T> pClass = null;

      // We should repeat the algorithm two times,
      // one for config and one for properties

      // ////////////////////////////////////////////////////////////////////
      // Check first the configuration manager
      if (config != null) {
         pClass = getConfigPropertyClass(name, type, classType, config, null);

         if (pClass == null) {
            String domain = ModuleConstants.DEFAULT_MODULE_CONFIG_DOMAIN;
            // Check for the property under the default domain
            pClass = getConfigPropertyClassWithDefaultForDomain(domain, name,
                  classType, null, config, null);
         }
      }
      // //////////////////////////////////////////////////////////////////////
      // Check the properties map
      if (pClass == null && properties != null) {
         pClass = getConfigPropertyClass(name, type, classType, null,
               properties);

         if (pClass == null) {
            String domain = ModuleConstants.DEFAULT_MODULE_CONFIG_DOMAIN;
            // Check for the property under the default domain
            pClass = getConfigPropertyClassWithDefaultForDomain(domain, name,
                  classType, defaultType, null, properties);
         }
      }
      if (pClass == null) {
         pClass = defaultType;
      }
      return pClass;
   }

   /**
    * Will try to look up for a provider of {@code type} instances that are of
    * type {@code provider}.
    * <p>
    * First will look for the domain calling at
    * {@link ModuleConstants#getDefaultConfigFor(Class)} where the Class is the
    * {@code type} parameter. When looking under this domain the property name
    * of the provider should be {@link ModuleConstants#PROVIDER_PROPERTY}. If
    * the provider was not found under this domain, it will look under
    * {@linkplain ModuleConstants#DEFAULT_MODULE_CONFIG_DOMAIN default modules
    * domain}, and the property name will be retrieved by calling
    * {@link ModuleConstants#getProviderNameFor(Class)}. The property will be
    * retrieved using
    * {@linkplain #getProperty(String, String, Class, ConfigManager, Map) get
    * property}.
    * <p>
    * This method works the same as
    * {@link #getConfigPropertyObject(String, Class, Class, ConfigManager, Map)}
    * with the difference that when looking at default config domain the
    * property name is different from the name when looking at module domain.
    * <p>
    * Note that the strategy of looking up the property is defined at get
    * property, which looks first the config manager, and then the properties
    * provided. That is in both cases (whether the class domain, or default
    * domain) it will first look up the config manager and then properties, and
    * repeat the same procedure for the default domain. If the client wants to
    * first look up either one of the sources and then the other it should make
    * two calls to this method, specifying each time one of the sources (config
    * manager or properties).
    * 
    * @param type
    *           to look for its provider
    * @param provider
    *           the type of the provider to look for
    * @param config
    *           the configuration manager to look for the provider
    * @param properties
    *           look under this config if not found at configuration manager
    * @return a module provider for {@code type} or null if it was not found
    */
   @Override
   public <T> T getModuleProvider(Class<?> type, Class<T> provider,
         ConfigManager config, Map<String, Map<String, Object>> properties) {
      String name = ModuleConstants.PROVIDER_PROPERTY;
      return getConfigPropertyObject(name, type, provider, config, properties);
   }

   /**
    * Get the provider class for the given type.
    * <p>
    * Check only the {@code clazz} domain if there is a property with the given
    * {@link ModuleConstants#PROVIDER_PROPERTY}, if so then return its type, if
    * not then check for the class of this property calling
    * {@link #getConfigPropertyClassForDomain(String, String, Class, ConfigManager, Map)}
    * . If a class was not found then look under the default modules domain with
    * property name
    * {@link ModuleConstants#getProviderNameFor(ConfigManager, Map)}.
    * <p>
    * Note that the strategy of looking up the property is defined at get
    * property, which looks first the config manager, and then the properties
    * provided. That is in both cases (whether the class domain, or default
    * domain) it will first look up the config manager and then properties, and
    * repeat the same procedure for the default domain. If the client wants to
    * first look up either one of the sources and then the other it should make
    * two calls to this method, specifying each time one of the sources (config
    * manager or properties).
    * 
    * @param clazz
    *           of which the provider will provide
    * @param config
    *           to look for it first
    * @param properties
    *           to look for it if it was not found under config
    * @return a provider class
    */
   @Override
   public Class<?> getModuleProviderClassFor(Class<?> clazz,
         ConfigManager config, Map<String, Map<String, Object>> properties) {

      // Check first for the provider class within the module domain
      String name = ModuleConstants.PROVIDER_PROPERTY;

      Class<?> pClass = getConfigPropertyClass(name, clazz, Object.class,
            config, properties);

      return pClass;
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
    * {@link #getProperty(String, String, Class, ConfigManager, Map)}. If the
    * provider was not found at this domain then it will look under the default
    * module domain {@link ModuleConstants#DEFAULT_MODULE_CONFIG_DOMAIN} .
    * Because a parameter provider is a general purpose instance if it is not
    * found at at default domain with the previous name, it will look the
    * default provider under the property with name
    * {@link ModuleConstants#PARAMETER_PROVIDER_PROPERTY}.
    * <p>
    * Note that the strategy of looking up the property is defined at get
    * property, which looks first the config manager, and then the properties
    * provided. That is in both cases (whether the class domain, or default
    * domain) it will first look up the config manager and then properties, and
    * repeat the same procedure for the default domain. If the client wants to
    * first look up either one of the sources and then the other it should make
    * two calls to this method, specifying each time one of the sources (config
    * manager or properties).
    * 
    * @param type
    *           of parameter to load
    * @param config
    *           the config manager from where to look first the provider
    * @param properties
    *           a fallback map of properties to find the provider if it was not
    *           found under config manager
    * @return a parameter provider instance for the given class
    */
   @Override
   public ParameterProvider getParameterProvider(Class<?> type,
         ConfigManager config, Map<String, Map<String, Object>> properties) {

      // Check first under the domain of the given class
      // if there is any parameter provider available
      String name = ModuleConstants.PARAMETER_PROVIDER_PROPERTY;
      ParameterProvider provider = getConfigPropertyObjectWithDefault(name,
            type, ParameterProvider.class, config, properties);

      return provider;
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
    *           to look for the property if it was not found within config
    * @return a parameter provider class
    */
   @Override
   public Class<? extends ParameterProvider> getDefaultParameterProviderClass(
         ConfigManager config, Map<String, Map<String, Object>> properties) {

      String domain = ModuleConstants.DEFAULT_MODULE_CONFIG_DOMAIN;
      // Check for the property under the default domain
      Class<? extends ParameterProvider> pClass = getConfigPropertyClassWithDefaultForDomain(
            domain, ModuleConstants.PARAMETER_PROVIDER_PROPERTY,
            ParameterProvider.class, DefaultParameterProvider.class, config,
            properties);
      return pClass;
   }

   /**
    * Get the parameter provider class for the given type.
    * <p>
    * Check only the {@code clazz} domain if there is a property with the given
    * {@link ModuleConstants#PARAMETER_PROVIDER_PROPERTY} and the type
    * {@link ParameterProvider} if so then return its type, if not then check
    * for the class of this property calling
    * {@link #getConfigPropertyClassForDomain(String, String, Class, ConfigManager, Map)}
    * . If a class was not found then look under the default domain calling
    * {@link #getDefaultParameterProviderClass(ConfigManager, Map)}.
    * 
    * @param clazz
    *           of which the parameter provider will provide
    * @param config
    *           to look for it first
    * @param properties
    *           to look for it if it was not found under config
    * @return a parameter provider class
    */
   @Override
   public Class<? extends ParameterProvider> getParameterProviderClassFor(
         Class<?> clazz, ConfigManager config,
         Map<String, Map<String, Object>> properties) {

      // Check first for the provider class within the module domain
      String name = ModuleConstants.PARAMETER_PROVIDER_PROPERTY;

      Class<? extends ParameterProvider> pClass = getConfigPropertyClassWithDefault(
            name, clazz, ParameterProvider.class,
            DefaultParameterProvider.class, config, properties);

      return pClass;
   }

   /**
    * Ensure that this method will return a provider, even if the
    * {@link #getParameterProvider(Class, ConfigManager, Map)} doesn't return
    * any provider.
    * <p>
    * The provider returned by this method is of type
    * {@link DefaultParameterProvider} if another provider was not found.
    * 
    * @param type
    *           the type to get the loader for
    * @param config
    *           to look primarily for the loader
    * @param properties
    *           to look for a loader as an alternative if no config is specified
    * @return a loader for the given type
    */
   @Override
   public ParameterProvider resolveParameterProvider(Class<?> type,
         ConfigManager config, Map<String, Map<String, Object>> properties) {

      ParameterProvider provider = getParameterProvider(type, config,
            properties);

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
            provider = resolveLoader(providerClass, config, properties).load(
                  providerClass);
         }
      }
      return provider;
   }

   /**
    * Get the loader class for the given type.
    * <p>
    * Check only the {@code clazz} domain if there is a property with the given
    * {@link ModuleConstants#LOADER_PROPERTY} and the type {@link ModuleLoader}
    * if so then return its type, if not then check for the class of this
    * property calling
    * {@link #getConfigPropertyClassForDomain(String, String, Class, ConfigManager, Map)}
    * . If a class was not found then look under the default domain calling
    * {@link #getDefaulLoaderClass(ConfigManager, Map)}.
    * 
    * @param clazz
    *           of which the loader will load
    * @param config
    *           to look at it first
    * @param properties
    *           to look at it if it was not found under config
    * @return a loader class
    */
   @Override
   public Class<? extends ModuleLoader> getLoaderClassFor(Class<?> clazz,
         ConfigManager config, Map<String, Map<String, Object>> properties) {

      // Check first for the provider class within the module domain
      String name = ModuleConstants.LOADER_PROPERTY;

      Class<? extends ModuleLoader> lClass = getConfigPropertyClassWithDefault(
            name, clazz, ModuleLoader.class, DefaultModuleLoader.class, config,
            properties);

      return lClass;
   }

   /**
    * Check at the default config domain for a loader class.
    * <p>
    * If no property class was found it will return a
    * {@link DefaultModuleLoader} class.
    * 
    * @param config
    *           to look for the property first
    * @param properties
    *           to look for the property if it was not found within config
    * @return a loader class
    */
   @Override
   public Class<? extends ModuleLoader> getDefaultLoaderClass(
         ConfigManager config, Map<String, Map<String, Object>> properties) {

      String domain = ModuleConstants.DEFAULT_MODULE_CONFIG_DOMAIN;

      Class<? extends ModuleLoader> loaderClass = getConfigPropertyClassWithDefaultForDomain(
            domain, ModuleConstants.LOADER_PROPERTY, ModuleLoader.class,
            DefaultModuleLoader.class, config, properties);

      return loaderClass;
   }

   /**
    * Return a cached loader for the given type by looking at the default
    * places.
    * <p>
    * 
    * First will look for the domain calling at
    * {@link ModuleConstants#getDefaultConfigFor(Class)} where the Class is the
    * {@code type} parameter. And the property name will be retrieved by calling
    * {@link ModuleConstants#LOADER_PROPERTY}. The property will be retrieved
    * using {@link #getProperty(String, String, Class, ConfigManager, Map)}. If
    * the provider was not found at this domain then it will look under the
    * default module domain {@link ModuleConstants#DEFAULT_MODULE_CONFIG_DOMAIN}
    * . Because a loader is a general purpose instance it will be searched under
    * the property with name {@link ModuleConstants#LOADER_PROPERTY}.
    * 
    * @param type
    *           of parameter to load
    * @param properties
    *           default config to look for if the manager has not the
    *           cachedLoader
    * @return a cachedLoader for the given type
    */
   @Override
   public ModuleLoader getLoader(Class<?> type, ConfigManager config,
         Map<String, Map<String, Object>> properties) {

      // Check first under the domain of the given class
      // if there is any loader available
      String name = ModuleConstants.LOADER_PROPERTY;

      ModuleLoader loader = getConfigPropertyObjectWithDefault(name, type,
            ModuleLoader.class, config, properties);

      return loader;
   }

   /**
    * Ensure that this method will return a loader, even if the
    * {@link #getLoader(Class, ConfigManager, Map)} doesn't return any loader.
    * <p>
    * The loader returned by this method is of type {@link DefaultModuleLoader}.
    * 
    * @param type
    *           the type to get the loader for
    * @param config
    *           to look primarily for the loader
    * @param properties
    *           to look for a loader as an alternative if no config is specified
    * @return a loader for the given type
    */
   @Override
   public ModuleLoader resolveLoader(Class<?> type, ConfigManager config,
         Map<String, Map<String, Object>> properties) {
      ModuleLoader loader = getLoader(type, config, properties);
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
            loader = resolveLoader(loaderClass, config, properties).load(
                  loaderClass);
         }
      }
      return loader;
   }

   /**
    * Return a cached injector for the given type by looking at the default
    * places.
    * <p>
    * 
    * First will look for the domain calling at
    * {@link ModuleConstants#getDefaultConfigFor(Class)} where the Class is the
    * {@code type} parameter. And the property name will be retrieved by calling
    * {@link ModuleConstants#PROPERTY_INJECTOR_PROPERTY}. The property will be
    * retrieved using
    * {@link #getProperty(String, String, Class, ConfigManager, Map)}. If the
    * injector was not found at this domain then it will look under the default
    * module domain {@link ModuleConstants#DEFAULT_MODULE_CONFIG_DOMAIN} .
    * Because a cached injector is a general purpose instance it will be
    * searched under the property with name
    * {@link ModuleConstants#PROPERTY_INJECTOR_PROPERTY}.
    * 
    * @param type
    *           of parameter to load
    * @param properties
    *           default config to look for if the manager has not the
    *           cachedLoader
    * @return a cachedLoader for the given type
    */
   @Override
   public PropertyInjector getPropertyInjector(Class<?> type,
         ConfigManager config, Map<String, Map<String, Object>> properties) {

      // Check first under the domain of the given class
      // if there is any injector available
      String name = ModuleConstants.PROPERTY_INJECTOR_PROPERTY;

      PropertyInjector loader = getConfigPropertyObjectWithDefault(name, type,
            PropertyInjector.class, config, properties);

      return loader;
   }

   /**
    * Check at the default config domain for an injector class.
    * <p>
    * If no property class was found it will return a
    * {@link DefaultPropertyInjector} class.
    * 
    * @param config
    *           to look for the property first
    * @param properties
    *           to look for the property if it was not found within config
    * @return a parameter provider class
    */
   @Override
   public Class<? extends PropertyInjector> getDefaultPropertyInjectorClass(
         ConfigManager config, Map<String, Map<String, Object>> properties) {

      String domain = ModuleConstants.DEFAULT_MODULE_CONFIG_DOMAIN;

      Class<? extends PropertyInjector> injectorClass = getConfigPropertyClassWithDefaultForDomain(
            domain, ModuleConstants.PROPERTY_INJECTOR_PROPERTY,
            PropertyInjector.class, DefaultPropertyInjector.class, config,
            properties);

      return injectorClass;
   }

   /**
    * Get the property injector class for the given type.
    * <p>
    * Check only the {@code clazz} domain if there is a property with the given
    * {@link ModuleConstants#PROPERTY_INJECTOR_PROPERTY} and the type
    * {@link PropertyInjector} if so then return its type, if not then check for
    * the class of this property calling
    * {@link #getConfigPropertyClassForDomain(String, String, Class, ConfigManager, Map)}
    * . If a class was not found then look under the default domain calling
    * {@link #getDefaultPropertyInjectorClass(ConfigManager, Map)}.
    * 
    * @param clazz
    *           of which the property injector will inject properties
    * @param config
    *           to look for it first
    * @param properties
    *           to look for it if it was not found under config
    * @return a property injector class
    */
   @Override
   public Class<? extends PropertyInjector> getPropertyInjectorClassFor(
         Class<?> clazz, ConfigManager config,
         Map<String, Map<String, Object>> properties) {

      // Check first for the injector class within the module domain
      String name = ModuleConstants.PROPERTY_INJECTOR_PROPERTY;

      Class<? extends PropertyInjector> lClass = getConfigPropertyClassWithDefault(
            name, clazz, PropertyInjector.class, DefaultPropertyInjector.class,
            config, properties);

      return lClass;
   }

   /**
    * Ensure that this method will return a property injector, even if the
    * {@link #getPropertyInjector(Class, ConfigManager, Map)} doesn't return any
    * injector.
    * <p>
    * The injector returned by this method is of type
    * {@link DefaultPropertyInjector}.
    * 
    * @param type
    *           the type to get the loader for
    * @param config
    *           to look primarily for the loader
    * @param properties
    *           to look for a loader as an alternative if no config is specified
    * @return a loader for the given type
    */
   @Override
   public PropertyInjector resolvePropertyInjector(Class<?> type,
         ConfigManager config, Map<String, Map<String, Object>> properties) {
      PropertyInjector injector = getPropertyInjector(type, config, properties);
      // If no injector was found then try to find a injector class
      // for the given type
      if (injector == null) {
         Class<? extends PropertyInjector> injectorClass = getPropertyInjectorClassFor(
               type, config, properties);
         // No injector class was found for this type
         // we should provide the default injector
         if (injectorClass.equals(DefaultPropertyInjector.class)) {
            injector = new DefaultPropertyInjector(config, null);
         } else {
            // The injector class for the given type is
            // not the default injector so we must load
            // this custom injector by resolving a loader for
            // it
            injector = resolveLoader(injectorClass, config, properties).load(
                  injectorClass);
         }
      }
      return injector;
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
    *           the module's type
    * @param from
    *           the type from which to convert to
    * @param to
    *           the type to convert to
    * @param config
    *           to look up the mapper
    * @param properties
    *           to lookup the mapper
    * @return a mapper for the given module type, that should convert from
    *         {@code from} to {@code to}
    */
   @Override
   public Mapper getMapperOfType(Class<?> type, Class<?> from, Class<?> to,
         ConfigManager config, Map<String, Map<String, Object>> properties) {

      // Try to resolve a mapper object first from both sources
      String property = ModuleConstants.getMapperNameFor(from, to);
      Mapper mapper = getConfigPropertyObjectWithDefault(property, type,
            Mapper.class, config, properties);

      // If mapper object was not found then try to resolve a
      // mapper class, and load the mapper from that class.
      if (mapper == null) {
         Class<? extends Mapper> mapperClass = getMapperClass(type, from, to,
               config, properties);
         if (mapperClass != null) {
            mapper = resolveLoader(mapperClass, config, properties).load(
                  mapperClass);
         }
      }

      // Mapper object or mapper class was not resolved now try
      // to resolve a factory, so we can get the mapper from there
      if (mapper == null) {

         MapperFactory factory = getMapperFactory(type, from, to, config,
               properties);
         mapper = factory.getMapper(from, to);
      }
      return mapper;
   }

   /**
    * Return a mapper factory for the given type, or the default one.
    * <p>
    * It will look in all three default places (under two domains). If a factory
    * object is specified it will return it, otherwise it will lookup a factory
    * class, if it is found it will try to load it, if not it will return the
    * default factory.
    * 
    * @param type
    * @param from
    * @param to
    * @param config
    * @param properties
    * @return
    */
   private MapperFactory getMapperFactory(Class<?> type, Class<?> from,
         Class<?> to, ConfigManager config,
         Map<String, Map<String, Object>> properties) {

      // Try to resolve a mapper object first from both sources
      String property = ModuleConstants.DEFAULT_MAPPER_FACTORY_PROPERTY;
      MapperFactory mapper = getConfigPropertyObjectWithDefault(property, type,
            MapperFactory.class, config, properties);

      // If mapper object was not found then try to resolve a
      // mapper class, and load the mapper from that class.
      if (mapper == null) {
         Class<? extends MapperFactory> mapperClass = getConfigPropertyClassWithDefault(
               property, type, MapperFactory.class, null, config, properties);
         if (mapperClass != null) {
            mapper = resolveLoader(mapperClass, config, properties).load(
                  mapperClass);
         } else {
            mapper = MapperFactory.getInstance();
         }
      }
      return mapper;
   }

   /**
    * Return a mapper class for the given type.
    * <p>
    * It will look in all three default places (under two domains).
    * 
    * @param type
    * @param from
    * @param to
    * @param config
    * @param properties
    * @return
    */
   private Class<? extends Mapper> getMapperClass(Class<?> type, Class<?> from,
         Class<?> to, ConfigManager config,
         Map<String, Map<String, Object>> properties) {

      String name = ModuleConstants.getMapperNameFor(from, to);
      Class<? extends Mapper> mapperClass = null;

      mapperClass = getConfigPropertyClassWithDefault(name, type, Mapper.class,
            null, config, properties);
      return mapperClass;
   }
}
