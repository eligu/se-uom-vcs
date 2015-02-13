/**
 * 
 */
package gr.uom.se.util.manager.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark a type as an activator.
 * <p>
 * An activator can be marked as an activator by annotating it with an
 * annotation of this instance, or by declaring it in managers' configuration
 * file.
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
@Target(value = ElementType.TYPE)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface Activator {
   /**
    * Dependencies of this activator.
    * <p>
    * An activator may depend on other activators to run first. Each dependency
    * must be an activator, even by annotating it with an annotation of this
    * type or by declaring it in managers' configurations.
    * 
    * @return the other activator types this activator depends on.
    */
   Class<?>[] dependencies() default {};
}
