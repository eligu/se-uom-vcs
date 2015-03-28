/**
 * 
 */
package gr.uom.se.util.manager;

import gr.uom.se.util.module.ModuleConstants;
import gr.uom.se.util.module.ModulePropertyLocator;
import gr.uom.se.util.module.ParameterProvider;
import gr.uom.se.util.module.annotations.Property;
import gr.uom.se.util.module.annotations.ProvideModule;
import gr.uom.se.util.validation.ArgsCheck;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * A custom provider that will wrap the default provider in order to provide a
 * registered manager when a parameter of a given type is a manager type.
 * <p>
 * When using managers API, it is often required to inject different managers to
 * modules. Because managers are considered singleton instances, that means a
 * module that has a dependency on a manager type (specifying its interface or
 * implementation) is requiring that manager (the single manager) to be
 * injected. This provider will wrap the default provider and for each call to
 * getParameter() method it will check if the given parameter type is a manager
 * type, if so it will provide that manager, otherwise it will delegate the call
 * to the wrapping provider.
 * <p>
 * WARNING: if a dynamic injection is required (that is, the loading of a module
 * using properties at runtime) the managers can not be overridden with supplied
 * manager instances at runtime. In order to do so the client must specify a
 * custom property provider for that module.
 * 
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public class ManagerProvider implements ParameterProvider {

   private final ParameterProvider defaultProvider;
   private final MainManager manager;

   @ProvideModule
   public ManagerProvider(
         @Property(domain = ModuleConstants.DEFAULT_MODULE_CONFIG_DOMAIN, name = ModuleConstants.PARAMETER_PROVIDER_PROPERTY) ParameterProvider defaultProvider,
         @Property(domain = ManagerConstants.DEFAULT_DOMAIN, name = ManagerConstants.DEFAULT_MANAGER_PROPERTY) MainManager manager) {
      ArgsCheck.notNull("defaultProvider", defaultProvider);
      ArgsCheck.notNull("manager", manager);
      this.defaultProvider = defaultProvider;
      this.manager = manager;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public <T> T getParameter(Class<T> parameterType, Annotation[] annotations,
         Map<String, Map<String, Object>> properties,
         ModulePropertyLocator propertyLocator) {
      if (manager.isRegistered(parameterType)) {
         return manager.getManager(parameterType);
      }
      return defaultProvider.getParameter(parameterType, annotations,
            properties, propertyLocator);
   }
}
