/**
 * 
 */
package gr.uom.se.vcs.jgit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import gr.uom.se.vcs.VCSChange;
import gr.uom.se.vcs.VCSCommit;
import gr.uom.se.vcs.VCSFileDiff;
import gr.uom.se.vcs.VCSRepository;
import gr.uom.se.vcs.VCSResource;
import gr.uom.se.vcs.exceptions.VCSRepositoryException;
import gr.uom.se.vcs.jgit.VCSCommitImp;
import gr.uom.se.vcs.jgit.VCSRepositoryImp;
import gr.uom.se.vcs.jgit.mocks.ChangeMock;
import gr.uom.se.vcs.jgit.mocks.CommitMock;
import gr.uom.se.vcs.jgit.mocks.RepoMock;
import gr.uom.se.vcs.jgit.mocks.ResourceMock;
import gr.uom.se.vcs.walker.ChangeVisitor;
import gr.uom.se.vcs.walker.CommitVisitor;
import gr.uom.se.vcs.walker.ResourceVisitor;
import gr.uom.se.vcs.walker.Visitor;
import gr.uom.se.vcs.walker.filter.VCSFilter;
import gr.uom.se.vcs.walker.filter.commit.VCSCommitFilter;
import gr.uom.se.vcs.walker.filter.resource.ResourceFilterUtility;
import gr.uom.se.vcs.walker.filter.resource.VCSResourceFilter;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Test;


/**
 * Unit test for {@link VCSCommitImp}.
 * <p>
 * 
 * @author Elvis Ligu
 * 
 */
public class VCSCommitImpTest extends VCSTest {
   
   /**
    * Test method for {@link VCSCommitImp#walkChanges(VCSCommit, Visitor)} and
    * {@link VCSCommitImp#walkFileChanges(VCSCommit, Visitor)}.
    * <p>
    */
   @Test
   public void testWalkChanges() throws Exception {

      // Get repo
      final VCSRepository repo = this.UTILS.smallRepo();

      /*
       * We will check all change sets from ChangeMock. The following will walk
       * all changes, limit on path will perform if it is applicable.
       */
      final List<ChangeMock.ChangeSet> changeSets = Arrays.asList(
            ChangeMock.CHANGES1, ChangeMock.CHANGES2, ChangeMock.CHANGES3);

      for (final ChangeMock.ChangeSet changeSet : changeSets) {

         // Get old and new commits from current change set
         final VCSCommit newC = repo.resolveCommit(changeSet.newCommit);
         assertNotNull(newC);
         final VCSCommit oldC = repo.resolveCommit(changeSet.oldCommit);
         assertNotNull(oldC);
         
         // Empty change list where the changes will be stored
         final List<VCSChange<?>> changes = new ArrayList<VCSChange<?>>();
         // Change mocks from the current change set
         final List<ChangeMock> mocks = changeSet.changes();

         // A visitor to collect the changes
         // Will collect each change and put it in changes list
         final ChangeVisitor<VCSChange<?>> visitor = new ChangeVisitor<VCSChange<?>>() {

            @Override
            public boolean visit(final VCSChange<?> change) {
               changes.add(change);
               return true;
            }

            @Override
            public VCSResourceFilter<VCSResource> getResourceFilter() {
               if (changeSet.paths != null) {
                  return ResourceFilterUtility.prefix(changeSet.paths);
               }
               return null;
            }

            @Override
            public <F extends VCSFilter<VCSChange<?>>> F getFilter() {
               return null;
            }
         };

         // Perform a walk on changes and check the results to be equal to
         // mocks
         this.testWalkChanges(newC, oldC, mocks, changes, visitor);

         // Empty change list where the changes will be stored
         final List<VCSFileDiff<?>> fileChanges = new ArrayList<VCSFileDiff<?>>();

         // A visitor to collect the changes
         // Will collect each change and put it in changes list
         final ChangeVisitor<VCSFileDiff<?>> fileVisitor = new ChangeVisitor<VCSFileDiff<?>>() {

            @Override
            public boolean visit(final VCSFileDiff<?> change) {
               fileChanges.add(change);
               return true;
            }

            @Override
            public VCSResourceFilter<VCSResource> getResourceFilter() {
               if (changeSet.paths != null) {
                  return ResourceFilterUtility.prefix(changeSet.paths);
               }
               return null;
            }

            @Override
            public <F extends VCSFilter<VCSFileDiff<?>>> F getFilter() {
               return null;
            }
         };

         // Perform a walk on changes and check the results to be equal to
         // mocks
         this.testWalkFileChanges(newC, oldC, mocks, fileChanges, fileVisitor);
      }
   }

