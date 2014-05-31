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
 * allows only one parent for each commit.
 * <p>
 * A commit is considered a full featured entity within a repository, that is:
 * <ul>
 * <li>he knows how to walk through its tree ({@link #walkTree(ResourceVisitor)}
 * <li>he can perform a full checkout of tree contents (
 * {@link #checkout(String, String...)}
 * <li>he knows how to walk all commits back (
 * {@link #walkCommits(CommitVisitor, boolean)}
 * <li>he knows how to produce the differences with an other commit (
 * {@link #walkFileChanges(VCSCommit, ChangeVisitor)}
 * </ul>
 * To see the changes of a given commit one must provide a previous one (be it a
 * parent or an older one). A commit must be able to produce a list of
 * {@link VCSChange}, given an other commit. The changes must be based on
 * commits times, where they are produced from older to newer.
 * <p>
 * Given a commit one can see all modifications that this commit has made to the
 * repository:
 * 
 * <code><p>
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;// Get the parents. 
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;// If this commit has two or more parents it is usually a merge commit
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;Collection&ltVCSCommit&gt parents = commit.{@link #getPrevious()};
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;// For each parent produce a list of changes
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;for(VCSCommit parent : parents) {
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;List&ltVCSChange&gt changes = commit.{@link #getChanges(parent)};
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;...
 * <br>&nbsp;&nbsp;&nbsp;&nbsp;}
 * </code>
 * <p>
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
    * In most situations the author is the same as the committer, however
    * distributed VCSs keep track of an author and a committer. The author is
    * considered to be the one who did the modification at his own local copy of
    * repository, and the committer is the person who committed this change to
    * the central repository.
    * 
    * @return the author of this change
    */
   String getAuthor();

   /**
    * The date when the change was committed.
    * <p>
    * 
    * @return date when the change was committed
    */
   Date getCommitDate();

   /**
    * The date when the change has happened.
    * <p>
    * In case the implementation is a distributed VCS, the change may have been
    * first committed to the local repository of the author, and after committed
    * to central repository.
    * 
    * @return date when the change has happened
    * @see #getAuthor()
    */
   Date getAuthorDate();

   /**
    * Given a commit return a list of changes.
    * <p>
    * The changes will be calculated from older to new commit. That is, if this
    * commit is older than the given one the changes are calculated as
    * modifications from this to other one. Changes are returned only if the
    * current path is a directory or a file. Other VCS systems support paths for
    * symlink and such, they will be ignored.
    * <p>
    * <b>WARNING:</b> There are VCSs that do not track changes on directories,
    * that means no directory will be returned. In such situations this will
    * behave exactly as {#link {@link #getFileChanges(VCSCommit)}. Consult the
    * implementation for more.
    * 
    * @param commit
    *           to calculate the changes against
    * @return a list of changes from older to newer
    * @throws VCSRepositoryException
    *            in case there is a problem with change calculating
    * @see {@link VCSChange}
    */
   List<VCSChange<?>> getChanges(VCSCommit commit)
         throws VCSRepositoryException;

   /**
    * Given a commit return a list of changes for the specified paths.
    * <p>
    * Given an array of paths and a commit return the changes for these paths.
    * If paths is null or no path was specified this method will behave the same
    * as {@link #getChanges(VCSCommit)}. Changes are returned only if the
    * current path is a directory or a file. Other VCS systems support paths for
    * symlink and such, they will be ignored.<br>
    * A path can be a directory or a file. If the path is directory then if
    * recursive is true all the paths under this directory will be checked for
    * changes.
    * <p>
    * <b>WARNING:</b> There are VCSs that do not track changes on directories,
    * that means no directory will be returned. In such situations this will
    * behave exactly as {#link
    * {@link #getFileChanges(VCSCommit, boolean, String...)}. Consult the
    * implementation for more.
    * 
    * @param commit
    *           the commit to check against
    * @param recursive
    *           if true all the paths under the specified paths will be checked
    * @param paths
    *           if specifying any, limits the changes to only those paths
    * @return a list of changes from older to newer
    * @throws VCSRepositoryException
    *            in case there is a problem with change calculating
    */
   List<VCSChange<?>> getChanges(VCSCommit commit, boolean recursive,
         String... paths) throws VCSRepositoryException;

   /**
    * Get only file changes (do not include directories).
    * <p>
    * This method behaves the same as {@link #getChanges(VCSCommit)} and it only
    * produces changes to files. Only regular files will be returned, other
    * files such as symlinks will not.
    * 
    * @param commit
    *           the commit to check against
    * @return a list of file changes
    * @throws VCSRepositoryException
    *            in case there is a problem with change calculating
    * @see {@link #getChanges(VCSCommit)}
    */
   List<VCSFileDiff<?>> getFileChanges(VCSCommit commit)
         throws VCSRepositoryException;

   /**
    * Get only file changes that are under the specified paths (do not include
    * directories).
    * <p>
    * This method behaves the same as
    * {@link #getChanges(VCSCommit, boolean, String...)} and it only produces
    * changes to files. Only regular files will be returned, other files such as
    * symlinks will not.
    * 
    * @param commit
    *           the commit to check against
    * @param recursive
    *           if true, return all changes under the specified paths (if they
    *           are directories)
    * @param paths
    *           if specifying any, limit the changes to only those paths
    * @return a list of changes
    * @throws VCSRepositoryException
    *            in case there is a problem with change calculating
    */
   List<VCSFileDiff<?>> getFileChanges(VCSCommit commit, boolean recursive,
         String... paths) throws VCSRepositoryException;

   /**
    * Check if this commit is a merge commit.
    * <p>
    * Generally speaking a merge commit must have at least two parents. This
    * method might not be supported by some implementations (it depends on the
    * type of VCS). However, even in the case the current VCS does not support
    * merging, the implementation may choose to return a meaningful value.
    * 
    * @return true if this commit is a merge
    */
   boolean isMergeCommit();

   /**
    * Return the next commits (the immediate descendants, a.k.a. children) if
    * any.
    * <p>
    * Note that some VCSs support a Directed Acyclic Graph of the commits. That
    * is a commit may have multiply parents (in case this is a merge commit) or
    * may have multiply children (in case from this commit start multiply
    * branches).
    * 
    * @return a list of immediate descendants (a.k.a. children)
    * @throws VCSRepositoryException
    *            in case there is a problem
    */
   Collection<VCSCommit> getNext() throws VCSRepositoryException;

   /**
    * Get a list of all previous commits (immediate ancestors, a.k.a. parents).
    * <p>
    * Usually there is only one commit prior to this one, however some VCSs
    * support the merging of two or more branches, in this case a commit would
    * have more than one previous commits. Note that if this commit is the first
    * then it has no previous.
    * 
    * @return a list of immediate ancestors (a.k.a. parents)
    * @throws VCSRepositoryException
    */
   Collection<VCSCommit> getPrevious() throws VCSRepositoryException;

   /**
    * Walk diff entries until there are no more entries or the
    * {@link ChangeVisitor#visit(Object)} returns false.
    * <p>
    * If a resource filter is specified, the changes will be limited only to
    * those resources that this filter allow.
    * <p>
    * If a change filter is specified, only those changes that are allowed by
    * this filter will be visited.
    * <p>
    * This method is usually more efficient if you specify a resource filter to
    * limit the number of tree paths to check, and you don't need all diff
    * entries saved at one collection. Here is an example of using filters to
    * query for changes in a specific path:
    * 
    * <pre>
    * Commit head = repo.resolveCommit("some_id");
    * Commit commit = repo.resolveCommit("some_id_2");
    * 
    * ResourceFilterFactory rf = new ResourceFilterFactory();
    * final VCSResourceFilter&ltVCSResource&gt filter = rf.and(rf.suffix(".java"), 
    * 		rf.prefix("src/main/java/some/package"));
    * 
    * ChangeVisitot&ltVCSChange&lt?&gt&gt visitor = new ChangeVisitor&ltVCSChange&lt?&gt&gt(){
    * 
    *   public boolean visit(VCSChange&lt?&gt change) { ... }
    * 
    *   public VCSResourceFilter&ltVCSResource&gt getResourceFilter(){ return filter; }
    *   
    *   public VCSChangeFilter&ltVCSChange&lt?&gt&gt getFilter() { return null; }
    * }
    * 
    * head.walkChanges(commit, visitor);
    * </pre>
    * 
    * The above snippet will visit all changes of .java files that are under the
    * package <code>some/package</code>.
    * <p>
    * 
    * @param commit
    *           the commit to check against
    * @param visitor
    *           that will be accepting changes
    * @throws VCSRepositoryException
    *            in case there is a problem with change calculating
    * @see #getChanges(VCSCommit)
    * @see ChangeVisitor
    * @see VCSResource
    */
   void walkChanges(VCSCommit commit, ChangeVisitor<VCSChange<?>> visitor)
         throws VCSRepositoryException;

   /**
    * Walk diff entries until there are no more entries or the
    * {@link ChangeVisitor#visit(Object)} returns false.
    * <p>
    * If a resource filter is specified, the changes will be limited only to
    * those resources that this filter allows.
    * <p>
    * If a change filter is specified, only those changes that are allowed by
    * this filter will be visited.
    * <p>
    * This method is usually more efficient if you specify a resource filter to
    * limit the number of tree paths to check, and you don't need all diff
    * entries saved at one collection.
    * <p>
    * This method works the same as
    * {@link #walkChanges(VCSCommit, ChangeVisitor)} but it only visits files
    * (not directories will be included in results).
    * 
    * @param commit
    *           the commit to check against
    * @param visitor
    *           that will be accepting changes
    * @throws VCSRepositoryException
    *            in case there is a problem with change calculating
    * @see #walkChanges(VCSCommit, ChangeVisitor)
    * @see VCSFile
    */
   public void walkFileChanges(VCSCommit commit,
         ChangeVisitor<VCSFileDiff<?>> visitor) throws VCSRepositoryException;

   /**
    * Get the resource in the given path.
    * <p>
    * This will throw an exception if the given resource is not present. An
    * {@link VCSResourceNotFoundException} will be thrown in case this resource
    * is not available. You can check first if the given resource is available
    * {@link #isResourceAvailable(String)}, however note, that checking resource
    * availability add a processing burden.
    * 
    * @param path
    *           to resource
    * @return a resource for the given path
    * @throws VCSResourceNotFoundException
    *            if resource was not found or is unknown (see
    *            {@link VCSResource.Type#NONE}
    * @throws VCSRepositoryException
    *            if a problem occurs while reading repository objects
    */
   public VCSResource getResource(String path) throws VCSRepositoryException,
         VCSResourceNotFoundException;

   /**
    * Check if the resource in the given path is available.
    * <p>
    * 
    * @param path
    *           to check for availability
    * @return true if there is a resource at the given path
    */
   public boolean isResourceAvailable(String path);

   /**
    * Walk the tree that is specified by this commit.
    * <p>
    * If a resource filter is specified, the changes will be limited only to
    * those resources that this filter allow.
    * <p>
    * Note: tree traversal will only return results for those resources that are
    * either DIR (directories) or FILE (files). Other resources that a VCS can
    * contain such as symlinks, will be excluded.
    * <p>
    * It is advisable to use filters, if the visitor is interested only in
    * certain paths, as this will escape any unnecessary tree walking (early
    * discarding paths that are not allowed by the filter). DO NOT filter the
    * resources at <code>visit()</code> method of the visitor, because this will
    * create a lot of objects in memory that are probably not used at all.
    * 
    * @param visitor
    *           to accept each resource
    * @throws VCSRepositoryException
    *            when something goes wrong
    * @see ResourceVisitor
    */
   public void walkTree(ResourceVisitor<VCSResource> visitor)
         throws VCSRepositoryException;

   /**
    * Check out repository contents at the time this commit made the changes.
    * <p>
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
    *           directory path were the contents will be written
    * @param paths
    *           limit the check out only to given paths
    * @throws VCSRepositoryException
    */
   void checkout(final String path, String... paths)
         throws VCSRepositoryException;

   /**
    * Starting from this commit, walk backward to all ancestors of this one.
    * <p>
    * 
    * A commit is an ancestor of another one only if it is reachable by this one
    * (checking its parents), that is it contains a previous history of this
    * commit.
    * <p>
    * If a resource filter is specified, the changes will be limited only to
    * those resources that this filter allows.
    * <p>
    * If a commit filter is specified, only those commits that are allowed by
    * this filter will be visited.
    * <p>
    * 
    * <b>NOTE:</b> if the implementation does not support branching this method
    * may return all commits of repository.
    * <p>
    * 
    * @param visitor
    *           to accept the commits
    * @param descending
    *           if true the walk with start from HEAD until the first commit,
    *           otherwise it will start from the oldest to newest
    * @throws VCSRepositoryException
    *            if a problem occurs during walk
    */
   void walkCommits(final CommitVisitor<VCSCommit> visitor, boolean descending)
         throws VCSRepositoryException;
}
