/**
 * 
 */
package se.uom.vcs;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import se.uom.vcs.exceptions.VCSRepositoryException;
import se.uom.vcs.exceptions.VCSResourceNotFoundException;
import se.uom.vcs.walker.ChangeVisitor;
import se.uom.vcs.walker.CommitVisitor;
import se.uom.vcs.walker.ResourceVisitor;
import se.uom.vcs.walker.Visitor;

/**
 * Represents the commit of a {@link VCSRepository}.
 * <p>
 * 
 * Each change made to repository corresponds to a commit. A commit must
 * describe all the changes made to repository (file/dir
 * deletion/addition/modification and so on), at the given point in time. Some
 * VCSs system provide the ability of branching, and merging two or more
 * commits, so a modification made with the given commit is always related to
 * its previous one (its parent) or to all its parents. Some other VCSs system
 * allows only one parent for each commit.<br>
 * To see the changes of a given commit one must provide a previous one (be it a
 * parent or an older one). A commit must be able to produce a list of
 * {@link VCSChange}, given an other commit. The changes must be based on
 * commits times, where they are produced from older to newer.
 * <p>
 * 
 * Given a commit one can see all modifications that this commit has made to the
 * repository:
 * 
 * <code><p>
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;// Get the parents. 
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;// If this commit has two or more parents it is usually a merge commit
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;Collection<VCSCommit> parents = commit.{@link #getPrevious()};
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;// For each parent produce a list of changes
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;for(VCSCommit parent : parents) {
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;List<VCSChange> changes = commit.{@link #getChanges(parent)};
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;...
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;}
 * </code>
 * <p>
 * 
 * A commit is considered an independent entity within a repository, that is, a
 * commit must be able to reproduce its file tree (one can walk tree calling
 * {@link #walkTree(Visitor, boolean, boolean)}), must be able to be asked if a
 * given path is available (see {@link #isResourceAvailable(String)}), must be
 * able to checkout a working copy from this commit (see
 * {@link #checkout(String...)}). This decision was made to keep the API simple
 * and low the burden of its user by limiting the number of classes used to
 * produce a given result.
 * 
 * @author Elvis Ligu
 * @since 0.0.1
 * @version 0.0.1
 */
public interface VCSCommit {

    /**
     * Return the commit ID (revision ID).
     * <p>
     * 
     * Generally speaking a commit id should be used to parse a commit object
     * from repository calling {@link VCSRepository#resolveCommit(String)}.
     * 
     * @return commit id
     */
    String getID();

    /**
     * Return the message of this commit.
     * <p>
     * 
     * @return commit message
     */
    String getMessage();

    /**
     * The id of committer (usually email address).
     * <p>
     * 
     * @return committer id
     */
    String getCommiter();

    /**
     * The id of author (usually email address).
     * <p>
     * In most situations the author is the same as the committer,
     * however distributed VCSs keep track of an author and a
     * committer. The author is considered to be the one who did
     * primary the modification at his own local copy of repository, 
     * and the committer is the person who committed this change 
     * to the central repository.
     * 
     * @return
     * 		the author of this change
     */
    String getAuthor();
    
    /**
     * The date when the change happened (this change was committed).
     * <p>
     * 
     * @return date when this change happened
     */
    Date getDate();

    /**
     * Given a commit return a list of changes.
     * <p>
     * 
     * The changes will be calculated from older to new commit. That is, if this
     * commit is older than the given one the changes are calculated as
     * modifications from this to other one. Changes are returned only if the
     * current path is a directory or a file. Other VCS systems support paths
     * for symlink and such, they will be ignored.
     * <p>
     * <b>WARNING:</b> There are VCSs that do not track changes on directories,
     * that means no directory will be returned. In such situations this will
     * behave exactly as {#link {@link #getFileChanges(VCSCommit)}.
     * Consult the implementation for more.
     * 
     * @param commit
     *            to calculate the changes against
     * @return 
     * 		a list of changes from older to newer
     * @throws VCSRepositoryException
     *             in case there is a problem with change calculating
     * @see {@link VCSChange}
     */
    List<VCSChange<?>> getChanges(VCSCommit commit)
	    throws VCSRepositoryException;

