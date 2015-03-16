/**
 * 
 */
package gr.uom.se.util.module;

import gr.uom.se.util.config.ConfigManager;
import gr.uom.se.util.mapper.Mapper;
import gr.uom.se.util.mapper.MapperFactory;
import gr.uom.se.util.module.annotations.ProvideModule;

import java.util.Map;

/**
 * Property locator for each module.
 * <p>
 * This interface defines a strategy for looking up different module properties.
 * In modules API jargon, properties are considered all injected parameters
 * (when a method or constructor is executed with reflection) or instance
 * properties ( when a property injector is used). However some properties, are
 * of special interest:
 * <ul>
 * <li>A module provider, is a type which can provide a given module instance.
 * Usually the module provider will contain an instance method annotated with
 * {@link ProvideModule} annotation, or a static method. If the provider is a
 * subtype of the module it will be considered the module itself.</li>
 * <li>A loader, is an instance who contains methods to load a given module by
 * providing its class.</li>
 * <li>A parameter provider, is an instance used by other parts of modules API
 * such as the loader, in order to provide the parameters (to inject) of a
 * method/constructor when the loader needs to execute one, or by a property
 * injector to provide module instance's properties.</li>
 * <li>Property injector, is an instance who given a module's instance will try
 * to inject to its properties different values.</li>
 * </ul>
 * Instances of a property locator defines the strategy of how a property should
 * be located for a given module. They can locate properties from a
 * configuration manager or from a properties map. This interface defines the
 * contract for all default places of a special property (provider, loader,
 * e.t.c). All implementations should at least try to look up properties at
 * those default places, however the strategy of where to look first or which to
 * look is totally relied to the implementations.
 * 
 * @author Elvis Ligu
 */
public interface ModulePropertyLocator {

   /**
    * Will try to resolve a property from the configuration manager, or from the
    * given properties.
    * <p>
    * The returned value will be the same as the given type.
    * 
    * @param domain
    *           of the configuration where the property will be searched for
    * @param name
    *           the name of the property to resolve
    * @param type
    *           the type of the property
    * @param config
    *           the configuration manager to look for the property
    * @param properties
    *           map of properties to look for the property
    * @return a value of the given property or null if it was not found
    */
   <T> T getProperty(String domain, String name, Class<T> type,
         ConfigManager config, Map<String, Map<String, Object>> properties);

   /**
    * Get a config property instance.
    * <p>
    * For each given {@code type} there is a default config domain which can be
    * obtained by calling {@link ModuleConstants#getDefaultConfigFor(Class)}.
    * Different configurations that can affect the operation of modules are
    * stored within this default type config. This method will look under this
    * domain for an instance of the given type {@code objectType}. If an
    * instance was not found there, it will look under the default domain
    * {@linkplain ModuleConstants#DEFAULT_MODULE_CONFIG_DOMAIN default modules
    * domain}. If no instance was found a null will be returned.
    * <p>
    * Note: This is the same as calling
    * {@linkplain #getProperty(String, String, Class, ConfigManager, Map) get
    * property} method, the only difference is that, it will look for the
    * property in two different domains, the first domain will be the type
    * domain and the second domain will be the default modules config domain.
    * That is, if the client wants to look only one of the sources he should
    * specify only that source (config manager or properties).
    * 
    * @param name
    *           the name of the property where to find instance
    * @param type
    *           the type to find its domain for the instance
    * @param objectType
    *           the type of the instance we are looking
    * @param config
    *           to look for the instance
    * @param properties
    *           to look for the instance
    * @return an instance of type {@code objectType} or null if the instance was
    *         not provided or sources are null.
    */
   <T> T getConfigPropertyObject(String name, Class<?> type,
         Class<T> objectType, ConfigManager config,
         Map<String, Map<String, Object>> properties);

