/**
 * 
 */
package gr.uom.se.util.module;

import java.lang.annotation.Annotation;
import java.util.Map;

import gr.uom.se.util.context.Context;

/**
 * @author Elvis Ligu
 */
public interface ModuleContext extends Context {

   /**
    * Get the loader for the given context's type.
    * <p>
    * 
    * @param type
    *           the type to get the loader for
    * @return a loader to load the given class
    */
   ModuleLoader getLoader(Class<?> type);

   /**
    * Set the module loader for this context.
    * <p>
    * 
    * @param type
    *           set the loader for the given type
    * @param loader
    *           define the module loader for this context.
    */
   void setLoader(ModuleLoader loader, Class<?> type);

   /**
    * Get the loader class for the given context.
    * <p>
    * 
    * @param type
    *           the type to get the loader class for.
    * @return the loader class for this context or null.
    */
   Class<? extends ModuleLoader> getLoaderClass(Class<?> type);

   /**
    * Set the loader class of this context to be the one provided by the
    * parameter.
    * <p>
    * This method will throw an exception if the provided argument is not a
    * fully qualified name of a class of a {@link ModuleLoader} subtype.
    * 
    * @param type
    *           the type to set the loader class for.
    * @param loaderClass
    *           the fully qualified name of the loader class, null is not
    *           allowed.
    */
   void setLoaderClass(String loaderClass, Class<?> type);

   /**
    * Set the loader class of this context to be the one provided by the
    * parameter.
    * <p>
    * 
    * @param type
    *           the type to set the loader class for
    * @param loaderClass
    *           the loader class, null is not allowed.
    */
   void setLoaderClass(Class<? extends ModuleLoader> loaderClass, Class<?> type);

   /**
    * Get the provider for the given module type supported by this context.
    * <p>
    * This method should return an instance of the provider for the given class.
    * Before calling this method check the type this supports. Use
    * {@link #getType()} in order to check if the given parameter type is the
    * same as the type of this context.
    * 
    * @param type
    *           the type to get the provider for.
    * @return a module provider for this context's type.
    */
   Object getProvider(Class<?> type);

   /**
    * Specify the provider object for this context.
    * <p>
    * 
    * @param type
    *           the type to specify the provider for
    * @param provider
    *           the provider of modules of this context.
    */
   void setProvider(Object provider, Class<?> type);

   /**
    * Get the provider class for this context's type.
    * <p>
    * This method should return the class of the provider for the context's
    * type. If there is not a registered provider it will return null. To load
    * this provider the caller must obtain a loader for this provider, however
    * it is generally not required to load the provider of a class as it will be
    * resolved by the loader of the class. Though, there are circumstances when
    * it is preferable to load a provider eagerly.
    * <p>
    * Note that a provider is an abstract instance for the system and it is only
    * required by the module loader. Thus it depends on the module loader
    * implementation how to use the provider to get instances of a class.
    * <p>
    * This method may be useful when it is required to check the default
    * provider class at the time of execution.
    *
    * @param type
    *           the type to get the provider class for.
    * @return a provider class for the given context's type.
    */
   Class<?> getProviderClass(Class<?> type);

   /**
    * Set the provider class of this context to be the one provided by the
    * parameter.
    * <p>
    * This method will throw an exception if the provided argument is not a
    * fully qualified name of a class.
    * 
    * @param type
    *           the type to set the provider class for.
    * @param providerClass
    *           the fully qualified name of the provider class, null is not
    *           allowed.
    */
   void setProviderClass(String providerClass, Class<?> type);

   /**
    * Set the provider class of this context to be the one provided by the
    * parameter.
    * <p>
    * 
    * @param the
    *           type to set the provider class for.
    * @param providerClass
    *           the provider class, null is not allowed.
    */
   void setProviderClass(Class<?> providerClass, Class<?> type);

   /**
    * Get the provider for the given module type.
    * <p>
    * This method should return an instance of the provider for the given class.
    * Before calling this method check the type this supports. Use
    * {@link #getType()} in order to check if the given parameter type is the
    * same as the type of this context.
    * 
    * @param type
    *           get the provider for the given type
    * @return a loader to load the given class
    */
   ParameterProvider getParameterProvider(Class<?> type);

