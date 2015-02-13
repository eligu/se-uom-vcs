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
   
   /**
    * When a property has this name it will not be lookup in config manager
    * not or it will be loaded by the loader. Use this as a property name
    * when annotating properties to ensure that a property must be provided
    * by another point of execution.
    * <p>
    */
   public static final String NO_PROP = "$NoPrOp$";
}
