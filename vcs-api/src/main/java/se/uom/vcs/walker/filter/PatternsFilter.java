/**
 * 
 */
package se.uom.vcs.walker.filter;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Abstract class that requires filters based on patterns.<p>
 * 
 * @author Elvis Ligu
 * @since 0.0.1
 * @version 0.0.1
 */
public abstract class PatternsFilter<T> implements VCSFilter<T> {

    protected Set<Pattern> patterns;
    
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
}
