/**
 * 
 */
package se.uom.vcs.walker;

import java.util.Collection;

/**
 * An interface used at various method that requires a {@link Visitor}.<p>
 * 
 * When this interface is used, usually the implementation must provide a
 * collection of paths in order to limit the visiting only to those paths.<p>
 * 
 * This is usually used at tree walks or even commit differences when we have
 * to deal with paths.
 * 
 * @author Elvis Ligu
 * @since 0.0.1
 * @version 0.0.1
 */
public interface PathLimitingVisitor<T> extends Visitor<T> {

	/**
	 * Return a collection of paths that will be used to limit
	 * the results this visitor will be accepted.<p>
	 * 
	 * @return
	 */
	Collection<String> getPaths();
}
