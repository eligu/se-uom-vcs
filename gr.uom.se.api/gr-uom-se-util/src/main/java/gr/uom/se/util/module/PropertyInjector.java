/**
 * 
 */
package gr.uom.se.util.module;

import java.util.Map;

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

   /**
    * Inject value to properties (fields) of the given bean instance.
    * <p>
    * This method will try to resolve the values from the given map, if they are
    * available. Values that are not available can be resolved from different
    * configuration sources, however that depends on the implementation of this
    * injector. This method will ensure that the injected values will be from
    * the provided properties (if they are found) and then will check for other
    * sources if they are not available.
    * 
    * @param bean
    *           the instance to inject values, must not be null.
    * @param properties
    *           the map to check for properties.
    */
   public void injectProperties(Object bean,
         Map<String, Map<String, Object>> properties);

   /**
    * Inject value to properties (fields) of the given bean instance.
    * <p>
    * In contrast with other methods, this method will use the property locator
    * in order to locate properties. If the property locator is null, it will
    * use a default strategy for locating properties.
    * 
    * @param bean
    *           the instance to inject values, must not be null.
    * @param propertyLocator
    *           the locator strategy to lookup properties.
    */
   public void injectProperties(Object bean,
         ModulePropertyLocator propertyLocator);
}
