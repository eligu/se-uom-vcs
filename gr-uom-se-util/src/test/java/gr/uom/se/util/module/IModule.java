/**
 * 
 */
package gr.uom.se.util.module;

import gr.uom.se.util.module.annotations.Module;
import gr.uom.se.util.module.annotations.Property;

/**
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
@Module(
      provider = ModuleProvider.class,
      properties = @Property(domain = "default", name = "var", stringVal = "22")
      )
public interface IModule {

   public int getVar();
}
