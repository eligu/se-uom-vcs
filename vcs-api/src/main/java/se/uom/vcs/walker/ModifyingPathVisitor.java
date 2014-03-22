/**
 * 
 */
package se.uom.vcs.walker;

/**
 * An interface used at various method that requires a {@link Visitor}.<p>
 * 
 * This is a marker interface that works the same as {@link PathLimitingVisitor}
 * but requires from the user to include only those paths that are modified.
 * 
 * @author Elvis Ligu
 * @since 0.0.1
 * @version 0.0.1
 *
 */
public interface ModifyingPathVisitor<T> extends PathLimitingVisitor<T> {

}
