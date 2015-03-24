package gr.uom.se.util.module;

import gr.uom.se.util.config.ConfigManager;
import gr.uom.se.util.filter.Filter;
import gr.uom.se.util.filter.FilterUtils;
import gr.uom.se.util.module.annotations.Module;
import gr.uom.se.util.module.annotations.NULLVal;
import gr.uom.se.util.module.annotations.Property;
import gr.uom.se.util.module.annotations.ProvideModule;
import gr.uom.se.util.reflect.AccessibleMemberFilter;
import gr.uom.se.util.reflect.ReflectionUtils;
import gr.uom.se.util.validation.ArgsCheck;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Set;

/**
 * The default implementation of {@link ModuleLoader}.
 * <p>
 * The strategy of this loader is based on annotations, thus it is class
 * agnostic. It provides a fall-back mechanism that allows the creation of
 * instances even when the required annotations are absent. The module algorithm
 * is as follows:
 * <ol>
 * <li>{@link #load(Class)} call with a type.</li>
 * <li>If a {@link Module} annotation is present read the properties it contains
 * and creates configuration domains.</li>
 * <li>If a provider type is specified then:</li>
 * <ol>
 * <li>Look for an instance method {@link ProvideModule} annotation.</li>
 * <li>If an instance method was found then proceed with loading of provider as
 * follows:</li>
 * <ol>
 * <li>Look up the provider at a config domain
 * {@link ModuleConstants#getDefaultConfigFor(Class)} where the Class is the
 * module that the provider should return, with a property name of
 * {@link ModuleConstants#getProviderNameFor(Class)}.</li>
 * <li>If the provider was not found there then look up at config domain
 * {@link ModuleConstants#DEFAULT_MODULE_CONFIG_DOMAIN} which is the default
 * module configuration domain.</li>
 * <li>If the provider is not found at these default places within configuration
 * manager then try to load the provider using an instance of
 * {@link ParameterProvider#getParameter(Class, Annotation[], Map, ModulePropertyLocator)}
 * </li>
 * <li>If the provider can not be loaded it will cause a null pointer exception.
 * </li>
 * </ol>
 * <li>If not, then find a static method with {@link ProvideModule} annotation.</li>
 * <li>If the method that provides the module is found execute it (if an
 * instance method, then use the provider instance at step 3.2). The parameters
 * of provider method will be retrieved using an instance of
 * {@link ParameterProvider}.</li>
 * </ol>
 * <li>If no loader is provided then find a constructor of the module with
 * annotation {@link ProvideModule}. If found then execute it just like in step
 * 3.4.</li>
 * <li>If no annotated constructor is found then look for the default
 * constructor to create the new instance, and execute it.</li>
 * </ol>
 * 
 * After locating the method that provides the module (an annotated method or a
 * constructor) then the strategy of invoking it to get the module is as
 * follows:
 * <ol>
 * <li>If the target (method or constructor) has no parameters then invoke it.</li>
 * <li>If the target has parameters then use an instance of
 * {@link ParameterProvider} to get the value for each parameter.</li>
 * </ol>
 * 
 * NOTE 1: this loader may fall into recursive calls in order to load any
 * module. This is the case when a module defines a specific provider and that
 * provider defines the module itself as its provider. Especially when both of
 * them must be created.
 * <p>
 * NOTE 2: the strategy of injecting the values at parameters of a provider
 * method (or constructor) is left to an instance of {@link ParameterProvider}
 * which is specified when constructing this loader. A known implementation of
 * it is {@link DefaultParameterProvider}.
 * 
 * @author Elvis Ligu
 * @see Module
 * @see DefaultParameterProvider
 */
@Property(domain = ModuleConstants.DEFAULT_MODULE_CONFIG_DOMAIN, name = ModuleConstants.LOADER_PROPERTY)
public class DefaultModuleLoader implements ModuleLoader {

   /**
    * The global config manager used to load values of properties, when creating
    * modules.
    * <p>
    * Values of properties are required at method arguments when a method is
    * annotated using @ProvideModule.
    */
   private ConfigManager config;

   /**
    * The locator strategy to locate module's properties. This will first locate
    * properties from a configuration manager and then from a properties map.
    */
   private final ModulePropertyLocator locator;

   /**
    * The parameter provider is used to provide the parameters when a call to a
    * method or constructor is required.
    * <p>
    */
   private volatile ParameterProvider parameterProvider;

