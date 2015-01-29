/**
 * 
 */
package gr.uom.se.util.manager;

import gr.uom.se.util.config.ConfigManager;
import gr.uom.se.util.config.DefaultConfigManager;
import gr.uom.se.util.module.DefaultModuleManager;
import gr.uom.se.util.module.ModuleManager;
import gr.uom.se.util.module.annotations.Property;

/**
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
@Property(domain = ManagerConstants.DEFAULT_DOMAIN, name = "mainManager")
public class DefaultManager extends AbstractManager {

   private ModuleManager moduleManager;

   public DefaultManager(ModuleManager moduleManager, ConfigManager config) {
      if (moduleManager == null) {
         if (config == null) {
            config = new DefaultConfigManager();
         }
         moduleManager = new DefaultModuleManager(config);
      }
      this.moduleManager = moduleManager;
      registerLoaded(config);
      registerLoaded(moduleManager);
      registerLoaded(this);
      ManagerProvider provider = new ManagerProvider(
            moduleManager.getParameterProvider(ManagerProvider.class), this);
      this.moduleManager
            .registerDefaultParameterProvider(provider);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected ModuleManager getModuleManager() {
      return moduleManager;
   }
}
