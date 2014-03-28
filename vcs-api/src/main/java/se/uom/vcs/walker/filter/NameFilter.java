/**
 * 
 */
package se.uom.vcs.walker.filter;

import java.util.Collection;
import java.util.regex.Pattern;

import se.uom.vcs.VCSResource;

/**
 * Base class for all name filters matching a given pattern.<p>
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public abstract class NameFilter<T extends VCSResource> extends PatternsFilter<T> implements VCSResourceFilter<T> {

    /**
     * Creates a new name filter based on given patterns.<p>
     * 
     * @param patterns
     */
    public NameFilter(Collection<String> patterns) {
	super(patterns);
    }
    
    @Override
    public boolean include(T entity) {
        String path = entity.getPath();
        
        if(path.equals("/")) {
            return false;
        }
        path = AbstractPathFilter.correctAndCheckPath(path);
        String[] segments = path.split("/");
        String name = segments[segments.length - 1];
        
        for(Pattern pattern : patterns) {
            if(pattern.matcher(name).matches()) {
        	return true;
            }
        }
        return false;
    }
}