   @ProvideModule
   public DefaultModuleLoader(
         @Property(domain = ModuleConstants.DEFAULT_MODULE_CONFIG_DOMAIN, name = ModuleConstants.CONFIG_MANAGER_PROPERTY) ConfigManager config,
         @Property(domain = ModuleConstants.DEFAULT_MODULE_CONFIG_DOMAIN, name = ModuleConstants.PARAMETER_PROVIDER_PROPERTY) ParameterProvider provider) {
      this.config = config;
      this.parameterProvider = provider;
      this.locator = new DefaultModulePropertyLocator();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public <T> T load(Class<T> clazz) {
      return load(clazz, locator);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public <T> T load(Class<T> clazz, Class<?> provider) {
      // Make the necessary checks
      ArgsCheck.notNull("clazz", clazz);

      // Try to load with the specified provider
      // If provider is not null or it is not a type of
      // NULLVal that means we have a provider
      if (provider != null && !provider.equals(NULLVal.class)) {
         // Resolve the properties of this module (clazz). These are the
         // static properties that are described by @Module annotation
         Map<String, Map<String, Object>> properties = ModuleUtils
               .resolveModuleConfig(clazz);
         return loadModule(clazz, provider, properties, locator);
      }
      return load(clazz, locator);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public <T> T load(Class<T> clazz, Map<String, Map<String, Object>> properties) {
      ArgsCheck.notNull("clazz", clazz);
      // Because this will be a dynamic properties resolving strategy we should
      // create a dynamic locator that will look into the provided properties
      ModulePropertyLocator thisLocator;
      // If there are provided properties then just set this
      // module to be loaded with dynamic properties
      if (properties != null) {
         thisLocator = new DynamicModulePropertyLocator(properties);
      } else {
         // Use default locating strategy if there are not dynamic properties
         thisLocator = locator;
      }
      return load(clazz, thisLocator);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public <T> T load(Class<T> clazz, ModulePropertyLocator propertyLocator) {
      ArgsCheck.notNull("clazz", clazz);
      // If locator is null we should use the default locating strategy
      if (propertyLocator == null) {
         propertyLocator = locator;
      }
      // Load the static properties described at @Module annotation for
      // the given module
      Map<String, Map<String, Object>> properties = ModuleUtils
            .resolveModuleConfig(clazz);
      Class<?> provider = propertyLocator.getModuleProviderClassFor(clazz,
            config, properties);
      // If there is not a provider then load without a provider
      if (provider == null) {
         return loadNoLoader(clazz, properties, propertyLocator);
      }

      // Load with provider
      return loadModule(clazz, provider, properties, propertyLocator);
   }

   /**
    * Load an instance of the {@code moduleType} using an instance of
    * {@code provider}.
    * <p>
    * 
    * @param moduleType
    *           the type of the new instance to be loaded
    * @param provider
    *           the type of the provider
    * @return a new instance of T
    */
   @SuppressWarnings("unchecked")
   private <T> T loadModule(Class<T> moduleType, Class<?> provider,
         Map<String, Map<String, Object>> properties,
         ModulePropertyLocator propertyLocator) {
      // Try to get an instance method first.
      // We prioritize instance methods because they can provide
      // a better loading in general (they can be overrided by subtypes,
      // which allows the loading to be dynamic).
      Method method = getInstanceLoaderMethod(moduleType, provider);
      Object providerInstance = null;

      // If method is found then the provider will be considered a module
      // so try to resolve the provider (load it, or get it from properties).
      if (method != null) {
         providerInstance = resolveModuleProvider(moduleType, provider,
               properties, propertyLocator);
      } else {
         // The provider doesn't have an instance method so we should
         // try to find a static method
         method = getStaticLoaderMethod(moduleType, provider);
      }

      // If the provider doesn't have a provider method but is a concrete
      // subtype of the
      // given moduleType then we should load the provider and return the
      // provider instead.
      // That should be the case when an interface is registering as a provider
      // its implementation
      if (method == null) {
         // The case when the moduleType is a super type of the provider
         // so we can safely create an instance of the provider itself
         if (moduleType.isAssignableFrom(provider)) {
            // Resolve a loader (from configurations, or create one), and load
            // the provider
            return (T) resolveLoader(provider, properties, propertyLocator)
                  .load(provider, propertyLocator);
         }

         // We should throw an exception here, because the provider doesn't have
         // an
         // instance method not or a static method, also he is not a subtype of
         // the module
         // so we can load it.
         throw new IllegalArgumentException(
               "the specified provider: "
                     + provider
                     + " doesn't have a method annotated with @ProvideModule with a return type of: "
                     + moduleType);
      }

      // Execute the provider method and load the module
      return executor.execute(providerInstance, provider, method, properties,
            propertyLocator);
   }

   /**
    * If module doesn't have a provider, it should be loaded using its
    * constructors, that means that the module must have a constructor annotated
    * with @ProvideModule (a.k.a. the provider constructor), or a default
    * constructor (no parameters).
    * 
    * @param moduleType
    * @param properties
    * @param propertyLocator
    * @return
    */
   private <T> T loadNoLoader(Class<T> moduleType,
         Map<String, Map<String, Object>> properties,
         ModulePropertyLocator propertyLocator) {

      // Create filters to look up the constructors
      // A constructor must be accessible by this loader
      Filter<Constructor<?>> accessFilter = new AccessibleMemberFilter<Constructor<?>>(
            moduleType, getClass());
      Filter<Constructor<?>> filter = FilterUtils.and(constructorLoaderFilter,
            accessFilter);

      // Find any constructor with @ProvideModule annotation
      // If no such constructor then find the default one
      Set<Constructor<T>> cons = ReflectionUtils.getConstructors(moduleType,
            filter);
      Constructor<T> constructor = null;
      if (!cons.isEmpty()) {
         constructor = cons.iterator().next();
      } else {
         constructor = ReflectionUtils.getDefaultConstructor(moduleType);
      }

      // If no suitable constructor was find that means we have
      // module that can not be loaded
      if (constructor == null) {
         throw new IllegalArgumentException(
               "no annotated (@ProvideModule) or default constructor found for type "
                     + moduleType);
      }

      // Execute the default constructor if this is the default
      if (constructor.getParameterTypes().length == 0) {
         try {
            return constructor.newInstance();
         } catch (InstantiationException | IllegalAccessException
               | IllegalArgumentException | InvocationTargetException ex) {
            throw new IllegalArgumentException(ex);
         }
      }
      // Execute the provider constructor
      return executor.execute(moduleType, constructor, properties,
            propertyLocator);
   }

   /**
    * Resolve the provider for the given type.
    * <p>
    * Will look first at the default configuration domains if there is a
    * provider instance already there, using
    * {@link ModulePropertyLocator#getModuleProvider(Class, Class, ConfigManager, Map)}
    * . If the provider was not found it will retrieve it using a loader. If it
    * was not found, then the provider will be considered a module and will be
    * loaded by this loader.
    * 
    * @param type
    * @param provider
    * @param properties
    * @param propertyLocator
    *           if its different from default locator then the strategy of
    *           looking up properties will be implemented by this locator.
    * @return
    */
   protected <T> T resolveModuleProvider(Class<?> type, Class<T> provider,
         Map<String, Map<String, Object>> properties,
         ModulePropertyLocator propertyLocator) {

      // Look up a provider for the given module (type)
      T pInstance = propertyLocator.getModuleProvider(type, provider, config,
            properties);
      // If the provider was not found then we should create
      // the provider by using a loader
      if (pInstance == null) {
         // It will create it first by looking for a loader (like this one)
         // if a loader was not found then it will create a loader
         // and load the provider. We look up for a loader because we
         // allows the loaders of a class to be changed at runtime, so
         // we may have dynamic loading.
         ModuleLoader providerLoader = resolveLoader(provider, properties,
               propertyLocator);
         // Load the instance
         pInstance = providerLoader.load(provider, propertyLocator);
      }
      return pInstance;
   }

   /**
    * This will resolve a loader for the given type.
    * <p>
    * It is different to
    * {@link ModulePropertyLocator#resolveLoader(Class, ConfigManager, Map)} in
    * that it will not create a an instance of this type each time a loader is
    * not found, but it will return this instance. We assume that each instance
    * of this loader perform the same in any circumstance.
    * 
    * @param type
    * @param properties
    * @param propertyLocator
    *           if its different from default locator then the strategy of
    *           looking up properties will be implemented by this locator.
    * @return
    */
   protected ModuleLoader resolveLoader(Class<?> type,
         Map<String, Map<String, Object>> properties,
         ModulePropertyLocator propertyLocator) {

      // Try to locate the loader
      ModuleLoader loader = propertyLocator.getLoader(type, config, properties);
      // If no loader was found then try to find a loader class
      // for the given type
      if (loader == null) {
         Class<? extends ModuleLoader> loaderClass = propertyLocator
               .getLoaderClassFor(type, config, properties);
         // No loader class was found for this type
         // we should provide the default loader,
         // this will ensure that we do not create
         // each time the loader
         if (loaderClass.equals(DefaultModuleLoader.class)) {
            loader = this;
         } else {
            // The loader class for the given type is
            // not the default loader so we must load
            // this custom loader by resolving a loader for
            // it. This will make recursive calls which means
            // we may have cyclic dependencies!!!
            loader = resolveLoader(loaderClass,
                  ModuleUtils.resolveModuleConfig(loaderClass), propertyLocator)
                  .load(loaderClass, propertyLocator);
         }
      }
      return loader;
   }

   /**
    * Get the static method annotated with {@link ProvideModule}, member of
    * loader with a return type of returnType.
    * <p>
    * 
    * @param returnType
    *           of the method to return
    * @param loader
    *           which contain the method
    * @return a static loader method
    */
   protected Method getStaticLoaderMethod(Class<?> returnType, Class<?> loader) {

      Set<Method> methods = ReflectionUtils.getAccessibleMethods(loader,
            getClass(), staticMethodLoaderFilter);
      Method method = null;
      for (Method m : methods) {
         if (m.getReturnType().equals(returnType)) {
            return m;
         } else if (method == null
               && returnType.isAssignableFrom(m.getReturnType())) {
            method = m;
         }
      }
      return method;
   }

   /**
    * Get the instance method annotated with {@link ProvideModule}, member of
    * loader with a return type of returnType.
    * <p>
    * 
    * @param returnType
    *           of the method to return
    * @param loader
    *           which contain the method
    * @return an instance loader method
    */
   protected Method getInstanceLoaderMethod(Class<?> returnType, Class<?> loader) {
      Set<Method> methods = ReflectionUtils.getAccessibleMethods(loader,
            getClass(), instanceMethodLoaderFilter);
      Method method = null;
      for (Method m : methods) {
         if (m.getReturnType().equals(returnType)) {
            return m;
         } else if (method == null
               && returnType.isAssignableFrom(m.getReturnType())) {
            method = m;
         }
      }
      return method;
   }

   /**
    * Resolve a parameter provider for the given type.
    * <p>
    * The parameter will be searched at the default domains using
    * {@link ModulePropertyLocator#getParameterProvider(Class, ConfigManager, Map)}
    * if it was not found there, it will be created using a default parameter
    * provider.
    * 
    * @param type
    * @param properties
    * @return
    */
   protected ParameterProvider resolveParameterProvider(Class<?> type,
         Map<String, Map<String, Object>> properties,
         ModulePropertyLocator propertyLocator) {
      // Locate a parameter provider for the given type
      ParameterProvider provider = propertyLocator.getParameterProvider(type,
            config, properties);
      // If no provider was found then create a default provider
      if (provider == null) {
         Class<? extends ParameterProvider> pClass = propertyLocator
               .getParameterProviderClassFor(type, config, properties);
         // If there is the default parameter provider, but it is not
         // resolved then try to resolve it
         if (pClass.equals(DefaultParameterProvider.class)) {
            // Try to load the cached default parameter provider instance
            if (this.parameterProvider == null) {
               // We do not need a synchronize here because even if another
               // thread creates another provider it will be of the same type,
               // and it will have the same functionality
               this.parameterProvider = new DefaultParameterProvider(config,
                     this);
            }
            provider = this.parameterProvider;
         } else {
            // The provider class for the given type is
            // not the default provider so we must load
            // this custom provider by resolving a loader for
            // it
            provider = resolveLoader(pClass,
                  ModuleUtils.resolveModuleConfig(pClass), propertyLocator)
                  .load(pClass, propertyLocator);
         }
      }
      return provider;
   }

   /**
    * Used to find an annotated with @ProvideModule instance method.
    * <p>
    * 
    * @author Elvis
    */
   private static final class InstanceMethodLoaderFilter implements
         Filter<Method> {

      @Override
      public boolean accept(Method t) {
         return !Modifier.isStatic(t.getModifiers())
               && t.getAnnotation(ProvideModule.class) != null;
      }
   }

   static final InstanceMethodLoaderFilter instanceMethodLoaderFilter = new InstanceMethodLoaderFilter();

   /**
    * Used to find an annotated with @ProvideModule static method.
    * <p>
    * 
    * @author Elvis
    */
   static final class StaticMethodLoaderFilter implements Filter<Method> {

      @Override
      public boolean accept(Method t) {
         return Modifier.isStatic(t.getModifiers())
               && t.getAnnotation(ProvideModule.class) != null;
      }
   }

   private static final StaticMethodLoaderFilter staticMethodLoaderFilter = new StaticMethodLoaderFilter();

   /**
    * Used to find an annotated with @ProvideModule constructor.
    * <p>
    * 
    * @author Elvis
    */
   static final class ConstructorLoaderFilter implements Filter<Constructor<?>> {

      @Override
      public boolean accept(Constructor<?> t) {
         return t.getAnnotation(ProvideModule.class) != null;
      }
   }

   static final ConstructorLoaderFilter constructorLoaderFilter = new ConstructorLoaderFilter();

   /**
    * A class to execute method providers and constructors.
    * <p>
    * 
    * @author Elvis Ligu
    * @version 0.0.1
    * @since 0.0.1
    */
   private final class Executor extends AbstractMethodConstructorExecutor {

      @Override
      protected ParameterProvider resolveParameterProvider(Class<?> type,
            Map<String, Map<String, Object>> properties,
            ModulePropertyLocator propertyLocator) {
         return DefaultModuleLoader.this.resolveParameterProvider(type,
               properties, propertyLocator);
      }
   }

   /**
    * A method and constructor executor.
    * <p>
    */
   private final Executor executor = new Executor();
}