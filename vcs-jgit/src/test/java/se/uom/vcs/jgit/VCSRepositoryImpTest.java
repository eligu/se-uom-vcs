package se.uom.vcs.jgit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import se.uom.vcs.VCSBranch;
import se.uom.vcs.VCSCommit;
import se.uom.vcs.VCSRepository;
import se.uom.vcs.VCSTag;
import se.uom.vcs.exceptions.VCSRepositoryException;
import se.uom.vcs.jgit.VCSRepositoryImp;
import se.uom.vcs.jgit.mocks.BranchMock;
import se.uom.vcs.jgit.mocks.CommitMock;
import se.uom.vcs.jgit.mocks.RepoMock;
import se.uom.vcs.jgit.mocks.TagMock;


/**
 * Unit test for {@link VCSRepositoryImp}.<p>
 * 
 * @author Elvis Ligu
 *
 */
public class VCSRepositoryImpTest extends VCSTest {

	/**
	 * Test method for {@link VCSRepository#cloneRemote()}.<p>
	 * 
	 * Will clone a remote repository to a local one. Then will check if the two
	 * repositories have exactly the same branches and tags. The remote repository is
	 * described at {@link RepoMock}.
	 * 
	 * @throws VCSRepositoryException
	 * @throws IOException
	 */
	@Test
	public void testCloneRemote() throws VCSRepositoryException, IOException {

		String remote = RepoMock.REMOTE_GIT_SMALL;
		String local = RepoMock.RESOURCES + "test_repo_clone";

		// Ensure local path will be deleted first
		FileUtils.deleteDirectory(new File(local));

		VCSRepository localRepo = null;
		VCSRepository remoteRepo = null;

		try {// Create repo and clone remote
			localRepo = new VCSRepositoryImp(local,
					new File(remote).getAbsolutePath());
			localRepo.cloneRemote();

			// Open the remote repository
			remoteRepo = new VCSRepositoryImp(remote, null);

			// Check branches
			Collection<VCSBranch> localBranches = localRepo.getBranches();
			Collection<VCSBranch> remoteBranches = remoteRepo.getBranches();

			// Use the utility method at TestUtils to compare the two
			// lists. Pass as an argument a Comparator which uses equals() for
			// two branches
			boolean branchesEquals = TestUtils.equals(
					localBranches, remoteBranches, new Comparator<VCSBranch>() {

						public int compare(VCSBranch o1, VCSBranch o2) {
							try {
								return (TestUtils.equals(o1, o2) ? 0
										: -1);
							} catch (VCSRepositoryException e) {
								return -1;
							}
						}

					});

			// Check the two collection of branches
			assertTrue("equal branches", branchesEquals);

			// Check tags
			Collection<VCSTag> localTags = localRepo.getTags();
			Collection<VCSTag> remoteTags = remoteRepo.getTags();

			// Use the utility method at TestUtils to compare the two
			// lists. Pass as an argument a Comparator which uses equals() for
			// two tags
			boolean tagsEquals = TestUtils.equals(localTags,
					remoteTags, new Comparator<VCSTag>() {

						public int compare(VCSTag o1, VCSTag o2) {
							try {
								return (TestUtils.equals(o1, o2) ? 0
										: -1);
							} catch (VCSRepositoryException e) {
								return -1;
							}
						}

					});

			// Check the two collection of tags
			assertTrue("equal tags", tagsEquals);

		} finally {
			if (localRepo != null)
				localRepo.close();
			if (remoteRepo != null)
				remoteRepo.close();
			// Clean up directory
			FileUtils.deleteQuietly(new File(local));
		}
	}

	/**
	 * Test method for {@link VCSRepository#getBranches()}.<p>
	 * 
	 * Test all returned branches of local repository {@link RepoMock#LOCAL_GIT_SMALL} if they are as expected.
	 * The branch names comes from {@link BranchMock#SMALL_REPO_BRANCH_NAMES}
	 * 
	 * @throws Exception
	 */
	@Test
	public void testGetBranches() throws Exception {

		// Get local small repository
		VCSRepository repo = UTILS.smallRepo();
		ArrayList<String> names = new ArrayList<String>();

		// Get branches from
		Collection<VCSBranch> branches = repo.getBranches();
		for (VCSBranch branch : branches) {
			names.add(TestUtils.extractName(branch.getID()));
		}

		// Check branches of local small repository if they have same names as
		// those defined in TestUtils
		List<String> expectedNames = new ArrayList<String>();

		for (BranchMock b : BranchMock.BRANCHES) {
			expectedNames.add(b.name);
		}
		assertTrue(TestUtils.equals(names, expectedNames, null));
	}

	/**
	 * Test method for {@link VCSRepository#getTags()}.<p>
	 * 
	 * Test all returned tags of local repository ({@link RepoMock#LOCAL_GIT_SMALL}
	 * if they are as expected.
	 * The tag names comes from {@link TagMock#SMALL_REPO_TAG_NAMES}
	 * 
	 * @throws Exception
	 */
	@Test
	public void testGetTags() throws Exception {

		// Get local small repository
		VCSRepository repo = UTILS.smallRepo();
		ArrayList<String> names = new ArrayList<String>();

		// Get tags from
		Collection<VCSTag> tags = repo.getTags();
		for (VCSTag tag : tags) {
			names.add(TestUtils.extractName(tag.getID()));
		}

		// Check tags of local small repository if they have same names as
		// those defined in TestUtils
		List<String> expectedNames = new ArrayList<String>();

		for (TagMock b : TagMock.TAGS) {
			expectedNames.add(b.name);
		}

		assertTrue(TestUtils.equals(names, expectedNames, null));
	}

