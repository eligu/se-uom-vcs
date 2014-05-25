package se.uom.vcs.walker.filter.resource.test;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

public class PrefixFilter {

    Set<String> paths;
    
    public PrefixFilter(Set<String> paths) {
	if(paths == null){
	    throw new IllegalArgumentException("paths is null");
	} else if (paths.isEmpty()) {
	    throw new IllegalArgumentException("paths is empty");
	}
	
	this.paths = new HashSet<String>(paths.size());
	this.paths.addAll(paths);
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
	// TODO Auto-generated method stub
    }

    public static class PrefixPath extends Path {

	public PrefixPath(String path) {
	    super(path, EnumSet.of(Path.Flag.PREFIX));
	}
	
	@Override
	public Path and(Path path) {
	    //if(path.)
	    return null;
	}

	@Override
	public Path or(Path path) {
	    // TODO Auto-generated method stub
	    return null;
	}

	@Override
	public Path not(Path path) {
	    // TODO Auto-generated method stub
	    return null;
	}
	
    }
}
