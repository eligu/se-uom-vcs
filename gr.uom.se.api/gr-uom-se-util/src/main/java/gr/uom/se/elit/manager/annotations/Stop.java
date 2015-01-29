/**
 * 
 */
package gr.uom.se.elit.manager.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to mark a method as a stopper.
 * <p>
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
@Target(value = ElementType.METHOD)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface Stop {
}
