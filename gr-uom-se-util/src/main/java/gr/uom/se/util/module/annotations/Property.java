package gr.uom.se.util.module.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Elvis Ligu
 */
@Target(value = {ElementType.TYPE, ElementType.PARAMETER, ElementType.FIELD})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface Property {
    
    String name();
    String domain() default "default";
    String stringVal() default "";
}