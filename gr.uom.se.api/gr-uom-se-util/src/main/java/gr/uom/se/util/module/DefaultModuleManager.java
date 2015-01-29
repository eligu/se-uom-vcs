/**
 * 
 */
package gr.uom.se.util.module;

import gr.uom.se.util.config.AbstractConfigManager;
import gr.uom.se.util.config.ConfigManager;
import gr.uom.se.util.config.DefaultConfigManager;
import gr.uom.se.util.manager.ManagerConstants;
import gr.uom.se.util.module.annotations.Property;

/**
 * Default implementation of module manager.
 * <p>
 * This module manager implementation is based on {@link AbstractModuleManager}
 * and it only provides the implementation for {@link #getConfig()} method to
 * provide the module manager a config manager implementation. If a config
 * manager is not provided during the construction of this manager then it will
 * use the {@link AbstractConfigManager}, which will be created in memory
 * without a configuration.
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
@Property(domain = ManagerConstants.DEFAULT_DOMAIN, name = "moduleManager")
public class DefaultModuleManager extends AbstractModuleManager {

   private ConfigManager manager;

   public DefaultModuleManager(ConfigManager manager) {
      if (manager == null) {
         manager = new DefaultConfigManager();
      }
      this.manager = manager;
      
      ModuleLoader loader = this.getLoader(Object.class);
      this.registerDefaultLoader(loader);
      ParameterProvider provider = this.getParameterProvider(Object.class);
      this.registerDefaultParameterProvider(provider);
   }

   @Override
   protected ConfigManager getConfig() {
      return manager;
   }
}
