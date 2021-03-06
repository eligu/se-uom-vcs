/**
 * 
 */
package gr.uom.se.util.module;

import gr.uom.se.util.context.Context;
import gr.uom.se.util.module.annotations.Property;
import gr.uom.se.util.property.DomainPropertyHandler;

import java.util.Map;

/**
 * A module manager interface that defines methods and rules about module
 * composition, and module loading.
 * <p>
 * There are four instances that are defined when working with module API. A
 * Module is a class which describe how it should be treated by a loader in
 * order to load it. The default implementation of module API recognize modules
 * based on annotations of {@code gr.uom.se.util.module.annotations} package.
 * <p>
 * The central role in module API plays the module loader {@link ModuleLoader}
 * who is given a class and is required to load an instance of it. It is up to
 * implementation to define the strategy of loading, however the default loader
 * implementation {@link DefaultModuleLoader} reads the descritpion of a module
 * loading procedure based on the annotations of the module (the class to be
 * loaded.
 * <p>
 * A module can define a provider who can be used to get the instances of the
 * module. The default implementation of module loader will resolve the provider
 * and request it to get provide an instance of the required method (usually by
 * calling a method on the provider. The default implementation allows a module
 * to be wired with a provider by using annotations, or configuration. The
 * provider instance doesn't need to be known beforehand, because it can use
 * annotations to provide the default loader which method to call to get the
 * module instance. The default implementation of module loader can be very
 * flexible in that it can call a module constructor if it is required without
 * the need of a provider.
 * <p>
 * When loading a module it is usually required to call a provider method or a
 * module constructor. This will require the injecting of different parameters
 * to provider method or constructor. Therefore, there is an instance of
 * parameter provider {@link ParameterProvider} who can be used to inject the
 * parameters. The default implementation of parameter provider
 * {@link DefaultParameterProvider} is not bound to a specific module loader,
 * and can cooperate with any implementation of module loader. However it
 * requires the use of annotation {@link Property} to resolve the values of
 * parameters. Keep in mind that the default implementation of module loader can
 * resolve the default implementation of parameter provider without the need of
 * it to be provided (and vice versa).
 * <p>
 * The last semantic in the module API is the use of a property injector which
 * is of type {@link PropertyInjector}. Although the default implementation of
 * module API doesn't use a property injector, it should be useful in cases when
 * initializing different parts of a system, to inject properties to modules.
 * The default implementation of property injector uses an instance of parameter
 * provider to resolve the values of the properties. Although it is based in
 * {@link Property} annotations, it can be used with a different parameter
 * provider implementation.
 * <p>
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 * @see ModuleLoader
 * @see ParameterProvider
 * @see PropertyInjector
 */
public interface ModuleManager {

   /**
    * Register the given loader at the default place for the given class.
    * <p>
    * If the loader is null it will remove the previous loader for this class.
    * 
    * @param loader
    *           the loader of the class.
    * @param forClass
    *           the class which should be loaded by the provided loader. Must
    *           not be null.
    * @see #registerLoaderClass(Class, Class)
    * @see #getLoader(Class)
    */
   void registerLoader(ModuleLoader loader, Class<?> forClass);

   /**
    * Register the given object as a property.
    * <p>
    * The place from where this property can be retrieved is implementation
    * specific.
    * 
    * @param property
    *           to be registered. Must not be null.
    * @see #registerAsProperty(Class)
    */
   void registerAsProperty(Object property);

   /**
    * Remove the given object as a property.
    * <p>
    * The place from where this property can be retrieved is implementation
    * specific.
    * 
    * @param property
    *           to be registered. Must not be null.
    */
   void removeAsProperty(Object property);

   /**
    * Register the given provider object for the given class.
    * <p>
    * Registering a provider for the class, instructs the loader of this class
    * to use this provider when loading class instances. If the provider is null
    * it will remove the previous provider for this class.
    * 
    * @param provider
    *           that provides instances of the class
    * @param forClass
    *           the class that provider should provides instance for. Must not
    *           be null.
    * @see #registerProviderClass(Class, Class)
    * @see #getProvider(Class)
    */
   void registerProvider(Object provider, Class<?> forClass);