    /**
     * Given a commit return a list of changes for the specified paths.
     * <p>
     * 
     * Given an array of paths and a commit return the changes for these paths.
     * If paths is null or no path was specified this method will behave the
     * same as {@link #getChanges(VCSCommit)}. Changes are returned only if the
     * current path is a directory or a file. Other VCS systems support paths
     * for symlink and such, they will be ignored.<br>
     * A path can be a directory or a file. If the path is directory then if
     * recursive is true all the paths under this directory will be checked for
     * changes.
     * <p>
     * <b>WARNING:</b> There are VCSs that do not track changes on directories,
     * that means no directory will be returned. In such situations this will
     * behave exactly as {#link {@link #getFileChanges(VCSCommit, boolean, String...)}.
     * Consult the implementation for more.
     * 
     * @param commit
     *            the commit to check against
     * @param recursive
     *            if true all the paths under the specified paths will be
     *            checked
     * @param paths
     *            if specifying any, limit the changes to only those paths
     * @return 
     * 		a list of changes from older to newer
     * @throws VCSRepositoryException
     *             in case there is a problem with change calculating
     */
    List<VCSChange<?>> getChanges(VCSCommit commit, boolean recursive,
	    String... paths) throws VCSRepositoryException;

    /**
     * Get only file changes (do not include directories).
     * <p>
     * 
     * This method behaves the same as {@link #getChanges(VCSCommit)} and it
     * only produces changes to files. Only regular files will be returned,
     * other files such as symlinks will not.
     * 
     * @param commit
     *            the commit to check against
     * @return a list of file changes
     * @throws VCSRepositoryException
     *             in case there is a problem with change calculating
     * @see {@link #getChanges(VCSCommit)}
     */
    List<VCSFileDiff<?>> getFileChanges(VCSCommit commit)
	    throws VCSRepositoryException;

    /**
     * Get only file changes that are under the specified paths (do not include
     * directories).
     * <p>
     * 
     * This method behaves the same as
     * {@link #getChanges(VCSCommit, boolean, String...)} and it only produces
     * changes to files. Only regular files will be returned, other files such
     * as symlinks will not.
     * 
     * @param commit
     *            the commit to check against
     * @param recursive
     *            if true, return all changes under the specified path (if they
     *            are directories)
     * @param paths
     *            if specifying any, limit the changes to only those paths
     * @return a list of changes
     * @throws VCSRepositoryException
     *             in case there is a problem with change calculating
     */
    List<VCSFileDiff<?>> getFileChanges(VCSCommit commit, boolean recursive,
	    String... paths) throws VCSRepositoryException;

    /**
     * Check if this commit is a merge commit.
     * <p>
     * 
     * Generally speaking a merge commit must have at least two parents. This
     * method might not be supported from some implementations (it depends on
     * the type of VCS)
     * 
     * @return true if this commit is a merge
     */
    boolean isMergeCommit();

    /**
     * Return the next commits (the immediate descendants, a.k.a. children) if
     * any.
     * <p>
     * 
     * Note that some VCSs support a Directed Acyclic Graph of the commits. That
     * is a commit may have multiply parents (in case this is a merge commit) or
     * may have multiply children (in case from this commit starts multiply
     * branches).
     * 
     * @return a list of immediate descendants (a.k.a. children)
     * @throws VCSRepositoryException
     *             in case there is a problem
     */
    Collection<VCSCommit> getNext() throws VCSRepositoryException;

    /**
     * Get a list of all previous commits (immediate ancestors, a.k.a. parents).
     * <p>
     * 
     * Usually there is only one commit prior to this one, however some VCSs
     * support the merging of two or more branches, in this case a commit would
     * have more than one previous commits. Note that if this commit is the
     * first then it has no previous.
     * 
     * @return a list of immediate ancestors (a.k.a. parents)
     * @throws VCSRepositoryException
     */
    Collection<VCSCommit> getPrevious() throws VCSRepositoryException;

    /**
     * Walk diff entries until there are no more entries or the
     * {@link ChangeVisitor#visit(Object)} returns false.
     * <p>
     * If a resource filter is specified, the changes will be limited only to those
     * resources that this filter allows.
     * <p>
     * If a change filter is specified, only those changes that are allowed by
     * this filter will be visited.
     * <p>
     * This method is usually more efficient if you specify a resource filter to limit the
     * number of tree paths to check, and you don't need all diff entries saved
     * at one collection.
     * 
     * @param commit
     *            the commit to check against
     * @param visitor
     *            that will be accepting changes
     * @throws VCSRepositoryException
     *             in case there is a problem with change calculating
     * @see {@link #getChanges(VCSCommit)}
     * @see {@link ChangeVisitor}
     */
    void walkChanges(VCSCommit commit, ChangeVisitor<VCSChange<?>> visitor)
	    throws VCSRepositoryException;

