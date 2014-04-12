/**
 * 
 */
package se.uom.vcs.jgit.walker;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.TreeFilter;

import se.uom.vcs.walker.filter.resource.AbstractPathFilter;

/**
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public class JGitChildFilter extends TreeFilter {

    //protected Set<String> paths;
    protected byte[][] paths;
    public static final byte LINE_SEP = 0x2f; // '/' char
    public static final String ENCODING = "UTF-8";
    
    /**
     * 
     */
    public JGitChildFilter(Set<String> paths) {
	if (paths == null) {
	    throw new IllegalArgumentException("paths must not be null");
	}
	if (paths.isEmpty()) {
	    throw new IllegalArgumentException("paths must not be empty");
	}
	 
	Set<String> temp = new LinkedHashSet<String>();
	for(String p : paths) {
	    temp.add(AbstractPathFilter.correctAndCheckPath(p));
	}
	
	this.paths = new byte[temp.size()][];
	int i = 0;
	for (String path : temp) {
	    try {
		this.paths[i++] = path.getBytes(ENCODING);
	    } catch (UnsupportedEncodingException e) {
		throw new IllegalStateException(ENCODING + " is not supported");
	    }
	}
	
    }

    public static boolean isChild(byte[] prefix, byte[] path) {
	
	// Prefix length should be less than path length
	if(prefix.length >= path.length) {
	    return false;
	}
	
	// If the character in path at prefix length is not
	// a line separator that means this path has no
	// prefix the given one
	if(path[prefix.length] != LINE_SEP) {
	    return false;
	}
	
	// Find last char of '/'
	int i = prefix.length + 1;
	while(i < path.length) {
	    if(path[i] == LINE_SEP) {
		break;
	    }
	    i++;
	}
	
	// If i is equal to path that means no other '/'
	// char was found so we only have to check the
	// characters starting from last prefix
	// char
	if(i < path.length) {
	    return false;
	}
	
	i = prefix.length;
	while(i-- != 0) {
	    if(prefix[i] != path[i]){
		return false;
	    }
	}
	
	return true;
    }
    
    public static boolean isRootPath(byte[] path) {
	// If we find any '/' that means
	// this is not a root path
	int i = 0;
	while(i < path.length) {
	    if(path[i] == LINE_SEP) {
		return false;
	    }
	    i++;
	}
	return true;
    }
    
    
    public static boolean isPrefix(byte[] prefix, byte[] path) {
	
	// Prefix length should be less than path length
	if(prefix.length > path.length) {
	    return false;
	} else if (prefix.length == path.length) {
	    return Arrays.equals(prefix, path);
	}
	
	// If the character in path at prefix length is not
	// a line separator that means this path has no
	// prefix the given one
	if(path[prefix.length] != LINE_SEP) {
	    return false;
	}
	
	// If i is equal to path that means no other '/'
	// char was found so we only have to check the
	// characters starting from last prefix
	// char
	int i = prefix.length;
	while(i-- != 0) {
	    if(prefix[i] != path[i]){
		return false;
	    }
	}
	
	return true;
    }
    
    /**
     *  {@inheritDoc)
     * @see TreeFilter#include(TreeWalk)
     */
    @Override
    public boolean include(TreeWalk walker) throws MissingObjectException,
	    IncorrectObjectTypeException, IOException {
	byte[] path = walker.getRawPath();
	for(byte[] prefix : paths) {
	    if(prefix.length > 0) {
		if(isPrefix(path, prefix) || isChild(prefix, path)) {
		    return true;
		}
	    } else {
		if(isRootPath(path)) {
		    return true;
		}
	    }
	}
	return false;
    }

    /**
     *  {@inheritDoc)
     * @see TreeFilter#shouldBeRecursive()
     */
    @Override
    public boolean shouldBeRecursive() {
	return false;
    }

    /* {@inheritDoc)
     * @see TreeFilter#clone()
     */
    @Override
    public TreeFilter clone() {
	return this;
    }

}
