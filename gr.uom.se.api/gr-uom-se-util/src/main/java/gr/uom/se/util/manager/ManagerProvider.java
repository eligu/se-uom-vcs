/**
 * 
 */
package gr.uom.se.util.manager;

import gr.uom.se.util.module.ModuleConstants;
import gr.uom.se.util.module.ParameterProvider;
import gr.uom.se.util.module.annotations.Property;
import gr.uom.se.util.module.annotations.ProvideModule;
import gr.uom.se.util.validation.ArgsCheck;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public class ManagerProvider implements ParameterProvider {

   private ParameterProvider defaultProvider;
   private AbstractManager manager;
   
   @ProvideModule
   public ManagerProvider(
         @Property(
               domain = ModuleConstants.DEFAULT_MODULE_CONFIG_DOMAIN, 
               name = ModuleConstants.PARAMETER_PROVIDER_PROPERTY) 
         ParameterProvider defaultProvider,
         @Property(
               domain = ManagerConstants.DEFAULT_DOMAIN, 
               name = "mainManager") 
         AbstractManager manager) {
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
         Map<String, Map<String, Object>> properties) {
      if (manager.isRegistered(parameterType)) {
         return manager.getManager(parameterType);
      }
      return defaultProvider.getParameter(
            parameterType, annotations, properties);
   }
}
