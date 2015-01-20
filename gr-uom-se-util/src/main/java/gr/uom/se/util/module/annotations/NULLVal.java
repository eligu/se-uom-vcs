package gr.uom.se.util.module.annotations;

/**
 * A simple non functional type, representing null values. When required a null
 * class use {@link NULLVal}.class or when required a null property as string
 * use {@link #NULL_STR}.
 * 
 * @author Elvis Ligu
 */
public class NULLVal {
   /**
    * The value to be used, in places when objects of this instances, are not
    * applicable and only string values can be used.
    * <p>
    */
   public static final String NULL_STR = "$NULL$";

   /**
    * When a string value of a property has this value, it should be loaded if
    * the property can not be resolved from a configuration.
    * <p>
    */
   public static final String LOAD_STR = "$LOAD$";
}