   /**
    * Get the loader for the given class.
    * <p>
    * This method should return an instance of the loader for the given class.
    * If a loader is not registered before for this class it will provide a
    * default implementation.
    * 
    * @param forClass
    *           the class to get the loader for.
    * @return a loader to load the given class
    * @see #registerLoader(ModuleLoader, Class)
    */
   ModuleLoader getLoader(Class<?> forClass);

   /**
    * Register a loader class ({@code loaderClass}) for the given class (
    * {@code forClass}).
    * <p>
    * This will instruct the module manager that the next time a loader is
    * requested for the given class, to provide that loader. It is useful when
    * the system is partially initializing modules, but they are not loaded
    * until it is requested. If the loader of the class is null it will remove
    * the loader class for the given class.
    * 
    * @param loaderClass
    *           the fully qualified name of the class of the loader. The class
    *           must be compatible with a module loader.
    * @param forClass
    *           the class to register the loader class for. Must not be null.
    * @see #registerLoaderClass(Class, Class)
    * @see #registerLoader(ModuleLoader, Class)
    */
   void registerLoaderClass(String loaderClass, Class<?> forClass);

   /**
    * Register a loader class ({@code loaderClass}) for the given class (
    * {@code forClass}).
    * <p>
    * This will instruct the module manager that the next time a loader is
    * requested for the given class, to provide that loader. It is useful when
    * the system is partially initializing modules, but they are not loaded
    * until it is requested. If the loader of the class is null it will remove
    * the loader class for the given class.
    * 
    * @param loaderClass
    *           the class of the loader.
    * @param forClass
    *           the class to register the loader class for. Must not be null.
    * @see #registerLoaderClass(String, Class)
    * @see #registerLoader(ModuleLoader, Class)
    */
   void registerLoaderClass(Class<? extends ModuleLoader> loaderClass,
         Class<?> forClass);

   /**
    * Register a provider class ({@code providerClass}) for the given type (
    * {@code forClass}).
    * <p>
    * This will instruct the module manager that the next time a provider is
    * requested for the given class, to provide that class provider. It is
    * useful when the system is partially initializing modules, but they are not
    * loaded until it is requested. If the provider of the class is null it will
    * remove the previous provider class for the given class.
    * 
    * @param providerClass
    *           the class of the provider for the given type
    * @param forClass
    *           the type instances of whom are provided by the provider. It must
    *           not be null.
    * @see #registerProviderClass(String, Class)
    * @see #registerProvider(Object, Class)
    */
   void registerProviderClass(Class<?> providerClass, Class<?> forClass);

   /**
    * Register a provider class ({@code providerClass}) for the given type (
    * {@code forClass}).
    * <p>
    * This will instruct the module manager that the next time a provider is
    * requested for the given class, to provide that class provider. It is
    * useful when the system is partially initializing modules, but they are not
    * loaded until it is requested. If the provider of the class is null it will
    * remove the previous provider class for the given class.
    * 
    * @param providerClass
    *           the fully qualified name of the class of the provider for the
    *           given type.
    * @param forClass
    *           the type instances of whom are provided by the provider. It must
    *           not be null.
    * @see #registerProviderClass(String, Class)
    * @see #registerProvider(Object, Class)
    */
   void registerProviderClass(String providerClass, Class<?> forClass);

   /**
    * Register the given parameter provider object for the given class.
    * <p>
    * Registering a provider for the class, instructs the loader of this class
    * to use this provider when providing values to instances of the given
    * class. If the provider is null it will remove the previous provider for
    * this class.
    * 
    * @param provider
    *           that provides values (parameters) to instances of the class
    * @param forClass
    *           the class that provider should provides values for. Must not be
    *           null.
    * @see #registerParameterProviderClass(Class, Class)
    * @see #getParameterProvider(Class)
    */
   void registerParameterProvider(ParameterProvider provider, Class<?> forClass);

