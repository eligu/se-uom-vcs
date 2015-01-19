package gr.uom.se.util.module.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A simple annotation to mark a provider method, that may be used to provide an
 * instance of a given type (called module).
 * <p>
 * 
 * @author Elvis Ligu
 */
@Target(value = { ElementType.METHOD, ElementType.CONSTRUCTOR })
@Retention(value = RetentionPolicy.RUNTIME)
public @interface ProvideModule {
}