   /**
    * Given old and new commits, a list of change mocks and a list of changes,
    * it will call {@link VCSCommit#walkChanges(VCSCommit, Visitor)} from new to
    * old and collect all changes, then will check whether changes list is equal
    * to mocks list.
    * 
    * @param c1
    *           new commit
    * @param c2
    *           old commit
    * @param mocks
    *           a list to check the results against
    * @param changes
    *           the list were the changes will be collected
    * @param visitor
    *           will collect the results
    * @throws VCSRepositoryException
    */
   private void testWalkChanges(final VCSCommit c1, final VCSCommit c2,
         final List<ChangeMock> mocks, final List<VCSChange<?>> changes,
         final ChangeVisitor<VCSChange<?>> visitor)
         throws VCSRepositoryException {

      // Walk on changes
      c1.walkChanges(c2, visitor);
      // check the results
      this.checkChangeResults(mocks, changes);
   }

   /**
    * Given old and new commits, a list of change mocks and a list of changes,
    * it will call {@link VCSCommit#walkFileChanges(VCSCommit, Visitor)} from
    * new to old and collect all changes, then will check whether changes list
    * is equal to mocks list.
    * 
    * @param c1
    *           new commit
    * @param c2
    *           old commit
    * @param mocks
    *           a list to check the results against
    * @param changes
    *           the list were the changes will be collected
    * @param visitor
    *           will collect the results
    * @throws VCSRepositoryException
    */
   private void testWalkFileChanges(final VCSCommit c1, final VCSCommit c2,
         final List<ChangeMock> mocks, final List<VCSFileDiff<?>> changes,
         final ChangeVisitor<VCSFileDiff<?>> visitor)
         throws VCSRepositoryException {

      c1.walkFileChanges(c2, visitor);
      this.checkChangeResults(mocks, changes);
   }

   /**
    * Will check if results are equals to mocks.
    * <p>
    * 
    * @param mocks
    * @param changes
    */
   private void checkChangeResults(final List<ChangeMock> mocks,
         @SuppressWarnings("rawtypes") final List changes) {
      // Check if we have the expected number of changes
      assertEquals(mocks.size(), changes.size());

      // Compare two lists of mocks and changes if they are equal
      @SuppressWarnings("rawtypes")
      final boolean equals = TestUtils.equals(mocks, changes, new Comparator() {

         @Override
         public int compare(final Object o1, final Object o2) {

            final ChangeMock mock = (o1 instanceof ChangeMock ? (ChangeMock) o1
                  : (ChangeMock) o2);
            final VCSChange<?> change = (o1 instanceof VCSChange ? (VCSChange<?>) o1
                  : (VCSChange<?>) o2);
            return (TestUtils.equals(mock, change) ? 0 : -1);
         }
      });

      assertTrue(equals);
   }

