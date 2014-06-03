/**
 * 
 */
package se.uom.vcs.jgit.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import se.uom.vcs.VCSChange;
import se.uom.vcs.VCSCommit;
import se.uom.vcs.VCSFileDiff;
import se.uom.vcs.VCSResource;
import se.uom.vcs.exceptions.VCSRepositoryException;
import se.uom.vcs.walker.ChangeVisitor;
import se.uom.vcs.walker.filter.VCSFilter;
import se.uom.vcs.walker.filter.resource.VCSResourceFilter;

import static se.uom.vcs.walker.filter.resource.ResourceFilterUtility.*;

/**
 * A test case that conducts tests on a real repository, for VCSChangeImp and
 * its derivatives.
 * <p>
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public class VCSChangeImpTest extends MainSuite {

   @Test
   public void testGetChangesFileAddition() throws VCSRepositoryException {

      // The following commit adds two files FreeLancerAPI.java and
      // FreelancerExample.java
      VCSCommit commit = thisRepo
            .resolveCommit("3132a9dfe7f9db00edbf9b19f132b64a6b25cc9c");
      assertNotNull(commit);

      String path1 = "src/main/java/org/scribe/builder/api/FreelancerAPI.java";
      String path2 = "src/test/java/org/scribe/examples/FreelancerExample.java";

      List<VCSChange<?>> changes = commit.getChanges(commit);
      // This must be empty because we are producing changes with the same
      // commit
      assertTrue(changes.isEmpty());

      // The parent of commit
      VCSCommit oldCommit = thisRepo
            .resolveCommit("675216792546e7a636ee6186be82fbccab474873");

      changes = commit.getChanges(oldCommit);
      // There are only two changes (two additions)
      assertEquals(2, changes.size());

      // Lets test the changes
      for (VCSChange<?> change : changes) {
         assertEquals(commit, change.getNewCommit());
         assertNull(change.getOldCommit());
         assertNull(change.getOldResource());
         assertNotNull(change.getNewResource());
         assertEquals(VCSChange.Type.ADDED, change.getType());
         assertFalse(oldCommit.isResourceAvailable(change.getNewResource()
               .getPath()));
         assertTrue(commit.isResourceAvailable(change.getNewResource()
               .getPath()));

         String path = change.getNewResource().getPath();
         assertTrue(path.equals(path1) || path.equals(path2));
      }

      // This is not actually a test, but its meant to check each time the
      // implementation because the file changes and general changes
      // return always changes on files. This is due to git not keeping
      // track on directories
      List<VCSFileDiff<?>> fileChanges = commit.getFileChanges(oldCommit);
      assertEquals(fileChanges, changes);

      // Now check all the paths under the specified path
      // These two checks return the same as plain getChanges because
      // there are no paths specified
      assertEquals(changes, commit.getChanges(oldCommit, true, (String[]) null));
      assertEquals(fileChanges,
            commit.getFileChanges(oldCommit, true, (String[]) null));

      // Will return only one change and that would be the added file
      // at "src/main/java/org/scribe/builder/api/FreelancerAPI.java"
      changes = commit.getChanges(oldCommit, true, "src/main");
      fileChanges = commit.getFileChanges(oldCommit, true, "src/main");
      // The two sets of changes will be the same
      assertEquals(changes, fileChanges);
      // There will be only one change
      assertEquals(1, changes.size());
      VCSChange<?> change = changes.get(0);
      assertEquals("src/main/java/org/scribe/builder/api/FreelancerAPI.java",
            change.getNewResource().getPath());
      assertEquals(VCSChange.Type.ADDED, change.getType());
      assertNull(change.getOldResource());
      assertNull(change.getOldCommit());
   }

   @Test
   public void walkChangesFileModification() throws VCSRepositoryException {

      // The following commit modifies three files
      VCSCommit commit = thisRepo
            .resolveCommit("d721204939523368794b62bd760e72ed15b40960");
      assertNotNull(commit);

      VCSCommit oldCommit = thisRepo
            .resolveCommit("0a45342004252c91264bd400cf108187088cbd60");
      assertNotNull(oldCommit);

      String path1 = "src/main/java/org/scribe/builder/api/EvernoteApi.java";
      String path2 = "src/main/java/org/scribe/extractors/TokenExtractorImpl.java";
      String path3 = "src/test/java/org/scribe/extractors/TokenExtractorTest.java";

      final List<VCSChange<?>> changes = new ArrayList<VCSChange<?>>();
      commit.walkChanges(oldCommit, new ChangeVisitor<VCSChange<?>>() {

         @Override
         public <F extends VCSFilter<VCSChange<?>>> F getFilter() {
            return null;
         }

         @Override
         public boolean visit(VCSChange<?> entity) {
            changes.add(entity);
            return true;
         }

         @Override
         public <R extends VCSResource> VCSResourceFilter<R> getResourceFilter() {
            return null;
         }
      });

      assertEquals(3, changes.size());
      for (VCSChange<?> change : changes) {
         assertNotNull(change.getNewCommit());
         assertNotNull(change.getNewResource());
         assertNotNull(change.getOldCommit());
         assertNotNull(change.getOldResource());
         assertEquals(VCSChange.Type.MODIFIED, change.getType());
         assertEquals(change.getNewResource().getPath(), change
               .getOldResource().getPath());
         assertTrue(!change.getNewCommit().equals(change.getOldCommit()));

         String path = change.getNewResource().getPath();
         assertTrue(path.equals(path1) || path.equals(path2)
               || path.equals(path3));
      }

      final List<VCSFileDiff<?>> fileChanges = new ArrayList<VCSFileDiff<?>>();
      commit.walkFileChanges(oldCommit, new ChangeVisitor<VCSFileDiff<?>>() {

         @Override
         public <F extends VCSFilter<VCSFileDiff<?>>> F getFilter() {
            return null;
         }

         @Override
         public boolean visit(VCSFileDiff<?> entity) {
            fileChanges.add(entity);
            return true;
         }

         @Override
         public <R extends VCSResource> VCSResourceFilter<R> getResourceFilter() {
            return null;
         }
      });

      assertEquals(changes, fileChanges);
   }

   @Test
   public void walkChangesWithChangeFilterFileModification()
         throws VCSRepositoryException {

      // The following commit modifies three files
      VCSCommit commit = thisRepo
            .resolveCommit("d721204939523368794b62bd760e72ed15b40960");
      assertNotNull(commit);

      VCSCommit oldCommit = thisRepo
            .resolveCommit("0a45342004252c91264bd400cf108187088cbd60");
      assertNotNull(oldCommit);

      final List<VCSChange<?>> changes = new ArrayList<VCSChange<?>>();

      commit.walkChanges(oldCommit, new ChangeVisitor<VCSChange<?>>() {

         @SuppressWarnings("unchecked")
         @Override
         public <F extends VCSFilter<VCSChange<?>>> F getFilter() {
            return (F) new VCSFilter<VCSChange<?>>() {

               @Override
               public boolean include(VCSChange<?> entity) {
                  if (entity.getType().isAdd())
                     return true;
                  return false;
               }
            };
         }

         @Override
         public boolean visit(VCSChange<?> entity) {
            changes.add(entity);
            return true;
         }

         @Override
         public <R extends VCSResource> VCSResourceFilter<R> getResourceFilter() {
            return null;
         }
      });

      assertEquals(0, changes.size());

      final List<VCSFileDiff<?>> fileChanges = new ArrayList<VCSFileDiff<?>>();
      commit.walkFileChanges(oldCommit, new ChangeVisitor<VCSFileDiff<?>>() {

         @SuppressWarnings("unchecked")
         @Override
         public <F extends VCSFilter<VCSFileDiff<?>>> F getFilter() {

            return (F) new VCSFilter<VCSFileDiff<?>>() {

               @Override
               public boolean include(VCSFileDiff<?> entity) {
                  if (entity.getType().isModify())
                     return true;
                  return false;
               }
            };
         }

         @Override
         public boolean visit(VCSFileDiff<?> entity) {
            fileChanges.add(entity);
            return true;
         }

         @Override
         public <R extends VCSResource> VCSResourceFilter<R> getResourceFilter() {
            return null;
         }
      });

      String path1 = "src/main/java/org/scribe/builder/api/EvernoteApi.java";
      String path2 = "src/main/java/org/scribe/extractors/TokenExtractorImpl.java";
      String path3 = "src/test/java/org/scribe/extractors/TokenExtractorTest.java";

      assertEquals(3, fileChanges.size());
      for (VCSFileDiff<?> change : fileChanges) {
         assertNotNull(change.getNewCommit());
         assertNotNull(change.getNewResource());
         assertNotNull(change.getOldCommit());
         assertNotNull(change.getOldResource());
         assertEquals(VCSChange.Type.MODIFIED, change.getType());
         assertEquals(change.getNewResource().getPath(), change
               .getOldResource().getPath());
         assertTrue(!change.getNewCommit().equals(change.getOldCommit()));

         String path = change.getNewResource().getPath();
         assertTrue(path.equals(path1) || path.equals(path2)
               || path.equals(path3));
      }
   }

   @Test
   public void testWalkChangesWithResourceFilter() throws VCSRepositoryException {

      // The following commit modifies three files
      VCSCommit commit = thisRepo
            .resolveCommit("d721204939523368794b62bd760e72ed15b40960");
      assertNotNull(commit);

      VCSCommit oldCommit = thisRepo
            .resolveCommit("0a45342004252c91264bd400cf108187088cbd60");
      assertNotNull(oldCommit);

      final List<VCSChange<?>> changes = new ArrayList<VCSChange<?>>();

      // We will create a very strange resource filter
      @SuppressWarnings("unchecked")
      final VCSResourceFilter<VCSResource> filter = or(
            and(prefix("src/main/java"), suffix(".java")),
            path("some/strange/path"));
      
      commit.walkChanges(oldCommit, new ChangeVisitor<VCSChange<?>>() {

         @Override
         public <F extends VCSFilter<VCSChange<?>>> F getFilter() {
            return null;
         }

         @Override
         public boolean visit(VCSChange<?> entity) {
            changes.add(entity);
            return true;
         }

         @SuppressWarnings("unchecked")
         @Override
         public <R extends VCSResource> VCSResourceFilter<R> getResourceFilter() {
            return (VCSResourceFilter<R>) filter;
         }
      });
      
      String path1 = "src/main/java/org/scribe/builder/api/EvernoteApi.java";
      String path2 = "src/main/java/org/scribe/extractors/TokenExtractorImpl.java";
      String path3 = "src/test/java/org/scribe/extractors/TokenExtractorTest.java";

      assertEquals(2, changes.size());
      for (VCSChange<?> change : changes) {
         assertNotNull(change.getNewCommit());
         assertNotNull(change.getNewResource());
         assertNotNull(change.getOldCommit());
         assertNotNull(change.getOldResource());
         assertEquals(VCSChange.Type.MODIFIED, change.getType());
         assertEquals(change.getNewResource().getPath(), change
               .getOldResource().getPath());
         assertTrue(!change.getNewCommit().equals(change.getOldCommit()));

         String path = change.getNewResource().getPath();
         assertTrue((path.equals(path1) || path.equals(path2))
               && !path.equals(path3));
      }
   }
}
