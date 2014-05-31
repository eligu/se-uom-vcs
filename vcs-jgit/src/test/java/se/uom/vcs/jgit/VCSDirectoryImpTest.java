/**
 * 
 */
package se.uom.vcs.jgit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Set;

import org.junit.Test;

import se.uom.vcs.VCSCommit;
import se.uom.vcs.VCSDirectory;
import se.uom.vcs.VCSFile;
import se.uom.vcs.VCSRepository;
import se.uom.vcs.VCSResource;
import se.uom.vcs.jgit.VCSDirectoryImp;
import se.uom.vcs.jgit.mocks.CommitMock;
import se.uom.vcs.jgit.mocks.ResourceMock;

/**
 * Unit test for {@link VCSDirectoryImp}.
 * <p>
 * 
 * @author Elvis Ligu
 * 
 */
public class VCSDirectoryImpTest extends VCSTest {

   /**
    * Test method for {@link VCSDirectoryImp#getFiles()}.
    * <p>
    */
   @Test
   public void testGetFiles() throws Exception {

      VCSRepository repo = UTILS.smallRepo();

      /*
       * For each dir mock resolve a commit where this dir is available, get the
       * VCSDirectory resource and call getFiles(). Check for each file that is
       * returned if it is contained in this commit.
       */
      for (ResourceMock dirMock : ResourceMock.DIRS) {

         CommitMock commitMock = dirMock.commits()[0];
         VCSCommit commit = repo.resolveCommit(commitMock.id);
         assertNotNull(commit);

         assertTrue(commit.isResourceAvailable(dirMock.path()));

         VCSResource resource = commit.getResource(dirMock.path());
         assertNotNull(resource);
         assertTrue(resource instanceof VCSDirectoryImp);

         VCSDirectory dir = (VCSDirectory) resource;

         Set<VCSFile> files = dir.getFiles();
         List<ResourceMock> mockFiles = ResourceMock.resources(dirMock.path(),
               commitMock.id);
         assertEquals(mockFiles.size(), files.size());

         for (VCSFile file : files) {
            ResourceMock rf = ResourceMock.resource(file.getPath(),
                  commitMock.id);
            assertNotNull(rf);
            assertTrue(rf.isChildOf(dirMock));
         }
      }
   }

   /**
    * Test method for {@link VCSDirectoryImp#getDirectories()}.
    */
   @Test
   public void testGetDirectories() throws Exception {

      VCSRepository repo = UTILS.smallRepo();
      for (ResourceMock dirMock : ResourceMock.DIRS) {

         CommitMock commitMock = dirMock.commits()[dirMock.commits().length - 1];
         VCSCommit commit = repo.resolveCommit(commitMock.id);
         assertNotNull(commit);

         assertTrue(commit.isResourceAvailable(dirMock.path()));
         VCSResource resources = commit.getResource(dirMock.path());
         assertNotNull(resources);
         assertTrue(resources instanceof VCSDirectoryImp);

         VCSDirectory dir = (VCSDirectory) resources;
         Set<VCSDirectory> dirs = dir.getDirectories();

         for (VCSDirectory d : dirs) {

            ResourceMock rd = ResourceMock.dir(d.getPath(), commitMock.id);
            assertNotNull(rd);
            assertTrue(rd.isChildOf(dirMock));
         }
      }
   }
}
