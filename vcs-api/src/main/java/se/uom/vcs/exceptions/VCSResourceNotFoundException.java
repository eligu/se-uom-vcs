/**
 * 
 */
package se.uom.vcs.exceptions;

/**
 * An exception used when a resource is not available or unknown.
 * <p>
 * 
 * @author Elvis Ligu
 * @since 0.0.1
 * @version 0.0.1
 * 
 */
public class VCSResourceNotFoundException extends Exception {

   /**
    * Unique serial ID used during serialization
    */
   private static final long serialVersionUID = 1758582163901373518L;

   /**
    * Creates a new instance of this exception.
    * <p>
    */
   public VCSResourceNotFoundException() {
      super();
   }

   /**
    * Creates a new instance of this exception with the specified message.
    * <p>
    * 
    * @param msg
    *           the message of this exception
    */
   public VCSResourceNotFoundException(String msg) {
      super(msg);
   }

   /**
    * Creates a new exception based on the given parameter.
    * <p>
    * 
    * @param ex
    *           the exception this instance wraps
    */
   public VCSResourceNotFoundException(Throwable ex) {
      super(ex);
   }
}
