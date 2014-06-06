/**
 * 
 */
package gr.uom.se.vcs.jgit;

import gr.uom.se.vcs.VCSBranch;
import gr.uom.se.vcs.VCSCommit;
import gr.uom.se.vcs.VCSRepository;
import gr.uom.se.vcs.VCSTag;
import gr.uom.se.vcs.exceptions.VCSRepositoryException;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand.ListMode;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevSort;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.TagOpt;


/**
 * Implementation of {@link VCSRepository} based on JGit library.
 * <p>
 * 
 * This is a one shot repository, and instances of this should be created only
 * if there is a repository available (local or remote) and it must be opened
 * for reading. When the instance of this repository is created, if the
 * specified local path contains a repository (.git dir) it will load it
 * automatically, and that would require time. If there is no repository
 * available, you can not use this instance until you call cloneRemote() in
 * order to clone a remote repository and store it to the local path.
 * <p>
 * This object is not thread safe, in that it doesn't lock the repository's
 * folder, so another object (or thread) can change the contents and cause
 * problems to this repository. You can not share this repository among multiply
 * threads, unless you provide a file lock to the local directory of this
 * repository, and be sure that no other thread/process change the contents of
 * this dir.
 * <p>
 * When the repository loads (see constructor) it will select a default branch,
 * using {@link #selectDefaultBranch()}. This method will check all branches and
 * pick the one that ends with '/master' or 'master'. If this doesn't satisfy,
 * you can subclass this, and provide a different implementation for this
 * method.
 * 
 * @author Elvis Ligu
 * @since 0.0.1
 * @version 0.0.1
 * @see VCSRepository
 */
public class VCSRepositoryImp implements VCSRepository {

   /**
    * The path where .git file will be stored.
    * <p>
    * This can be a clean path, when the remote path is specified and it will be
    * cloned
    */
   protected String localPath;

   /**
    * The remote path (if any) from where this repository is cloned.
    * <p>
    * Remote path is needed in case there is no repository (.git) in the
    * {@link #localPath} and a new remote repository will be cloned.
    */
   protected String remotePath;

   /**
    * The JGit repository object this repository is linked to.
    * <p>
    */
   protected Repository repo;

   /**
    * The selected branch from which we must get the head when parsing commits.
    * <p>
    * When repository is created the default selected branch will be MASTER (if
    * any).
    */
   protected VCSBranchImp selectedBranch;

   /**
    * Creates a new instance given local and remote paths.
    * <p>
    * 
    * <code>localPath</code> and <code>remotePath</code> can not be both null.
    * 
    * This will try to open a repository if the local path is not null and
    * contains a .git directory. If this doesn't succeed it will throw an
    * exception. Local path is not allowed to be null, however the remote path
    * may be null if there is a repository under local path and this repository
    * doesn't have a remote one.
    * 
    * @param localPath
    *           where the repository is stored or will be stored in case of
    *           cloning. Can not be null.
    * @param remotePath
    *           the path from where the repository in <code>localPath</code>
    *           comes from or will be cloned.
    * @throws VCSRepositoryException
    *            in case the localPath contains a .git directory which can not
    *            be loaded
    */
   public VCSRepositoryImp(String localPath, final String remotePath)
         throws VCSRepositoryException {

      // Check local path
      ArgsCheck.notEmpty("localPath", localPath);

      // Correct path
      localPath = localPath.trim().replace('\\', '/');

      // If a .git directory is not contained in local path and a remote path is
      // not specified
      // we can not create the instance because there will be no cloning, and no
      // repo instance will
      // be loaded.
      if (!containsGitDir(localPath)) {

         ArgsCheck.notEmpty("no .git directory under local path, remotePath",
               remotePath);

      } else {
         // there is a .git directory in local path so we will try to load it
         try {

            this.repo = openRepo(localPath);
            this.selectDefaultBranch();

         } catch (final IOException e) {
            throw new VCSRepositoryException(e);
         } catch (final URISyntaxException e) {
            throw new VCSRepositoryException(e);
         }
      }

      // Correct local path to not point to the .git directory
      if (localPath.endsWith("/.git")) {
         localPath = localPath.substring(0, localPath.length() - 4);
         if (localPath.isEmpty()) {
            localPath = ".";
         }
      }

      this.localPath = localPath;
      this.remotePath = remotePath;
   }

   /**
    * Open a repository from the path <code>local</code>.
    * <p>
    * The path must contain a .git directory or point to.
    * 
    * @param local
    *           the path to load the repo from
    * @return a new repository object
    * @throws IOException
    * @throws URISyntaxException
    */
   public static Repository openRepo(final String local) throws IOException,
         URISyntaxException {

      final File gitDir = createGitDir(local);

      final FileRepositoryBuilder builder = new FileRepositoryBuilder();

      final Repository repository = builder.setGitDir(gitDir).readEnvironment()
            .findGitDir() // scan up the file system tree
            .build();

      return repository;
   }

