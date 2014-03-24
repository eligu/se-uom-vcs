/**
 * 
 */
package se.uom.vcs.jgit.mocks;

import java.io.File;

import se.uom.vcs.VCSRepository;
import se.uom.vcs.exceptions.VCSRepositoryException;
import se.uom.vcs.jgit.VCSRepositoryImp;

/**
 * Test mock for class {@link VCSRepositoryImp}.<p>
 * 
 * This class represents a repository mock that is stored at {@link #REMOTE_GIT_SMALL}.
 * The repository will be first cloned at {@link #LOCAL_GIT_SMALL} if it is not
 * already there and will load from there.<p>
 * 
 * NOTE: the {@link #REMOTE_GIT_SMALL} is a local repository to because it is stored
 * in a local path within project's test resources, however we refer to it as the remote
 * because we want to check the cloning capabilities of {@link VCSRepository}.
 * <p>
 * 
 * @author Elvis Ligu
 * 
 */
public class RepoMock {

	/**
	 * Path to test resources.<p>
	 */
	public static final String RESOURCES = "src/test/resources/";
	
	/**
	 * Path to a small local git repository.<p>
	 */
	public static final String LOCAL_GIT_SMALL = RESOURCES + "local_small";

	/**
	 * Path to a small 'remote' git repository.<p>
	 */
	public static final String REMOTE_GIT_SMALL = RESOURCES
			+ "remote_small/git-test/.git";

	/**
	 * The small repo.<p>
	 */
	private VCSRepository repoSmall = null;

	/**
	 * @throws VCSRepositoryException
	 * 
	 */
	public RepoMock() throws VCSRepositoryException {

		// There is a bug which prevent a fresh cloned repository to be deleted
		// disable packedGitMMAP
		// Look at http://permalink.gmane.org/gmane.comp.version-control.git/101989
		System.setProperty("jgit.junit.usemmmap", "false");
	}

	public VCSRepository repo() throws VCSRepositoryException {

		if (repoSmall == null) {
			// Create the small repository

			repoSmall = new VCSRepositoryImp(LOCAL_GIT_SMALL, new File(
					REMOTE_GIT_SMALL).getAbsolutePath());
			if (!VCSRepositoryImp.containsGitDir(LOCAL_GIT_SMALL)) {
				repoSmall.cloneRemote();
			} else {
				repoSmall.update();
			}
		}
		
		return repoSmall;
	}
}
