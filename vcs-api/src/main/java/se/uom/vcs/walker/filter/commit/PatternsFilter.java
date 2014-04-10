/**
 * 
 */
package se.uom.vcs.walker.filter.commit;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;

import se.uom.vcs.walker.filter.VCSFilter;

/**
 * Abstract class that requires filters based on patterns.<p>
 * 
 * This implementation is based on Java patterns so any string passed at
 * the constructor should take care of the special characters used by
 * Java.
 * 
 * @author Elvis Ligu
 * @since 0.0.1
 * @version 0.0.1
 */
public abstract class PatternsFilter<T> implements VCSFilter<T> {

    /**
     * Java pattern collection used by this filter.<p>
     */
    protected Set<Pattern> patterns;
    
    /**
     * Create a new instance based on given patterns.<p>
     * 
     * All sub classes must call this constructor as it will check
     * the given patterns if it is null, empty, or contain any null
     * element.
     * 
     * @param patterns
     */
    public PatternsFilter(Collection<String> patterns) {
	if (patterns == null) {
	    throw new IllegalArgumentException("patterns must not be null");
	}
	if (patterns.isEmpty()) {
	    throw new IllegalArgumentException("patterns must not be empty");
	}
	
	this.patterns = new LinkedHashSet<Pattern>();
	
	for(String str : patterns) {
	    if(str == null) {
		throw new IllegalArgumentException("patterns must not contain null");
	    }
	    this.patterns.add(Pattern.compile(str));
	}
    }
    
    /**
     * @return
     * 		the patterns this filter was created
     */
    public Set<String> getPatterns() {
	
	Set<String> set = new LinkedHashSet<String>();
	for(Pattern p : patterns) {
	    set.add(p.pattern());
	}
	return set;
    }
}
