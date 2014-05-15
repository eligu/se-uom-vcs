package se.uom.vcs.jgit;

import java.util.Arrays;

import se.uom.vcs.VCSRepository;
import se.uom.vcs.jgit.walker.filter.resource.JGitChildFilter;

public class Main {

    private static final String REMOTE = "https://github.com/TooTallNate/Java-WebSocket.git";
    private static final String LOCAL = System.getProperty("user.home") + "/Desktop/Java-WebSocket";
    private static VCSRepository repo = null;
    
    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
	
	String path = "java/path";
	byte[] bytes = path.getBytes("UTF-8");
	System.out.println(JGitChildFilter.isPrefix("java".getBytes("UTF-8"), bytes));
	System.out.println(JGitChildFilter.isChild("java".getBytes("UTF-8"), bytes));
	System.out.println(JGitChildFilter.isRootPath("path".getBytes("UTF-8")));
	System.out.println(path.length());
	System.out.println(bytes.length);
	
	/*
	init();
	if(!isAvailable()) {
	    cloneRepo();
	}
	
	VCSCommit newC  = repo.resolveCommit("60425255a37b1ee95260ed54b6d13925c05d8225");
	VCSCommit oldC = repo.resolveCommit("aef92290adbd53850337cfc2c651699764988038");
	
	// Filter for only .xml files
	long start = System.nanoTime();
	final ResourceFilter rfutils = FilterUtils.resource();
	final List<VCSResourceFilter<VCSResource>> filters = new ArrayList<VCSResourceFilter<VCSResource>>();
	final ResourceFilterFactory filterFactory = new ResourceFilterFactory();
	
	filters.add(filterFactory.fileName(".*.java"));
	System.out.println(Pattern.compile(".*.java").matcher("a.java").matches());
	filters.add(filterFactory.path("src/main/java/org/java_websocket/client/DefaultWebSocketClientFactory.java",
		"src/main/java/org/java_websocket/client/WebSocketClient.java"));
	filters.add(filterFactory.prefix(
		"src/main/java/org/java_websocket/"));
	//filters.add(rfutils.modification());
	
	FilteredVisitor<VCSFileDiff<?>> visitor = new FilteredVisitor<VCSFileDiff<?>>() {

	    @Override
	    public boolean visit(VCSFileDiff<?> entity) {
		
		System.out.println(entity);
		System.out.println(entity.getEdits());
		return true;
	    }

	    @Override
	    public VCSCommitFilter getCommitFilter() {
		return null;
	    }

	    @Override
	    public VCSResourceFilter<VCSResource> getResourceFilter() {
		//return rfutils.modification();
		return filterFactory.and(filters);
	    }

	    @Override
	    public VCSFilter getEntityFilter() {
		return null;
	    }
	    
	};
	
	newC.walkFileChanges(oldC, visitor);
	
	System.out.println("\n" + (System.nanoTime() - start)/(1000.0*1000*1000));
	*/
	
    }
    
    static boolean isPrefix(byte[] prefix, byte[] path) {
	if(prefix.length > path.length) {
	    return false;
	} else if (prefix.length == path.length) {
	    return Arrays.equals(prefix, path);
	}
	
	byte line_sep = 0x2f;
	
	if(path[prefix.length] != line_sep) {
	    return false;
	}
	
	int i = prefix.length;
	while(i-- != 0) {
	    if(prefix[i] != path[i]){
		return false;
	    }
	}
	
	return true;
    }
    
    static void init() throws Exception {
	repo = new VCSRepositoryImp(LOCAL, REMOTE);
    }
    
    static void cloneRepo() throws Exception {
	repo.cloneRemote();
    }
    
    static boolean isAvailable() {
	return VCSRepositoryImp.containsGitDir(LOCAL);
    }
}
