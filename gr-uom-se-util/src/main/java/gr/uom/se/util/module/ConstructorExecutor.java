/**
 * 
 */
package gr.uom.se.util.module;

import java.lang.reflect.Constructor;
import java.util.Map;

/**
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public interface ConstructorExecutor {

   /**
    * Execute the given constructor and return the type.
    * <p>
    * This will try to execute the given constructor and return a type. If an
    * exception is thrown by the execution of this constructor it will be
    * returned to the caller as a RuntimeException.
    * 
    * @param constructor
    *           the constructor to execute, must not be null.
    * @param onBehalf
    *           the class which this constructor is executed on behalf
    * @param properties
    *           that may be useful to resolve parameter values and other
    * @return the result of the method after executing it.
    */
   <T> T execute(Class<?> onBehalf, Constructor<T> constructor,
         Map<String, Map<String, Object>> properties);
}