   /**
    * Register a parameter provider class ({@code providerClass}) for the given
    * type ( {@code forClass}).
    * <p>
    * This will instruct the module manager that the next time a parameter
    * provider is requested for the given class, to provide that class provider.
    * It is useful when the system is partially initializing modules, but they
    * are not loaded until it is requested. If the provider of the class is null
    * it will remove the previous provider class for the given class.
    * 
    * @param providerClass
    *           the fully qualified name of the class of the provider for the
    *           given type.
    * @param forClass
    *           the type instances of whom should be provided parameter values
    *           by this provider. It must not be null.
    * @see #registerParameterProviderClass(String, Class)
    * @see #registerParameterProvider(Object, Class)
    */
   void registerParameterProviderClass(
         Class<? extends ParameterProvider> providerClass, Class<?> forClass);

   /**
    * Register a parameter provider class ({@code providerClass}) for the given
    * type ( {@code forClass}).
    * <p>
    * This will instruct the module manager that the next time a parameter
    * provider is requested for the given class, to provide that class provider.
    * It is useful when the system is partially initializing modules, but they
    * are not loaded until it is requested. If the provider of the class is null
    * it will remove the previous provider class for the given class.
    * 
    * @param providerClass
    *           the class of the provider for the given type.
    * @param forClass
    *           the type instances of whom should be provided parameter values
    *           by this provider. It must not be null.
    * @see #registerParameterProviderClass(Class, Class)
    * @see #registerParameterProvider(Object, Class)
    */
   void registerParameterProviderClass(String providerClass, Class<?> forClass);

   /**
    * Register the given property injector object for the given class.
    * <p>
    * If the injector is null it will remove the previous injector for this
    * class.
    * 
    * @param propertyInjector
    *           that inject values (properties) to instances of the class
    * @param forClass
    *           the class that injector should provides values for. Must not be
    *           null.
    * @see #registerPropertyInjectorClass(Class, Class)
    * @see #getPropertyInjector(Class)
    */
   void registerPropertyInjector(PropertyInjector propertyInjector,
         Class<?> forClass);

   /**
    * Register a property injector class ({@code injectorClass}) for the given
    * type ( {@code forClass}).
    * <p>
    * This will instruct the module manager that the next time a property
    * injector is requested for the given class, to provide that class injector.
    * It is useful when the system is partially initializing modules, but they
    * are not loaded until it is requested. If the injector of the class is null
    * it will remove the previous provider class for the given class.
    * 
    * @param injectorClass
    *           the class of the property injector for the given type.
    * @param forClass
    *           the type instances of whom should be provided property values by
    *           this injector. It must not be null.
    * @see #registerPropertyInjectorClass(String, Class)
    * @see #registerPropertyInjector(Object, Class)
    */
   void registerPropertyInjectorClass(
         Class<? extends PropertyInjector> injectorClass, Class<?> forClass);

   /**
    * Register a property injector class ({@code injectorClass}) for the given
    * type ( {@code forClass}).
    * <p>
    * This will instruct the module manager that the next time a property
    * injector is requested for the given class, to provide that class injector.
    * It is useful when the system is partially initializing modules, but they
    * are not loaded until it is requested. If the injector of the class is null
    * it will remove the previous provider class for the given class.
    * 
    * @param injectorClass
    *           the fully qualified name of the class of the property injector
    *           for the given type.
    * @param forClass
    *           the type instances of whom should be provided property values by
    *           this injector. It must not be null.
    * @see #registerPropertyInjectorClass(Class, Class)
    * @see #registerPropertyInjector(Object, Class)
    */
   void registerPropertyInjectorClass(String injectorClass, Class<?> forClass);

   /**
    * Get the loader class for the given class.
    * <p>
    * This method should return the class of the loader for the given class. If
    * a loader class is not registered before for this class it will provide a
    * default implementation.
    * 
    * @param forClass
    *           the class to get the loader.
    * @return a loader to load the given class
    * @see #registerLoaderClass(Class, Class)
    */
   Class<? extends ModuleLoader> getLoaderClass(Class<?> forClass);

