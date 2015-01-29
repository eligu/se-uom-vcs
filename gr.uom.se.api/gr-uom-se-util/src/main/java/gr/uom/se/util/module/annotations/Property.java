package gr.uom.se.util.module.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A property applied at different places in a class.
 * <p>
 * Each property has a domain which is the place from where this property comes.
 * The default value of the domain is {@code default}. Using the {@link #name()}
 * and the {@link #domain()} a reflector can obtain the coordinates of the value
 * of this property, that is the places from where to find the property. In
 * cases where the property can not be resolved from a configuration the user
 * can specify a default value as a string representation, by using
 * {@link #stringVal()}. However the use of string value should be limited as it
 * is relying in the ability of the annotation processor to construct a variable
 * using a string representation. Generally speaking a string value should be
 * applicable when the annotated type is a primitive type or a date type, as it
 * can be easily represented by a string.
 * 
 * @author Elvis Ligu
 */
@Target(value = { ElementType.TYPE, ElementType.PARAMETER, ElementType.FIELD })
@Retention(value = RetentionPolicy.RUNTIME)
public @interface Property {

   /**
    * The name of the property, stored in a configuration or in any other place.
    * <p>
    * 
    * @return the name of the property
    */
   String name();

   /**
    * The domain of the property where the property with the name provided by
    * this annotation is stored.
    * <p>
    * The default value is {@code default}
    * 
    * @return the domain of this property
    */
   String domain() default "default";

   /**
    * The string representation of this property, usually used as a default
    * value if the property can not be resolved from the annotation processor.
    * <p>
    * The default value is {@link NULLVal#NULL_STR}.
    * 
    * @return the string representation of this property
    */
   String stringVal() default NULLVal.NULL_STR;
}