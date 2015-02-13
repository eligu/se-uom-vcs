/**
 * 
 */
package gr.uom.se.util.manager;

import gr.uom.se.util.config.ConfigDomain;
import gr.uom.se.util.config.ConfigManager;
import gr.uom.se.util.manager.annotations.Init;
import gr.uom.se.util.module.annotations.ProvideModule;
import gr.uom.se.util.validation.ArgsCheck;

import java.util.Map;

/**
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public class DefaultActivatorManager extends AbstractActivatorManager {

   private MainManager manager;

   @ProvideModule
   public DefaultActivatorManager(MainManager manager) {
      ArgsCheck.notNull("manager", manager);
      this.manager = manager;
   }

   /**
    * Will activate any registered activator at default manager domain.
    * <p>
    * The property name of an activator should be prefixed with 'activator.' and
    * the remaining part should be the fully qualified name of the activator
    * class. If the class can not be resolved or is not an activator class it
    * will throw an error.
    */
   @Init
   public void init() {
      ConfigManager config = manager.getManager(ConfigManager.class);
      if (config == null) {
         return;
      }
      // Try to get first the default manager domain
      String domain = config.getProperty(
            ManagerConstants.DEFAULT_DOMAIN_PROPERTY, String.class);
      if (domain == null) {
         domain = ManagerConstants.DEFAULT_DOMAIN;
      }
      ConfigDomain cfgd = config.getDomain(domain);
      if (cfgd == null) {
         return;
      }
      // Look under the manager domain if there
      // are properties that starts with the prefix 'activator.'.
      // For each such property resolve the class name which comes
      // after activator prefix and activate it.
      Map<String, Object> props = cfgd
            .getPropertiesWithPrefix(ManagerConstants.ACTIVATOR_PREFIX);
      int prefixLen = ManagerConstants.ACTIVATOR_PREFIX.length();
      for (String act : props.keySet()) {
         String actClassName = act.substring(prefixLen);
         this.activate(actClassName);
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected MainManager getMainManager() {
      return manager;
   }
}
