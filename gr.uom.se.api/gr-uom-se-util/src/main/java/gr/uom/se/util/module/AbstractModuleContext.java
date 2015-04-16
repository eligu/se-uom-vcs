/**
 * 
 */
package gr.uom.se.util.module;

import java.lang.annotation.Annotation;
import java.util.Map;

import gr.uom.se.util.property.DomainPropertyHandler;
import gr.uom.se.util.validation.ArgsCheck;

/**
 * The default abstract implementation for a module context.
 * <p>
 * This implementation is based on {@link DomainPropertyHandler} and
 * {@link ModulePropertyLocator} so it can be plugged in to module manager and
 * other modules API implementations. The resolving of modules config properties
 * is based on module property locator implementation, while the managing of
 * modules config properties is based on default modules config domain. That is
 * all properties will be stored in property handler in the default domain.
 * However the exact property name they will get while being stored should be
 * defined by subtypes. This allows to construct different contexts based on the
 * type of the module. For example if we want to construct a default context for
 * all types of modules, the subtype must not prefix the properties with type's
 * name when a null type or an Object type is specified. That is when specifying
 * for example a module loader for null type that would mean we are specifying
 * it for all modules so it should be stored in the default place.
 * <p>
 * One key feature of this context implementation is that it allows other parts
 * of the application to define a sandboxed module context that all the
 * properties will be overridden, without affecting the whole modules API
 * functionality. However when client needs a property and that property is not
 * present this implementation will use its parent context to find it. For
 * example suppose that the client has a specialized context to construct
 * modules in a very specialized way, that should escape from the defaul modules
 * API. The client can not define modules configuration in modules manager
 * because that would affect the whole modules of the application. In that
 * situation the client is requesting this specialized context which some
 * context provider will provide it, and will use this context either to define
 * some properties that should be injected in modules or either to load the
 * modules he needs. If for a requested module the context doesn't have a
 * special meaning than the context will require the parent context (which
 * normally should be the default context) to do the work (loading, injecting,
 * etc). That should be the case if the context can not find for example a
 * module loader for a given module type, it will request it from parent
 * context.
 * 
 * @author Elvis Ligu
 */
public abstract class AbstractModuleContext implements ModuleContext {

   /**
    * The source of config properties.
    */
   private final DomainPropertyHandler propertyHandler;

   /**
    * The property locator to look for the properties of this context.
    */
   private final ModulePropertyLocator locator;

   /**
    * The parent context to use if a requested property is not found using this
    * context.
    */
   private final ModuleContext parent;