   /**
    * Given a domain and a property name, try to locate a class of type
    * {@code classType}.
    * <p>
    * It will first look up an instance within the given domain with the given
    * name, if it is of type {@code classType}. If an instance was not found it
    * will look under the same domain with a name
    * {@link ModuleConstants#getPropertyNameForConfigClass(String)}, to find a
    * class property. The property found at this location may be a class or a
    * string, if the property is class it will check if the class is of the
    * required type, if not it will throw an exception, if the property is
    * string it will load the class with the given name and check if the class
    * is of the required type. If the class is not of the required type it will
    * throw an exception. Note that, this may throw an exception if the property
    * is string but it is not the fully qualified name of a class, so the class
    * can not be found.
    * 
    * @param domain
    *           to look for property
    * @param name
    *           of the property
    * @param classType
    *           the type of the class to look for
    * @param config
    *           where it will look for the class
    * @param properties
    *           where it will look for the class
    * @return a class for the given type
    */
   <T> Class<? extends T> getConfigPropertyClassForDomain(String domain,
         String name, Class<T> classType, ConfigManager config,
         Map<String, Map<String, Object>> properties);

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
   <T> Class<? extends T> getConfigPropertyClass(String name, Class<?> type,
         Class<T> classType, ConfigManager config,
         Map<String, Map<String, Object>> properties);

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
    * 
    * @param type
    *           to look for its provider
    * @param provider
    *           the type of the provider to look for
    * @param config
    *           the configuration manager to look for the provider
    * @param properties
    *           to look for the provider
    * @return a module provider for {@code type} or null if it was not found
    */
   <T> T getModuleProvider(Class<?> type, Class<T> provider,
         ConfigManager config, Map<String, Map<String, Object>> properties);

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
    * 
    * @param clazz
    *           of which the provider will provide
    * @param config
    *           to look for provider class
    * @param properties
    *           to look for provider class
    * @return a provider class
    */
   Class<?> getModuleProviderClassFor(Class<?> clazz, ConfigManager config,
         Map<String, Map<String, Object>> properties);

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
    * found at default domain with the previous name, it will look the default
    * provider under the property with name
    * {@link ModuleConstants#PARAMETER_PROVIDER_PROPERTY}.
    * 
    * @param type
    *           of parameter to load
    * @param config
    *           the config manager from where to look the provider
    * @param properties
    *           to look for provider
    * @return a parameter provider instance for the given class
    */
   ParameterProvider getParameterProvider(Class<?> type, ConfigManager config,
         Map<String, Map<String, Object>> properties);

   /**
    * Check at the default config domain for a parameter provider class.
    * <p>
    * If no property class was found it should return a default parameter
    * provider class.
    * 
    * @param config
    *           to look for the property
    * @param properties
    *           to look for the property
    * @return a parameter provider class
    */
   Class<? extends ParameterProvider> getDefaultParameterProviderClass(
         ConfigManager config, Map<String, Map<String, Object>> properties);

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
    *           to look for the property
    * @param properties
    *           to look for the property
    * @return a parameter provider class
    */
   Class<? extends ParameterProvider> getParameterProviderClassFor(
         Class<?> clazz, ConfigManager config,
         Map<String, Map<String, Object>> properties);

   /**
    * Ensure that this method will return a provider, even if the
    * {@link #getParameterProvider(Class, ConfigManager, Map)} doesn't return
    * any provider.
    * <p>
    * 
    * @param type
    *           the type to get the loader for
    * @param config
    *           to look for the provider
    * @param properties
    *           to look for the provider
    * @return a parameter provider for the given type
    */
   ParameterProvider resolveParameterProvider(Class<?> type,
         ConfigManager config, Map<String, Map<String, Object>> properties);

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
    *           to look for the loader class
    * @param properties
    *           to look for the loader class
    * @return a loader class
    */
   Class<? extends ModuleLoader> getLoaderClassFor(Class<?> clazz,
         ConfigManager config, Map<String, Map<String, Object>> properties);

   /**
    * Check at the default config domain for a loader class.
    * <p>
    * If no property class was found it should return a default class for the
    * loader.
    * 
    * @param config
    *           to look for the loader class
    * @param properties
    *           to look for the loader class
    * @return a loader class
    */
   Class<? extends ModuleLoader> getDefaultLoaderClass(ConfigManager config,
         Map<String, Map<String, Object>> properties);

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
    *           of module to get the loader for
    * @param config
    *           to look for the loader
    * @param properties
    *           to look for the loader
    * @return a loader for the given type
    */
   ModuleLoader getLoader(Class<?> type, ConfigManager config,
         Map<String, Map<String, Object>> properties);

   /**
    * Ensure that this method will return a loader, even if the
    * {@link #getLoader(Class, ConfigManager, Map)} doesn't return any loader.
    * <p>
    * 
    * @param type
    *           of module to get the loader for
    * @param config
    *           to look for the loader
    * @param properties
    *           to look for the loader
    * @return a loader for the given type
    */
   ModuleLoader resolveLoader(Class<?> type, ConfigManager config,
         Map<String, Map<String, Object>> properties);

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
    *           of module to get the injector for
    * @param config
    *           to look for the injector
    * @param properties
    *           to look for the injector
    * @return a property injector for the given type
    */
   PropertyInjector getPropertyInjector(Class<?> type, ConfigManager config,
         Map<String, Map<String, Object>> properties);

   /**
    * Check at the default config domain for an injector class.
    * <p>
    * If no property class was found it should return a a default injector
    * class.
    * 
    * @param config
    *           to look for the injector
    * @param properties
    *           to look for the injector
    * @return a property injector class
    */
   Class<? extends PropertyInjector> getDefaultPropertyInjectorClass(
         ConfigManager config, Map<String, Map<String, Object>> properties);

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
    *           to look for injector
    * @param properties
    *           to look for injector
    * @return a property injector class
    */
   Class<? extends PropertyInjector> getPropertyInjectorClassFor(
         Class<?> clazz, ConfigManager config,
         Map<String, Map<String, Object>> properties);

   /**
    * Ensure that this method will return a property injector, even if the
    * {@link #getPropertyInjector(Class, ConfigManager, Map)} doesn't return any
    * injector.
    * <p>
    * 
    * @param type
    *           the type to get the injector for
    * @param config
    *           to look for the injector
    * @param properties
    *           to look for the injector
    * @return a property injector for the given type
    */
   PropertyInjector resolvePropertyInjector(Class<?> type,
         ConfigManager config, Map<String, Map<String, Object>> properties);

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
    * @return a mapper for the given module type, that should convet from
    *         {@code from} to {@code to}
    */
   Mapper getMapperOfType(Class<?> type, Class<?> from, Class<?> to,
         ConfigManager config, Map<String, Map<String, Object>> properties);
}