   /**
    * Test method for {@link VCSCommitImp#getChanges(VCSCommit)},
    * {@link VCSCommitImp#getChanges(VCSCommit, boolean, String...)}
    * {@link VCSCommitImp#getFileChanges(VCSCommit)},
    * {@link VCSCommitImp#getFileChanges(VCSCommit, boolean, String...)}.
    * <p>
    */
   @Test
   public void testGetChangesVCSCommit() throws Exception {

      // Get repo
      final VCSRepository repo = this.UTILS.smallRepo();

      /*
       * We will check all change sets from ChangeMock. The following will walk
       * all changes, limit on path will perform when it is applicable.
       */
      final List<ChangeMock.ChangeSet> changeSets = Arrays.asList(
            ChangeMock.CHANGES1, ChangeMock.CHANGES2, ChangeMock.CHANGES3);

      for (final ChangeMock.ChangeSet changeSet : changeSets) {

         // Get old and new commits from current change set
         final VCSCommit newC = repo.resolveCommit(changeSet.newCommit);
         final VCSCommit oldC = repo.resolveCommit(changeSet.oldCommit);

         // Change mocks from the current change set
         final List<ChangeMock> mocks = changeSet.changes();

         // Perform a walk on changes and check the results to be equal to
         // mocks
         this.testGetChanges(newC, oldC, mocks, true, changeSet.paths);
      }
   }

   /**
    * Given old and new commits, a list of change mocks and a list of changes,
    * it will call {@link VCSCommit#getChanges(VCSCommit, boolean, String...)}
    * from new to old and collect all changes, then will check whether changes
    * list is equal to mocks list.
    * 
    * @param c1
    *           new commit
    * @param c2
    *           old commit
    * @param mocks
    *           a list to check the results against
    * @param recursive
    *           whether to check under a specified directory
    * @param paths
    *           if we need to limit the results
    * @throws VCSRepositoryException
    */
   private void testGetChanges(final VCSCommit c1, final VCSCommit c2,
         final List<ChangeMock> mocks, final boolean recursive,
         final String[] paths) throws VCSRepositoryException {

      final List<VCSChange<?>> changes = c1.getChanges(c2, recursive, paths);
      this.checkChangeResults(mocks, changes);

      final List<VCSFileDiff<?>> fileChanges = c1.getFileChanges(c2, recursive,
            paths);
      this.checkChangeResults(mocks, fileChanges);
   }

   /**
    * Test method for {@link VCSCommitImp#getNext()}.
    * <p>
    * This method will check both {@link VCSCommitImp#getNext()} and
    * {@link VCSCommitImp#getPrevious()}.
    */
   @Test
   public void testGetNext() throws Exception {

      // Get repo
      final VCSRepository repo = this.UTILS.smallRepo();

      for (final CommitMock mock : CommitMock.COMMITS) {

         final VCSCommit commit = repo.resolveCommit(mock.id);
         assertNotNull(commit);
         
         // Check if mock and commit have the same id,
         // have the same number of parents with the same ids,
         // and have the same number of children with the same ids.
         final boolean equals = TestUtils.equals(mock, commit);
         assertTrue(equals);
      }
   }

   /**
    * Test method for {@link VCSCommitImp#getResource(String)}.
    * <p>
    */
   @Test
   public void testGetResource() throws Exception {

      // Get repo
      final VCSRepository repo = this.UTILS.smallRepo();

      for (final ResourceMock resourceMock : ResourceMock.RESOURCES) {

         for (final CommitMock commitMock : resourceMock.commits()) {

            final VCSCommit commit = repo.resolveCommit(commitMock.id);
            assertNotNull(commit);

            final VCSResource resource = commit
                  .getResource(resourceMock.path());
            assertNotNull(resource);

            assertEquals(resourceMock.path(), resource.getPath());
         }
      }
   }

   /**
    * Test method for {@link VCSCommitImp#isResourceAvailable(String)}.
    * <p>
    */
   @Test
   public void testIsResourceAvailable() throws Exception {

      // Get repo
      final VCSRepository repo = this.UTILS.smallRepo();

      for (final ResourceMock resourceMock : ResourceMock.RESOURCES) {

         for (final CommitMock commitMock : resourceMock.commits()) {

            final VCSCommit commit = repo.resolveCommit(commitMock.id);
            assertNotNull(commit);

            assertTrue(commit.isResourceAvailable(resourceMock.path()));
         }

         for (final CommitMock commitMock : resourceMock.deleted()) {

            final VCSCommit commit = repo.resolveCommit(commitMock.id);
            assertNotNull(commit);

            assertTrue(!commit.isResourceAvailable(resourceMock.path()));
         }
      }
   }

