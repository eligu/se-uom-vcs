/**
 * 
 */
package se.uom.vcs.walker.filter;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import se.uom.vcs.VCSResource;

/**
 * Abstract base class for path filtering of {@link VCSResource}s.<p>
 * 
 * Each subclass may wish to check if the required path is contained within
 * the list of paths or is a path prefix of these paths or a path suffix.
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public abstract class AbstractPathFilter<T extends VCSResource> implements VCSResourceFilter<T> {

    protected Set<String> paths;

    /**
     * Creates a new filter based on the given paths.<p>
     * 
     * @param paths
     * 		to check each entity if it is contained within paths. 
     * 		Must not be null not or empty or contain a null path.
     * 		Paths like '/' or '/+s/' or empty are not allowed.
     */
    public AbstractPathFilter(Collection<String> paths) {
	if (paths == null) {
	    throw new IllegalArgumentException("paths must not be null");
	}
	if (paths.isEmpty()) {
	    throw new IllegalArgumentException("paths must not be empty");
	}
	
	this.paths = new LinkedHashSet<String>();
	for (String path : paths) {
	    this.paths.add(correctAndCheckPath(path));
	}
    }

    /**
     * Replace all \ with / and remove / from start and from end of the string.
     * Check path is a valid one.
     * 
     * @param path
     * 		to check and correct
     * @return
     * 		corrected path
     */
    static String correctAndCheckPath(String path) {
	if (path == null) {
	    throw new IllegalArgumentException("a path must not be null");
	}
	path = path.trim();
	if (path.isEmpty()) {
	    throw new IllegalArgumentException("a path must not be empty");
	}

	path = path.replaceAll("\\", "/");
	if (path.charAt(0) == '/') {
	    if (path.length() == 1) {
		throw new IllegalArgumentException(
			"/ or \\ is not allowed as a path");
	    }
	    path = path.substring(1).trim();
	}
	if (path.charAt(path.length() - 1) == '/') {
	    if (path.length() == 1) {
		throw new IllegalArgumentException(
			"/ or \\ is not allowed as a path");
	    }
	    path = path.substring(0, path.length() - 1).trim();
	}
	return path;
    }
}
