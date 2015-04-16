/**
 *
 */
package gr.uom.se.util.module;

import java.lang.reflect.Constructor;
import java.util.Map;

/**
 * A constructor executor that can execute a constructor.
 * <p>
 *
 * @author Elvis Ligu
 * @version 1.0.0
 * @since 1.0.0
 */
public interface ConstructorExecutor {

   /**
    * Execute the given constructor and return the type.
    * <p>
    * This will try to execute the given constructor and return a type. If an
    * exception is thrown by the execution of this constructor it will be
    * returned to the caller as a RuntimeException.
    *
    * @param <T>
    *           the type of the new instance that will be returned
    * @param onBehalf
    *           the class which this constructor is executed on behalf
    * @param constructor
    *           the constructor to execute, must not be null.
    * @param properties
    *           that may be useful to resolve parameter values and other
    * @param propertyLocator
    *           used by implementations to locate different properties in a
    *           context of modules API. That is, implementations may need to
    *           resolve a parameter provider in order to get all parameter
    *           values for a method to execute.
    *
    * @return the type created after the constructor is executed.
    */
   <T> T execute(Class<?> onBehalf, Constructor<T> constructor,
         Map<String, Map<String, Object>> properties,
         ModulePropertyLocator propertyLocator);
}
