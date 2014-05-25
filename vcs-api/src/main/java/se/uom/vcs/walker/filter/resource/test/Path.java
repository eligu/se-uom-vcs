package se.uom.vcs.walker.filter.resource.test;

import java.util.EnumSet;


public abstract class Path {

    EnumSet<Flag> flags;
    String path = null;
    
    public Path(String path, EnumSet<Flag> flags) {
	if(path == null) {
	    throw new IllegalArgumentException("path is null");
	}
	if(flags == null) {
	    throw new IllegalArgumentException("flags is null");
	}
	
	for(Flag flag : flags) {
	    if(flag.conflicts.contains(flag)){
		throw new IllegalArgumentException("path flag conflicts: " + flags);
	    }
	}
	
	this.path = path;
	this.flags = flags;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
    }

    public static enum Flag {
	MODIFIED,
	PREFIX,
	SUFFIX,
	PATH,
	CHILD;
	
	Flag() {
	    this.conflicts = getConflicts(this);
	}
	
	private static EnumSet<Flag> getConflicts(final Flag flag) {
	    switch(flag) {
	    case MODIFIED :
		return EnumSet.noneOf(Flag.class);
	    default:
		EnumSet<Flag> set = EnumSet.allOf(Flag.class);
		set.remove(MODIFIED);
		set.remove(flag);
		return set;
	    }
	}
	
	protected final EnumSet<Flag> conflicts;
	
	public EnumSet<Flag> conflicts() {
	    return conflicts;
	}
    }
    
    public static final Path MODIFIED = new Path(">", EnumSet.of(Flag.MODIFIED)) {

	@Override
	public Path and(Path path) {
	    
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
	
    };
    
    public abstract Path and(Path path);
    public abstract Path or(Path path);
    public abstract Path not(Path path);
}