    /**
     * Walk diff entries until there are no more entries or the
     * {@link ChangeVisitor#visit(Object)} returns false.
     * <p>
     * If a resource filter is specified, the changes will be limited only to those
     * resources that this filter allows.
     * <p>
     * If a change filter is specified, only those changes that are allowed by
     * this filter will be visited.
     * <p>
     *  This method is usually more efficient if you specify a resource filter to limit the
     * number of tree paths to check, and you don't need all diff entries saved
     * at one collection.
     * 
     * @param commit
     *            the commit to check against
     * @param visitor
     *            that will be accepting changes
     * @throws VCSRepositoryException
     *             in case there is a problem with change calculating
     * @see {@link #getFileChanges(VCSCommit)}
     */
    public void walkFileChanges(VCSCommit commit,
	    ChangeVisitor<VCSFileDiff<?>> visitor) throws VCSRepositoryException;

    /**
     * Get the resource in the given path.
     * <p>
     * 
     * This will throw an exception if the given resource is not present. An
     * {@link VCSResourceNotFoundException} will be thrown in case this resource is not
     * available. You can check first if the given resource is available
     * {@link #isResourceAvailable(String)}, however note, that checking
     * resource availability add a processing burden.
     * 
     * @param path
     *            to resource
     * @return a resource for the given path
     * @throws VCSRepositoryException
     *             when a problem occurs
     * @throws VCSResourceNotFoundException
     *             if resource was not found or is unknown (see
     *             {@link VCSResource.Type#NONE}
     */
    public VCSResource getResource(String path) throws VCSRepositoryException,
	    VCSResourceNotFoundException;

    /**
     * Check if the resource in the given path is available.
     * <p>
     * 
     * @param path
     *            to check for availability
     * @return true if there is a resource at the given path
     */
    public boolean isResourceAvailable(String path);

    // TODO Have to rewrite comments for path limiting visitor and modifying visitors
    
    /**
     * Walk the tree that is specified by this commit.
     * <p>
     * 
     * If a resource filter is specified, the changes will be limited only to those
     * resources that this filter allows.
     * <p>
     * 
     * Note: tree traversal will only return results for those resources that
     * are either DIR (directories) or FILE (files). Other resources that a VCS
     * can contain such as symlinks, will be excluded.
     * 
     * @param visitor
     *            to accept each resource
     * @param dirs
     *            true if the walk should include directories
     * @param files
     *            true if the walk should include files
     * @throws VCSRepositoryException
     *             when something goes wrong
     */
    public void walkTree(ResourceVisitor<VCSResource> visitor, boolean dirs,
	    boolean files) throws VCSRepositoryException;

    /**
     * Check out repository contents at the time this commit made the changes.
     * <p>
     * 
     * The checkout files will be stored in the directory denoted by the first
     * argument. The directory must exists, and be accessible. All contents of
     * directory will be cleaned before making any checkout.
     * <p>
     * 
     * WARNING: if a problem occurs the previous contents this directory was
     * having are not restored, so be sure to backup all the contents before
     * making the checkout.
     * 
     * @param path
     *            directory path were the contents will be written
     * @param paths
     *            limit the check out to only given paths
     * @throws VCSRepositoryException
     */
    void checkout(final String path, String... paths)
	    throws VCSRepositoryException;

    /**
     * Starting from this commit, walk backward to all ancestors of this one.
     * <p>
     * 
     * A commit is an ancestor of another one only if it is reachable by this
     * one (checking its parents), that is it contains a previous history of
     * this commit.
     * <p>
     * If a resource filter is specified, the changes will be limited only to those
     * resources that this filter allows.
     * <p>
     * If a commit filter is specified, only those commits that are allowed by this
     * filter will be visited.
     * <p>
     * 
     * <b>NOTE:</b> if the implementation does not support branching this method
     * may return all commits of repository.
     * <p>
     * 
     * @param visitor
     *            to accept the commits
     * @throws VCSRepositoryException
     *             if a problem occurs during walk
     */
    void walkCommitBack(final CommitVisitor<VCSCommit> visitor)
	    throws VCSRepositoryException;
}
