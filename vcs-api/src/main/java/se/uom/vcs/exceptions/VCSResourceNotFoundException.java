/**
 * 
 */
package se.uom.vcs.exceptions;

/**
 * An exception used when a resource is not available or unknown.
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
	 * Creates a new instance
	 */
	public VCSResourceNotFoundException() {
		super();
	}

	/**
	 * @param msg
	 */
	public VCSResourceNotFoundException(String msg) {
		super(msg);
	}

	/**
	 * @param ex
	 */
	public VCSResourceNotFoundException(Throwable ex) {
		super(ex);
	}
}
