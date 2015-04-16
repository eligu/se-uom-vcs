/**
 * 
 */
package gr.uom.se.util.module;

import gr.uom.se.util.mapper.Mapper;
import gr.uom.se.util.property.DomainPropertyProvider;
import gr.uom.se.util.property.PropertyUtils;
import gr.uom.se.util.validation.ArgsCheck;

import java.util.Map;

/**
 * A property locator that will maintain a map of properties from where to look
 * first for a property and then delegate to its superclass if the property was
 * not found.
 * <p>
 * Generally speaking a default property locator will look first for a property
 * within the configuration manager (passed as a parameter to its methods) and
 * then to a properties map. This implementation will try to look first at its
 * properties map (the one supplied when constructed) and then will delegate the
 * call to the super implementation if the property was not found, in order to
 * continue the normal execution.
 * <p>
 * This locator serves as a wrapper for another locator (provided at
 * constructor) and will not affect the locating strategy. However each time a
 * method of this locator is called it will create a chain property provider
 * where the first provider in the chain is the property provider this locator
 * maintain and the second is the passed as 'config' parameter, therefore
 * ensuring that properties this locator maintains will be queries first.
 * 
 * @author Elvis Ligu
 */
public class DynamicModulePropertyLocator implements ModulePropertyLocator {

   /**
    * These properties will be used by this locator to be queried first, for a
    * property to be found.
    */
   private final DomainPropertyProvider dynamicProperties;
   private final ModulePropertyLocator locator;

   /**
    * Create an instance of property locator.
    */
   public DynamicModulePropertyLocator(
         Map<String, Map<String, Object>> properties) {
      this(PropertyUtils.newProvider(properties));
   }

   public DynamicModulePropertyLocator(DomainPropertyProvider provider) {
      this(new DefaultModulePropertyLocator(), provider);
   }

   public DynamicModulePropertyLocator(ModulePropertyLocator locator,
         Map<String, Map<String, Object>> properties) {
      this(locator, PropertyUtils.newProvider(properties));
   }

   /**
    * 
    */
   public DynamicModulePropertyLocator(ModulePropertyLocator locator,
         DomainPropertyProvider provider) {
      ArgsCheck.notNull("provider", provider);
      ArgsCheck.notNull("locator", locator);
      this.locator = locator;
      this.dynamicProperties = provider;
   }

   private DomainPropertyProvider dynamicProvider(
         DomainPropertyProvider provider) {
      if (provider != null) {
         return PropertyUtils.newProvider(dynamicProperties, provider);
      } else {
         return dynamicProperties;
      }
   }

   public <T> T getProperty(String domain, String name, Class<T> type,
         DomainPropertyProvider config,
         Map<String, Map<String, Object>> properties) {
      return locator.getProperty(domain, name, type, dynamicProvider(config),
            properties);
   }

   public <T> T getConfigPropertyObject(String name, Class<?> type,
         Class<T> objectType, DomainPropertyProvider config,
         Map<String, Map<String, Object>> properties) {
      return locator.getConfigPropertyObject(name, type, objectType,
            dynamicProvider(config), properties);
   }

   public <T> Class<? extends T> getConfigPropertyClassForDomain(String domain,
         String name, Class<T> classType, DomainPropertyProvider config,
         Map<String, Map<String, Object>> properties) {
      return locator.getConfigPropertyClassForDomain(domain, name, classType,
            dynamicProvider(config), properties);
   }

   public <T> Class<? extends T> getConfigPropertyClass(String name,
         Class<?> type, Class<T> classType, DomainPropertyProvider config,
         Map<String, Map<String, Object>> properties) {
      return locator.getConfigPropertyClass(name, type, classType,
            dynamicProvider(config), properties);
   }

   public <T> T getModuleProvider(Class<?> type, Class<T> provider,
         DomainPropertyProvider config,
         Map<String, Map<String, Object>> properties) {
      return locator.getModuleProvider(type, provider, dynamicProvider(config),
            properties);
   }

   public Class<?> getModuleProviderClassFor(Class<?> clazz,
         DomainPropertyProvider config,
         Map<String, Map<String, Object>> properties) {
      return locator.getModuleProviderClassFor(clazz, dynamicProvider(config),
            properties);
   }

   public ParameterProvider getParameterProvider(Class<?> type,
         DomainPropertyProvider config,
         Map<String, Map<String, Object>> properties) {
      return locator.getParameterProvider(type, dynamicProvider(config),
            properties);
   }

   public Class<? extends ParameterProvider> getDefaultParameterProviderClass(
         DomainPropertyProvider config,
         Map<String, Map<String, Object>> properties) {
      return locator.getDefaultParameterProviderClass(dynamicProvider(config),
            properties);
   }

   public Class<? extends ParameterProvider> getParameterProviderClassFor(
         Class<?> clazz, DomainPropertyProvider config,
         Map<String, Map<String, Object>> properties) {
      return locator.getParameterProviderClassFor(clazz,
            dynamicProvider(config), properties);
   }

   public ParameterProvider resolveParameterProvider(Class<?> type,
         DomainPropertyProvider config,
         Map<String, Map<String, Object>> properties) {
      return locator.resolveParameterProvider(type, dynamicProvider(config),
            properties);
   }

   public Class<? extends ModuleLoader> getLoaderClassFor(Class<?> clazz,
         DomainPropertyProvider config,
         Map<String, Map<String, Object>> properties) {
      return locator.getLoaderClassFor(clazz, dynamicProvider(config),
            properties);
   }

   public Class<? extends ModuleLoader> getDefaultLoaderClass(
         DomainPropertyProvider config,
         Map<String, Map<String, Object>> properties) {
      return locator.getDefaultLoaderClass(dynamicProvider(config), properties);
   }

   public ModuleLoader getLoader(Class<?> type, DomainPropertyProvider config,
         Map<String, Map<String, Object>> properties) {
      return locator.getLoader(type, dynamicProvider(config), properties);
   }

   public ModuleLoader resolveLoader(Class<?> type,
         DomainPropertyProvider config,
         Map<String, Map<String, Object>> properties) {
      return locator.resolveLoader(type, dynamicProvider(config), properties);
   }

   public PropertyInjector getPropertyInjector(Class<?> type,
         DomainPropertyProvider config,
         Map<String, Map<String, Object>> properties) {
      return locator.getPropertyInjector(type, dynamicProvider(config),
            properties);
   }

   public Class<? extends PropertyInjector> getDefaultPropertyInjectorClass(
         DomainPropertyProvider config,
         Map<String, Map<String, Object>> properties) {
      return locator.getDefaultPropertyInjectorClass(dynamicProvider(config),
            properties);
   }

   public Class<? extends PropertyInjector> getPropertyInjectorClassFor(
         Class<?> clazz, DomainPropertyProvider config,
         Map<String, Map<String, Object>> properties) {
      return locator.getPropertyInjectorClassFor(clazz,
            dynamicProvider(config), properties);
   }

   public PropertyInjector resolvePropertyInjector(Class<?> type,
         DomainPropertyProvider config,
         Map<String, Map<String, Object>> properties) {
      return locator.resolvePropertyInjector(type, dynamicProvider(config),
            properties);
   }

   public Mapper getMapperOfType(Class<?> type, Class<?> from, Class<?> to,
         DomainPropertyProvider config,
         Map<String, Map<String, Object>> properties) {
      return locator.getMapperOfType(type, from, to, dynamicProvider(config),
            properties);
   }
}
