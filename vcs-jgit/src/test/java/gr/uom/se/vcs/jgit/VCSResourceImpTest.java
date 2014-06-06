/**
 * 
 */
package gr.uom.se.vcs.jgit;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import gr.uom.se.vcs.VCSCommit;
import gr.uom.se.vcs.VCSRepository;
import gr.uom.se.vcs.VCSResource;
import gr.uom.se.vcs.jgit.VCSResourceImp;
import gr.uom.se.vcs.jgit.mocks.CommitMock;
import gr.uom.se.vcs.jgit.mocks.ResourceMock;
import gr.uom.se.vcs.walker.Visitor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import org.junit.Test;


/**
 * Unit test for {@link VCSResourceImp}.
 * <p>
 * 
 * @author Elvis Ligu
 * 
 */
public class VCSResourceImpTest extends VCSTest {

   /**
    * Test method for {@link VCSResourceImp#walkAllCommits(Visitor)} and
    * {@link VCSResourceImp#getAllCommits()}.
    * <p>
    */
   @SuppressWarnings("rawtypes")
   @Test
   public void testAllCommits() throws Exception {

      VCSRepository repo = UTILS.smallRepo();

      for (ResourceMock rm : ResourceMock.RESOURCES) {

         // Resolve the first commit where this resource is alive
         CommitMock cm = rm.commits()[0];
         VCSCommit commit = repo.resolveCommit(cm.id);
         assertNotNull(commit);

         // Resource must be available at the given commit
         assertTrue(commit.isResourceAvailable(rm.path()));

         // Resource must not be null taken from the commit
         VCSResource resource = commit.getResource(rm.path());
         assertNotNull(resource);

         // Collect all commits
         final List<VCSCommit> commitsWalked = new ArrayList<VCSCommit>();
         resource.walkAllCommits(new Visitor<VCSCommit>() {

            public boolean visit(VCSCommit entity) {
               commitsWalked.add(entity);
               return true;
            }

         });

         // Add all mocks that modify this commit (or create or delete)
         final List<CommitMock> mocks = new ArrayList<CommitMock>();
         mocks.addAll(Arrays.asList(rm.deleted()));
         mocks.addAll(Arrays.asList(rm.created()));
         mocks.addAll(Arrays.asList(rm.modified()));

         // The result walking should be all commits that creates, modify or
         // delete this resource
         boolean equals = TestUtils.equals(mocks, commitsWalked,
               new Comparator() {

                  public int compare(Object o1, Object o2) {

                     CommitMock mock = o1 instanceof CommitMock ? (CommitMock) o1
                           : (CommitMock) o2;
                     VCSCommit commit = o1 instanceof VCSCommit ? (VCSCommit) o1
                           : (VCSCommit) o2;
                     return TestUtils.equals(mock, commit) ? 0 : -1;
                  }

               });
         assertTrue(equals);

         // Now check get all commits
         // This method should work the same as walk all commits
         final Collection<VCSCommit> commitsGet = resource.getAllCommits();

         // The result walking should be all commits that creates, modify or
         // delete this resource
         equals = TestUtils.equals(mocks, commitsGet, new Comparator() {

            public int compare(Object o1, Object o2) {

               CommitMock mock = o1 instanceof CommitMock ? (CommitMock) o1
                     : (CommitMock) o2;
               VCSCommit commit = o1 instanceof VCSCommit ? (VCSCommit) o1
                     : (VCSCommit) o2;
               return TestUtils.equals(mock, commit) ? 0 : -1;
            }

         });
         assertTrue(equals);
      }
   }

   /**
    * Test method for {@link VCSResourceImp#getCreationCommits()} and
    * {@link VCSResourceImp#getDeletionCommits()}.
    * <p>
    */
   @SuppressWarnings("rawtypes")
   @Test
   public void testGetCreationDeletionCommits() throws Exception {

      VCSRepository repo = UTILS.smallRepo();

      for (ResourceMock rm : ResourceMock.RESOURCES) {

         // Resolve the first commit where this resource is alive
         CommitMock cm = rm.commits()[0];
         VCSCommit commit = repo.resolveCommit(cm.id);
         assertNotNull(commit);

         // Resource must be available at the given commit
         assertTrue(commit.isResourceAvailable(rm.path()));

         // Resource must not be null taken from the commit
         VCSResource resource = commit.getResource(rm.path());
         assertNotNull(resource);

         // Collect all commits
         final Collection<VCSCommit> commitsCreate = resource
               .getCreationCommits();

         // Add all mocks that modify this commit (or create or delete)
         final List<CommitMock> mocks = new ArrayList<CommitMock>();
         mocks.addAll(Arrays.asList(rm.created()));

         // The result walking should be all commits that creates this resource
         boolean equals = TestUtils.equals(mocks, commitsCreate,
               new Comparator() {

                  public int compare(Object o1, Object o2) {

                     CommitMock mock = o1 instanceof CommitMock ? (CommitMock) o1
                           : (CommitMock) o2;
                     VCSCommit commit = o1 instanceof VCSCommit ? (VCSCommit) o1
                           : (VCSCommit) o2;
                     return TestUtils.equals(mock, commit) ? 0 : -1;
                  }

               });
         assertTrue(equals);

         // Now check get all commits
         // This method should work the same as walk all commits
         final Collection<VCSCommit> commitsDelete = resource
               .getDeletionCommits();

         // Clear the mock list and add only the deleting commits
         mocks.clear();
         mocks.addAll(Arrays.asList(rm.deleted()));

         // The result walking should be all commits that creates, modify or
         // delete this resource
         equals = TestUtils.equals(mocks, commitsDelete, new Comparator() {

            public int compare(Object o1, Object o2) {

               CommitMock mock = o1 instanceof CommitMock ? (CommitMock) o1
                     : (CommitMock) o2;
               VCSCommit commit = o1 instanceof VCSCommit ? (VCSCommit) o1
                     : (VCSCommit) o2;
               return TestUtils.equals(mock, commit) ? 0 : -1;
            }

         });
         assertTrue(equals);
      }
   }

   /**
    * Test method for {@link VCSResourceImp#isModified()} and
    * {@link VCSResourceImp#isAdded()}.
    */
   @Test
   public void testIsModified() throws Exception {
      VCSRepository repo = UTILS.smallRepo();

      for (ResourceMock rm : ResourceMock.RESOURCES) {

         for (CommitMock cm : rm.modified()) {

            VCSCommit commit = repo.resolveCommit(cm.id);
            assertNotNull(commit);

            assertTrue(commit.isResourceAvailable(rm.path()));

            VCSResource resource = commit.getResource(rm.path());
            assertNotNull(resource);

            assertTrue(resource.isModified());
         }

         for (CommitMock cm : rm.created()) {

            VCSCommit commit = repo.resolveCommit(cm.id);
            assertNotNull(commit);

            assertTrue(commit.isResourceAvailable(rm.path()));

            VCSResource resource = commit.getResource(rm.path());
            assertNotNull(resource);

            assertTrue(resource.isAdded());
         }
      }
   }
}
