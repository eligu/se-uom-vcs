/**
 * 
 */
package se.uom.vcs.jgit.utils;

import java.io.IOException;

import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffEntry.ChangeType;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

import se.uom.vcs.VCSChange;
import se.uom.vcs.VCSDirectory;
import se.uom.vcs.VCSFileDiff;
import se.uom.vcs.VCSResource;
import se.uom.vcs.exceptions.VCSRepositoryException;
import se.uom.vcs.jgit.VCSChangeImp;
import se.uom.vcs.jgit.VCSCommitImp;
import se.uom.vcs.jgit.VCSDirectoryImp;
import se.uom.vcs.jgit.VCSFileDiffImp;
import se.uom.vcs.jgit.VCSFileImp;
import se.uom.vcs.jgit.VCSResourceImp;

/**
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public class RevUtils {

    /**
     * Convert to {@link VCSChange#Type}
     * 
     * @param type
     *            type of change
     * @return a {@link VCSChange.Type}
     */
    public static VCSChange.Type changeType(final ChangeType type) {
    
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
     * Convert to {@link VCSResource#Type}
     * 
     * @param mode
     *            of file
     * @return the type of resource
     */
    public static VCSResource.Type resourceType(final FileMode mode) {
    
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
     * @return a {@link VCSChange} if entry refers to a directory or a
     *         normal/executable file, null if not
     * @see DiffEntry#ChangeType
     * @see RevUtils#createDirChange(DiffEntry, VCSCommitImp, VCSCommitImp)
     * @see RevUtils#createFileChange(DiffEntry, VCSCommitImp, VCSCommitImp)
     */
    public static VCSChange<?> createChange(final DiffEntry entry,
            final VCSCommitImp oldC, final VCSCommitImp newC) {
    
        VCSChange<?> change = null;
    
        // case for directory
        if (RevUtils.isDirDiff(entry)) {
    
            change = RevUtils.createDirChange(entry, oldC, newC);
    
            // case for file
        } else if (RevUtils.isFileDiff(entry)) {
    
            change = RevUtils.createFileChange(entry, oldC, newC);
        }
    
        return change;
    }

    /**
     * If entry's mode is {@link FileMode#TREE} then this will return true.
     * <p>
     * 
     * @param entry
     *            to check if it its a directory
     * @return true if entry is referred to a directory
     */
    public static boolean isDirDiff(final DiffEntry entry) {
    
        final ChangeType type = entry.getChangeType();
    
        // if this is an add then we have to check only the new file mode
        // if this is a delete then we have to check only the old file mode
        if (type.equals(DiffEntry.ChangeType.ADD)) {
    
            return RevUtils.isDirMode(entry.getNewMode());
    
        } else if (type.equals(DiffEntry.ChangeType.DELETE)) {
    
            return RevUtils.isDirMode(entry.getOldMode());
        }
    
        // if there is modified/renamed/copied we must check both old and new
        // modes
        return RevUtils.isDirMode(entry.getNewMode()) || RevUtils.isDirMode(entry.getOldMode());
    }

    /**
     * If entry's mode is {@link FileMode#EXECUTABLE_FILE} or
     * {@link FileMode#REGULAR_FILE} then this will return true.
     * <p>
     * 
     * @param entry
     *            to check if its a file
     * @return true if entry is referred to a file
     */
    public static boolean isFileDiff(final DiffEntry entry) {
    
        final ChangeType type = entry.getChangeType();
    
        // if this is an add then we have to check only the new file mode
        // if this is a delete then we have to check only the old file mode
        if (type.equals(DiffEntry.ChangeType.ADD)) {
    
            return RevUtils.isFileMode(entry.getNewMode());
    
        } else if (type.equals(DiffEntry.ChangeType.DELETE)) {
    
            return RevUtils.isFileMode(entry.getOldMode());
        }
    
        // if there is modified/renamed/copied we must check both old and new
        // modes
        return RevUtils.isFileMode(entry.getNewMode()) || RevUtils.isFileMode(entry.getOldMode());
    }

    /**
     * If entry's mode is {@link FileMode#EXECUTABLE_FILE} or
     * {@link FileMode#REGULAR_FILE} then this will return true.
     * <p>
     * 
     * @param mode
     *            to check if it is a file
     * @return true if this mode is a file
     */
    public static boolean isFileMode(final FileMode mode) {
        return mode.equals(FileMode.EXECUTABLE_FILE.getBits())
        	|| mode.equals(FileMode.REGULAR_FILE.getBits());
    }

    /**
     * Return true only if this is a {@link FileMode#TREE} mode.
     * <p>
     * 
     * @param mode
     *            to check if this is a directory
     * @return true if this mode is a directory
     */
    public static boolean isDirMode(final FileMode mode) {
        return mode.equals(FileMode.TREE.getBits());
    }

    /**
     * Given a {@link DiffEntry} and the commits this diff was produced for,
     * return a {@link VCSFileDiffImp}.
     * <p>
     * 
     * <b>WARNING:</b> this method may fail if {@link isFileMode}
     * returns false.
     * 
     * @param entry
     *            the diff entry
     * @param oldC
     *            old commit
     * @param newC
     *            new commit
     * @return the corresponding {@link VCSFileDiff} of this entry
     */
    public static VCSFileDiffImp<VCSFileImp> createFileChange(final DiffEntry entry,
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
     * <b>WARNING:</b> this method may fail if {@link isDirMode}
     * returns false.
     * 
     * @param entry
     *            the dif entry
     * @param oldC
     *            old commit
     * @param newC
     *            new commit
     * @return the corresponding {@link VCSDirectory} of this entry
     */
    public static VCSChange<?> createDirChange(final DiffEntry entry,
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
     * Check parents of the child if one of them is equal to specified parent
     * return true.
     * <p>
     * 
     * @param parent
     *            to check if its a parent of child
     * @param child
     *            to check if parent is its parent
     * @return true if <code>parent</code> is a parent of <code>child</code>
     */
    public static boolean isParent(final RevCommit parent, final RevCommit child) {
    
        for (final RevCommit p : child.getParents()) {
            if (AnyObjectId.equals(parent, p)) {
        	return true;
            }
        }
        return false;
    }

    /**
     * Check commit1 is ancestor of commit2.
     * <p>
     * 
     * @param commit1
     *            the ancestor to check
     * @param commit2
     *            the descendant of commit2
     * @param repo
     *            from where these commits comes from
     * @return true if commit1 is ancestor of commit2
     * @throws VCSRepositoryException
     */
    public static boolean isAncestor(final RevCommit commit1, final RevCommit commit2,
            final Repository repo) throws VCSRepositoryException {
    
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
    
            if (rCommit.getCommitTime() > bHEAD.getCommitTime()) {
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
     * Get the parents of this commit.
     * <p>
     * 
     * @param child
     *            the commit to get the parents
     * @param repo
     *            from where this commit comes from
     * @return an array of <code>child</code>'s parents
     * @throws MissingObjectException
     * @throws IncorrectObjectTypeException
     * @throws IOException
     */
    public static RevCommit[] parentOf(final RevCommit child, final Repository repo)
            throws MissingObjectException, IncorrectObjectTypeException,
            IOException {
        final RevWalk walk = new RevWalk(repo);
    
        try {
            final RevCommit commit = walk.parseCommit(child.getId());
            return commit.getParents();
        } finally {
            walk.release();
        }
    }

}