   /**
    * Test method for {@link VCSCommitImp#walkTree(Visitor)}.
    * <p>
    */
   @Test
   public void testWalkTree() throws Exception {

      final VCSRepository repo = this.UTILS.smallRepo();

      /*
       * For each commit mock, get its commit object from repository. Walk tree
       * of commit object. Visiting each tree should check if the given resource
       * is available for the given commit.
       */
      for (final CommitMock commitMock : CommitMock.COMMITS) {

         final VCSCommit commit = repo.resolveCommit(commitMock.id);
         assertNotNull(commit);

         commit.walkTree(new ResourceVisitor<VCSResource>() {

            @Override
            public boolean visit(final VCSResource entity) {

               final ResourceMock resource = ResourceMock.resource(
                     entity.getPath(), entity.getCommit().getID());
               
               assertNotNull(resource);
               return true;
            }

            @SuppressWarnings("unchecked")
            @Override
            public VCSResourceFilter<VCSResource> getFilter() {
               return null;
            }

            @Override
            public boolean includeDirs() {
               return false;
            }

            @Override
            public boolean includeFiles() {
               return true;
            }
         });
      }
   }

   /**
    * Test method for {@link VCSCommitImp#checkout(String[])}.
    * <p>
    */
   @Test
   public void testCheckout() throws Exception {

      final VCSRepository repo = this.UTILS.smallRepo();
      final VCSRepository remoteRepo = new VCSRepositoryImp(
            RepoMock.REMOTE_GIT_SMALL, null);

      for (final CommitMock commitMock : CommitMock.COMMITS) {

         final VCSCommit commit = repo.resolveCommit(commitMock.id);
         assertNotNull(commit);

         // Construct the folder where to write the files of this commit
         final String dir = RepoMock.RESOURCES + "/c_"
               + commit.getID().substring(0, 6);

         // Perform the checkout
         commit.checkout(dir, (String[]) null);

         // Directory where the checkout will be written
         final File checkoutDir = new File(dir);

         // Collect the files that were checked out
         final List<String> paths = new ArrayList<String>();
         collectFiles(dir, checkoutDir, paths);

         // Check each created file if it should be there in this commit
         for (final String file : paths) {

            final ResourceMock resourceMock = ResourceMock.resource(file,
                  commit.getID());

            // If resource is not found the check will stop here
            // so we need to delete the directory
            if (resourceMock == null) {
               FileUtils.deleteQuietly(checkoutDir);
            }
            assertNotNull(resourceMock);

            // Now need to check the given files if they are available at
            // remote repo
            final VCSCommit remoteCommit = remoteRepo
                  .resolveCommit(commitMock.id);
            assertNotNull(remoteCommit);

            assertTrue(remoteCommit.isResourceAvailable(resourceMock.path()));
            final VCSResource remoteResource = remoteCommit
                  .getResource(resourceMock.path());
            assertNotNull(remoteResource);

         }

         // Cleanup the checked out directory
         FileUtils.deleteQuietly(checkoutDir);
      }
   }

   /**
    * Will collect all file path recursively. Each collected will be removed the
    * prefix <code>prefixRemove</code>.
    * <p>
    * 
    * @param prefixRemove
    *           prefix to remove for each collected path
    * @param file
    *           to collect file paths
    * @param paths
    *           the list where paths will be stored
    */
   private static void collectFiles(final String prefixRemove, final File file,
         final List<String> paths) {

      if (file.isFile()) {
         paths.add(file.getPath().substring(prefixRemove.length()));
         return;
      }

      if (file.isDirectory()) {
         if (file.getPath().equals(".git")) {
            return;
         }
         for (final File path : file.listFiles()) {
            collectFiles(prefixRemove, path, paths);
         }
      }
   }

