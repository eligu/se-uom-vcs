/**
 * 
 */
package se.uom.vcs.jgit;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand.ListMode;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevSort;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.AndTreeFilter;
import org.eclipse.jgit.treewalk.filter.PathFilterGroup;
import org.eclipse.jgit.treewalk.filter.TreeFilter;

import se.uom.vcs.VCSChange;
import se.uom.vcs.VCSCommit;
import se.uom.vcs.VCSDirectory;
import se.uom.vcs.VCSFileDiff;
import se.uom.vcs.VCSResource;
import se.uom.vcs.exceptions.VCSRepositoryException;
import se.uom.vcs.exceptions.VCSResourceNotFound;
import se.uom.vcs.jgit.walker.DiffCollector;
import se.uom.vcs.walker.ChangeVisitor;
import se.uom.vcs.walker.FileDirVisitor;
import se.uom.vcs.walker.ModifyingPathVisitor;
import se.uom.vcs.walker.PathLimitingVisitor;
import se.uom.vcs.walker.Visitor;

/**
 * Implementation of {@link VCSCommit} based on JGit library.
 * <p>
 * 
 * @author Elvis
 * @since 0.0.1
 * @version 0.0.1
 * @see VCSCommit
 */
public class VCSCommitImp implements VCSCommit {

	/**
	 * JGit commit that is linked to this commit.
	 * <p>
	 */
	protected RevCommit commit;

	/**
	 * The author that originated the commit.
	 * <p>
	 * 
	 * This field is for caching purposes
	 */
	protected PersonIdent author;

	/**
	 * JGit repository from where this commit comes from.
	 * <p>
	 */
	protected Repository repo;

	/**
	 * Keep cached the next commits of this one.
	 * <p>
	 * 
	 * In order to find next commits (children) we have to parse all heads that
	 * this commit is merged into, and walk all of them until we reach this
	 * commit. So it is a better idea to keep the children cached. If children
	 * collection is null that means they are not calculated, if it is not, they
	 * are calculated so don't calculate them.
	 */
	protected List<VCSCommit> children = null;