   /**
    * Will correct the path and create a {@link File} instance that points to a
    * .git directory under the specified path.
    * <p>
    * 
    * @param path
    *           to create the file that point to .git dir
    * @return a file that points to .git directory
    */
   public static File createGitDir(String path) {

      File gitDir = null;
      path = path.replace('\\', '/');
      if (!path.endsWith("/.git") || !path.endsWith(".git")) {
         gitDir = new File(path, "/.git");
      } else {
         gitDir = new File(path);
      }

      return gitDir;
   }

   /**
    * Check if path <code>local</code> points to an existing .git directory or
    * contains one.
    * <p>
    * 
    * @param local
    *           the path to check
    * @return true if local contains a .git directory
    */
   public static boolean containsGitDir(final String local) {

      final File gitDir = createGitDir(local);
      return gitDir.isDirectory();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public String getLocalPath() {
      return this.localPath;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public String getRemotePath() {
      return this.remotePath;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public synchronized void cloneRemote() throws VCSRepositoryException {

      if (repo != null) {
         throw new VCSRepositoryException(
               "Can not clone repository. A repository is already opened. Create a new repo instance for a different location or try to update the current one");
      }

      try {
         // Local path will not point to a .git directory
         // that because we have corrected it in the constructor
         final File gitDir = new File(this.localPath);

         // Clean localPath if it exists
         if (gitDir.exists()) {
            FileUtils.cleanDirectory(gitDir);
         }

         // Create a clone command and clone all branches.
         // Do not checkout, to improve speed
         this.repo = Git.cloneRepository().setURI(this.remotePath)
               .setDirectory(gitDir).setCloneAllBranches(true)
               .setNoCheckout(true).call().getRepository();

         // We must ensure that a default branch will be always selected
         // each time a repository is created.
         this.selectDefaultBranch();

      } catch (final InvalidRemoteException e) {
         throw new VCSRepositoryException(e);
      } catch (final TransportException e) {
         throw new VCSRepositoryException(e);
      } catch (final GitAPIException e) {
         throw new VCSRepositoryException(e);
      } catch (final IOException e) {
         throw new VCSRepositoryException(e);
      } finally {
         this.close();
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void close() {
      if (this.repo != null) {
         this.repo.close();
      }
   }

   /**
    * Check all branches and select the one that ends with /master or has the
    * name master.
    * <p>
    * 
    * @throws VCSRepositoryException
    */
   protected void selectDefaultBranch() throws VCSRepositoryException {

      final Collection<VCSBranch> branches = this.getBranches();
      for (final VCSBranch branch : branches) {
         if (branch.getName().endsWith("/master")) {
            this.selectedBranch = (VCSBranchImp) branch;
            return;
         } else if (branch.getName().equals("master")) {
            this.selectedBranch = (VCSBranchImp) branch;
            return;
         }
      }
   }

   /**
    * Check if repo is null, if so throw an exception
    * 
    * @param repo
    *           to check
    * @throws VCSRepositoryException
    */
   private static void checkRepo(final Repository repo)
         throws VCSRepositoryException {
      if (repo == null) {
         throw new VCSRepositoryException(
               "repository can not be loaded, either a local repo is not specified or a remote repository has never been cloned to the local path");
      }
   }

   /**
    * {@inheritDoc} <b>NOTE:</b> This will return all branches, local and remote
    * ones if this repository is not been cloned, and there are local branches
    * that follows remote ones.
    */
   @Override
   public Collection<VCSBranch> getBranches() throws VCSRepositoryException {

      // Always check repository for null
      checkRepo(this.repo);

      try {

         // Create a list branch command and return all branches
         // Actually the repository will have only the remote branches (if this
         // was cloned)
         // because we don't need to create a local branch.
         final List<VCSBranch> branches = new ArrayList<VCSBranch>();
         final List<Ref> refs = new Git(this.repo).branchList()
               .setListMode(ListMode.ALL).call();
         for (final Ref r : refs) {
            branches.add(new VCSBranchImp(r.getLeaf(), this.repo));
         }
         return branches;
      } catch (final GitAPIException e) {
         throw new VCSRepositoryException(e);
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Collection<VCSTag> getTags() throws VCSRepositoryException {

      // Always check for repository if null
      checkRepo(this.repo);

      try {

         // Create a list tag command and execute it
         final List<VCSTag> tags = new ArrayList<VCSTag>();
         final List<Ref> refs = new Git(this.repo).tagList().call();

         for (final Ref r : refs) {
            tags.add(new VCSTagImp(r.getLeaf(), this.repo));
         }

         return tags;

      } catch (final GitAPIException e) {
         throw new VCSRepositoryException(e);
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public synchronized void update() throws VCSRepositoryException {

      // Always check repo for null
      checkRepo(this.repo);

      try {
         // Update all branches.
         // Create a configuration so all branches will be updated
         // Generally all branches under refs/remote/origin/* will be
         final Git git = new Git(this.repo);
         final RemoteConfig config = new RemoteConfig(git.getRepository()
               .getConfig(), Constants.DEFAULT_REMOTE_NAME);

         // Destination is refs/remote/origin/*
         final String dst = Constants.R_REMOTES + config.getName() + "/*"; //$NON-NLS-1$

         RefSpec wcrs = new RefSpec();
         wcrs = wcrs.setForceUpdate(true);
         // The source branches of remote repository (refs/heads/*) will be
         // copied to remote
         // branches of local repository.
         wcrs = wcrs.setSourceDestination(Constants.R_HEADS + "*", dst); //$NON-NLS-1$

         // Execute the command to update the repository
         git.fetch().setRemote(Constants.DEFAULT_REMOTE_NAME)
               .setTagOpt(TagOpt.FETCH_TAGS).setRefSpecs(wcrs).call();

      } catch (final URISyntaxException e) {
         throw new VCSRepositoryException(e);
      } catch (final InvalidRemoteException e) {
         throw new VCSRepositoryException(e);
      } catch (final TransportException e) {
         throw new VCSRepositoryException(e);
      } catch (final GitAPIException e) {
         throw new VCSRepositoryException(e);
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public VCSCommit resolveCommit(final String cid)
         throws VCSRepositoryException {

      // Always check repo for null
      checkRepo(this.repo);

      final RevWalk walker = new RevWalk(this.repo);

      try {
         final ObjectId oid = this.repo.resolve(cid);
         if (oid == null) {
            throw new VCSRepositoryException(cid
                  + " can not be resolved to a commit");
         }
         return new VCSCommitImp(walker.parseCommit(oid), this.repo);

      } catch (final RevisionSyntaxException e) {
         throw new VCSRepositoryException(e);
      } catch (final MissingObjectException e) {
         throw new VCSRepositoryException(e);
      } catch (final IncorrectObjectTypeException e) {
         throw new VCSRepositoryException(e);
      } catch (final AmbiguousObjectException e) {
         throw new VCSRepositoryException(e);
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
   public VCSBranch resolveBranch(final String bid)
         throws VCSRepositoryException {

      // Always check repo for null
      checkRepo(this.repo);

      try {
         final Ref ref = this.repo.getRef(bid);
         if (ref == null) {
            throw new VCSRepositoryException(bid
                  + " can not be resolved to e reference");
         }
         return new VCSBranchImp(ref, this.repo);

      } catch (final IOException e) {
         throw new VCSRepositoryException(e);
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public VCSTag resolveTag(final String tag) throws VCSRepositoryException {

      // Always check repo for null
      checkRepo(this.repo);

      try {
         final Ref ref = this.repo.getRef(tag);
         if (ref == null) {
            throw new VCSRepositoryException(tag
                  + " can not be resolved to e reference");
         }
         return new VCSTagImp(ref, this.repo);

      } catch (final IOException e) {
         throw new VCSRepositoryException(e);
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public VCSCommit getHead() throws VCSRepositoryException {

      checkRepo(this.repo);
      if (this.selectedBranch == null) {
         throw new VCSRepositoryException("There is not any selected branch");
      }

      return this.selectedBranch.getHead();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public VCSCommit getFirst() throws VCSRepositoryException {

      checkRepo(this.repo);
      if (this.selectedBranch == null) {
         throw new VCSRepositoryException("There is not any selected branch");
      }

      final RevWalk walker = new RevWalk(this.repo);
      try {

         final RevCommit root = walker.parseCommit(this.selectedBranch.ref
               .getObjectId());
         walker.sort(RevSort.REVERSE);
         walker.markStart(root);

         return new VCSCommitImp(walker.next(), this.repo);

      } catch (final RevisionSyntaxException e) {
         throw new VCSRepositoryException(e);
      } catch (final AmbiguousObjectException e) {
         throw new VCSRepositoryException(e);
      } catch (final IncorrectObjectTypeException e) {
         throw new VCSRepositoryException(e);
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
   public VCSBranch getSelectedBranch() throws VCSRepositoryException {
      return this.selectedBranch;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public synchronized void selectBranch(final VCSBranch branch)
         throws VCSRepositoryException {
      if (branch == null) {
         this.selectDefaultBranch();
      } else {
         this.selectedBranch = (VCSBranchImp) branch;
      }
   }
}
