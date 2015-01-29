/**
 * 
 */
package gr.uom.se.util.module;

/**
 * A property injector is used to inject properties to a given instance.
 * <p>
 * The algorithm of injecting properties is specified by the underlying
 * implementation
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 * @see DefaultPropertyInjector
 */
public interface PropertyInjector {

   /**
    * Inject values to properties of the given instance.
    * <p>
    * 
    * @param bean
    *           the instance to inject values, must not be null
    */
   public void injectProperties(Object bean);
}
