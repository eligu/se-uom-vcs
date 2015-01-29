/**
 * 
 */
package gr.uom.se.util.module;

import gr.uom.se.util.module.annotations.Property;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * The default method executor that is based on a module manager which will
 * provide this method a parameter provider for the
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public abstract class AbstractMethodConstructorExecutor implements MethodExecutor,
      ConstructorExecutor {

   /**
    * {@inheritDoc}
    */
   @SuppressWarnings("unchecked")
   @Override
   public <T> T execute(Object instance, Class<?> onBehalf, Method method,
         Map<String, Map<String, Object>> properties) {

      // Try to execute the method with annotation
      Class<?>[] parameterTypes = method.getParameterTypes();
      Annotation[][] annotations = method.getParameterAnnotations();
      Object[] args = getParameters(onBehalf, parameterTypes, annotations,
            properties);
      try {
         return (T) method.invoke(instance, args);
      } catch (IllegalAccessException | IllegalArgumentException
            | InvocationTargetException ex) {
         throw new IllegalArgumentException(ex);
      }
   }

   @Override
   public <T> T execute(Class<?> onBehalf, Constructor<T> constructor,
         Map<String, Map<String, Object>> properties) {
      
      // Get the parameter types and its values in order to execute
      Class<?>[] parameterTypes = constructor.getParameterTypes();
      Annotation[][] annotations = constructor.getParameterAnnotations();
      // Get the values for each parameter
      Object[] args = getParameters(onBehalf, parameterTypes, annotations,
            properties);
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
   protected Object[] getParameters(Class<?> type, Class<?>[] parameterTypes,
         Annotation[][] annotations, Map<String, Map<String, Object>> properties) {

      Object[] parameterValues = new Object[parameterTypes.length];
      for (int i = 0; i < parameterTypes.length; i++) {
         parameterValues[i] = resolveParameterProvider(type, properties)
               .getParameter(parameterTypes[i], annotations[i], properties);
      }
      return parameterValues;
   }

   protected abstract ParameterProvider resolveParameterProvider(Class<?> type,
         Map<String, Map<String, Object>> properties);
}