   /**
    * Get the provider for the given class.
    * <p>
    * This method should return the provider, if any, for the given class. If
    * there is not a registered provider it will return null. To load this
    * provider the caller must obtain a loader for this provider, however it is
    * generally not required to load the provider of a class as it will be
    * resolved by the loader of the class. Though, there are circumstances when
    * it is preferable to load a provider eagerly.
    * <p>
    * Note that a provider is an abstract instance for the system and it is only
    * required by the module loader. Thus it depends on the module loader
    * implementation how to use the provider to get instances of a class.
    * 
    * @param forClass
    *           the class to get the provider.
    * @return a provider for the given class
    * @see #registerProvider(Object, Class)
    */
   Object getProvider(Class<?> forClass);

   /**
    * Get the provider class for the given class.
    * <p>
    * This method should return the class of the provider for the given class.
    * If there is not a registered provider it will return null. To load this
    * provider the caller must obtain a loader for this provider, however it is
    * generally not required to load the provider of a class as it will be
    * resolved by the loader of the class. Though, there are circumstances when
    * it is preferable to load a provider eagerly.
    * <p>
    * Note that a provider is an abstract instance for the system and it is only
    * required by the module loader. Thus it depends on the module loader
    * implementation how to use the provider to get instances of a class.
    * <p>
    * This method may be useful when it is required to check the default
    * provider class for the current time.
    * 
    * @param forClass
    *           the class to get the provider class.
    * @return a provider class for the given class
    * @see #registerProviderClass(Class, Class)
    */
   Class<?> getProviderClass(Class<?> forClass);

   /**
    * Get the parameter provider for the given class.
    * <p>
    * This method should return the parameter provider for the given class. If a
    * parameter provider is not registered before for this class it will provide
    * a default implementation.
    * 
    * @param forClass
    *           the class to get the provider.
    * @return a provider for the given class
    * @see #registerParameterProvider(Object, Class)
    */
   ParameterProvider getParameterProvider(Class<?> forClass);

   /**
    * Get the parameter provider class for the given class.
    * <p>
    * This method should return the parameter provider class for the given
    * class. If a parameter provider class is not registered before for this
    * class it will provide a default implementation.
    * 
    * @param forClass
    *           the class to get the provider class.
    * @return a provider for the given class
    * @see #registerParameterProviderClass(Class, Class)
    */
   Class<? extends ParameterProvider> getParameterProviderClass(
         Class<?> forClass);

   /**
    * Get the property injector for the given class.
    * <p>
    * This method should return the property injector for the given class. If an
    * injector is not registered before for this class it will provide a default
    * implementation.
    * 
    * @param forClass
    *           the class to get the injector.
    * @return an injector for the given class
    * @see #registerPropertyInjector(Object, Class)
    */
   PropertyInjector getPropertyInjector(Class<?> forClass);

   /**
    * Get the property injector class for the given class.
    * <p>
    * This method should return the property injector class for the given class.
    * If a property injector class is not registered before for this class it
    * will provide a default implementation.
    * 
    * @param forClass
    *           the class to get the injector class.
    * @return a class of the injector for the given class
    * @see #registerPropertyInjectorClass(Class, Class)
    */
   Class<? extends PropertyInjector> getPropertyInjectorClass(Class<?> forClass);

   /**
    * Register the given class as a property.
    * <p>
    * The place from where this property can be retrieved is implementation
    * specific. The difference with {@link #registerAsProperty(Object)} method
    * is that the module manager can distinct an object instance from a class,
    * and this plays important role especially in the configuration of modules.
    * For example, if a module is requesting a parameter injection the parameter
    * provider may look up that property if it is registered. On the other hand
    * if a module is requesting a class implementation for an operation it can
    * be retrieved only if it was registered with this method.
    * 
    * @param clazz
    *           to be registered. Must not be null.
    * @see #registerAsProperty(Object)
    */
   void registerAsProperty(Class<?> clazz);

