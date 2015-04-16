package gr.uom.se.util.module;

import gr.uom.se.util.module.annotations.Property;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * The default method executor that is based on a module manager which will
 * provide this method a parameter provider to resolve the parameters needed for
 * execution of a method and or constructor.
 * <p>
 * 
 * @author Elvis Ligu
 * @version 1.0.0
 * @since 1.0.0
 */
public abstract class AbstractMethodConstructorExecutor implements
      MethodExecutor, ConstructorExecutor {

   /**
    * {@inheritDoc}
    */
   @SuppressWarnings("unchecked")
   @Override
   public <T> T execute(Object instance, Class<?> onBehalf, Method method,
         Map<String, Map<String, Object>> properties,
         ModulePropertyLocator propertyLocator) {

      // Try to execute the method
      // Read parameter metadata
      Class<?>[] parameterTypes = method.getParameterTypes();
      Annotation[][] annotations = method.getParameterAnnotations();
      // Get parameter values
      Object[] args = getParameters(onBehalf, parameterTypes, annotations,
            properties, propertyLocator);
      try {
         // Execute the method and return a type
         return (T) method.invoke(instance, args);
      } catch (IllegalAccessException | IllegalArgumentException
            | InvocationTargetException ex) {
         throw new IllegalArgumentException(ex);
      }
   }

   @Override
   public <T> T execute(Class<?> onBehalf, Constructor<T> constructor,
         Map<String, Map<String, Object>> properties,
         ModulePropertyLocator propertyLocator) {

      // Get the parameter types and its values in order to execute
      Class<?>[] parameterTypes = constructor.getParameterTypes();
      Annotation[][] annotations = constructor.getParameterAnnotations();
      // Get the values for each parameter
      Object[] args = getParameters(onBehalf, parameterTypes, annotations,
            properties, propertyLocator);
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
    * @param type
    *           of the parameter to be resolved
    * @param parameterTypes
    *           the types of method parameters
    * @param annotations
    *           annotations of parameters
    * @param properties
    *           the default config created from @Module annotation
    * @param propertyLocator
    *           to locate a parameter provider and then a value for the given
    *           parameter
    * @return parameter values
    */
   protected Object[] getParameters(Class<?> type, Class<?>[] parameterTypes,
         Annotation[][] annotations,
         Map<String, Map<String, Object>> properties,
         ModulePropertyLocator propertyLocator) {

      Object[] parameterValues = new Object[parameterTypes.length];
      for (int i = 0; i < parameterTypes.length; i++) {
         parameterValues[i] = resolveParameterProvider(type, properties,
               propertyLocator).getParameter(parameterTypes[i], annotations[i],
               properties, propertyLocator);
      }
      return parameterValues;
   }

   /**
    * Resolve a parameter provider in order to inject the values of each method
    * or constructor when they will be executed.
    * <p>
    * Subclasses should always return a parameter provider, even in case there
    * is no parameter provider for the given type.
    *
    * @param type
    *           the type to get the parameter provider for, usually the type of
    *           the parameter of a method or a constructor.
    * @param properties
    *           a map of domain:property like properties where the
    *           implementations can look for properties in order to resolve the
    *           parameter provider.
    * @param propertyLocator
    *           a locator to locate properties in order to resolve the parameter
    *           provider. Implementations should use this locator because this
    *           is usually passed by the caller which define the strategy of how
    *           to look for properties.
    * @return a parameter provider for the given type.
    */
   protected abstract ParameterProvider resolveParameterProvider(Class<?> type,
         Map<String, Map<String, Object>> properties,
         ModulePropertyLocator propertyLocator);
}
