/**
 * 
 */
package gr.uom.se.util.module;

import gr.uom.se.util.config.ConfigManager;
import gr.uom.se.util.config.DefaultConfigManager;

/**
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public class DefaultModuleManager extends AbstractModuleManager {

   private ConfigManager manager;
   
   public DefaultModuleManager(ConfigManager manager) {
      if(manager == null) {
         manager = new DefaultConfigManager();
      }
      this.manager = manager;
   }
   
   @Override
   protected ConfigManager getConfig() {
      return manager;
   }
}
