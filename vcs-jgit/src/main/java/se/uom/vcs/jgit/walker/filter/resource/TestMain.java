package se.uom.vcs.jgit.walker.filter.resource;

import java.io.IOException;
import java.net.URISyntaxException;

import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;

import se.uom.vcs.VCSResource;
import se.uom.vcs.jgit.TreeUtils;
import se.uom.vcs.jgit.VCSRepositoryImp;
import se.uom.vcs.walker.filter.resource.AbstractResourceFilter;
import se.uom.vcs.walker.filter.resource.ResourceFilterFactory;

public class TestMain {

    public TestMain() {
	// TODO Auto-generated constructor stub
    }

    /**
     * @param args
     * @throws URISyntaxException 
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException, URISyntaxException {
	ResourceFilterFactory filters = new ResourceFilterFactory();
	    
	@SuppressWarnings("unchecked")
	AbstractResourceFilter<VCSResource> filter = 
		(AbstractResourceFilter<VCSResource>) filters.or(
	// allow all files under vcs-jgit/src/test
	filters.and(filters.prefix("vcs-jgit/src/test")),
	// allow files under vcs-api that ends with .xml
	filters.and(filters.childFilter("vcs-api"), filters.suffix(".xml")),
	// or paths
	filters.path("vcs-api/src/main/java/se/uom/vcs/Edit.java", "vcs-api/src/main/java/se/uom/vcs/VCSBranch.java"),
	// or children of java/path that ends with .txt
	filters.and(filters.childFilter("vcs-jgit"), filters.suffix(".xml")));
	
	ResourceFilter rf = new ResourceFilter();
	OptimizedResourceFilter<VCSResource> ofilter = rf.parse(filter, null);
	
	Repository repo = VCSRepositoryImp.openRepo("C:\\Users\\elvis\\Desktop\\vcs");
	RevWalk rWalk = new RevWalk(repo);
	RevCommit rCommit = rWalk.parseCommit(repo.resolve("5864d481e6aeb70367f86c6c1b68118e1e5f1175"));
	TreeWalk treeWalk = TreeUtils.createTreeWalk(rCommit, repo, false);
	treeWalk.setFilter(ofilter.getCurrent());
	
	while(treeWalk.next()) {
	    System.out.println(treeWalk.getPathString());
	    if(treeWalk.isSubtree()) {
		treeWalk.enterSubtree();
	    }
	}
	
    }

}
