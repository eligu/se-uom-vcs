package se.uom.vcs.exceptions;

/**
 * A general exception used when something is wrong using the API.
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
	 * Creates a new exception
	 */
	public VCSRepositoryException() {
		super("");
	}
	
	/**
	 * Creates new with the specified message
	 * @param msg
	 */
	public VCSRepositoryException(String msg) {
		super(msg);
	}
	
	/**
	 * Creates new based on the given exception
	 * @param e
	 */
	public VCSRepositoryException(Exception e) {
		super(e);
	}
}