   /**
    * Create this instance specifying a property source.
    * 
    * @param propertyHandler
    *           the source of properties to read and write to modules
    *           properties.
    * @param locator
    *           to look for different modules properties. If null a default
    *           locator will be used.
    * @param parent
    *           the parent context to look for config properties if they are not
    *           found in this context. This can be null.
    */
   public AbstractModuleContext(DomainPropertyHandler propertyHandler,
         ModulePropertyLocator locator, ModuleContext parent) {
      ArgsCheck.notNull("propertyHandler", propertyHandler);
      this.propertyHandler = propertyHandler;
      if (locator == null) {
         locator = new DefaultModulePropertyLocator();
      }
      this.locator = locator;
      this.parent = parent;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public ModuleLoader getLoader(Class<?> type) {
      Map<String, Map<String, Object>> properties = ModuleUtils
            .resolveModuleConfig(type);
      ModuleLoader loader;
      // If there is a parent context we should get the loader using
      // get that will ensure that we query the parent if the loader is
      // not found at it default place. If the parent is null that means
      // we should use the resolve so even if a loader is not found it
      // will construct it.
      if (parent != null) {
         loader = locator.getLoader(type, propertyHandler, properties);
         if (loader == null) {
            loader = parent.getLoader(type);
         }
         // At this point the loader can not be found
         // so we must resolve it by providing a default loader
         // using the locator resolve method
         if (loader == null) {
            loader = locator.resolveLoader(type, propertyHandler, properties);
         }
      } else {
         loader = locator.resolveLoader(type, propertyHandler, properties);
      }
      return loader;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Class<? extends ModuleLoader> getLoaderClass(Class<?> type) {
      Map<String, Map<String, Object>> properties = ModuleUtils
            .resolveModuleConfig(type);
      Class<? extends ModuleLoader> clazz = locator.getLoaderClassFor(type,
            propertyHandler, properties);
      if (clazz == null && parent != null) {
         clazz = parent.getLoaderClass(type);
      }
      return clazz;
   }

   /**
    * This should return the property name for the configuration property,
    * depending on the context.
    * <p>
    * If this is a default context which applies to all modules then all
    * properties will not have the qualified name of the type as a prefix,
    * otherwise they will.
    * 
    * @param type
    *           the type to get the property name
    * @param name
    *           the config property name, must not be null.
    * @return the proper config property name for the given property.
    */
   protected abstract String getPropertyNameForConfig(String name, Class<?> type);

   /**
    * {@inheritDoc}
    */
   @Override
   public void setLoader(ModuleLoader loader, Class<?> type) {

      String property = getPropertyNameForConfig(
            ModuleConstants.LOADER_PROPERTY, type);
      String domain = ModuleConstants.DEFAULT_MODULE_CONFIG_DOMAIN;
      propertyHandler.setProperty(domain, property, loader);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void setLoaderClass(String loaderClass, Class<?> type) {
      String property = getPropertyNameForConfig(
            ModuleConstants.LOADER_CLASS_PROPERTY, type);
      String domain = ModuleConstants.DEFAULT_MODULE_CONFIG_DOMAIN;
      propertyHandler.setProperty(domain, property, loaderClass);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void setLoaderClass(Class<? extends ModuleLoader> loaderClass,
         Class<?> type) {
      String property = getPropertyNameForConfig(
            ModuleConstants.LOADER_CLASS_PROPERTY, type);
      String domain = ModuleConstants.DEFAULT_MODULE_CONFIG_DOMAIN;
      propertyHandler.setProperty(domain, property, loaderClass);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Object getProvider(Class<?> type) {
      Map<String, Map<String, Object>> properties = ModuleUtils
            .resolveModuleConfig(type);
      Object provider = locator.getModuleProvider(type, Object.class,
            propertyHandler, properties);
      if (provider == null && parent != null) {
         provider = parent.getProvider(type);
      }
      return provider;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Class<?> getProviderClass(Class<?> type) {
      Map<String, Map<String, Object>> properties = ModuleUtils
            .resolveModuleConfig(type);
      Class<?> provider = locator.getModuleProviderClassFor(type,
            propertyHandler, properties);
      if (provider == null && parent != null) {
         provider = parent.getProviderClass(type);
      }
      return provider;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void setProvider(Object provider, Class<?> type) {
      ArgsCheck.notNull("type", type);
      String property = ModuleConstants.getProviderNameFor(type);
      String domain = ModuleConstants.DEFAULT_MODULE_CONFIG_DOMAIN;
      propertyHandler.setProperty(domain, property, provider);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void setProviderClass(String providerClass, Class<?> type) {
      ArgsCheck.notNull("type", type);
      String property = ModuleConstants.getProviderClassNameFor(type);
      String domain = ModuleConstants.DEFAULT_MODULE_CONFIG_DOMAIN;
      propertyHandler.setProperty(domain, property, providerClass);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void setProviderClass(Class<?> providerClass, Class<?> type) {
      ArgsCheck.notNull("type", type);
      String property = ModuleConstants.getProviderClassNameFor(type);
      String domain = ModuleConstants.DEFAULT_MODULE_CONFIG_DOMAIN;
      propertyHandler.setProperty(domain, property, providerClass);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public ParameterProvider getParameterProvider(Class<?> type) {
      Map<String, Map<String, Object>> properties = ModuleUtils
            .resolveModuleConfig(type);
      ParameterProvider provider;

      // If there is a parent context we should get the provider using
      // 'get' that will ensure that we query the parent if the loader is
      // not found at it default place. If the parent is null that means
      // we should use the resolve so even if a provider is not found it
      // will construct it.
      if (parent != null) {
         provider = locator.getParameterProvider(type, propertyHandler,
               properties);
         if (provider == null) {
            provider = parent.getParameterProvider(type);
         }
         // At this point we must resolve the parameter provider because
         // it is not found in the default places so by calling this method
         // we ensure that a default parameter provider is returned
         if (provider == null) {
            provider = locator.resolveParameterProvider(type, propertyHandler,
                  properties);
         }
      } else {
         provider = locator.resolveParameterProvider(type, propertyHandler,
               properties);
      }
      return provider;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Class<? extends ParameterProvider> getParameterProviderClass(
         Class<?> type) {
      Map<String, Map<String, Object>> properties = ModuleUtils
            .resolveModuleConfig(type);
      Class<? extends ParameterProvider> provider = locator
            .getParameterProviderClassFor(type, propertyHandler, properties);
      if (provider == null && parent != null) {
         provider = parent.getParameterProviderClass(type);
      }
      return provider;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void setParameterProvider(ParameterProvider provider, Class<?> type) {
      String property = getPropertyNameForConfig(
            ModuleConstants.PARAMETER_PROVIDER_PROPERTY, type);
      String domain = ModuleConstants.DEFAULT_MODULE_CONFIG_DOMAIN;
      propertyHandler.setProperty(domain, property, provider);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void setParameterProviderClass(String providerClass, Class<?> type) {
      String property = getPropertyNameForConfig(
            ModuleConstants.PARAMETER_PROVIDER_CLASS_PROPERTY, type);
      String domain = ModuleConstants.DEFAULT_MODULE_CONFIG_DOMAIN;
      propertyHandler.setProperty(domain, property, providerClass);

   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void setParamterProviderClass(
         Class<? extends ParameterProvider> providerClass, Class<?> type) {
      String property = getPropertyNameForConfig(
            ModuleConstants.PARAMETER_PROVIDER_CLASS_PROPERTY, type);
      String domain = ModuleConstants.DEFAULT_MODULE_CONFIG_DOMAIN;
      propertyHandler.setProperty(domain, property, providerClass);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public PropertyInjector getPropertyInjector(Class<?> type) {
      Map<String, Map<String, Object>> properties = ModuleUtils
            .resolveModuleConfig(type);
      PropertyInjector provider;
      if (parent != null) {
         provider = locator.getPropertyInjector(type, propertyHandler,
               properties);
         if (provider == null) {
            provider = parent.getPropertyInjector(type);
         }
         if (provider == null) {
            provider = locator.resolvePropertyInjector(type, propertyHandler,
                  properties);
         }
      } else {
         provider = locator.resolvePropertyInjector(type, propertyHandler,
               properties);
      }
      return provider;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Class<? extends PropertyInjector> getPropertyInjectorClass(
         Class<?> type) {
      Map<String, Map<String, Object>> properties = ModuleUtils
            .resolveModuleConfig(type);
      Class<? extends PropertyInjector> provider = locator
            .getPropertyInjectorClassFor(type, propertyHandler, properties);
      if (provider == null && parent != null) {
         provider = parent.getPropertyInjectorClass(type);
      }
      return provider;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void setPropertyInjector(PropertyInjector provider, Class<?> type) {
      String property = getPropertyNameForConfig(
            ModuleConstants.PROPERTY_INJECTOR_PROPERTY, type);
      String domain = ModuleConstants.DEFAULT_MODULE_CONFIG_DOMAIN;
      propertyHandler.setProperty(domain, property, provider);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void setPropertyInjectorClass(String injectorClass, Class<?> type) {
      String property = getPropertyNameForConfig(
            ModuleConstants.PROPERTY_INJECTOR_CLASS_PROPERTY, type);
      String domain = ModuleConstants.DEFAULT_MODULE_CONFIG_DOMAIN;
      propertyHandler.setProperty(domain, property, injectorClass);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void setPropertyInjectorClass(
         Class<? extends ParameterProvider> injectorClass, Class<?> type) {
      String property = getPropertyNameForConfig(
            ModuleConstants.PROPERTY_INJECTOR_CLASS_PROPERTY, type);
      String domain = ModuleConstants.DEFAULT_MODULE_CONFIG_DOMAIN;
      propertyHandler.setProperty(domain, property, injectorClass);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public <T> T load(Class<T> type) {
      ArgsCheck.notNull("type", type);
      ModuleLoader loader = getLoader(type);
      ArgsCheck.notNull("module loader", loader);
      return loader.load(type, locator);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void inject(Object bean) {
      ArgsCheck.notNull("instance", bean);
      PropertyInjector injector = getPropertyInjector(bean.getClass());
      ArgsCheck.notNull("property injector", injector);
      injector.injectProperties(bean, locator);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public <T> T getParameter(Class<T> parameterType, Annotation[] annotations,
         Map<String, Map<String, Object>> properties) {
      ArgsCheck.notNull("parameterType", parameterType);
      ParameterProvider provider = getParameterProvider(parameterType);
      if (properties == null) {
         properties = ModuleUtils.resolveModuleConfig(parameterType);
      }
      return provider.getParameter(parameterType, annotations, properties,
            locator);
   }
}
