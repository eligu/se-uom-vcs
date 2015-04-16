/**
 * 
 */
package gr.uom.se.util.module;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * An interface for a parameter provider, usually required by a module loader
 * when loading a module, or by a property injector.
 * <p>
 * 
 * @author Elvis Ligu
 * @version 1.0.0
 * @since 1.0.0
 * @see DefaultParameterProvider
 */
public interface ParameterProvider {

   /**
    * Return a value for the given type based on its annotations and the default
    * config.
    * <p>
    * 
    * When encountering a parameter (weather it is a method parameter or a
    * field) we can provide a value, by analyzing its annotations. A value can
    * be either retrieved by a configuration manager within system, or provided
    * by default values within the provided properties, or by annotations.
    * 
    * @param parameterType
    *           the type of the parameter the value is required
    * @param annotations
    *           the annotations for the given type
    * @param properties
    *           the default properties to find a value for the given type
    * @param propertyLocator
    *           the locator of modules properties that are essential to resolve
    *           the value of the given parameter (either by loading it or by
    *           injecting a value from a property source).
    * @return a value for the given parameter type
    */
   <T> T getParameter(Class<T> parameterType, Annotation[] annotations,
         Map<String, Map<String, Object>> properties,
         ModulePropertyLocator propertyLocator);
}
