package gr.uom.se.vcs.exceptions;

/**
 * A general exception used when something is wrong using the API.
 * <p>
 * Generally speaking this exception will be thrown in cases when reading
 * repository's objects is impossible.
 * 
 * @author Elvis Ligu
 * @since 0.0.1
 * @version 0.0.1
 * 
 */
public class VCSRepositoryException extends Exception {

   /**
    * Unique serial ID used during serialization
    */
   private static final long serialVersionUID = 1952411748624565599L;

   /**
    * Creates a new exception.
    * <p>
    */
   public VCSRepositoryException() {
      super("");
   }

   /**
    * Creates a new exception with the specified message.
    * <p>
    * 
    * @param msg
    *           the message of this exception
    */
   public VCSRepositoryException(String msg) {
      super(msg);
   }

   /**
    * Creates a new exception based on the given exception.
    * <p>
    * 
    * @param e
    *           that this exception wraps
    */
   public VCSRepositoryException(Throwable e) {
      super(e);
   }
}