	/**
	 * UNIMPLEMENTED.
	 * <p>
	 * 
	 * This test case must make changes to remote repository
	 * {@link RepoMock#REMOTE_GIT_SMALL} and then call
	 * {@link VCSRepository#update()} to check if the updates are ok within
	 * {@link RepoMock#LOCAL_GIT_SMALL}.
	 */
	@Test
	public void testUpdate() {
		// TODO UNIMPLEMENTED
	}

	/**
	 * Test method for {@link VCSRepository#resolveCommit(String).<p>
	 * 
	 * Will resolve the commit IDs from {@link CommitMock#COMMITS}.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testResolveCommit() throws Exception {

		// Get local small repository
		VCSRepository repo = UTILS.smallRepo();

		for (CommitMock cid : CommitMock.COMMITS) {
			VCSCommit commit = repo.resolveCommit(cid.id);
			assertNotNull(commit);
		}
	}

	/**
	 * Test method for {@link VCSRepository#resolveBranch(String)}.<p>
	 * 
	 * Will resolve the branches from {@link BranchMock#BRANCHES}.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testResolveBranch() throws Exception {

		// Get local small repository
		VCSRepository repo = UTILS.smallRepo();

		for (BranchMock bid : BranchMock.BRANCHES) {
			VCSBranch branch = repo.resolveBranch(bid.id);
			assertNotNull(branch);
			assertEquals(bid.id, branch.getID());
			assertEquals(bid.head, branch.head().getID());
		}
		
	}

	/**
	 * Test method for {@link VCSRepository#resolveTag(String)}.<p>
	 * 
	 * Will resolve the tags from {@link TagMock#TAGS}.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testResolveTag() throws Exception {

		// Get local small repository
		VCSRepository repo = UTILS.smallRepo();

		for (TagMock tid : TagMock.TAGS) {
			VCSTag tag = repo.resolveTag(tid.id);
			assertNotNull(tag);
			assertEquals(tid.id, tag.getID());
			assertEquals(tid.commit, tag.getCommit().getID());
		}
	}

	/**
	 * Test method for {@link VCSRepository#getHead()}.<p>
	 *
	 * The head will be the last commit of the default selected branch. The
	 * default selected branch is <code>master</code>, and its commit SHA-1 is
	 * {@link BranchMock#INDEX_MASTER_HEAD}
	 */
	@Test
	public void testGetHead() throws Exception {

		// Get local small repository
		VCSRepository repo = UTILS.smallRepo();

		for(BranchMock b : BranchMock.BRANCHES) {
			
			// Resolve current branch
			VCSBranch branch = repo.resolveBranch(b.id);
			assertNotNull(branch);
			
			// Select the current branch
			repo.selectBranch(branch);
			
			// Check if branch head
			assertEquals(b.head, branch.head().getID());
			
			// Check repo's head
			assertEquals(branch.head().getID(), repo.getHead().getID());
		}
	}

	/**
	 * Test method for {@link VCSRepository#getFirst()}.<p>
	 * 
	 * Test whether the first commit will be returned.
	 */
	@Test
	public void testGetFirst() throws Exception {

		// Get local small repository
		VCSRepository repo = UTILS.smallRepo();

		VCSCommit first = repo.getFirst();

		assertNotNull(first);
		assertEquals(CommitMock.COMMITS[CommitMock.INDEX_FIRST_COMMIT].id,
				first.getID());
	}

	/**
	 * Test method for {@link VCSRepository#getSelectedBranch()}.<p>
	 * The default selected branch will be master.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testGetSelectedBranch() throws Exception {

		// Get local small repository
		VCSRepository repo = UTILS.smallRepo();

		VCSBranch selected = repo.getSelectedBranch();
		assertNotNull(selected);

		// Check the head of master
		VCSCommit head = selected.head();
		assertNotNull(head);

		// Check the head if it is the same as expected
		assertEquals(head.getID(), repo.getHead().getID());
	}

	/**
	 * Test method for {@link VCSRepository#selectBranch(VCSBranch)}.<p>
	 * 
	 * Select each branch and check if it is selected.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testSelectBranch() throws Exception {

		// Get local small repository
		VCSRepository repo = UTILS.smallRepo();

		// For each branch, select it and check if the selected is
		// equal to the selected
		for(BranchMock b : BranchMock.BRANCHES) {
			
			VCSBranch branch = repo.resolveBranch(b.id);
			assertNotNull(branch);
			
			repo.selectBranch(branch);
			
			VCSBranch selected = repo.getSelectedBranch();
			assertNotNull(selected);
			
			assertEquals(b.id, selected.getID());
			assertEquals(b.head, selected.head().getID());
		}
	}

}
