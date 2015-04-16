/**
 * 
 */
package gr.uom.se.util.module;

import gr.uom.se.util.config.ConfigManager;
import gr.uom.se.util.mapper.Mapper;
import gr.uom.se.util.mapper.MapperFactory;
import gr.uom.se.util.module.annotations.Module;
import gr.uom.se.util.module.annotations.ProvideModule;
import gr.uom.se.util.property.DomainPropertyProvider;

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
 * <li>A <b>module provider</b>, is a type which can provide a given module
 * instance. Usually the module provider will contain an instance method
 * annotated with {@link ProvideModule} annotation, or a static method. If the
 * provider is a subtype of the module it will be considered the module itself.</li>
 * <li>A <b>loader</b>, is an instance who contains methods to load a given
 * module by providing its class.</li>
 * <li>A <b>parameter provider</b>, is an instance used by other parts of
 * modules API such as the loader, in order to provide the parameters (to
 * inject) of a method/constructor when the loader needs to execute one, or by a
 * property injector to provide module instance's properties.</li>
 * <li><b>Property injector</b>, is an instance who given a module's instance
 * will try to inject to its properties different values.</li>
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
@Module(provider = DefaultModulePropertyLocator.class)
public interface ModulePropertyLocator {

   /**
    * Will try to resolve a property from a property provider, or from the given
    * properties.
    * <p>
    * The returned value will be the same as the given type. This may make
    * conversion on the fly if a property required is found but is not of the
    * type provided. For example if the value is of type string but the required
    * type is int, it may try to convert the string to int.
    * 
    * @param domain
    *           of the configuration where the property will be searched for
    * @param name
    *           the name of the property to resolve
    * @param type
    *           the type of the property
    * @param config
    *           the property provider to look for the property
    * @param properties
    *           map of properties to look for the property
    * @return a value of the given property or null if it was not found
    */
   <T> T getProperty(String domain, String name, Class<T> type,
         DomainPropertyProvider config,
         Map<String, Map<String, Object>> properties);

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
    * when a module stores a property in default modules domain, the property
    * should be prefixed with a proper prefix unique for the module. The
    * prefixed property can be obtained calling
    * {@link ModuleConstants#getPropertyNameForConfig(Class, String)}.
    * <p>
    * This method will first look for the object (be it a java object or a
    * primitive type) in module domain. If the object was not found it will look
    * under the default modules domain. And if not found it will return null.
    * <p>
    * Note: This is the same as calling
    * {@linkplain #getProperty(String, String, Class, DomainPropertyProvider, Map)
    * get property} method, however it will consider that the required property
    * is a config object so it will adjust the domains where to look for and the
    * name of it accordingly.
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
    *           an alternative source of properties to look for the property
    * @return an instance of type {@code objectType} or null if the instance was
    *         not provided or sources are null.
    */
   <T> T getConfigPropertyObject(String name, Class<?> type,
         Class<T> objectType, DomainPropertyProvider config,
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
         String name, Class<T> classType, DomainPropertyProvider config,
         Map<String, Map<String, Object>> properties);

   /**
    * Given a type and a property name, try to locate a class of type
    * {@code classType} under the type's domain or under the default domain.
    * <p>
    * This is the equivalent of
    * {@link #getConfigPropertyClassForDomain(String, String, Class, DomainPropertyProvider, Map)}
    * the difference is that it will adjust the domain and the name of the
    * property accordingly to look for the config class. That is it will try to
    * locate it under the module domain and if not found it will look for it
    * under the default modules domain (same strategy as finding a config
    * property object).
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
         Class<T> classType, DomainPropertyProvider config,
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
    * {@link ModuleConstants#getProviderNameFor(Class)}.
    * <p>
    * This method works the same as
    * {@link #getConfigPropertyObject(String, Class, Class, DomainPropertyProvider, Map)}
    * with the difference that when looking at default config domain the
    * property name is different from the name when looking at module domain.
    * 
    * @param type
    *           to look for its provider
    * @param provider
    *           the type of the provider to look for
    * @param config
    *           the property source to look for the provider
    * @param properties
    *           to look for the provider
    * @return a module provider for {@code type} or null if it was not found
    */
   <T> T getModuleProvider(Class<?> type, Class<T> provider,
         DomainPropertyProvider config,
         Map<String, Map<String, Object>> properties);

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
   Class<?> getModuleProviderClassFor(Class<?> clazz,
         DomainPropertyProvider config,
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
    *           the properties source from where to look the provider
    * @param properties
    *           to look for provider
    * @return a parameter provider instance for the given class
    */
   ParameterProvider getParameterProvider(Class<?> type,
         DomainPropertyProvider config,
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
         DomainPropertyProvider config,
         Map<String, Map<String, Object>> properties);

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
         Class<?> clazz, DomainPropertyProvider config,
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
         DomainPropertyProvider config,
         Map<String, Map<String, Object>> properties);

   /**
    * Get the loader class for the given type.
    * <p>
    * Check only the {@code clazz} domain if there is a property with the given
    * {@link ModuleConstants#LOADER_PROPERTY} and the type {@link ModuleLoader}
    * if so then return its type, if not then check for the class of this
    * property calling
    * {@link #getConfigPropertyClassForDomain(String, String, Class, DomainPropertyProvider, Map)}
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
         DomainPropertyProvider config,
         Map<String, Map<String, Object>> properties);

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
   Class<? extends ModuleLoader> getDefaultLoaderClass(
         DomainPropertyProvider config,
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
   ModuleLoader getLoader(Class<?> type, DomainPropertyProvider config,
         Map<String, Map<String, Object>> properties);

   /**
    * Ensure that this method will return a loader, even if the
    * {@link #getLoader(Class, DomainPropertyProvider, Map)} doesn't return any
    * loader.
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
   ModuleLoader resolveLoader(Class<?> type, DomainPropertyProvider config,
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
   PropertyInjector getPropertyInjector(Class<?> type,
         DomainPropertyProvider config,
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
         DomainPropertyProvider config,
         Map<String, Map<String, Object>> properties);

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
         Class<?> clazz, DomainPropertyProvider config,
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
         DomainPropertyProvider config,
         Map<String, Map<String, Object>> properties);

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
         DomainPropertyProvider config,
         Map<String, Map<String, Object>> properties);
}