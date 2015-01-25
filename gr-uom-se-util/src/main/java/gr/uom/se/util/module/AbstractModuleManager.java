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
    */
   @Override
   public void registerLoaderClass(Class<? extends ModuleLoader> loaderClass,
         Class<?> forClass) {
      registerLoaderClass0(loaderClass, forClass);
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
    */
   @Override
   public void registerLoaderClass(String loaderClass, Class<?> forClass) {
      registerLoaderClass0(loaderClass, forClass);
   }

   /**
    * {@inheritDoc}
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
    */
   @Override
   public void registerProviderClass(String providerClass, Class<?> forClass) {
      registerProviderClass0(providerClass, forClass);
   }

   /**
    * {@inheritDoc}
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
    */
   @Override
   public void registerParameterProviderClass(
         Class<? extends ParameterProvider> providerClass, Class<?> forClass) {
      registerParameterProviderClass0(providerClass, forClass);
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
    */
   @Override
   public void registerParameterProviderClass(String providerClass,
         Class<?> forClass) {
      registerParameterProviderClass0(providerClass, forClass);
   }

   /**
    * {@inheritDoc}
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
    */
   @Override
   public void registerPropertyInjectorClass(
         Class<? extends PropertyInjector> injectorClass, Class<?> forClass) {
      registerPropertyInjectorClass0(injectorClass, forClass);
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
    */
   @Override
   public void registerPropertyInjectorClass(String injectorClass,
         Class<?> forClass) {
      registerPropertyInjectorClass0(injectorClass, forClass);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void registerAsProperty(Object property) {

      ArgsCheck.notNull("property", property);

      Property annotation = ModuleUtils.getPropertyAnnotation(property
            .getClass().getAnnotations());
      ArgsCheck.notNull("@Property annotation", annotation);

      String domain = annotation.domain();
      String name = annotation.name();
      setConfig(domain, name, property);
   }

   /**
    * {@inheritDoc}
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
            ModuleUtils.getModuleConfig(forClass));
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Class<? extends ModuleLoader> getLoaderClass(Class<?> forClass) {
      return ModuleUtils.getLoaderClassFor(forClass, resolveConfig(),
            ModuleUtils.getModuleConfig(forClass));
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Object getProvider(Class<?> forClass) {
      return ModuleUtils.getModuleProvider(forClass, Object.class,
            resolveConfig(), ModuleUtils.getModuleConfig(forClass));
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Class<?> getProviderClass(Class<?> forClass) {
      return ModuleUtils.getProviderClassFor(forClass, resolveConfig(),
            ModuleUtils.getModuleConfig(forClass));
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public ParameterProvider getParameterProvider(Class<?> forClass) {
      return ModuleUtils.getParameterProvider(forClass, resolveConfig(),
            ModuleUtils.getModuleConfig(forClass));
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Class<? extends ParameterProvider> getParameterProviderClass(
         Class<?> forClass) {
      return ModuleUtils.getParameterProviderClassFor(forClass,
            resolveConfig(), ModuleUtils.getModuleConfig(forClass));
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public PropertyInjector getPropertyInjector(Class<?> forClass) {
      return ModuleUtils.getPropertyInjector(forClass, resolveConfig(),
            ModuleUtils.getModuleConfig(forClass));
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Class<? extends PropertyInjector> getPropertyInjectorClass(
         Class<?> forClass) {
      return ModuleUtils.getPropertyInjectorClassFor(forClass, resolveConfig(),
            ModuleUtils.getModuleConfig(forClass));
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

   protected abstract ConfigManager getConfig();
}
