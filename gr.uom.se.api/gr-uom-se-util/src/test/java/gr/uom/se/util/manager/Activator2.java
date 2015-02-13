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
@Activator
public class Activator2 {

   MainManager manager;
   static int count = 0;

   @ProvideModule
   public Activator2(MainManager manager) {
      this.manager = manager;
   }

   @Init
   public void init() {
      count++;
      System.out.println("Activating:  " + this.getClass());
   }
}
