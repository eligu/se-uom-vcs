/**
 * 
 */
package se.uom.vcs.jgit.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import se.uom.vcs.VCSCommit;
import se.uom.vcs.VCSDirectory;
import se.uom.vcs.VCSFile;
import se.uom.vcs.VCSResource;
import se.uom.vcs.exceptions.VCSRepositoryException;
import se.uom.vcs.exceptions.VCSResourceNotFoundException;
import se.uom.vcs.jgit.VCSDirectoryImp;
import se.uom.vcs.walker.ResourceVisitor;
import se.uom.vcs.walker.Visitor;
import se.uom.vcs.walker.filter.resource.VCSResourceFilter;

import static se.uom.vcs.walker.filter.resource.ResourceFilterUtility.*;

/**
 * A test case that conducts tests on a real repository, for VCSResourceImp and
 * its derivatives.
 * <p>
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public class VCSResourceImpTest extends MainSuite {

   @Test
   public void testSimpleGetters() throws VCSRepositoryException,
         VCSResourceNotFoundException {

      // The following commit adds two files FreeLancerAPI.java and
      // FreelancerExample.java
      VCSCommit commit = thisRepo
            .resolveCommit("3132a9dfe7f9db00edbf9b19f132b64a6b25cc9c");
      assertNotNull(commit);

      // The resource at this path must be a file
      VCSResource resource = commit
            .getResource("src/main/java/org/scribe/builder/api/FreelancerAPI.java");
      assertNotNull(resource);
      assertTrue(resource instanceof VCSFile);

      VCSFile file = (VCSFile) resource;

      // This file is only added and renamed at last commit.
      // There are 4 commits that modify this path,
      // including the one that adds it and the one that deletes it.
      assertTrue(file.getAllCommits().size() == 4);

      assertTrue(file.getCommit().equals(commit));

      // Must have a file type
      assertTrue(file.getType().equals(VCSResource.Type.FILE));

      // There will be only a commit that creates this file
      Collection<VCSCommit> commits = file.getCreationCommits();
      assertTrue(commits.size() == 1);
      // The creation commit is the same as the commit this file is at
      assertTrue(commits.iterator().next().equals(commit));
      assertTrue(file.isAdded());
      assertTrue(!file.isModified());

      // The file name is changed at the last commit,
      // and this means this file is RENAMED. However,
      // that appears to JGit as a DELETE, and an ADD
      // for the new file.
      assertTrue(file.getDeletionCommits().size() == 1);

      // We now for sure that the above path was renamed (see below) at the
      // commit 1208fd..., however current implementation returns DELETE for
      // previous name, and ADD for current name.
      commit = thisRepo
            .resolveCommit("1208fd815ac023f5acf2de491badbe8b68f98c40");
      resource = commit
            .getResource("src/main/java/org/scribe/builder/api/FreelancerApi.java");
      assertTrue(resource.isAdded());

      // Just check the path
      assertEquals("src/main/java/org/scribe/builder/api/FreelancerApi.java",
            resource.getPath());
   }

   @Test
   public void walkCommits() throws VCSRepositoryException,
         VCSResourceNotFoundException {

      // Will get a resource and walk all its commits,
      // and check if the walked commits are the same as those that we know
      VCSCommit commit = thisRepo
            .resolveCommit("c4dbbcf98992d4f83f51e06ed2b71dc6f7eacb6a");
      assertNotNull(commit);

      assertTrue(commit
            .isResourceAvailable("src/main/java/org/scribe/oauth/OAuth10aServiceImpl.java"));

      VCSResource resource = commit
            .getResource("src/main/java/org/scribe/oauth/OAuth10aServiceImpl.java");

      // Ids that modify this resource
      Set<String> cIDs = new HashSet<String>();
      cIDs.add("71022007663ce4480c1e7ec79d762330d4e4207c");
      cIDs.add("86f12eb5bcc1059833c0b8b19d5eb0c2a1726eaa");
      cIDs.add("9a99a44c5f5a7e557f0a4bfa5d64a2587947b590");
      cIDs.add("09d85d6713af6c97ae7d4450f88ceac20c60e2fd");
      cIDs.add("97fa26fce79f98d467edc1997b1a49df162bdb19");
      cIDs.add("0a63f935d231eb50ba9db52c0735fbcb96e7589c");
      cIDs.add("109c93b4e4aa1d9e20b0344ad7286b8e7efd0e2c");
      cIDs.add("193dcd2e25efcca804632119f9585346e31dc1a9");
      cIDs.add("931236561a0c8f88cfb98ee4d208875a789d8253");
      cIDs.add("4f23cfaf20bc622f4a8ccf679e54a0bde3d75330");
      cIDs.add("d8ab4ba404ec0a6ee797bf7aa36d7f6e1d186be2");
      cIDs.add("5ac849313dcb248502df14939c850d2101d2a60b");
      cIDs.add("8f0760664f270fae50a7a1cc3a217a4806d95adc");
      cIDs.add("0be82d68323616d2312db447c4c09ad381c2a0e8");
      cIDs.add("806d548629193eecac15c32ba27c6d897287d11e");
      cIDs.add("aad8dac5802dd49d1febb5e2d14a059b2b9a02bb");
      cIDs.add("1f949971208519a1f73edcde3dd148aaf8d94c83");
      cIDs.add("bfbe536886fc3c614b785110f8eba663644862d7");

      final Set<String> other = new HashSet<String>();
      resource.walkAllCommits(new Visitor<VCSCommit>() {

         @Override
         public boolean visit(VCSCommit entity) {
            other.add(entity.getID());
            return true;
         }
      });

      assertTrue(other.containsAll(cIDs));
   }

   @Test
   public void testDirectory1() throws VCSRepositoryException,
         VCSResourceNotFoundException {

      String dirPath = "src/main/java/org/scribe/oauth";
      VCSCommit commit = thisRepo
            .resolveCommit("c4dbbcf98992d4f83f51e06ed2b71dc6f7eacb6a");

      assertTrue(commit.isResourceAvailable(dirPath));
      VCSDirectoryImp directory = (VCSDirectoryImp) commit.getResource(dirPath);
      assertNotNull(directory);

      // There are no directories within this path
      assertTrue(directory.getDirectories().isEmpty());

      // There are three files within this path
      Set<VCSFile> files = directory.getFiles();
      assertEquals(files.size(), 3);

      // Expected files should be
      Set<String> expectedFiles = new HashSet<String>();
      expectedFiles.add(directory.getPath() + "/" + "OAuth10aServiceImpl.java");
      expectedFiles.add(directory.getPath() + "/" + "OAuth20ServiceImpl.java");
      expectedFiles.add(directory.getPath() + "/" + "OAuthService.java");
      // Check now the returned files
      for (VCSFile file : files) {
         assertTrue(expectedFiles.contains(file.getPath()));
      }
   }

   @Test
   public void testDirectory2() throws VCSRepositoryException,
         VCSResourceNotFoundException {

      // Here we will check the main source directory
      String dirPath = "src/main/java/org/scribe";
      VCSCommit commit = thisRepo
            .resolveCommit("c4dbbcf98992d4f83f51e06ed2b71dc6f7eacb6a");

      assertTrue(commit.isResourceAvailable(dirPath));
      VCSDirectoryImp directory = (VCSDirectoryImp) commit.getResource(dirPath);
      assertNotNull(directory);

      Set<VCSDirectory> dirs = directory.getDirectories();

      // Expected dirs should be
      Set<String> expectedDirs = new HashSet<String>();
      expectedDirs.add(directory.getPath() + "/" + "builder");
      expectedDirs.add(directory.getPath() + "/" + "exceptions");
      expectedDirs.add(directory.getPath() + "/" + "extractors");
      expectedDirs.add(directory.getPath() + "/" + "model");
      expectedDirs.add(directory.getPath() + "/" + "oauth");
      expectedDirs.add(directory.getPath() + "/" + "services");
      expectedDirs.add(directory.getPath() + "/" + "utils");

      for (VCSDirectory dir : dirs) {
         assertTrue(expectedDirs.contains(dir.getPath()));
      }

      // Try walk 1
      // The following code must throw an exception, because the filter
      // doesn't allow to get resources under this directory
      boolean passed = false;
      try {
         directory.walkResources(new ResourceVisitor<VCSResource>() {

            @Override
            public boolean visit(VCSResource entity) {
               throw new IllegalStateException(entity + " shouldn't be visited");
            }

            @SuppressWarnings("unchecked")
            @Override
            public VCSResourceFilter<VCSResource> getFilter() {
               return prefix("src/test");
            }

            @Override
            public boolean includeDirs() {
               return true;
            }

            @Override
            public boolean includeFiles() {
               return true;
            }
         });
      } catch (IllegalArgumentException e) {
         passed = true;
      } finally {
         assertTrue(passed);
      }
   }
}
