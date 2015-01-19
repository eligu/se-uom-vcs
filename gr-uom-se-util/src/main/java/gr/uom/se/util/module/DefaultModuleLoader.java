package gr.uom.se.util.module;

import gr.uom.se.filter.Filter;
import gr.uom.se.filter.FilterUtils;
import gr.uom.se.util.manager.ConfigManager;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * The default implementation of {@link ModuleLoader}.
 * <p>
 * The strategy of this loader is based on annotations, thus it is class
 * agnostic. It provides a fall-back mechanism that allows the creation of
 * instances even when the required annotation are absent. The module algorithm
 * is as follows:
 * <ol>
 * <li>{@link #load(Class)} call with a type.</li>
 * <li>If a {@link Module} annotation is present read the properties it contains
 * and creates configuration domains.</li>
 * <li>If a loader type is specified then:</li>
 * <ol>
 * <li>Look for an instance method {@link ProvideModule} annotation.</li>
 * <li>If an instance method was found then go to step 1, to load the loader.</li>
 * <li>If not, then find a static method with {@link ProvideModule} annotation.</li>
 * </ol>
 * <li>If no loader is provided then find a constructor of the module with
 * annotation {@link ProvideModule}.</li>
 * <li>If no annotated constructor is found then look for the default
 * constructor to create the new instance.</li>
 * </ol>
 * 
 * After locating the method to load the module (an annotated method or a
 * constructor) then the strategy of invoking it to load the module is as
 * follows (except in the case of default constructor):
 * <ol>
 * <li>If the target (method or constructor) has no parameters then invoke it.</li>
 * <li>If the target has parameters then if the parameter is annotated with
 * {@link Property} try to get its value using the provided
 * {@link ConfigManager} (use annotation info for domain and property name), if
 * no value is contained in config manager, then use the default properties that
 * are specified in {@link Module} annotation if it is present. If a property
 * can not be loaded even after looking at default properties then try to load
 * it from the annotation itself by using {@code stringval} value. The string
 * value will be converted to a Java value by using json-smart library.
 * Primitive types are supported unless they are multidimensional arrays.</li>
 * <li>If a parameter has no annotation it will be considered a module and it
 * will be loaded using this loader (start from step 1).</li>
 * </ol>
 * 
 * NOTE: this loader makes recursive calls to load any object that it doesn't
 * recognize (the module loader is considered a module if it should be loaded),
 * thus the caller should ensure this doesn't lead to endless recursive loops.
 * 
 * @author Elvis Ligu
 */
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

   public DefaultModuleLoader(ConfigManager config, ParameterProvider provider) {
      this.config = config;
      ArgsCheck.notNull("provider", provider);
      this.parameterProvider = provider;
   }

   @Override
   public <T> T load(Class<T> clazz) {
      return load(clazz, null);
   }

   /**
    * Given bean type and a loader class create an instance of bean using
    * loader.
    * <p>
    */
   @Override
   public <T> T load(Class<T> clazz, Class<?> loader) {
      // Make the necessary checks
      ArgsCheck.notNull("clazz", clazz);

      // Try to load with the specified loader
      if (loader != null && !loader.equals(NULLVal.class)) {
         return loadModule(clazz, loader);
      }

      // Fallback to default strategy
      // 1 - Case where there is not a default loader
      // 2 - Case where there is a default loader

      // Check for annotations
      Module moduleAnnotation = clazz.getAnnotation(Module.class);

      // 1 - Case when there is no @Module annotation
      // or a loader specified
      // The defaults will be used to load the module
      if (moduleAnnotation == null
            || moduleAnnotation.provider().equals(NULLVal.class)) {
         return loadNoLoader(clazz);
      }

      // 2 - Case when a @Module annotation is present and a default loader
      // is specified
      return loadModule(clazz, moduleAnnotation.provider());
   }

   /**
    * Load an instance of the {@code moduleType} using an instance of
    * {@code loader}.
    * <p>
    * 
    * @param moduleType
    *           the type of the new instance to be loaded
    * @param loader
    *           the type of the loader
    * @return a new instance of T
    */
   @SuppressWarnings("unchecked")
   private <T> T loadModule(Class<T> moduleType, Class<?> loader) {
      // To load a module with @Module annotation
      // 1- There is a specified loader, so this loader should be used to
      // load the module
      // 2 - There is not a specified loader
      // a) The class has a constructor with the @ProvideModule instance
      // b) The class has a default constructor which will be used to load it

      Method method = getInstanceLoaderMethod(moduleType, loader);
      Object providerInstance = null;
      if (method != null) {
         providerInstance = getProvider(moduleType, loader);
      } else {
         method = getStaticLoaderMethod(moduleType, loader);
      }

      if (method == null) {
         throw new IllegalArgumentException(
               "the specified loader: "
                     + loader
                     + " doesn't have a method annotated with @ProvideModule with a return type of: "
                     + moduleType);
      }
      // Try to execute the method with annotation
      // @ProvideModule
      Map<String, Map<String, Object>> properties = getModuleConfig(moduleType
            .getAnnotation(Module.class));
      Class<?>[] parameterTypes = method.getParameterTypes();
      Annotation[][] annotations = method.getParameterAnnotations();
      Object[] args = getParameters(parameterTypes, annotations, properties);
      try {
         return (T) method.invoke(providerInstance, args);
      } catch (IllegalAccessException | IllegalArgumentException
            | InvocationTargetException ex) {
         Arrays.toString(args);
         throw new IllegalArgumentException(ex);
      }
   }

   private <T> T getProvider(Class<?> type, Class<T> provider) {
      // Given a type we need a provider
      // 1 - Try to find the provider under type's default config domain
      String domain = ModuleConstants.getDefaultConfigFor(type);
      String name = ModuleConstants.getProviderNameFor(type);
      T pInstance = DefaultParameterProvider.getCompatibleProperty(domain,
            name, provider, null, config);

      // If the provider was not found then try to find it under
      // module's default domain
      if (pInstance == null) {
         domain = ModuleConstants.DEFAULT_MODULE_CONFIG_DOMAIN;
         pInstance = DefaultParameterProvider.getCompatibleProperty(domain,
               name, provider, null, config);
      }

      // If the provider was not found then we should create
      // the provider by using the parameter provider
      if (pInstance == null) {
         // It will create it first by looking for a loader (like this one)
         // if a loader was not found then it will create a loader
         // and load the provider
         pInstance = parameterProvider.getParameter(provider, null, null);
         if (pInstance == null) {
            pInstance = load(provider);
         }
      }
      return pInstance;
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
      for (Method m : methods) {
         if (m.getReturnType().equals(returnType)) {
            return m;
         }
      }
      return null;
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
   private Method getInstanceLoaderMethod(Class<?> returnType, Class<?> loader) {
      Set<Method> methods = ReflectionUtils.getAccessibleMethods(loader,
            getClass(), instanceMethodLoaderFilter);
      for (Method m : methods) {
         if (m.getReturnType().equals(returnType)) {
            return m;
         }
      }
      return null;
   }

   /**
    * Create a configuration from a {@link Module} annotation.
    * <p>
    * The key of the map is the domain, and the value is a map with key value
    * pairs of properties. Values are the default stringVal defined at
    * {@link Property} annotation.
    * 
    * @param module
    *           the annotation with properties
    * @return a configuration based on module properties
    */
   static Map<String, Map<String, Object>> getModuleConfig(Module module) {
      Map<String, Map<String, Object>> properties = new HashMap<>();
      if (module != null) {
         for (Property p : module.properties()) {
            Map<String, Object> domain = properties.get(p.domain());
            if (domain == null) {
               domain = new HashMap<>();
               properties.put(p.domain(), domain);
            }
            domain.put(p.name(), p.stringVal());
         }
      }

      return properties;
   }

   private <T> T loadNoLoader(Class<T> moduleType) {
      // To load a module without a @Module annotations
      // 1) There is a constructor with an annotation @ProvideModule
      // 2) There is no constructor with such annotation but there is
      // a default constructor

      // Check for annotations
      Module module = moduleType.getAnnotation(Module.class);

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
               "no annotated (@ProvideModule) or default constructor found");
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
      Map<String, Map<String, Object>> properties = getModuleConfig(module);
      Class<?>[] parameterTypes = constructor.getParameterTypes();
      Annotation[][] annotations = constructor.getParameterAnnotations();
      Object[] args = getParameters(parameterTypes, annotations, properties);
      try {
         return constructor.newInstance(args);
      } catch (InstantiationException | IllegalAccessException
            | IllegalArgumentException | InvocationTargetException ex) {
         throw new IllegalArgumentException(ex);
      }
   }

   /**
    * Given parameter types, their annotations and a configuration from a module
    * annotation get the values of parameters of the loader method.
    * <p>
    * If a parameter is annotated with a {@link Property} annotation, then its
    * value will be first looked at configuration manager, if it was not found
    * there then it will be looked at module configuration, if it is not found
    * there, it will load it from its stringval property. If the parameter is
    * not annotated it will be considered a module and will try to load it with
    * {@link #load(Class)} method.
    * 
    * @param parameterTypes
    *           the types of method parameters
    * @param annotations
    *           annotations of parameters
    * @param properties
    *           the default config created from @Module annotation
    * @return parameter values
    */
   protected Object[] getParameters(Class<?>[] parameterTypes,
         Annotation[][] annotations, Map<String, Map<String, Object>> properties) {

      Object[] parameterValues = new Object[parameterTypes.length];
      for (int i = 0; i < parameterTypes.length; i++) {
         parameterValues[i] = parameterProvider.getParameter(parameterTypes[i],
               annotations[i], properties);
      }
      return parameterValues;
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

   private static final InstanceMethodLoaderFilter instanceMethodLoaderFilter = new InstanceMethodLoaderFilter();

   /**
    * Used to find an annotated with @ProvideModule static method.
    * <p>
    * 
    * @author Elvis
    */
   private static class StaticMethodLoaderFilter implements Filter<Method> {

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
   private static class ConstructorLoaderFilter implements
         Filter<Constructor<?>> {

      @Override
      public boolean accept(Constructor<?> t) {
         return t.getAnnotation(ProvideModule.class) != null;
      }
   }

   private static final ConstructorLoaderFilter constructorLoaderFilter = new ConstructorLoaderFilter();
}