   /**
    * Specify the parameter provider object for this context.
    * <p>
    * 
    * @param type
    *           the type to set the provider for.
    * @param provider
    *           the parameter provider of modules of this context.
    */
   void setParameterProvider(ParameterProvider provider, Class<?> type);

   /**
    * Get the parameter provider class for this context's type.
    * <p>
    * This method should return the class of the provider for the context type.
    * If there is not a registered provider it will return null.
    * <p>
    *
    * @param type
    *           get the provider class for the given type
    * @return a provider class for the given context's type.
    */
   Class<? extends ParameterProvider> getParameterProviderClass(Class<?> type);

   /**
    * Set the parameter provider class of this context to be the one provided by
    * the parameter.
    * <p>
    * This method will throw an exception if the provided argument is not a
    * fully qualified name of a class.
    * 
    * @param type
    *           set the provider class for the given type
    * @param providerClass
    *           the fully qualified name of the provider class, null is not
    *           allowed.
    */
   void setParameterProviderClass(String providerClass, Class<?> type);

   /**
    * Set the parameter provider class of this context to be the one provided by
    * the parameter.
    * <p>
    * 
    * @param type
    *           set the provider class for the given type
    * @param providerClass
    *           the provider class, null is not allowed.
    */
   void setParamterProviderClass(
         Class<? extends ParameterProvider> providerClass, Class<?> type);

   /**
    * Get the property injector for the given module type.
    * <p>
    * This method should return an instance of the injector for the given class.
    * Before calling this method check the type this supports. Use
    * {@link #getType()} in order to check if the given parameter type is the
    * same as the type of this context.
    * 
    * @param type
    *           get the property injector for this type
    * @return a loader to load the given class
    */
   PropertyInjector getPropertyInjector(Class<?> type);

   /**
    * Specify the property injector object for this context.
    * <p>
    * 
    * @param type
    *           set the property injector for this type
    * @param injector
    *           the property injector of modules of this context.
    */
   void setPropertyInjector(PropertyInjector provider, Class<?> type);

   /**
    * Get the property injector class for this context's type.
    * <p>
    * This method should return the class of the injector for the context type.
    * If there is not a registered injector it will return null.
    * <p>
    *
    * @param type
    *           get the property injector for this type
    * @return a injector class for the given context's type.
    */
   Class<? extends PropertyInjector> getPropertyInjectorClass(Class<?> type);

   /**
    * Set the property injector class of this context to be the one provided by
    * the parameter.
    * <p>
    * This method will throw an exception if the provided argument is not a
    * fully qualified name of a class.
    * 
    * @param type
    *           set the property injector class for this type
    * @param injectorClass
    *           the fully qualified name of the injector class, null is not
    *           allowed.
    */
   void setPropertyInjectorClass(String injectorClass, Class<?> type);

   /**
    * Set the property injector class of this context to be the one provided by
    * the parameter.
    * <p>
    * 
    * @param type
    *           set the property injector class for this type.
    * @param injectorClass
    *           the injector class, null is not allowed.
    */
   void setPropertyInjectorClass(
         Class<? extends ParameterProvider> injectorClass, Class<?> type);

   /**
    * Load the module of the given type using the loader of this context.
    * <p>
    * Users should check the given type as this may throw an exception if the
    * given type is not supported by the loader of this context.
    * 
    * @param type
    *           modules type to be loaded.
    * @return an instance of given type
    */
   <T> T load(Class<T> type);

   /**
    * Inject the properties to the given bean using the property injector of
    * this context.
    * <p>
    * Users should check the given type as this may throw an exception if the
    * given type is not supported by the loader of this context.
    * 
    * @param bean
    *           the bean to inject the properties to.
    */
   void inject(Object bean);

   /**
    * Get a parameter (usually one defined in a method signature) based on its
    * annotations, and the default properties.
    * <p>
    * This is a delegate to parameter provider of this context, however keep in
    * mind that using this method may override some default implementation of
    * parameter provider, depending on the context.
    * 
    * @param parameterType
    *           the type of the parameter to get the value for
    * @param annotations
    *           the annotations of this parameter, null or empty is allowed.
    * @param properties
    *           default 'domain:name' properties that should be checked for
    *           values.
    * @return an instance of the given parameter type.
    */
   <T> T getParameter(Class<T> parameterType, Annotation[] annotations,
         Map<String, Map<String, Object>> properties);
}
