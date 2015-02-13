/**
 * 
 */
package gr.uom.se.util.manager;

import gr.uom.se.util.manager.annotations.Activator;
import gr.uom.se.util.manager.annotations.Init;
import gr.uom.se.util.module.annotations.ProvideModule;

/**
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
@Activator(dependencies = {Activator2.class, Activator1.class})
public class Activator3 {

   MainManager manager;
   
   @ProvideModule
   public Activator3(MainManager manager) {
      this.manager = manager;
   }
   
   @Init
   public void init() {
      System.out.println("Activating:  " + this.getClass());
   }
}
