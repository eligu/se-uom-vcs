/**
 * 
 */
package se.uom.vcs.walker.filter;

import se.uom.vcs.VCSResource;

/**
 * Base interface used to filter the resources returned when tree walking is required.<p>
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public interface VCSResourceFilter<T extends VCSResource> extends VCSFilter<T> {

}
