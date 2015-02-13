/**
 * 
 */
package gr.uom.se.util.manager;

import gr.uom.se.util.module.annotations.ProvideModule;
import gr.uom.se.util.validation.ArgsCheck;

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
    * {@inheritDoc}
    */
   @Override
   protected MainManager getMainManager() {
      return manager;
   }

}