   /**
    * Get the provider for the given class.
    * <p>
    * This method should return the provider, if any, for the given class. If
    * there is not a registered provider it will return null. To load this
    * provider the caller must obtain a loader for this provider, however it is
    * generally not required to load the provider of a class as it will be
    * resolved by the loader of the class. Though, there are circumstances when
    * it is preferable to load a provider eagerly.
    * <p>
    * Note that a provider is an abstract instance for the system and it is only
    * required by the module loader. Thus it depends on the module loader
    * implementation how to use the provider to get instances of a class.
    * Although, the system may have more than one provider for a given class,
    * using this method we can require a specific provider.
    * 
    * @param forClass
    *           the class to get the provider.
    * @param providerType
    *           the type of the provider to be returned
    * @return a provider for the given class
    * @see #registerProvider(Object, Class)
    */
   <T> T getProvider(Class<?> forClass, Class<T> providerType);

   /**
    * Get a property from the properties pool of this manager.
    * <p>
    * In order for loaders, providers, and injectors to work they should be
    * supplied values of properties for different types of operations. The
    * distinct for properties of the same name should be their domain.
    * <p>
    * This manager should maintain a pool of properties and should provide
    * access to these properties to loaders, providers and injectors.
    * <p>
    * The {@code propertyType} is indicative to that of it should return a
    * property of the specified type. The manager may perform on the fly
    * conversions when a property for a given type is requested. For example if
    * a property of int type is requested and the value in the pool of
    * properties is of type String it may be converted to an int by this
    * manager. Keep in mind though that if a property can not be converted it
    * will throw an exception.
    * 
    * @param domain
    *           of the property with the given name
    * @param name
    *           of the property to return
    * @param propertyType
    *           the type of the property to return, must not be null
    * @return a property within the given domain with the given name
    */
   <T> T getProperty(String domain, String name, Class<T> propertyType);

   /**
    * Register the default loader for all modules.
    * <p>
    * If the provided loader is null it will remove this default loader from the
    * manager.
    * 
    * @param loader
    *           the default loader for all modules.
    */
   void registerDefaultLoader(ModuleLoader loader);

   /**
    * Register the default loader class for all modules.
    * <p>
    * If the provided class is null it will remove this default loader class
    * from the manager.
    * 
    * @param loaderClass
    *           the default loader class for all modules.
    */
   void registerDefaultLoaderClass(Class<? extends ModuleLoader> loaderClass);

   /**
    * Register the default parameter provider for all modules.
    * <p>
    * If the provider provider is null it will remove this default provider from
    * the manager.
    * 
    * @param parameterProvider
    *           the default parameter provider for all modules.
    */
   void registerDefaultParameterProvider(ParameterProvider parameterProvider);

   /**
    * Register the default parameter provider class for all modules.
    * <p>
    * If the provided class is null it will remove this default provider class
    * from the manager.
    * 
    * @param providerClass
    *           the default parameter provider class for all modules.
    */
   void registerDefaultParameterProviderClass(
         Class<? extends ParameterProvider> providerClass);

   /**
    * Register the default property injector for all modules.
    * <p>
    * If the property injector is null it will remove this default injector from
    * the manager.
    * 
    * @param propertyInjector
    *           the default property injector for all modules.
    */
   void registerDefaultPropertyInjector(PropertyInjector propertyInjector);

   /**
    * Register the default property injector class for all modules.
    * <p>
    * If the provided class is null it will remove this default injector class
    * from the manager.
    * 
    * @param injectorClass
    *           the default property injector class for all modules.
    */
   void registerDefaultPropertyInjectorClass(
         Class<? extends PropertyInjector> injectorClass);

   /**
    * Remove the given class as a property.
    * <p>
    * The place from where this property can be retrieved is implementation
    * specific.
    * 
    * @param property
    *           to be registered. Must not be null.
    */
   void removeAsProperty(Class<?> property);

   /**
    * It will look for the default loader class if it is registered, if not it
    * will return a default implementation class.
    * <p>
    * 
    * @return a registered default loader class, or a default implementation
    *         that can be registered.
    */
   Class<? extends ModuleLoader> getDefaultLoaderClass();

   /**
    * It will look for the default loader if it is registered.
    * <p>
    * 
    * @return the default loader if it is registered
    */
   ModuleLoader getDefaultLoader();

   /**
    * It will look for the default property injector class if it is registered,
    * if not it will return a default implementation class.
    * <p>
    * 
    * @return a registered default property injector class, or a default
    *         implementation that can be registered.
    */
   Class<? extends PropertyInjector> getDefaultPropertyInjectorClass();

