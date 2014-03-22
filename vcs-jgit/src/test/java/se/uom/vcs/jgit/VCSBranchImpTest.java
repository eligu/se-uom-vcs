/**
 * 
 */
package se.uom.vcs.jgit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.List;

import org.junit.Test;

import se.uom.vcs.VCSBranch;
import se.uom.vcs.VCSCommit;
import se.uom.vcs.VCSRepository;
import se.uom.vcs.jgit.VCSBranchImp;
import se.uom.vcs.jgit.mocks.BranchMock;
import se.uom.vcs.jgit.mocks.CommitMock;



/**
 * Unit test for {@link VCSBranchImp}.<p>
 * 
 * @author Elvis Ligu
 *
 */
public class VCSBranchImpTest extends VCSTest {

	/**
	 * Test method for {@link VCSBranchImp#head()}.<p>
	 * 
	 * For each branch in array {@link BranchMock#BRANCHES} will
	 * resolve them and check the head commit.
	 */
	@Test
	public void testHead() throws Exception {
		
		VCSRepository repo = UTILS.smallRepo();
		
		for(BranchMock branchMock : BranchMock.BRANCHES) {
			
			VCSBranch branch = repo.resolveBranch(branchMock.id);
			assertNotNull(branch);
			assertEquals(branchMock.head, branch.head().getID());
			
		}
	}

	/**
	 * Test method for {@link VCSBranchImp#getAllCommits()}.<p>
	 * For each branch in array {@link BranchMock#BRANCHES} will
	 * resolve them, will get commits for each of them and check if they
	 * are equals to expected commits as indicated by {@link BranchMock#commits()}
	 */
	@Test
	public void testGetAllCommits() throws Exception {
		
		VCSRepository repo = UTILS.smallRepo();
		
		for(BranchMock branchMock : BranchMock.BRANCHES) {
			
			VCSBranch branch = repo.resolveBranch(branchMock.id);
			assertNotNull(branch);
			
			List<String> expectedCids = branchMock.commitIds();
			Collection<VCSCommit> commits = branch.getAllCommits();
			assertEquals(expectedCids.size(), commits.size());
			
			for(VCSCommit c : commits) {
				assertTrue(expectedCids.contains(c.getID()));
			}
		}
	}

	/**
	 * Test method for {@link VCSBranchImp#isContained(VCSCommit)}.<p>
	 * 
	 * For each branch in array {@link BranchMock#BRANCHES} will
	 * resolve them and get each commit in {@link CommitMock#COMMITS} that is contained
	 * in the current branch and check it with {@link VCSBranch#isContained(VCSCommit)}.
	 */
	@Test
	public void testIsContained() throws Exception {

		VCSRepository repo = UTILS.smallRepo();
		
		// Check for all branches when isContained must return true
		for(BranchMock branchMock : BranchMock.BRANCHES) {
			
			VCSBranch branch = repo.resolveBranch(branchMock.id);
			assertNotNull(branch);
			
			for(String cid : branchMock.commitIds()) {
				
				VCSCommit commit = repo.resolveCommit(cid);
				assertNotNull(commit);
				assertTrue(branch.isContained(commit));
			}
		}
		
		// Check for all branches when isContained must return false
		for(BranchMock b : BranchMock.BRANCHES_BAD) {
					
			VCSBranch branch = repo.resolveBranch(b.id);
			assertNotNull(branch);
			
			for(String cid : b.commitIds()) {
						
				VCSCommit commit = repo.resolveCommit(cid);
				assertNotNull(commit);
				assertTrue(!branch.isContained(commit));
			}
		}
		
	}
}
