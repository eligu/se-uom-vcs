/**
 * 
 */
package gr.uom.se.util.manager;

import gr.uom.se.util.module.annotations.Module;

/**
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
@Module(provider = DefaultActivatorManager.class)
public interface ActivatorManager {

   public void activate(Class<?> activator);
}