   /**
    * Test method for {@link VCSCommitImp#getPrevious()}.
    * <p>
    */
   @Test
   public void testGetPrevious() throws Exception {

      final VCSRepository repo = this.UTILS.smallRepo();
      for (final CommitMock mock : CommitMock.COMMITS) {

         final VCSCommit commit = repo.resolveCommit(mock.id);
         assertNotNull(commit);

         assertEquals(mock.id, commit.getID());

         final Collection<VCSCommit> previous = commit.getPrevious();
         assertEquals(mock.parents().size(), commit.getPrevious().size());

         for (final VCSCommit parent : previous) {
            assertTrue(mock.isParent(parent.getID()));
         }
      }
   }

   /**
    * Test method for {@link VCSCommitImp#walkCommits(Visitor, boolean)}.
    * <p>
    * 
    * @throws Exception
    */
   @Test
   public void testWalkBackCommits() throws Exception {

      final VCSRepository repo = this.UTILS.smallRepo();

      // For each commit in repository
      // collect all previous commits,
      // and check them with the previous mock commits
      // Here we are testing only a plain visitor which
      // should return all previous commits
      for (final CommitMock mock : CommitMock.COMMITS) {

         final VCSCommit commit = repo.resolveCommit(mock.id);
         assertNotNull(commit);

         assertEquals(mock.id, commit.getID());

         final List<VCSCommit> commits = new ArrayList<VCSCommit>();
         commit.walkCommits(new CommitVisitor<VCSCommit>() {

            @Override
            public boolean visit(final VCSCommit entity) {
               commits.add(entity);
               return true;
            }

            @Override
            public <R extends VCSResource> VCSResourceFilter<R> getResourceFilter() {
               return null;
            }

            @SuppressWarnings("unchecked")
            @Override
            public VCSCommitFilter<VCSCommit> getFilter() {
               return null;
            }
         }, true);

         final List<CommitMock> mocks = new ArrayList<CommitMock>();
         mocks.add(mock);
         collectPreviousCommits(mock, mocks);

         @SuppressWarnings("rawtypes")
         final boolean equals = TestUtils.equals(mocks, commits,
               new Comparator() {

                  @Override
                  public int compare(final Object arg0, final Object arg1) {

                     final CommitMock cm = (arg0 instanceof CommitMock) ? (CommitMock) arg0
                           : (CommitMock) arg1;
                     final VCSCommit c = (arg0 instanceof VCSCommit) ? (VCSCommit) arg0
                           : (VCSCommit) arg1;
                     return TestUtils.equals(cm, c) ? 0 : -1;
                  }

               });

         assertTrue(equals);
      }

      // For each resource in repository get the head commit,
      // then walk from head all commits that modify this resource,
      // and check the mocks if they indeed modify it.
      // Here we are testing PathLimitingVisitor and a ModifyingPathVisitor
      // if they works correctly.
      for (final ResourceMock rMock : ResourceMock.RESOURCES) {

         final CommitMock mock = rMock.head();
         final VCSCommit commit = repo.resolveCommit(mock.id);
         assertNotNull(commit);

         assertEquals(mock.id, commit.getID());

         commit.walkCommits(new CommitVisitor<VCSCommit>() {

            @Override
            public boolean visit(final VCSCommit entity) {
               
               assertTrue(rMock.isAdded(entity.getID())
                     || rMock.isDeleted(entity.getID())
                     || rMock.isModified(entity.getID()));
               return true;
            }

            @SuppressWarnings("unchecked")
            @Override
            public VCSResourceFilter<VCSResource> getResourceFilter() {
               return ResourceFilterUtility.and(ResourceFilterUtility.prefix(rMock.path()),
                     ResourceFilterUtility.modified());
            }

            @SuppressWarnings("unchecked")
            @Override
            public VCSCommitFilter<VCSCommit> getFilter() {
               return null;
            }
         }, true);
      }
   }

   private static void collectPreviousCommits(final CommitMock mock,
         final List<CommitMock> previous) {

      for (final CommitMock parent : mock.parents()) {

         previous.add(parent);
         collectPreviousCommits(parent, previous);
      }
   }
}