	/**
	 * Creates a commit that is linked to a JGit commit and repository.
	 * <p>
	 * 
	 * @param commit
	 *            must not be null
	 * @param repository
	 *            must not be null
	 */
	public VCSCommitImp(final RevCommit commit, final Repository repository) {

		ArgsCheck.notNull("commit", commit);
		ArgsCheck.notNull("repository", repository);

		// We need to parse again this commit so we can ensure all his parents and children will be ok
		RevWalk walk = null;
		try {
			walk = new RevWalk(repository);
			this.commit = walk.parseCommit(commit);
		} catch (final IOException e) {
			throw new IllegalStateException("revision " + commit.getName() + " can not be parsed");
		} finally {
			if(walk != null) {
				walk.release();
			}
		}
		this.repo = repository;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * {@link RevCommit#getName()} is used as the id of this resource.
	 * 
	 * @return SHA-1 of this commit id as specified by Git
	 */
	@Override
	public String getID() {
		return this.commit.getName();
	}

	/**
	 * If the field {@link #author} is null this will create it from
	 * {@link #commit} for the first time this method will be called and return
	 * the author.
	 * 
	 * @return authors email
	 */
	public String getAuthor() {
		// getAuthorIdent() is a very resource expensive method
		// thus we keep author cached so next time it will be needed
		// it will remain within this commit
		if (this.author == null) {
			this.author = this.commit.getAuthorIdent();
		}
		return this.author.getEmailAddress();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getMessage() {
		return this.commit.getFullMessage();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getCommiter() {
		return this.commit.getCommitterIdent().getEmailAddress();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Date getDate() {
		return this.commit.getCommitterIdent().getWhen();
	}

	/**
	 * Convert to {@link VCSChange#Type}
	 * 
	 * @param 
	 * 		type type of change
	 * @return
	 * 		a {@link VCSChange.Type}
	 */
	static VCSChange.Type changeType(final ChangeType type) {

		switch (type) {
		case ADD:
			return VCSChange.Type.ADDED;
		case DELETE:
			return VCSChange.Type.DELETED;
		case COPY:
			return VCSChange.Type.COPIED;
		case MODIFY:
			return VCSChange.Type.MODIFIED;
		case RENAME:
			return VCSChange.Type.RENAMED;
		default:
			return VCSChange.Type.NONE;
		}
	}

	/**
	 * Convert to {@link Resource#Type}
	 * 
	 * @param 
	 * 		mode of file
	 * @return
	 * 		the type of resource
	 */
	static VCSResource.Type resourceType(final FileMode mode) {

		if (mode.equals(FileMode.REGULAR_FILE.getBits())
				|| mode.equals(FileMode.EXECUTABLE_FILE.getBits())) {
			return VCSResource.Type.FILE;
		} else if (mode.equals(FileMode.TREE.getBits())) {
			return VCSResource.Type.DIR;
		}

		return VCSResource.Type.NONE;
	}

	/**
	 * Given a JGit DiffEntry and the corresponding commits it was produced, it
	 * will create a VCSChange.
	 * <p>
	 * 
	 * This method will return null if entry doesn't correspond to a normal file
	 * or a directory.
	 * 
	 * @param entry
	 *            the diff
	 * @param oldC
	 *            the old commit
	 * @param newC
	 *            the new commit
	 * @return 
	 * 		a {@link VCSChange} if entry refers to a directory or a
	 *      normal/executable file, null if not
	 * @see 
	 * 		DiffEntry#ChangeType
	 * @see 
	 * 		#createDirChange(DiffEntry, VCSCommitImp, VCSCommitImp)
	 * @see 
	 * 		#createFileChange(DiffEntry, VCSCommitImp, VCSCommitImp)
	 */
	static VCSChange<?> createChange(
			final DiffEntry entry,
			final VCSCommitImp oldC, 
			final VCSCommitImp newC) {

		VCSChange<?> change = null;

		// case for directory
		if (isDirDiff(entry)) {

			change = createDirChange(entry, oldC, newC);

			// case for file
		} else if (isFileDiff(entry)) {

			change = createFileChange(entry, oldC, newC);
		}

		return change;
	}

	/**
	 * If entry's mode is {@link FileMode#TREE} then this will return true.
	 * <p>
	 * 
	 * @param 
	 * 		entry to check if it its a directory
	 * @return
	 * 		true if entry is referred to a directory
	 */
	static boolean isDirDiff(final DiffEntry entry) {

		final ChangeType type = entry.getChangeType();

		// if this is an add then we have to check only the new file mode
		// if this is a delete then we have to check only the old file mode
		if (type.equals(DiffEntry.ChangeType.ADD)) {

			return isDirMode(entry.getNewMode());

		} else if (type.equals(DiffEntry.ChangeType.DELETE)) {

			return isDirMode(entry.getOldMode());
		}

		// if there is modified/renamed/copied we must check both old and new
		// modes
		return isDirMode(entry.getNewMode()) || isDirMode(entry.getOldMode());
	}

	/**
	 * If entry's mode is {@link FileMode#EXECUTABLE_FILE} or
	 * {@link FileMode#REGULAR_FILE} then this will return true.
	 * <p>
	 * 
	 * @param 
	 * 		entry to check if its a file
	 * @return
	 * 		true if entry is referred to a file
	 */
	static boolean isFileDiff(final DiffEntry entry) {

		final ChangeType type = entry.getChangeType();

		// if this is an add then we have to check only the new file mode
		// if this is a delete then we have to check only the old file mode
		if (type.equals(DiffEntry.ChangeType.ADD)) {

			return isFileMode(entry.getNewMode());

		} else if (type.equals(DiffEntry.ChangeType.DELETE)) {

			return isFileMode(entry.getOldMode());
		}

		// if there is modified/renamed/copied we must check both old and new
		// modes
		return isFileMode(entry.getNewMode()) || isFileMode(entry.getOldMode());
	}

	/**
	 * If entry's mode is {@link FileMode#EXECUTABLE_FILE} or
	 * {@link FileMode#REGULAR_FILE} then this will return true.
	 * <p>
	 * 
	 * @param 
	 * 		mode to check if it is a file
	 * @return
	 * 		true if this mode is a file
	 */
	static boolean isFileMode(final FileMode mode) {
		return mode.equals(FileMode.EXECUTABLE_FILE.getBits())
				|| mode.equals(FileMode.REGULAR_FILE.getBits());
	}

	/**
	 * Return true only if this is a {@link FileMode#TREE} mode.
	 * <p>
	 * 
	 * @param 
	 * 		mode to check if this is a directory
	 * @return
	 * 		true if this mode is a directory
	 */
	static boolean isDirMode(final FileMode mode) {
		return mode.equals(FileMode.TREE.getBits());
	}

	/**
	 * Given a {@link DiffEntry} and the commits this diff was produced for,
	 * return a {@link VCSFileDiffImp}.
	 * <p>
	 * 
	 * <b>WARNING:</b> this method may fail if {@link #isFileMode(FileMode)} returns false.
	 * 
	 * @param entry
	 *            the diff entry
	 * @param oldC
	 *            old commit
	 * @param newC
	 *            new commit
	 * @return
	 * 		the corresponding {@link VCSFileDiff} of this entry
	 */
	static VCSFileDiffImp<VCSFileImp> createFileChange(final DiffEntry entry,
			final VCSCommitImp oldC, final VCSCommitImp newC) {

		VCSResource oldR = null;
		VCSResource newR = null;

		final VCSChange.Type chType = changeType(entry.getChangeType());

		// if added then there is no need for the old resource
		// so creates the old resource only if this is not an addition
		if (!chType.isAdd()) {
			oldR = new VCSFileImp(oldC, entry.getOldPath());
		}

		// if removed then there is no need for the new resource
		// so creates the new resource only if this is not a deletion
		if (!chType.isDelete()) {
			newR = new VCSFileImp(newC, entry.getNewPath());
		}

		return new VCSFileDiffImp<VCSFileImp>((VCSFileImp) newR,
				(VCSFileImp) oldR, chType);
	}

	/**
	 * Given a {@link DiffEntry} and the commits this diff was produced for,
	 * return a {@link VCSDirectoryImp}.
	 * <p>
	 * 
	 * <b>WARNING:</b> this method may fail if {@link #isDirMode(FileMode)} returns false.
	 * @param entry
	 *            the dif entry
	 * @param oldC
	 *            old commit
	 * @param newC
	 *            new commit
	 * @return
	 * 		the corresponding {@link VCSDirectory} of this entry
	 */
	static VCSChange<?> createDirChange(final DiffEntry entry,
			final VCSCommitImp oldC, final VCSCommitImp newC) {

		VCSResource oldR = null;
		VCSResource newR = null;

		final VCSChange.Type chType = changeType(entry.getChangeType());

		// if added then there is no need for the old resource
		if (!chType.isAdd()) {
			oldR = new VCSDirectoryImp(oldC, entry.getOldPath());
		}

		// if removed then there is no need for the new resource
		if (!chType.isDelete()) {
			newR = new VCSDirectoryImp(newC, entry.getNewPath());
		}

		return new VCSChangeImp<VCSResourceImp>((VCSResourceImp) newR,
				(VCSResourceImp) oldR, chType);
	}

	/**
	 * Check the two commits if they are from VCSCommitImp so we can have access
	 * to their {@link RevCommit} object.
	 * <p>
	 * 
	 * If not throw an {@link IllegalArgumentException}. If two commits are
	 * equal return null, otherwise return the new commit at index 0 and the old
	 * one at 1;
	 * 
	 * @param 
	 * 		commit1 the first commit
	 * @param 
	 * 		commit2 the second commit
	 * @return 
	 * 		an array that contains the new commit at index 0, and the old one
	 *      at index 1
	 */
	private static VCSCommitImp[] checkCommitsForDiffAndReturnNewOld(
			final VCSCommit commit1, final VCSCommit commit2) {

		ArgsCheck.isSubtype("commit1", VCSCommitImp.class, commit1);
		ArgsCheck.isSubtype("commit2", VCSCommitImp.class, commit2);

		// This array will have the new commit in position 0 and the old one in
		// position 1
		final VCSCommitImp[] commits = new VCSCommitImp[2];
		final short OLD = 1;
		final short NEW = 0;

		// Start by assuming commit1 is the new and commit2 the old
		commits[NEW] = (VCSCommitImp) commit1;
		commits[OLD] = (VCSCommitImp) commit2;

		// If commits are equals return null
		if (AnyObjectId.equals(commits[OLD].commit, commits[NEW].commit)) {
			return null;
		}

		// If commits[0] (at position new) is older than commits[1] (at position
		// old) swap the values
		if (commits[OLD].commit.getCommitTime() > commits[NEW].commit
				.getCommitTime()) {

			final VCSCommitImp temp = commits[NEW]; // the old one
			commits[NEW] = commits[OLD]; // assign the old to new
			commits[OLD] = temp;
		}

		return commits;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void walkChanges(
		final VCSCommit commit, 
		final ChangeVisitor<VCSChange<?>> visitor) 
					throws 
					VCSRepositoryException {

		// The new commit is at position 0 and the old one at position 1
		// if commits is null that means the old and the new are equal so no
		// need to walk changes
		final VCSCommitImp[] commits = checkCommitsForDiffAndReturnNewOld(commit,
				this);
		if (commits == null) {
			return;
		}

		final short OLD = 1;
		final short NEW = 0;

		// Check visitor to see if it is an instance of PathLimitingVisitor
		// if so get the paths from visitor to limit the walk on diffs
		Collection<String> paths = null;
		
		if (PathLimitingVisitor.class.isAssignableFrom(visitor.getClass())) {
			paths = ((PathLimitingVisitor<VCSChange<?>>) visitor).getPaths();
		}

		// Use Diff collector to collect diffs
		// Limit the diff only to the specified paths if any
		final DiffCollector<DiffEntry> diffs = new DiffCollector<DiffEntry>(
				this.repo, commits[OLD].commit, commits[NEW].commit);
		if ((paths != null) && !paths.isEmpty()) {
			diffs.setPathFilters(paths.toArray(new String[0]));
		}

		// Run diff entries until there are no more entries and visitor accept
		// each current entry
		// Create a VCSChange for each diff entry, if entry refers to TREE (aka
		// directory)
		// or REGULAR_FILE/EXECUTABLE_FILE, skip all others such as symlink and
		// other repository
		for (final DiffEntry entry : diffs) {

			final VCSChange<?> change = createChange(entry, commits[OLD],
					commits[NEW]);

			if (change != null) {
				// if the visitor returns false then we must stop the iteration
				if (!visitor.visit(change)) {
					break;
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<VCSChange<?>> getChanges(final VCSCommit commit)
			throws VCSRepositoryException {

		return this.getChanges(commit, true, (String[]) null);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * <b>WARNING:</b> this method will never consider <code>recursive</code> argument
	 * because Git doesn't keep track of directories. So it will probably not
	 * return any diff for a directory.
	 */
	@Override
	public List<VCSChange<?>> getChanges(
			final VCSCommit commit,
			final boolean recursive, 
			final String... paths)
					throws 
					VCSRepositoryException {

		ArgsCheck.notNull("commit", commit);

		/*
		 * We will use walkChanges to collect all changes. This will add an
		 * additional computation for each change (visit() will be use for each
		 * change) however this is preferable because we don't want to repeat
		 * code here
		 */

		// The list where we collect all changes
		final List<VCSChange<?>> changes = new ArrayList<VCSChange<?>>();

		// The visitor to use to collect changes
		final Visitor<VCSChange<?>> visitor = new PathLimitingVisitor<VCSChange<?>>() {

			/**
			 * Get each change and add it to changes list
			 * 
			 * @param entity
			 * @return
			 */
			@Override
			public boolean visit(final VCSChange<?> entity) {
				changes.add(entity);
				return true;
			}

			@Override
			public Collection<String> getPaths() {
				if (paths == null) {
					return null;
				} else {
					ArgsCheck.containsNoNull("paths", (Object[])paths);
					return Arrays.asList(paths);
				}
			}

		};

		// Run walkChanges to collect changes
		this.walkChanges(commit, visitor);

		return changes;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * <p>
	 * WARNING: this method will never consider <code>recursive</code> argument
	 * because Git doesn't keep track of directories. So it will probably not
	 * return any diff for a directory.
	 */
	@Override
	public void walkFileChanges(
			final VCSCommit commit,
			final Visitor<VCSFileDiff<?>> visitor)
					throws 
					VCSRepositoryException {

		ArgsCheck.notNull("commit", commit);
		ArgsCheck.notNull("visitor", commit);
		/*
		 * We will use walkChanges to collect all changes. This will add an
		 * additional computation for each change (visit() will be use for each
		 * change and check if each change is a file change) however this is
		 * preferable because we don't want to repeat code here
		 */

		// The visitor to use to collect changes
		final Visitor<VCSChange<?>> walkVisitor = new PathLimitingVisitor<VCSChange<?>>() {

			/**
			 * Get each change and add it to changes list
			 * 
			 * @param entity
			 * @return
			 */
			@Override
			public boolean visit(final VCSChange<?> entity) {
				if (VCSFileDiffImp.class.isAssignableFrom(entity.getClass())) {
					return visitor.visit((VCSFileDiff<?>) entity);
				}
				return true;
			}

			@Override
			public Collection<String> getPaths() {
				if (!PathLimitingVisitor.class.isAssignableFrom(visitor
						.getClass())) {
					return null;
				} else {
					return ((PathLimitingVisitor<VCSFileDiff<?>>) visitor)
							.getPaths();
				}
			}
		};

		// Run walkChanges to collect changes
		this.walkChanges(commit, walkVisitor);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<VCSFileDiff<?>> getFileChanges(final VCSCommit commit)
			throws VCSRepositoryException {

		return this.getFileChanges(commit, true, (String[]) null);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * <b>WARNING:</b>: this method will never consider <code>recursive</code> argument
	 * because Git doesn't keep track of directories. However whether this will
	 * include file changes under the specified paths (if they are dirs) this is
	 * lied at {@link org.eclipse.jgit.api.DiffCommand} implementation of JGit.
	 */
	@Override
	public List<VCSFileDiff<?>> getFileChanges(final VCSCommit commit,
			final boolean recursive, final String... paths)
					throws VCSRepositoryException {

		ArgsCheck.notNull("commit", commit);

		/*
		 * We will use walkChanges to collect all changes. This will add an
		 * additional computation for each change (visit() will be use for each
		 * change) however this is preferable because we don't want to repeat
		 * code here
		 */

		// The list where we collect all changes
		final List<VCSFileDiff<?>> changes = new ArrayList<VCSFileDiff<?>>();

		// The visitor to use to collect changes
		final Visitor<VCSChange<?>> visitor = new PathLimitingVisitor<VCSChange<?>>() {

			/**
			 * Get each change and add it to changes list
			 * 
			 * @param entity
			 * @return
			 */
			@Override
			public boolean visit(final VCSChange<?> entity) {
				if (VCSFileDiffImp.class.isAssignableFrom(entity.getClass())) {
					changes.add((VCSFileDiff<?>) entity);
				}
				return true;
			}

			@Override
			public Collection<String> getPaths() {
				if (paths == null) {
					return null;
				} else {
					return Arrays.asList(paths);
				}
			}

		};

		// Run walkChanges to collect changes
		this.walkChanges(commit, visitor);

		return changes;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isMergeCommit() {
		return this.commit.getParentCount() > 1;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<VCSCommit> getNext() throws VCSRepositoryException {

		// Check first if children are previously calculated
		// if so return them, otherwise calculate them
		if (this.children != null) {
			return this.children;
		}

		this.children = new ArrayList<VCSCommit>();
		RevWalk revWalk = null;
		// TODO check for accuracy if this implementation already return all
		// child commits
		try {

			// Check first if this commit is a head that has no children, if true then
			// getMergingHeads will return an empty list
			final List<RevCommit> heads = this.getMergingHeads();
			if (heads.isEmpty()) {
				return this.children;
			}

			// Walker to walk the commits until we find this one
			revWalk = new RevWalk(this.repo);

			// The current commit that we need to know its children
			final RevCommit required = revWalk.parseCommit(this.commit);

			// The walking will start from each head we found
			revWalk.markStart(heads);

			// The sorting here is important
			// We set sorting topology so the children will be parsed first
			// And the commit walking descending so the newest first
			revWalk.sort(RevSort.TOPO, true);
			revWalk.sort(RevSort.COMMIT_TIME_DESC, true);

			// Start from the current commit, the first one probably will be a
			// head
			// until the current commit is equal to this commit.
			// For each commit that we parse check if it has parent this commit
			RevCommit current = null;
			while (((current = revWalk.next()) != null)
					&& !AnyObjectId.equals(current, required)) {

				// If required (this commit) is parent of current
				// we found a child
				if (isParent(required, current)) {
					this.children.add(new VCSCommitImp(current, this.repo));
				}
			}

			// We must ensure no one can modify our cache
			return Collections.unmodifiableCollection(this.children);

		} catch (final IOException e) {
			throw new VCSRepositoryException(e);
		} catch (final GitAPIException e) {
			throw new VCSRepositoryException(e);
		} finally {
			if(revWalk != null) {
				revWalk.release();
			}
		}
	}

	/**
	 * Check all the branches of repository this commit belongs to, and find all
	 * heads this commit is merged into.
	 * 
	 * @return the heads this commit is reachable from, if empty that means this commit is a head which has no children
	 * @throws GitAPIException
	 * @throws IOException
	 * @throws VCSRepositoryException 
	 */
	private List<RevCommit> getMergingHeads()
			throws GitAPIException, IOException, VCSRepositoryException {

		final List<Ref> refs = new Git(this.repo).branchList()
				.setListMode(ListMode.ALL).call();
		final RevWalk walk = new RevWalk(this.repo);
		final RevCommit base = walk.parseCommit(this.commit);
		final List<RevCommit> heads = new ArrayList<RevCommit>();

		for (final Ref ref : refs) {

			final RevCommit head = walk.parseCommit(ref.getObjectId());

			// Case when this commit is head
			if (AnyObjectId.equals(head, base)) {
				continue;
			}

			// If this commit is newer then the current head
			// do nothing else if is ancestor of head add it to list
			if (isAncestor(base, head, this.repo)) {
				heads.add(head);
			}
		}

		return heads;

	}

	/**
	 * Check parents of the child if one of them is equal to specified parent return true.<p>
	 * 
	 * @param 
	 * 		parent to check if its a parent of child
	 * @param 
	 * 		child to check if parent is its parent
	 * @return
	 * 		true if <code>parent</code> is a parent of <code>child</code>
	 */
	static boolean isParent(final RevCommit parent, final RevCommit child) {

		for (final RevCommit p : child.getParents()) {
			if (AnyObjectId.equals(parent, p)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Check commit1 is ancestor of commit2.<p>
	 * 
	 * @param commit1 the ancestor to check
	 * @param commit2 the descendant of commit2
	 * @param repo from where these commits comes from
	 * @return true if commit1 is ancestor of commit2
	 * @throws VCSRepositoryException
	 */
	static boolean isAncestor(final RevCommit commit1, final RevCommit commit2, final Repository repo) throws VCSRepositoryException {

		// Create a revision walk and check if commit1
		// is reachable from commit2
		final RevWalk walk = new RevWalk(repo);

		try {

			final RevCommit bHEAD = walk.parseCommit(commit2.getId());
			final RevCommit rCommit = walk.parseCommit(commit1);

			// Check first if this commit is equal to head
			if (AnyObjectId.equals(rCommit, bHEAD)) {
				return true;
			}

			if(rCommit.getCommitTime() > bHEAD.getCommitTime()) {
				return false;
			}

			// Start the walk from the head
			walk.markStart(bHEAD);
			walk.setRetainBody(false);

			// Walk the commits until we find the given commit
			RevCommit current = null;
			while ((current = walk.next()) != null) {

				if (AnyObjectId.equals(rCommit, current)) {
					return true;
				}
			}

			// We have walked all commits and found nothing that has the same id
			// as the given one
			return false;

		} catch (final MissingObjectException e) {
			throw new VCSRepositoryException(e);
		} catch (final IncorrectObjectTypeException e) {
			throw new VCSRepositoryException(e);
		} catch (final IOException e) {
			throw new VCSRepositoryException(e);
		} finally {
			walk.release();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<VCSCommit> getPrevious() throws VCSRepositoryException {

		final List<VCSCommit> parents = new ArrayList<VCSCommit>();

		try {
			for (final RevCommit p : parentOf(this.commit, this.repo)) {
				parents.add(new VCSCommitImp(p, this.repo));
			}
		} catch (final MissingObjectException e) {
			throw new VCSRepositoryException(e);
		} catch (final IncorrectObjectTypeException e) {
			throw new VCSRepositoryException(e);
		} catch (final IOException e) {
			throw new VCSRepositoryException(e);
		}
		return parents;
	}

	/**
	 * Get the parents of this commit.<p>
	 * 
	 * @param child the commit to get the parents
	 * @param repo from where this commit comes from
	 * @return an array of <code>child</code>'s parents
	 * @throws MissingObjectException
	 * @throws IncorrectObjectTypeException
	 * @throws IOException
	 */
	static RevCommit[] parentOf(final RevCommit child, final Repository repo) throws MissingObjectException, IncorrectObjectTypeException, IOException {
		final RevWalk walk = new RevWalk(repo);

		try{ final RevCommit commit = walk.parseCommit(child.getId());
		return commit.getParents();
		} finally {
			walk.release();
		}
	}

	/**
	 * {@inheritDoc}
	 * {@link VCSResource.Type#NONE}
	 */
	@Override
	public VCSResource getResource(final String path) throws VCSRepositoryException,
	VCSResourceNotFound {

		// The walk will be returned only if the path is available and is a
		// known
		// path (file or dir) otherwise a an exception will be thrown. If
		// path is not correct an IllegalArgumentException will be thrown,
		// if its not available or is uknown a VCSResourceNotFound will be
		// thrown
		// and if there is a problem with the repository a
		// VCSRepositoryException

		ArgsCheck.notNull("path", path);

		final TreeWalk walk = TreeUtils.getTreeWalkForPath(this.commit, this.repo, path);

		// Construct the path accordingly
		final VCSResource.Type type = resourceType(walk.getFileMode(0));

		if (type.equals(VCSResource.Type.FILE)) {

			return new VCSFileImp(this, path);

		} else if (type.equals(VCSResource.Type.DIR)) {

			return new VCSDirectoryImp(this, path);
		}

		throw new VCSResourceNotFound("uknown path " + path);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isResourceAvailable(final String path) {
		TreeWalk walk = null;
		try {
			// We just need to create a walker, with recursive true and check if
			// the walker gives as
			// any entry, because each entry will be a file then we are sure our
			// path is a prefix of
			// given entry
			walk = TreeUtils.createTreeWalk(this.commit, this.repo, true, path);

			return walk.next();

		} catch (final IOException e) {
		} finally {
			if(walk != null) {
				walk.release();
			}
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void walkTree(final Visitor<VCSResource> visitor)
			throws VCSRepositoryException {

		ArgsCheck.notNull("visitor", visitor);
		// if recursive true then no directories will
		// be produced
		boolean recursive = true;
		String[] paths = null;

		// if the specified visitor is a resource visitor then get the paths to
		// limit
		// the tree traversal
		if (PathLimitingVisitor.class.isAssignableFrom(visitor.getClass())) {

			final PathLimitingVisitor<VCSResource> rvis = ((PathLimitingVisitor<VCSResource>) visitor);

			final Collection<String> pathCollection = rvis.getPaths();

			if((pathCollection != null) && !pathCollection.isEmpty()) {
				ArgsCheck.containsNoNull("paths", rvis.getPaths());
				paths = rvis.getPaths().toArray(new String[0]);
			}
		}

		boolean includeDirs = true;
		boolean includeFiles = true;

		// if the specified visitor is a file/dir visitor then get the option to
		// return
		// directories too
		if (FileDirVisitor.class.isAssignableFrom(visitor.getClass())) {
			final FileDirVisitor<VCSResource> fdvis = (FileDirVisitor<VCSResource>) visitor;
			// if include dirs is true then recursive must be false in order to
			// return dirs
			recursive = !fdvis.includeDirs();
			includeDirs = fdvis.includeDirs();
			includeFiles = fdvis.includeFiles();
			ArgsCheck.isTrue("includeDirs && includeFiles", includeDirs && includeFiles);
		}

		TreeWalk walker = null;
		try {
			walker = TreeUtils.createTreeWalk(this.commit, this.repo, recursive, paths);

			while (walker.next()) {

				final FileMode mode = walker.getFileMode(0);
				if (isDirMode(mode) && includeDirs) {

					final boolean visit = 
							visitor.visit(new VCSDirectoryImp(this, walker.getPathString()));

					// if visitor wants to stop tree traversal then return
					if (!visit) {
						return;
					}

					// continue to enter this directory
					walker.enterSubtree();

				} else if (isFileMode(mode) && includeFiles) {

					final boolean visit = 
							visitor.visit(new VCSFileImp(this, walker.getPathString()));

					// if visitor wants to stop tree traversal then return
					if (!visit) {
						return;
					}
				}
			}
		} catch (final IOException e) {
			throw new VCSRepositoryException(e);
		} finally {
			walker.release();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void checkout(final String path, final String... paths) throws VCSRepositoryException {

		ArgsCheck.notNull("path", path);

		TreeWalk walker = null;
		final File dir = new File(path);

		// Check if the current path exists and is not directory
		if(dir.exists() && !dir.isDirectory()) {
			throw new VCSRepositoryException("path is a file: " + dir.getAbsolutePath());
		}

		// If the directory exists try to clean it,
		// if not try to create it
		try {
			if(dir.exists()) {
				FileUtils.cleanDirectory(dir);
			} else {
				FileUtils.forceMkdir(dir);
			}
		} catch (final IOException e) {
			throw new VCSRepositoryException(e);
		}

		/*
		 * Here will walk the tree for the given commit. The tree walking will be
		 * recursive that is only the files will be written to disk, all empty
		 * directories will be ignored.
		 */
		try {
			walker = TreeUtils.createTreeWalk(this.commit, this.repo, true, paths);

			while (walker.next()) {

				// A check to be sure that the current entry is a regular file
				final FileMode mode = walker.getFileMode(0);
				if (isFileMode(mode)) {

					// Get the path for the current entry
					final String filePath = walker.getPathString();

					// Create the file object for the current entry
					final File file = new File(dir, filePath);

					// We have to check if the containing directory of the current entry
					// doesn't exists then create it
					if(!file.getParentFile().exists()) {
						FileUtils.forceMkdir(file.getParentFile());
					}

					// Open the stream to write contents to
					final FileOutputStream target = new FileOutputStream(file);

					try {
						// get the first object of this walker within tree
						final ObjectId objectId = walker.getObjectId(0);

						// If this is a zero id (usually denotes null object id)
						// then do nothing
						if(objectId.equals(ObjectId.zeroId())) {
							continue;
						}

						// Get the loader
						final ObjectLoader loader = this.repo.open(objectId);

						// use the loader to copy the contents to the stream
						loader.copyTo(target);
					} finally {
						target.flush();
						target.close();
					}
				}
			}

		} catch (final IOException e) {
			try {
				FileUtils.cleanDirectory(dir);
			} catch (final IOException e1) {}
			throw new VCSRepositoryException(e);
		} finally {
			walker.release();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void walkCommitBack(final Visitor<VCSCommit> visitor) throws VCSRepositoryException  {

		ArgsCheck.notNull("visitor", visitor);

		// We have to parse all commits within repository
		// For each commit we have to determine if it is reachable from
		// this commit

		// Start with a RevWalk
		final RevWalk walk = new RevWalk(this.repo);
		final RevCommit bHEAD;

		try {

			// Resolve this commit
			final ObjectId oid = this.commit.getId();
			bHEAD = walk.parseCommit(oid);

			// Start the walk from the head
			walk.markStart(bHEAD);

			// We need to check if the given visitor is of type PathLimitinVisitor
			// so we can limit only to commits that contains the given paths.
			if(PathLimitingVisitor.class.isAssignableFrom(visitor.getClass())) {

				final PathLimitingVisitor<VCSCommit> pls = (PathLimitingVisitor<VCSCommit>) visitor;
				final Collection<String> paths = pls.getPaths();
				ArgsCheck.containsNoNull("paths", paths);

				TreeFilter pathsFilter = PathFilterGroup.createFromStrings(paths);

				// If the given visitor is ModifyingPathVisitor then allow only the commits
				// that modify the specifying paths
				if(ModifyingPathVisitor.class.isAssignableFrom(visitor.getClass())) {
					pathsFilter = AndTreeFilter.create(Arrays.asList(
							PathFilterGroup.createFromStrings(paths),
							TreeFilter.ANY_DIFF));
				}
				walk.setTreeFilter(pathsFilter);
			}
			// All commits that are ancestors of the current HEAD
			// will be accessible with walk.next(), so collect the commits until
			// there is no other commit to walk
			RevCommit current = null;
			while((current = walk.next()) != null) {
				if(!visitor.visit(new VCSCommitImp(current, this.repo))) {
					return;
				}
			}

		} catch (final IOException e) {
			throw new VCSRepositoryException(e);
		} finally {
			walk.release();
		}
	}

	@Override
	public String toString() {
		return this.commit.getName();
	}
}
