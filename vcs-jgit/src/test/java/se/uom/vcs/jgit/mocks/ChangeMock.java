package se.uom.vcs.jgit.mocks;

import java.util.Arrays;
import java.util.List;

import se.uom.vcs.VCSChange;
import se.uom.vcs.VCSCommit;
import se.uom.vcs.jgit.VCSChangeImp;

/**
 * Test mock for class {@link VCSChangeImp}.<p>
 * 
 * Each mock have a one to one relation with a {@link VCSChange} that is
 * they will have all the fields specified by that class. Additionally we specify
 * here a {@link ChangeSet} class that contains a set of change mocks to be used.
 * <p>
 * Each change set contains mocks that represents a diff (see {@link VCSCommit#getChanges(VCSCommit)} 
 * from old to new commit. There are three sets:
 * <ul>
 * 	<li>{@link #CHANGES1}</li>
 * 	<li>{@link #CHANGES2}</li>
 * 	<li>{@link #CHANGES3}</li>
 * </ul>
 * 
 * @author Elvis Ligu
 * 
 */
public class ChangeMock {
	
	/**
	 * A class representing a set of changes.<p>
	 */
	public static class ChangeSet {
		/** The new commit SHA-1 */
		public final String newCommit;
		/** The old commit SHA-1 */
		public final String oldCommit;
		/** Array of change mocks this set contains */
		public final ChangeMock[] changes;
		/** Array of paths to limit the change's result */
		public final String[] paths;
		
		public ChangeSet(String oc, String nc, String[] paths, ChangeMock... set) {
			this.newCommit = nc;
			this.oldCommit = oc;
			this.changes = set;
			this.paths = paths;
		}
		
		/**
		 * @return The change mocks this set contains
		 */
		public List<ChangeMock> changes() {
			return Arrays.asList(changes);
		}
	}
	
	/**
	 * Is used as a reference to store the length of {@link CommitMock#COMMITS}.<p>
	 * All commits in the array are stored from new to old, and if we make other commits
	 * in repository, the commit references that each mock have will remain the same,
	 * because they are referred to as the distance from the length of the array.
	 */
	private static final int len = CommitMock.COMMITS.length;
	
	/**
	 * A set of changes type {@link VCSChange.Type#ADDED} from first to length-5 commit in {@link CommitMock#COMMITS}.<p>
	 */
	public static final ChangeSet CHANGES1 = new ChangeSet(
			
			CommitMock.COMMITS[CommitMock.INDEX_FIRST_COMMIT].id/* old commit */, 
			CommitMock.COMMITS[len-5].id 							/* new commit */,
			null, 												/* no paths to limit */
			
			/* Array of changes */
			new ChangeMock(null, null, "test_2.txt", CommitMock.COMMITS[len-5].id, VCSChange.Type.ADDED),
			new ChangeMock(null, null, "test_3.txt", CommitMock.COMMITS[len-5].id, VCSChange.Type.ADDED)
			);
	
	/**
	 * A set of changes from length-5 commit to master head in {@link CommitMock#COMMITS}.<p>
	 */
	public static final ChangeSet CHANGES2 = new ChangeSet (
			CommitMock.COMMITS[len-5].id 							/* old commit */,
			CommitMock.COMMITS[BranchMock.INDEX_MASTER_HEAD].id /* new commit */,
			null, 												/* no paths to limit */
			
			/* Array of changes */
			new ChangeMock("src/Test.txt", CommitMock.COMMITS[3].id, null, null, VCSChange.Type.DELETED),
			new ChangeMock(null, null, "src/Test_renamed.txt", CommitMock.COMMITS[BranchMock.INDEX_MASTER_HEAD].id, VCSChange.Type.ADDED),
			new ChangeMock("test_2.txt", CommitMock.COMMITS[3].id, null, null, VCSChange.Type.DELETED),
			new ChangeMock("test_3.txt", CommitMock.COMMITS[3].id, "test_3.txt",  CommitMock.COMMITS[BranchMock.INDEX_MASTER_HEAD].id, VCSChange.Type.MODIFIED)
			);
	
	/**
	 * A set of changes from length-5 commit to master head in {@link CommitMock#COMMITS}.<p>
	 */
	public static final ChangeSet CHANGES3 = new ChangeSet (
			CommitMock.COMMITS[len-5].id 							/* old commit */,
			CommitMock.COMMITS[BranchMock.INDEX_MASTER_HEAD].id /* new commit */,
			new String[]{"src/Test.txt", "test_3.txt"},			/* paths to limit */
			
			/* Array of changes */
			new ChangeMock("src/Test.txt", CommitMock.COMMITS[len-5].id, null, null, VCSChange.Type.DELETED),
			new ChangeMock("test_3.txt", CommitMock.COMMITS[len-5].id, "test_3.txt",  CommitMock.COMMITS[BranchMock.INDEX_MASTER_HEAD].id, VCSChange.Type.MODIFIED)
			);
	
	/**
	 * Old path.<p>
	 */
	public final String oldPath;
	
	/**
	 * New path.<p>
	 */
	public final String newPath;
	
	/**
	 * Old commit.<p>
	 */
	public String oldCommit;
	
	/**
	 * New commit.<p>
	 */
	public String newCommit;
	
	/**
	 * Change type.<p>
	 */
	public final VCSChange.Type type;

	public ChangeMock(String op, String oc, String np, String nc,
			VCSChange.Type type) {
		oldPath = op;
		newPath = np;
		oldCommit = oc;
		newCommit = nc;
		this.type = type;
	}
}