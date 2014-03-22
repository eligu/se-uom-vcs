/**
 * 
 */
package se.uom.vcs.walker;

import se.uom.vcs.VCSDirectory;
import se.uom.vcs.VCSFile;
import se.uom.vcs.VCSResource;

/**
 *  An interface used at various method that requires a {@link Visitor}.<p>
 *  
 *  This interface is used when we have to walk trees so it gives control on
 *  what kind of {@link VCSResource} we need to visit.
 *  
 * @author Elvis Ligu
 * @since 0.0.1
 * @version 0.0.1
 */
public interface FileDirVisitor<T extends VCSResource> extends PathLimitingVisitor<T> {

	/**
	 * True if the visitor should accept resources of type {@link VCSResource.Type#DIR}.<p>
	 * 
	 * If the type of a resource is DIR then it can be converted to {@link VCSDirectory}.
	 * <p>
	 * <b>NOTE:</b> {@link FileDirVisitor#includeDirs()} and {@link #includeFiles()} must not be both
	 * null.
	 * 
	 * @return true if the visitor should accept DIR type resources
	 */
	boolean includeDirs();

	/**
	 * True if the visitor should accept resources of type {@link VCSResource.Type#FILE}.<p>
	 * 
	 * If the type of a resource is FILE then it can be converted to {@link VCSFile}.
	 * <p>
	 * <b>NOTE:</b> {@link FileDirVisitor#includeDirs()} and {@link #includeFiles()} must not be both
	 * null.
	 * @return true if the visitor should accept FILE type resources
	 */
	boolean includeFiles();

	/**
	 * If tree walking is limited to a certain path, and this path is a DIR then if recursive
	 * true, all the resources that have prefix this path will be visited.<p> 
	 */
	boolean recursive();
}
