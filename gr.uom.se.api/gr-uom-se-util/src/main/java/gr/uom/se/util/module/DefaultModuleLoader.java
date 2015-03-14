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
 * {@link ParameterProvider#getParameter(Class, Annotation[], Map)}</li>
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
    * The parameter provider is used to provide the parameters when a call to a
    * method or constructor is required.
    * <p>
    */
   private ParameterProvider parameterProvider;

   @ProvideModule
   public DefaultModuleLoader(
         @Property(domain = ModuleConstants.DEFAULT_MODULE_CONFIG_DOMAIN, name = ModuleConstants.CONFIG_MANAGER_PROPERTY) ConfigManager config,
         @Property(domain = ModuleConstants.DEFAULT_MODULE_CONFIG_DOMAIN, name = ModuleConstants.PARAMETER_PROVIDER_PROPERTY) ParameterProvider provider) {
      this.config = config;
      this.parameterProvider = provider;
   }

   @Override
   public <T> T load(Class<T> clazz) {
      return load(clazz, (Class<?>) null);
   }

   /**
    * Given bean type and a loader class create an instance of bean using
    * loader.
    * <p>
    */
   @Override
   public <T> T load(Class<T> clazz, Class<?> provider) {
      // Make the necessary checks
      ArgsCheck.notNull("clazz", clazz);

      // Try to load with the specified loader
      if (provider != null && !provider.equals(NULLVal.class)) {
         return loadModule(clazz, provider, null);
      }

      // Fallback to default strategy
      // 1 - Case where there is not a default loader
      // 2 - Case where there is a default loader

      // 1 - Case when there is no @Module annotation
      // or a loader specified
      // The defaults will be used to load the module
      return load(clazz, (Map<String, Map<String, Object>>) null);
   }

   public <T> T load(Class<T> clazz, Map<String, Map<String, Object>> properties) {
      ArgsCheck.notNull("clazz", clazz);
      Class<?> provider = ModuleUtils.getProviderClassFor(clazz, config,
            properties);
      if (provider == null) {
         return loadNoLoader(clazz, properties);
      }

      // 2 - Case when a @Module annotation is present and a default loader
      // is specified
      return loadModule(clazz, provider, properties);
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
         Map<String, Map<String, Object>> properties) {
      // To load a module with @Module annotation
      // 1- There is a specified loader, so this loader should be used to
      // load the module
      // 2 - There is not a specified loader
      // a) The class has a constructor with the @ProvideModule instance
      // b) The class has a default constructor which will be used to load it

      Method method = getInstanceLoaderMethod(moduleType, provider);
      Object providerInstance = null;

      if (method != null) {
         if (properties == null) {
            properties = ModuleUtils.resolveModuleConfig(moduleType);
         }
         providerInstance = resolveModuleProvider(moduleType, provider,
               properties);
      } else {
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
            if (properties == null) {
               properties = ModuleUtils.resolveModuleConfig(provider);
            }
            return (T) resolveLoader(provider, properties).load(provider);
         }

         throw new IllegalArgumentException(
               "the specified provider: "
                     + provider
                     + " doesn't have a method annotated with @ProvideModule with a return type of: "
                     + moduleType);
      }
      // Try to execute the method with annotation
      // @ProvideModule
      if (properties == null) {
         properties = ModuleUtils.resolveModuleConfig(moduleType);
      }

      return executor.execute(providerInstance, provider, method, properties);
   }

   /**
    * Resolve the provider for the given type.
    * <p>
    * Will look first at the default configuration domains if there is a
    * provider instance already there, using
    * {@link ModuleUtils#getModuleProvider(Class, Class, ConfigManager, Map)} .
    * If the provider was not found it will retrieve it using a parameter
    * provider. If it was not found, then the provider will be considered a
    * module and will be loaded by this loader.
    * 
    * @param type
    * @param provider
    * @param properties
    * @return
    */
   protected <T> T resolveModuleProvider(Class<?> type, Class<T> provider,
         Map<String, Map<String, Object>> properties) {

      T pInstance = ModuleUtils.getModuleProvider(type, provider, config,
            properties);
      // If the provider was not found then we should create
      // the provider by using the parameter provider
      if (pInstance == null) {
         // It will create it first by looking for a loader (like this one)
         // if a loader was not found then it will create a loader
         // and load the provider
         ModuleLoader providerLoader = resolveLoader(provider, properties);
         // Load the instance
         pInstance = providerLoader.load(provider);
      }
      return pInstance;
   }

   /**
    * This will resolve a loader for the given type.
    * <p>
    * It is different to
    * {@link ModuleUtils#resolveLoader(Class, ConfigManager, Map)} in that it
    * will not create a an instance of this type each time a loader is not
    * found, but it will return this instance. We assume that each instance of
    * this loader perform the same in any circumstance.
    * 
    * @param type
    * @param properties
    * @return
    */
   protected ModuleLoader resolveLoader(Class<?> type,
         Map<String, Map<String, Object>> properties) {

      ModuleLoader loader = ModuleUtils.getLoader(type, config, properties);
      // If no loader was found then try to find a loader class
      // for the given type
      if (loader == null) {
         Class<? extends ModuleLoader> loaderClass = ModuleUtils
               .getLoaderClassFor(type, config, properties);
         // No loader class was found for this type
         // we should provide the default loader
         if (loaderClass.equals(DefaultModuleLoader.class)) {
            loader = this;
         } else {
            // The loader class for the given type is
            // not the default loader so we must load
            // this custom loader by resolving a loader for
            // it
            loader = resolveLoader(loaderClass,
                  ModuleUtils.resolveModuleConfig(loaderClass)).load(
                  loaderClass);
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

   private <T> T loadNoLoader(Class<T> moduleType,
         Map<String, Map<String, Object>> properties) {
      // To load a module without a @Module annotations
      // 1) There is a constructor with an annotation @ProvideModule
      // 2) There is no constructor with such annotation but there is
      // a default constructor

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

      // Try to execute the constructor with annotation
      // @ProvideModule
      if (properties == null) {
         properties = ModuleUtils.resolveModuleConfig(moduleType);
      }
      return executor.execute(moduleType, constructor, properties);
   }

   /**
    * Resolve a parameter provider for the given type.
    * <p>
    * The parameter will be searched at the default domains using
    * {@link ModuleUtils#getParameterProvider(Class, ConfigManager, Map)} if it
    * was not found there, it will be created using a default parameter
    * provider.
    * 
    * @param type
    * @param properties
    * @return
    */
   protected ParameterProvider resolveParameterProvider(Class<?> type,
         Map<String, Map<String, Object>> properties) {
      ParameterProvider provider = ModuleUtils.getParameterProvider(type,
            config, properties);
      // If no provider was found then create a default provider
      if (provider == null) {
         Class<? extends ParameterProvider> pClass = ModuleUtils
               .getParameterProviderClassFor(type, config, properties);
         // If there is the default parameter privder, but it is not
         // resolved then try to resolve it
         if (pClass.equals(DefaultParameterProvider.class)) {
            // Try to load the cached default parameter provider instance
            if (this.parameterProvider == null) {
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
                  ModuleUtils.resolveModuleConfig(pClass)).load(pClass);
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
   private static class InstanceMethodLoaderFilter implements Filter<Method> {

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
   static class StaticMethodLoaderFilter implements Filter<Method> {

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
   static class ConstructorLoaderFilter implements Filter<Constructor<?>> {

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
   private class Executor extends AbstractMethodConstructorExecutor {

      @Override
      protected ParameterProvider resolveParameterProvider(Class<?> type,
            Map<String, Map<String, Object>> properties) {
         return DefaultModuleLoader.this.resolveParameterProvider(type,
               properties);
      }
   }

   /**
    * A method and constructor executor.
    * <p>
    */
   private Executor executor = new Executor();
}