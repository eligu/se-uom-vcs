package gr.uom.se.vcs.jgit.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import gr.uom.se.vcs.VCSBranch;
import gr.uom.se.vcs.VCSCommit;
import gr.uom.se.vcs.VCSTag;
import gr.uom.se.vcs.exceptions.VCSRepositoryException;

import java.util.Collection;

import org.junit.Test;


/**
 * A test case that conducts unit tests on a real remote repository.
 * <p>
 * 
 * Before any test execution, a remote repository will be created.
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public class VCSRepositoryImpTest extends MainSuite {

   /**
    * Check all branches.
    * <p>
    * The branches must be at least one, each returned branch must me resolved
    * given its ID.
    * 
    * @throws VCSRepositoryException
    */
   @Test
   public void testBranches() throws VCSRepositoryException {

      Collection<VCSBranch> branches = thisRepo.getBranches();
      assertTrue(branches.size() > 0);

      for (VCSBranch branch : branches) {

         String id = branch.getID();
         VCSBranch resolved = thisRepo.resolveBranch(id);
         assertNotNull(resolved);
         assertEquals(branch, resolved);
      }
   }

   /**
    * Will get all tags and check if they can resolve.
    * <p>
    * 
    * @throws VCSRepositoryException
    */
   @Test
   public void testTags() throws VCSRepositoryException {
      Collection<VCSTag> tags = thisRepo.getTags();

      for (VCSTag tag : tags) {

         String id = tag.getID();
         VCSTag resolved = thisRepo.resolveTag(id);
         assertNotNull(resolved);
         assertEquals(tag, resolved);
      }
   }

   /**
    * Test the remote and local paths and if repository is properly initialized.
    * <p>
    * 
    * When a repository is created, either it was already in the local path,
    * or it was cloned, it will try to select a default branch, a master branch.
    * @throws VCSRepositoryException 
    * 
    */
   @Test
   public void testInitialization() throws VCSRepositoryException {
      
      
      assertEquals(LOCAL_GIT_PATH, thisRepo.getLocalPath());
      assertEquals(REMOTE_GIT_PATH, thisRepo.getRemotePath());
      // We now for sure that repository has been loaded, because
      // either it was in the local path, or it has been cloned
      // when the tests started
      assertNotNull(thisRepo.getSelectedBranch());
      
      // There must be a first commit
      VCSCommit first = thisRepo.getFirst();
      assertNotNull(first);
      assertEquals(FIRST_REV_SHA, first.getID());
      
      VCSCommit otherFirst = thisRepo.resolveCommit(FIRST_REV_SHA);
      assertNotNull(otherFirst);
      assertEquals(first, otherFirst);
   }
}
