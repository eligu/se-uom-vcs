/**
 * 
 */
package se.uom.vcs.walker.filter;

import se.uom.vcs.VCSCommit;

/**
 * Base interface used to filter the commits returned when commit walking is required.<p>
 * 
 * @author Elvis Ligu
 * @since 0.0.1
 * @version 0.0.1
 */
public interface VCSCommitFilter<T extends VCSCommit> extends VCSFilter<T> {
}
