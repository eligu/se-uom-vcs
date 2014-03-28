/**
 * 
 */
package se.uom.vcs.walker.filter;

import java.util.Collection;

import se.uom.vcs.VCSResource;

/**
 * Filter all paths that are contained within a given path.<p>
 * 
 * When applied this filter will check a given {@link VCSResource} if its path
 * has a prefix same as one of the given path prefixes.
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public class PathPrefixFilter<T extends VCSResource> extends AbstractPathFilter<T> {

    /**
     * Creates a filter for the given path prefixes.<p>
     * 
     * @param paths
     * 		prefixes to check each path against
     */
    public PathPrefixFilter(Collection<String> paths) {
	super(paths);
	
    }

    /**
     * Check whether <code>prefix</code> is a path prefix of <code>path</code>.
     * 
     * @param prefix
     * @param path
     * @return true if <code>prefix</code> is a path prefix of <code>path</code>
     */
    static boolean isPrefix(String prefix, String path) {
	
	String[] prefixSegments = prefix.split("/");
	String[] pathSegments = path.split("/");
	
	if(prefixSegments.length == 0) {
	    return false;
	}
	
	if(prefixSegments.length < pathSegments.length) {
	    return false;
	}
	
	for(int i = 0; i < prefixSegments.length; i++) {
	    if(!prefixSegments[i].equals(pathSegments[i])) {
		return false;
	    }
	}
	return true;
    }
    
    @Override
    public boolean include(T entity) {
	
	String path = entity.getPath();
	for(String prefix : paths) {
	    if(isPrefix(prefix, path)) {
		return true;
	    }
	}
	return false;
    }
}
