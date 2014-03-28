/**
 * 
 */
package se.uom.vcs.walker.filter;

import java.util.Collection;

import se.uom.vcs.VCSResource;

/**
 * Filters only the required path.<p>
 * 
 * This filter can be used to allow a certain path or a group of paths.
 * Each time a {@link VCSResource} is tested it will check if the is path contained
 * in the list of supplied paths when this filter was constructed.
 * The paths will be checked exactly, not matching or pattern processing will be done.
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public class PathFilter<T extends VCSResource> extends AbstractPathFilter<T> {

    
    /**
     * Creates a new filter based on given paths.<p>
     * 
     * @param paths
     * 		to check when a given resource will be included in the results.
     */
    public PathFilter(Collection<String> paths) {
	super(paths);
    }

    @Override
    public boolean include(T entity) {

	String path = entity.getPath();
	path = path.replaceAll("\\", "/").trim();
	if(path.equals("/")) {
	    return false;
	}
	path = AbstractPathFilter.correctAndCheckPath(path);
	return paths.contains(path);
    }

}
