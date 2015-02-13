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
@Activator(dependencies = Activator2.class)
public class Activator1 {

   MainManager manager;
   
   @ProvideModule
   public Activator1(MainManager manager) {
      this.manager = manager;
   }
   
   @Init
   public void init() {
      System.out.println("Activating:  " + this.getClass());
   }
   
   public static void main(String[] args) {
      Activator an = Activator1.class.getAnnotation(Activator.class);
      System.out.println(an);
   }
}
