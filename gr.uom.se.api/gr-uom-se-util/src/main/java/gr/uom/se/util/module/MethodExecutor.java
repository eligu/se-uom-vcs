/**
 * 
 */
package gr.uom.se.util.module;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * A method executor that can execute a method.
 * <p>
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public interface MethodExecutor {

   /**
    * Execute the given method and return a type, if any.
    * <p>
    * This will try to execute the given method and return a type. The returned
    * type may be {@link Void}. If an exception is thrown by the execution of
    * this method it will be returned to the caller as a RuntimeException.
    * 
    * @param method
    *           the method to execute, must not be null.
    * @param instance
    *           the instance on where to execute the method
    * @param onBehalf
    *           the class which this method is executed on behalf
    * @param properties
    *           that may be useful to resolve parameter values and other
    * @return the result of the method after executing it.
    */
   <T> T execute(Object instance, Class<?> onBehalf, Method method,
         Map<String, Map<String, Object>> properties);
}
