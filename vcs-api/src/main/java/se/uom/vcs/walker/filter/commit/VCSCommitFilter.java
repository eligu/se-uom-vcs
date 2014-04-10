/**
 * 
 */
package se.uom.vcs.walker.filter.commit;

import se.uom.vcs.VCSCommit;
import se.uom.vcs.walker.filter.VCSFilter;

/**
 * Base interface used to filter the commits returned when commit walking is required.<p>
 * 
 * This is a marker interface and also limits the type parameters to {@link VCSCommit}.
 * All commit filters must implement this interface in order to be used with a 
 * commit visitor.
 * 
 * @author Elvis Ligu
 * @since 0.0.1
 * @version 0.0.1
 */
public interface VCSCommitFilter<T extends VCSCommit> extends VCSFilter<T> {
}
