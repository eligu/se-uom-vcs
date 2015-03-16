/**
 * 
 */
package gr.uom.se.util.module;

import gr.uom.se.util.config.ConfigManager;
import gr.uom.se.util.module.annotations.Property;
import gr.uom.se.util.validation.ArgsCheck;

import java.util.HashMap;
import java.util.Map;

/**
 * The default abstract implementation of the module manager.
 * <p>
 * It uses all the default implementors of different interfaces defined in
 * module API.
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 * @see DefaultModuleLoader
 * @see DefaultParameterProvider
 * @see DefaultPropertyInjector
 * @see ModuleConstants
 */
public abstract class AbstractModuleManager implements ModuleManager {

   private final ModulePropertyLocator locator;

   /**
    * 
    */
   protected AbstractModuleManager(ModulePropertyLocator locator) {
      this.locator = locator;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void registerDefaultsForModule(String moduleClassName,
         Map<String, Object> properties) {
      ArgsCheck.notNull("moduleClassName", moduleClassName);
      ArgsCheck.notNull("properties", properties);
      if (properties.isEmpty()) {
         return;
      }
      try {
         Class<?> moduleClass = Class.forName(moduleClassName);
         registerDefaultsForModule(moduleClass, properties);
      } catch (ClassNotFoundException e) {
         throw new RuntimeException(e);
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void registerDefaultsForModule(Class<?> moduleClass,
         Map<String, Object> properties) {
      ArgsCheck.notNull("moduleClass", moduleClass);
      ArgsCheck.notNull("properties", properties);
      if (properties.isEmpty()) {
         return;
      }
      // Create a config for the given class based on the given properties
      Map<String, Map<String, Object>> props = new HashMap<>();
      String domain = ModuleConstants.getDefaultConfigFor(moduleClass);

      // Correct the property names so the comply with the
      // module manager implementation
      properties = correctProperties(properties, moduleClass);
      props.put(domain, properties);

      // Register the module provider
      this.registerDefaultProvider(moduleClass, props);

      // Register the parameter provider
      this.registerDefaultParameterProvider(moduleClass, props);

      // Register the property injector
      this.registerDefaultPropertyInjector(moduleClass, props);

      // Register the module loader
      this.registerDefaultLoader(moduleClass, props);
   }

   private Map<String, Object> correctProperties(Map<String, Object> props,
         Class<?> forClass) {
      Map<String, Object> copy = new HashMap<String, Object>();
      for (String name : props.keySet()) {
         copy.put(correctPropertyName(name, forClass), props.get(name));
      }
      return copy;
   }

   private String correctPropertyName(String name, Class<?> forClass) {
      String aname = ModuleConstants.getProviderNameFor(forClass);
      if (name.equals(aname)) {
         return ModuleConstants.PROVIDER_PROPERTY;
      }
      aname = ModuleConstants.getProviderClassNameFor(forClass);
      if (name.equals(aname)) {
         return ModuleConstants.PROVIDER_CLASS_PROPERTY;
      }

      aname = ModuleConstants.getParameterProviderNameFor(forClass);
      if (name.equals(aname)) {
         return ModuleConstants.PARAMETER_PROVIDER_PROPERTY;
      }
      aname = ModuleConstants.getParameterProviderClassNameFor(forClass);
      if (name.equals(aname)) {
         return ModuleConstants.PARAMETER_PROVIDER_CLASS_PROPERTY;
      }

      aname = ModuleConstants.getPropertyInjectorNameFor(forClass);
      if (name.equals(aname)) {
         return ModuleConstants.PROPERTY_INJECTOR_PROPERTY;
      }
      aname = ModuleConstants.getPropertyInjectorClassNameFor(forClass);
      if (name.equals(aname)) {
         return ModuleConstants.PROPERTY_INJECTOR_CLASS_PROPERTY;
      }

      aname = ModuleConstants.getLoaderNameFor(forClass);
      if (name.equals(aname)) {
         return ModuleConstants.LOADER_PROPERTY;
      }
      aname = ModuleConstants.getLoaderClassNameFor(forClass);
      if (name.equals(aname)) {
         return ModuleConstants.LOADER_CLASS_PROPERTY;
      }
      return name;
   }

   /**
    * Register the provider or its class for the given module if this manager
    * doesn't have any registered.
    * <p>
    * 
    * @param moduleClass
    *           the type of the module, must not be null.
    * @param properties
    *           a map of properties, must not be null.
    */
   private void registerDefaultProvider(Class<?> moduleClass,
         Map<String, Map<String, Object>> props) {
      // /////////////////////////////////////////////////////////////////////
      // 1 - Check if the class has any provider available
      // Check first at this manager to find a provider
      Object provider = locator.getModuleProvider(moduleClass, Object.class,
            resolveConfig(), null);

      // If provider is null then check at the given properties
      if (provider == null) {
         provider = locator.getModuleProvider(moduleClass, Object.class, null,
               props);
         // If provider is found at this place then register it
         if (provider != null) {
            this.registerProvider(provider, moduleClass);
         }
      }

      // 2 - Check if the class has any provider class available
      // There is not a provider available
      // So we must register the provider class if any
      if (provider == null) {
         // Get first the provider class from this manager if any
         Class<?> providerClass = locator.getModuleProviderClassFor(
               moduleClass, resolveConfig(), null);
         // If provider class was not defined in config then check at the given
         // properties
         if (providerClass == null) {
            providerClass = locator.getModuleProviderClassFor(moduleClass,
                  null, props);
         } else {
            // A provider class is found at this manager so no need to
            // register it again
            return;
         }

         // If provider class is found at given properties then register it
         if (providerClass != null) {
            this.registerProviderClass(providerClass, moduleClass);
         }
      }
   }

   /**
    * Register the parameter provider or its class for the given module if this
    * manager doesn't have any registered.
    * <p>
    * 
    * @param moduleClass
    *           the type of the module, must not be null.
    * @param properties
    *           a map of properties, must not be null.
    */
   private void registerDefaultParameterProvider(Class<?> moduleClass,
         Map<String, Map<String, Object>> props) {

      // 1 - Check if the class has any parameter provider available
      // Check first at this manager to find a provider
      ParameterProvider parameterProvider = locator.getParameterProvider(
            moduleClass, resolveConfig(), null);

      // If provider is null then check at the given properties
      if (parameterProvider == null) {
         parameterProvider = locator.getParameterProvider(moduleClass, null,
               props);

         // If provider is found at this place and it is not the default one
         // then register it
         if (parameterProvider != null
               && !parameterProvider.equals(getDefaultParameterProvider())) {
            this.registerParameterProvider(parameterProvider, moduleClass);
         }
      }

      // 2 - Check if the class has any provider class available
      // There is not a provider available
      // So we must register the provider class if any
      if (parameterProvider == null) {
         // Get first the provider class from this manager if any
         Class<? extends ParameterProvider> providerClass = locator
               .getParameterProviderClassFor(moduleClass, resolveConfig(), null);

         // If provider class was not defined in config then check at the given
         // properties
         if (providerClass == null) {
            providerClass = locator.getParameterProviderClassFor(
                  moduleClass, null, props);
         } else {
            // A provider class is found at this manager so no need to
            // register it again
            return;
         }

         // If provider class is found at given properties and it is not the
         // default one then register it
         if (providerClass != null
               && !providerClass.equals(getDefaultParameterProviderClass())) {
            this.registerParameterProviderClass(providerClass, moduleClass);
         }
      }
   }

   /**
    * Register the property injector or its class for the given module if this
    * manager doesn't have any registered.
    * <p>
    * 
    * @param moduleClass
    *           the type of the module, must not be null.
    * @param properties
    *           a map of properties, must not be null.
    */
   private void registerDefaultPropertyInjector(Class<?> moduleClass,
         Map<String, Map<String, Object>> props) {

      // 1 - Check if the class has any property injector available
      // Check first at this manager to find a provider
      PropertyInjector injector = locator.getPropertyInjector(moduleClass,
            resolveConfig(), null);

      // If injector is null then check at the given properties
      if (injector == null) {
         injector = locator.getPropertyInjector(moduleClass, null, props);

         // If injector is found at this place and it is not the default one
         // then register it
         if (injector != null && !injector.equals(getDefaultPropertyInjector())) {
            this.registerPropertyInjector(injector, moduleClass);
         }
      }

      // 2 - Check if the class has any injector class available
      // There is not an injector available
      // So we must register the injector class if any
      if (injector == null) {
         // Get first the injector class from this manager if any
         Class<? extends PropertyInjector> injectorClass = locator
               .getPropertyInjectorClassFor(moduleClass, resolveConfig(), null);

         // If injector class was not defined in config then check at the given
         // properties
         if (injectorClass == null) {
            injectorClass = locator.getPropertyInjectorClassFor(
                  moduleClass, null, props);
         } else {
            // A provider class is found at this manager so no need to
            // register it again
            return;
         }

         // If provider class is found at given properties and it is not the
         // default one then register it
         if (injectorClass != null
               && !injectorClass.equals(getDefaultPropertyInjectorClass())) {
            this.registerPropertyInjectorClass(injectorClass, moduleClass);
         }
      }
   }

   /**
    * Register the loader or its class for the given module if this manager
    * doesn't have any registered.
    * <p>
    * 
    * @param moduleClass
    *           the type of the module, must not be null.
    * @param properties
    *           a map of properties, must not be null.
    */
   private void registerDefaultLoader(Class<?> moduleClass,
         Map<String, Map<String, Object>> props) {

      // 1 - Check if the class has any loader available
      // Check first at this manager to find a loader
      ModuleLoader loader = locator.getLoader(moduleClass, resolveConfig(),
            null);

      // If loader is null then check at the given properties
      if (loader == null) {
         loader = locator.getLoader(moduleClass, null, props);

         // If loader is found at this place and it is not the default one
         // then register it
         if (loader != null && !loader.equals(getDefaultLoader())) {
            this.registerLoader(loader, moduleClass);
         }
      }

      // 2 - Check if the class has any loader class available
      // There is not a loader available
      // So we must register the loader class if any
      if (loader == null) {
         // Get first the loader class from this manager if any
         Class<? extends ModuleLoader> loaderClass = locator
               .getLoaderClassFor(moduleClass, resolveConfig(), null);

         // If loader class was not defined in config then check at the given
         // properties
         if (loaderClass == null) {
            loaderClass = locator.getLoaderClassFor(moduleClass, null,
                  props);
         } else {
            // A loader class is found at this manager so no need to
            // register it again
            return;
         }

         // If provider class is found at given properties and it is not the
         // default one then register it
         if (loaderClass != null
               && !loaderClass.equals(getDefaultLoaderClass())) {
            this.registerLoaderClass(loaderClass, moduleClass);
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public ParameterProvider getDefaultParameterProvider() {
      String name = ModuleConstants.PARAMETER_PROVIDER_PROPERTY;
      String domain = ModuleConstants.DEFAULT_MODULE_CONFIG_DOMAIN;
      return this.getProperty(domain, name, ParameterProvider.class);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Class<? extends ParameterProvider> getDefaultParameterProviderClass() {
      return locator
            .getDefaultParameterProviderClass(resolveConfig(), null);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public PropertyInjector getDefaultPropertyInjector() {
      String name = ModuleConstants.PROPERTY_INJECTOR_PROPERTY;
      String domain = ModuleConstants.DEFAULT_MODULE_CONFIG_DOMAIN;
      return this.getProperty(domain, name, PropertyInjector.class);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Class<? extends PropertyInjector> getDefaultPropertyInjectorClass() {
      return locator.getDefaultPropertyInjectorClass(resolveConfig(), null);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public ModuleLoader getDefaultLoader() {
      String name = ModuleConstants.LOADER_PROPERTY;
      String domain = ModuleConstants.DEFAULT_MODULE_CONFIG_DOMAIN;
      return this.getProperty(domain, name, ModuleLoader.class);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Class<? extends ModuleLoader> getDefaultLoaderClass() {
      return locator.getDefaultLoaderClass(resolveConfig(), null);
   }

   /**
    * {@inheritDoc}
    * <p>
    * The default strategy for looking for a loader given a class is to look
    * under the domain with the same name of the fully qualified name of the
    * class a property {@link ModuleConstants#LOADER_PROPERTY}. Therefore this
    * method will register the loader at that place.
    */
   @Override
   public void registerLoader(ModuleLoader loader, Class<?> forClass) {
      ArgsCheck.notNull("forClass", forClass);

      String property = ModuleConstants.LOADER_PROPERTY;
      String domain = ModuleConstants.getDefaultConfigFor(forClass);
      setConfig(domain, property, loader);
   }

   /**
    * {@inheritDoc}
    * <p>
    * Register this loader at domain
    * {@link ModuleConstants#DEFAULT_MODULE_CONFIG_DOMAIN}, with property name
    * {@link ModuleConstants#LOADER_PROPERTY}.
    */
   @Override
   public void registerDefaultLoader(ModuleLoader loader) {

      String property = ModuleConstants.LOADER_PROPERTY;
      String domain = ModuleConstants.DEFAULT_MODULE_CONFIG_DOMAIN;
      setConfig(domain, property, loader);
   }

   /**
    * {@inheritDoc}
    * <p>
    * The default strategy for looking for a loader class of a module is to look
    * under the domain with the same name of the fully qualified name of the
    * module a property {@link ModuleConstants#LOADER_PROPERTY} appended
    * {@code Class}. Therefore this method will register the loader at that
    * place.
    */
   @Override
   public void registerLoaderClass(Class<? extends ModuleLoader> loaderClass,
         Class<?> forClass) {
      registerLoaderClass0(loaderClass, forClass);
   }

   /**
    * {@inheritDoc}
    * <p>
    * Register this loader class at domain
    * {@link ModuleConstants#DEFAULT_MODULE_CONFIG_DOMAIN}, with property name
    * {@link ModuleConstants#LOADER_CLASS_PROPERTY}.
    */
   @Override
   public void registerDefaultLoaderClass(
         Class<? extends ModuleLoader> loaderClass) {

      String property = ModuleConstants
            .getPropertyNameForConfigClass(ModuleConstants.LOADER_PROPERTY);
      String domain = ModuleConstants.DEFAULT_MODULE_CONFIG_DOMAIN;
      setConfig(domain, property, loaderClass);
   }

   /**
    * Register a loader class to the its config domain.
    * <p>
    */
   private void registerLoaderClass0(Object loaderClass, Class<?> forClass) {
      ArgsCheck.notNull("forClass", forClass);

      String property = ModuleConstants
            .getPropertyNameForConfigClass(ModuleConstants.LOADER_PROPERTY);
      String domain = ModuleConstants.getDefaultConfigFor(forClass);
      setConfig(domain, property, loaderClass);
   }

   /**
    * {@inheritDoc}
    * <p>
    * The default strategy for looking for a loader class of a module is to look
    * under the domain with the same name of the fully qualified name of the
    * module a property {@link ModuleConstants#LOADER_PROPERTY} appended
    * {@code Class}. Therefore this method will register the loader at that
    * place.
    */
   @Override
   public void registerLoaderClass(String loaderClass, Class<?> forClass) {
      registerLoaderClass0(loaderClass, forClass);
   }

   /**
    * {@inheritDoc}
    * <p>
    * The default strategy for looking for a provider of a module is to look
    * under the domain with the same name of the fully qualified name of the
    * module a property {@link ModuleConstants#PROVIDER_PROPERTY}. Therefore
    * this method will register the provider at that place.
    */
   @Override
   public void registerProvider(Object provider, Class<?> forClass) {

      ArgsCheck.notNull("forClass", forClass);

      String domain = ModuleConstants.getDefaultConfigFor(forClass);
      String name = ModuleConstants.PROVIDER_PROPERTY;
      setConfig(domain, name, provider);
   }

   /**
    * {@inheritDoc}
    * <p>
    * The default strategy for looking for a provider class of a module is to
    * look under the domain with the same name of the fully qualified name of
    * the module a property {@link ModuleConstants#PROVIDER_CLASS_PROPERTY}
    * appended {@code Class}. Therefore this method will register the provider
    * at that place.
    */
   @Override
   public void registerProviderClass(Class<?> providerClass, Class<?> forClass) {
      registerProviderClass0(providerClass, forClass);
   }

   /**
    * Register a provider class at forClass domain.
    * <p>
    * 
    * @param providerClass
    * @param forClass
    */
   private void registerProviderClass0(Object providerClass, Class<?> forClass) {

      ArgsCheck.notNull("forClass", forClass);

      String domain = ModuleConstants.getDefaultConfigFor(forClass);
      String name = ModuleConstants.PROVIDER_CLASS_PROPERTY;
      setConfig(domain, name, providerClass);
   }

   /**
    * {@inheritDoc}
    * <p>
    * The default strategy for looking for a provider class of a module is to
    * look under the domain with the same name of the fully qualified name of
    * the module a property {@link ModuleConstants#PROVIDER_PROPERTY} appended
    * {@code Class}. Therefore this method will register the provider at that
    * place.
    */
   @Override
   public void registerProviderClass(String providerClass, Class<?> forClass) {
      registerProviderClass0(providerClass, forClass);
   }

   /**
    * {@inheritDoc}
    * <p>
    * The default strategy for looking for a parameter provider of a module is
    * to look under the domain with the same name of the fully qualified name of
    * the module a property {@link ModuleConstants#PARAMETER_PROVIDER_PROPERTY}.
    * Therefore this method will register the provider at that place.
    */
   @Override
   public void registerParameterProvider(ParameterProvider provider,
         Class<?> forClass) {

      ArgsCheck.notNull("forClass", forClass);

      String domain = ModuleConstants.getDefaultConfigFor(forClass);
      String name = ModuleConstants.PARAMETER_PROVIDER_PROPERTY;
      setConfig(domain, name, provider);
   }

   /**
    * {@inheritDoc}
    * <p>
    * Register this provider at domain
    * {@link ModuleConstants#DEFAULT_MODULE_CONFIG_DOMAIN}, with property name
    * {@link ModuleConstants#PARAMETER_PROVIDER_PROPERTY}.
    */
   @Override
   public void registerDefaultParameterProvider(
         ParameterProvider parameterProvider) {

      String property = ModuleConstants.PARAMETER_PROVIDER_PROPERTY;
      String domain = ModuleConstants.DEFAULT_MODULE_CONFIG_DOMAIN;
      setConfig(domain, property, parameterProvider);
   }

   /**
    * {@inheritDoc}
    * <p>
    * The default strategy for looking for a parameter provider class of a
    * module is to look under the domain with the same name of the fully
    * qualified name of the module a property
    * {@link ModuleConstants#PARAMETER_PROVIDER_PROPERTY} appended {@code Class}
    * . Therefore this method will register the provider at that place.
    */
   @Override
   public void registerParameterProviderClass(
         Class<? extends ParameterProvider> providerClass, Class<?> forClass) {
      registerParameterProviderClass0(providerClass, forClass);
   }

   /**
    * {@inheritDoc}
    * <p>
    * Register this parameter provider class at domain
    * {@link ModuleConstants#DEFAULT_MODULE_CONFIG_DOMAIN}, with property name
    * {@link ModuleConstants#PARAMETER_PROVIDER_CLASS_PROPERTY}.
    */
   @Override
   public void registerDefaultParameterProviderClass(
         Class<? extends ParameterProvider> providerClass) {

      String property = ModuleConstants
            .getPropertyNameForConfigClass(ModuleConstants.PARAMETER_PROVIDER_PROPERTY);
      String domain = ModuleConstants.DEFAULT_MODULE_CONFIG_DOMAIN;
      setConfig(domain, property, providerClass);
   }

   /**
    * Register a parameter provider class for the given class at its domain.
    * <p>
    * 
    * @param providerClass
    * @param forClass
    */
   private void registerParameterProviderClass0(Object providerClass,
         Class<?> forClass) {

      ArgsCheck.notNull("forClass", forClass);

      String domain = ModuleConstants.getDefaultConfigFor(forClass);
      String name = ModuleConstants
            .getPropertyNameForConfigClass(ModuleConstants.PARAMETER_PROVIDER_PROPERTY);
      setConfig(domain, name, providerClass);
   }

   /**
    * {@inheritDoc}
    * <p>
    * The default strategy for looking for a parameter provider class of a
    * module is to look under the domain with the same name of the fully
    * qualified name of the module a property
    * {@link ModuleConstants#PARAMETER_PROVIDER_PROPERTY} appended {@code Class}
    * . Therefore this method will register the provider at that place.
    */
   @Override
   public void registerParameterProviderClass(String providerClass,
         Class<?> forClass) {
      registerParameterProviderClass0(providerClass, forClass);
   }

   /**
    * {@inheritDoc}
    * <p>
    * The default strategy for looking for a property injector of a module is to
    * look under the domain with the same name of the fully qualified name of
    * the module a property {@link ModuleConstants#PROPERTY_INJECTOR_PROPERTY}.
    * Therefore this method will register the injector at that place.
    */
   @Override
   public void registerPropertyInjector(PropertyInjector propertyInjector,
         Class<?> forClass) {

      ArgsCheck.notNull("forClass", forClass);

      String domain = ModuleConstants.getDefaultConfigFor(forClass);
      String name = ModuleConstants.PROPERTY_INJECTOR_PROPERTY;
      setConfig(domain, name, propertyInjector);
   }

   /**
    * {@inheritDoc}
    * <p>
    * Register this property injector at domain
    * {@link ModuleConstants#DEFAULT_MODULE_CONFIG_DOMAIN}, with property name
    * {@link ModuleConstants#PROPERTY_INJECTOR_PROPERTY}.
    */
   @Override
   public void registerDefaultPropertyInjector(PropertyInjector propertyInjector) {

      String property = ModuleConstants.PROPERTY_INJECTOR_PROPERTY;
      String domain = ModuleConstants.DEFAULT_MODULE_CONFIG_DOMAIN;
      setConfig(domain, property, propertyInjector);
   }

   /**
    * {@inheritDoc}
    * <p>
    * The default strategy for looking for a property injector class of a module
    * is to look under the domain with the same name of the fully qualified name
    * of the module a property
    * {@link ModuleConstants#PROPERTY_INJECTOR_PROPERTY} appended {@code Class}
    * . Therefore this method will register the provider at that place.
    */
   @Override
   public void registerPropertyInjectorClass(
         Class<? extends PropertyInjector> injectorClass, Class<?> forClass) {
      registerPropertyInjectorClass0(injectorClass, forClass);
   }

   /**
    * {@inheritDoc}
    * <p>
    * Register this property injector class at domain
    * {@link ModuleConstants#DEFAULT_MODULE_CONFIG_DOMAIN}, with property name
    * {@link ModuleConstants#PROPERTY_INJECTOR_CLASS_PROPERTY}.
    */
   @Override
   public void registerDefaultPropertyInjectorClass(
         Class<? extends PropertyInjector> injectorClass) {

      String property = ModuleConstants
            .getPropertyNameForConfigClass(ModuleConstants.PROPERTY_INJECTOR_PROPERTY);
      String domain = ModuleConstants.DEFAULT_MODULE_CONFIG_DOMAIN;
      setConfig(domain, property, injectorClass);
   }

   /**
    * Register a property injector class for the given class at its domain.
    * <p>
    * 
    * @param providerClass
    * @param forClass
    */
   private void registerPropertyInjectorClass0(Object injectorClass,
         Class<?> forClass) {

      ArgsCheck.notNull("forClass", forClass);

      String domain = ModuleConstants.getDefaultConfigFor(forClass);
      String name = ModuleConstants
            .getPropertyNameForConfigClass(ModuleConstants.PARAMETER_PROVIDER_PROPERTY);
      setConfig(domain, name, injectorClass);
   }

   /**
    * {@inheritDoc}
    * <p>
    * The default strategy for looking for a property injector class of a module
    * is to look under the domain with the same name of the fully qualified name
    * of the module a property
    * {@link ModuleConstants#PROPERTY_INJECTOR_PROPERTY} appended {@code Class}
    * . Therefore this method will register the provider at that place.
    */
   @Override
   public void registerPropertyInjectorClass(String injectorClass,
         Class<?> forClass) {
      registerPropertyInjectorClass0(injectorClass, forClass);
   }

   /**
    * {@inheritDoc}
    * <p>
    * This method will look if the class of the provided property has an
    * annotation {@link Property}. If so it will register this property at the
    * domain with the name of this annotation. If the annotation is not present
    * this will throw an exception.
    */
   @Override
   public void registerAsProperty(Object property) {
      ArgsCheck.notNull("property", property);

      Property annotation = ModuleUtils.getPropertyAnnotation(property
            .getClass().getAnnotations());
      if (annotation != null) {
         String domain = annotation.domain();
         String name = annotation.name();
         setConfig(domain, name, property);
      }
   }

   /**
    * {@inheritDoc} This method will look if the class of the provided property
    * has an annotation {@link Property}. If so it will remove this property at
    * the domain with the name of this annotation. If the annotation is not
    * present this will throw an exception.
    */
   @Override
   public void removeAsProperty(Object property) {
      ArgsCheck.notNull("property", property);
      Property annotation = ModuleUtils.getPropertyAnnotation(property
            .getClass().getAnnotations());

      if (annotation != null) {
         String domain = annotation.domain();
         String name = annotation.name();
         setConfig(domain, name, null);
      }
   }

   /**
    * {@inheritDoc} This method will look if the class of the provided property
    * has an annotation {@link Property}. If so it will remove this property at
    * the domain with the name of this annotation. If the annotation is not
    * present this will throw an exception.
    */
   @Override
   public void removeAsProperty(Class<?> property) {
      ArgsCheck.notNull("property", property);
      Property annotation = ModuleUtils.getPropertyAnnotation(property
            .getAnnotations());
      if (annotation != null) {
         String domain = annotation.domain();
         String name = ModuleConstants.getPropertyNameForConfigClass(annotation
               .name());
         setConfig(domain, name, null);
      }
   }

   /**
    * {@inheritDoc}
    * <p>
    * This method will look if this class has an annotation {@link Property}. If
    * so it will register this property at the domain with the name of this
    * annotation. If the annotation is not present it will do nothing.
    */
   @Override
   public void registerAsProperty(Class<?> clazz) {
      ArgsCheck.notNull("property", clazz);

      Property annotation = ModuleUtils.getPropertyAnnotation(clazz
            .getAnnotations());

      if (annotation != null) {
         String domain = annotation.domain();
         String name = ModuleConstants.getPropertyNameForConfigClass(annotation
               .name());
         setConfig(domain, name, clazz);
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public ModuleLoader getLoader(Class<?> forClass) {
      return locator.resolveLoader(forClass, resolveConfig(),
            ModuleUtils.resolveModuleConfig(forClass));
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Class<? extends ModuleLoader> getLoaderClass(Class<?> forClass) {
      return locator.getLoaderClassFor(forClass, resolveConfig(),
            ModuleUtils.resolveModuleConfig(forClass));
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Object getProvider(Class<?> forClass) {
      return getProvider(forClass, Object.class);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public <T> T getProvider(Class<?> forClass, Class<T> providerType) {
      return locator.getModuleProvider(forClass, providerType,
            resolveConfig(), ModuleUtils.resolveModuleConfig(forClass));
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Class<?> getProviderClass(Class<?> forClass) {
      return locator.getModuleProviderClassFor(forClass, resolveConfig(),
            ModuleUtils.resolveModuleConfig(forClass));
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public ParameterProvider getParameterProvider(Class<?> forClass) {
      return locator.resolveParameterProvider(forClass, resolveConfig(),
            ModuleUtils.resolveModuleConfig(forClass));
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Class<? extends ParameterProvider> getParameterProviderClass(
         Class<?> forClass) {
      return locator.getParameterProviderClassFor(forClass,
            resolveConfig(), ModuleUtils.resolveModuleConfig(forClass));
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public PropertyInjector getPropertyInjector(Class<?> forClass) {
      return locator.resolvePropertyInjector(forClass, resolveConfig(),
            ModuleUtils.resolveModuleConfig(forClass));
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Class<? extends PropertyInjector> getPropertyInjectorClass(
         Class<?> forClass) {
      return locator.getPropertyInjectorClassFor(forClass, resolveConfig(),
            ModuleUtils.resolveModuleConfig(forClass));
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public <T> T getProperty(String domain, String name, Class<T> propertyType) {
      return resolveConfig().getProperty(domain, name, propertyType);
   }

   /**
    * Set a configuration to a config manager.
    * <p>
    * 
    * @param domain
    * @param name
    * @param value
    */
   private void setConfig(String domain, String name, Object value) {
      resolveConfig().setProperty(domain, name, value);
   }

   private ConfigManager resolveConfig() {
      ConfigManager config = getConfig();
      if (config == null) {
         throw new RuntimeException("Config manager is not available");
      }
      return config;
   }

   /**
    * Sub classes must implement this method in order to provide the
    * implementation of this class an access to a config manager.
    * <p>
    * The returned config manager should not be null.
    * 
    * @return
    */
   protected abstract ConfigManager getConfig();
}
