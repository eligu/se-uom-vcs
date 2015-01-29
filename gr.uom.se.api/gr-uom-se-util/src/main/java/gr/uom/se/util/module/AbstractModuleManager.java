/**
 * 
 */
package gr.uom.se.util.module;

import gr.uom.se.util.config.ConfigManager;
import gr.uom.se.util.module.annotations.Property;
import gr.uom.se.util.validation.ArgsCheck;

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
    * the module a property {@link ModuleConstants#PROVIDER_PROPERTY} appended
    * {@code Class}. Therefore this method will register the provider at that
    * place.
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
      String name = ModuleConstants.getProviderClassNameFor(forClass);
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
   public void registerDefaultPropertyInjector(
         PropertyInjector propertyInjector) {

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
    * domain with the name of this annotation. Ie the annotation is not present
    * this will throw an exception.
    */
   @Override
   public void registerAsProperty(Object property) {
      ArgsCheck.notNull("property", property);
      registerAsProperty(property.getClass());
   }

   /**
    * {@inheritDoc}
    * <p>
    * This method will look if this class has an annotation {@link Property}. If
    * so it will register this property at the domain with the name of this
    * annotation. Ie the annotation is not present this will throw an exception.
    */
   @Override
   public void registerAsProperty(Class<?> clazz) {
      ArgsCheck.notNull("property", clazz);

      Property annotation = ModuleUtils.getPropertyAnnotation(clazz
            .getAnnotations());
      ArgsCheck.notNull("@Property annotation", annotation);

      String domain = annotation.domain();
      String name = ModuleConstants.getPropertyNameForConfigClass(annotation
            .name());
      setConfig(domain, name, clazz);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public ModuleLoader getLoader(Class<?> forClass) {
      return ModuleUtils.resolveLoader(forClass, resolveConfig(),
            ModuleUtils.resolveModuleConfig(forClass));
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Class<? extends ModuleLoader> getLoaderClass(Class<?> forClass) {
      return ModuleUtils.getLoaderClassFor(forClass, resolveConfig(),
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
      return ModuleUtils.getModuleProvider(forClass, providerType,
            resolveConfig(), ModuleUtils.resolveModuleConfig(forClass));
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Class<?> getProviderClass(Class<?> forClass) {
      return ModuleUtils.getProviderClassFor(forClass, resolveConfig(),
            ModuleUtils.resolveModuleConfig(forClass));
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public ParameterProvider getParameterProvider(Class<?> forClass) {
      return ModuleUtils.resolveParameterProvider(forClass, resolveConfig(),
            ModuleUtils.resolveModuleConfig(forClass));
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Class<? extends ParameterProvider> getParameterProviderClass(
         Class<?> forClass) {
      return ModuleUtils.getParameterProviderClassFor(forClass,
            resolveConfig(), ModuleUtils.resolveModuleConfig(forClass));
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public PropertyInjector getPropertyInjector(Class<?> forClass) {
      return ModuleUtils.getPropertyInjector(forClass, resolveConfig(),
            ModuleUtils.resolveModuleConfig(forClass));
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Class<? extends PropertyInjector> getPropertyInjectorClass(
         Class<?> forClass) {
      return ModuleUtils.getPropertyInjectorClassFor(forClass, resolveConfig(),
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
