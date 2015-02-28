package gr.uom.se.util.module.annotations;

import gr.uom.se.util.module.ModuleLoader;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation that can describe properties of a module.
 * <p>
 * A module is a type whose instances can be loaded using an implementation of
 * {@link ModuleLoader} interface. Using this annotation the module can describe
 * properties that may be used by the loader, such as the provider of the class
 * who is annotated by this annotation or other properties.
 * 
 * @author Elvis Ligu
 */
@Target(value = { ElementType.TYPE })
@Retention(value = RetentionPolicy.RUNTIME)
@Inherited
public @interface Module {

   /**
    * Define the provider of the type this is annotating.
    * <p>
    * The default value is of type {@link NULLVal}, that is the module loader
    * can read it as non specified.
    * 
    * @return
    */
   Class<?> provider() default NULLVal.class;

   /**
    * Properties of this module, if any.
    * 
    * @return
    */
   Property[] properties() default {};
}
