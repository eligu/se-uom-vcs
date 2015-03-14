/**
 * 
 */
package gr.uom.se.util.manager;

import gr.uom.se.util.manager.annotations.Init;
import gr.uom.se.util.module.annotations.Module;

/**
 * An activator is a class whose instance should be instantiated once per whole
 * application lifecycle and contains an initializing method (usually annotated
 * with {@link Init}).
 * <p>
 * Activating is a process where some parts of the system should be activated
 * (should be known to other parts) or an event should occur at some time, but
 * only once. This is useful in cases when the system is performing some
 * operation and at some point in order the system to continue an event should
 * take place. For example when the system is initialized it may register some
 * listeners to some specific events, this could be done by using an activator
 * which will register that listener.
 * <p>
 * When an activator is created a method of it (or static method if no instance
 * is required) should be executed, which method is supposed to be an
 * initializer. Depending on the situation an activator class may be a module
 * which can be created by modules API. After an activator is activated (the
 * initializer method is called) it will not be activated for a seccond time, if
 * it is requested to do so.
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
@Module(provider = DefaultActivatorManager.class)
public interface ActivatorManager {

   /**
    * Given the following type, activate it, by loading an instance of it if he
    * has an instance initializer method (usually annotated with {@link Init})
    * or call the static initializer method if it is present.
    * <p>
    * If the same type is given twice for activation, it will only be activated
    * once. The mechanism of activating relies on implementation.
    * 
    * @param activator
    *           the type of activator to be activated
    */
   public void activate(Class<?> activator);
}