   /**
    * It will look for the default property injector if it is registered.
    * <p>
    * 
    * @return the default property injector if it is registered
    */
   PropertyInjector getDefaultPropertyInjector();

   /**
    * It will look for the default parameter provider class if it is registered,
    * if not it will return a default implementation class.
    * <p>
    * 
    * @return a registered default parameter provider class, or a default
    *         implementation that can be registered.
    */
   Class<? extends ParameterProvider> getDefaultParameterProviderClass();

   /**
    * It will look for the default parameter provider if it is registered.
    * <p>
    * 
    * @return the default parameter provider if it is registered
    */
   ParameterProvider getDefaultParameterProvider();

   /**
    * Try to register loader, provider, parameter provider and property injector
    * if no one could be resolved.
    * <p>
    * This method will first check if there is a provider available for the
    * given class, if not it will try to get the provider from the supplied
    * properties. If a provider was found it will register it for this class. If
    * not found then will try to find a registered provider class, if it was not
    * found then it will look for the provider class from the supplied
    * properties. If a class is found it will register it.
    * <p>
    * The work for registering loader, parameter provider and property injector
    * is the same, with the exception that if the loader (or any of the three)
    * which is supplied in properties is the same as the default loader than it
    * will not register it. If the loader class provided in properties is the
    * same as the default loader class it will not register it.
    * <p>
    * Keep in mind that this method will not override any previous configuration
    * for the module but will try to register only the missing configuration. So
    * an ideal use case of using this method is after this manager has been
    * initialized, and the client want to register additional configuration for
    * the given module without overriding the defaults.
    * 
    * @param moduleClass
    *           the type of the module, must not be null.
    * @param properties
    *           a map of properties, must not be null.
    */
   void registerDefaultsForModule(Class<?> moduleClass,
         Map<String, Object> properties);

   /**
    * Try to register loader, provider, parameter provider and property injector
    * if no one could be resolved.
    * <p>
    * This method will first check if there is a provider available for the
    * given class, if not it will try to get the provider from the supplied
    * properties. If a provider was found it will register it for this class. If
    * not found then will try to find a registered provider class, if it was not
    * found then it will look for the provider class from the supplied
    * properties. If a class is found it will register it.
    * <p>
    * The work for registering loader, parameter provider and property injector
    * is the same, with the exception that if the loader (or any of the three)
    * which is supplied in properties is the same as the default loader than it
    * will not register it. If the loader class provided in properties is the
    * same as the default loader class it will not register it.
    * <p>
    * Keep in mind that this method will not override any previous configuration
    * for the module but will try to register only the missing configuration. So
    * an ideal use case of using this method is after this manager has been
    * initialized, and the client want to register additional configuration for
    * the given module without overriding the defaults.
    * 
    * @param moduleClass
    *           the type of the module, must not be null.
    * @param properties
    *           a map of properties, must not be null.
    */
   void registerDefaultsForModule(String moduleClassName,
         Map<String, Object> properties);

   /**
    * Get a dynamic module context.
    * <p>
    * This method will return a new dynamic module context. However all modules'
    * config properties will be inherited by this context, that is loaders,
    * providers, injectors and so on. If you specify a null handler then any
    * change you make to the context will be reflected to this manager and
    * generally to all modules, for example setting a loader using the context
    * is the same as setting the loader using this manager. However if you
    * specify another handler rather than the one used by modules API any change
    * you make to the context will be reflected only to the context itself and
    * it will affect only the modules that use the context. Also for any
    * property that is not within the specified handler will be looked for into
    * the main properties of the modules' API.
    * 
    * @param type
    *           the type the context is using (used usually for information
    *           returned by {@link Context#getType()}. If null then an
    *           Object.class will be used.
    * @param handler
    *           the source of properties from where to read properties and where
    *           to write them. If null then the source will be the default
    *           modules' source, that is any change to the context will be
    *           reflected to all modules that use directly this manager.
    * 
    * @return dynamic modules context.
    */
   ModuleContext newDynamicContext(Class<?> type, DomainPropertyHandler handler);
}
