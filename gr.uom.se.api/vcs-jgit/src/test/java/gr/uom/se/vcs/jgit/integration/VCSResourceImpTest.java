/**
 * 
 */
package gr.uom.se.vcs.jgit.integration;

import static gr.uom.se.vcs.walker.filter.resource.ResourceFilterUtility.and;
import static gr.uom.se.vcs.walker.filter.resource.ResourceFilterUtility.child;
import static gr.uom.se.vcs.walker.filter.resource.ResourceFilterUtility.or;
import static gr.uom.se.vcs.walker.filter.resource.ResourceFilterUtility.path;
import static gr.uom.se.vcs.walker.filter.resource.ResourceFilterUtility.prefix;
import static gr.uom.se.vcs.walker.filter.resource.ResourceFilterUtility.suffix;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import gr.uom.se.vcs.VCSCommit;
import gr.uom.se.vcs.VCSDirectory;
import gr.uom.se.vcs.VCSFile;
import gr.uom.se.vcs.VCSResource;
import gr.uom.se.vcs.exceptions.VCSRepositoryException;
import gr.uom.se.vcs.exceptions.VCSResourceNotFoundException;
import gr.uom.se.vcs.jgit.VCSDirectoryImp;
import gr.uom.se.vcs.walker.ResourceVisitor;
import gr.uom.se.vcs.walker.Visitor;
import gr.uom.se.vcs.walker.filter.resource.AbstractPathFilter;
import gr.uom.se.vcs.walker.filter.resource.VCSResourceFilter;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;


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

   @Test
   public void walkTreeFiles() throws VCSRepositoryException,
         VCSResourceNotFoundException {

      // Here we will check the main source directory
      VCSCommit commit = thisRepo
            .resolveCommit("c4dbbcf98992d4f83f51e06ed2b71dc6f7eacb6a");

      assertTrue(commit.isResourceAvailable(dirPath));
      VCSDirectoryImp directory = (VCSDirectoryImp) commit.getResource(dirPath);
      assertNotNull(directory);

      // Walk 2
      // We will try to get only files (recursively) that are under this path
      directory.walkResources(new ResourceVisitor<VCSResource>() {

         @Override
         public boolean visit(VCSResource entity) {
            assertTrue(entity instanceof VCSFile);
            return true;
         }

         @SuppressWarnings("unchecked")
         @Override
         public VCSResourceFilter<VCSResource> getFilter() {
            return null;
         }

         @Override
         public boolean includeDirs() {
            // Do not allow dirs
            return false;
         }

         @Override
         public boolean includeFiles() {
            // Allow files
            return true;
         }
      });
   }

   @Test
   public void walkTreeDirs() throws VCSRepositoryException,
         VCSResourceNotFoundException {

      // Here we will check the main source directory
      VCSCommit commit = thisRepo
            .resolveCommit("c4dbbcf98992d4f83f51e06ed2b71dc6f7eacb6a");

      assertTrue(commit.isResourceAvailable(dirPath));
      VCSDirectoryImp directory = (VCSDirectoryImp) commit.getResource(dirPath);
      assertNotNull(directory);

      // Walk 3
      // We will try to get only dirs
      directory.walkResources(new ResourceVisitor<VCSResource>() {

         @Override
         public boolean visit(VCSResource entity) {
            assertTrue(entity instanceof VCSDirectory);
            return true;
         }

         @SuppressWarnings("unchecked")
         @Override
         public VCSResourceFilter<VCSResource> getFilter() {
            return null;
         }

         @Override
         public boolean includeDirs() {
            // Allow dirs to be visited
            return true;
         }

         @Override
         public boolean includeFiles() {
            // Do not allow files to be visited
            return false;
         }
      });
   }

   @Test
   public void walkTreeCheckResources() throws VCSRepositoryException,
         VCSResourceNotFoundException {

      // Here we will check the main source directory
      VCSCommit commit = thisRepo
            .resolveCommit("c4dbbcf98992d4f83f51e06ed2b71dc6f7eacb6a");

      assertTrue(commit.isResourceAvailable(dirPath));
      VCSDirectoryImp directory = (VCSDirectoryImp) commit.getResource(dirPath);
      assertNotNull(directory);

      // Walk 2
      // We will try to get all resources (recursively) under path
      directory.walkResources(new ResourceVisitor<VCSResource>() {

         @Override
         public boolean visit(VCSResource entity) {
            assertTrue(AbstractPathFilter.isPrefix(dirPath, entity.getPath()));
            return true;
         }

         @SuppressWarnings("unchecked")
         @Override
         public VCSResourceFilter<VCSResource> getFilter() {
            return null;
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
   }

   @SuppressWarnings("unchecked")
   @Test
   public void walkTreeFilters() throws VCSRepositoryException,
         VCSResourceNotFoundException {

      // Here we will check the main source directory
      VCSCommit commit = thisRepo
            .resolveCommit("c4dbbcf98992d4f83f51e06ed2b71dc6f7eacb6a");

      assertTrue(commit.isResourceAvailable(dirPath));
      VCSDirectoryImp directory = (VCSDirectoryImp) commit.getResource(dirPath);
      assertNotNull(directory);

      // A filter that allows all children of .../builder with suffix .java
      // and all .java files that are under .../oauth
      // and the single path .../builder/api/ImgUrApi.java
      final VCSResourceFilter<VCSResource> filter = or(
            and(child("src/main/java/org/scribe/builder"), suffix(".java")),
            and(prefix("src/main/java/org/scribe/oauth"), suffix(".java")),
            path("src/main/java/org/scribe/builder/api/ImgUrApi.java"),
            // This filter will not be considered because we are querying under
            // the specified directory
            prefix("src/test"));

      // Walk 2
      // We will try to get only files but apply a filter so we can 
      // check the consistency of implementation
      directory.walkResources(new ResourceVisitor<VCSResource>() {

         @Override
         public boolean visit(VCSResource entity) {

            assertTrue(AbstractPathFilter.isPrefix(dirPath, entity.getPath()));
            assertTrue(entity.getPath().endsWith(".java"));
            
            String parentPath = entity.getParent().getPath();
            boolean passed = parentPath.equals("src/main/java/org/scribe/builder")
                  || parentPath.equals("src/main/java/org/scribe/oauth")
                  || entity.getPath().equals("src/main/java/org/scribe/builder/api/ImgUrApi.java");
            
            assertTrue(passed);
            
            return true;
         }

         @Override
         public VCSResourceFilter<VCSResource> getFilter() {
            return filter;
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

   static final String dirPath = "src/main/java/org/scribe";
   static final Set<String> resources = new HashSet<String>();
   static {
      resources.add("src/main/java/org/scribe/builder");
      resources.add("src/main/java/org/scribe/builder/ServiceBuilder.java");
      resources.add("src/main/java/org/scribe/builder/api");
      resources.add("src/main/java/org/scribe/builder/api/AWeberApi.java");
      resources.add("src/main/java/org/scribe/builder/api/Api.java");
      resources
            .add("src/main/java/org/scribe/builder/api/ConstantContactApi.java");
      resources
            .add("src/main/java/org/scribe/builder/api/ConstantContactApi2.java");
      resources.add("src/main/java/org/scribe/builder/api/DefaultApi10a.java");
      resources.add("src/main/java/org/scribe/builder/api/DefaultApi20.java");
      resources.add("src/main/java/org/scribe/builder/api/DiggApi.java");
      resources.add("src/main/java/org/scribe/builder/api/DropBoxApi.java");
      resources.add("src/main/java/org/scribe/builder/api/EvernoteApi.java");
      resources.add("src/main/java/org/scribe/builder/api/FacebookApi.java");
      resources.add("src/main/java/org/scribe/builder/api/FlickrApi.java");
      resources.add("src/main/java/org/scribe/builder/api/Foursquare2Api.java");
      resources.add("src/main/java/org/scribe/builder/api/FoursquareApi.java");
      resources.add("src/main/java/org/scribe/builder/api/FreelancerApi.java");
      resources.add("src/main/java/org/scribe/builder/api/GetGlueApi.java");
      resources.add("src/main/java/org/scribe/builder/api/GoogleApi.java");
      resources.add("src/main/java/org/scribe/builder/api/ImgUrApi.java");
      resources.add("src/main/java/org/scribe/builder/api/KaixinApi.java");
      resources.add("src/main/java/org/scribe/builder/api/KaixinApi20.java");
      resources.add("src/main/java/org/scribe/builder/api/LinkedInApi.java");
      resources.add("src/main/java/org/scribe/builder/api/LiveApi.java");
      resources.add("src/main/java/org/scribe/builder/api/LoveFilmApi.java");
      resources.add("src/main/java/org/scribe/builder/api/MeetupApi.java");
      resources.add("src/main/java/org/scribe/builder/api/MendeleyApi.java");
      resources.add("src/main/java/org/scribe/builder/api/MisoApi.java");
      resources.add("src/main/java/org/scribe/builder/api/NetProspexApi.java");
      resources
            .add("src/main/java/org/scribe/builder/api/NeteaseWeibooApi.java");
      resources.add("src/main/java/org/scribe/builder/api/PlurkApi.java");
      resources.add("src/main/java/org/scribe/builder/api/Px500Api.java");
      resources.add("src/main/java/org/scribe/builder/api/QWeiboApi.java");
      resources.add("src/main/java/org/scribe/builder/api/RenrenApi.java");
      resources.add("src/main/java/org/scribe/builder/api/SapoApi.java");
      resources.add("src/main/java/org/scribe/builder/api/SimpleGeoApi.java");
      resources.add("src/main/java/org/scribe/builder/api/SinaWeiboApi.java");
      resources.add("src/main/java/org/scribe/builder/api/SinaWeiboApi20.java");
      resources.add("src/main/java/org/scribe/builder/api/SkyrockApi.java");
      resources.add("src/main/java/org/scribe/builder/api/SohuWeiboApi.java");
      resources.add("src/main/java/org/scribe/builder/api/TrelloApi.java");
      resources.add("src/main/java/org/scribe/builder/api/TumblrApi.java");
      resources.add("src/main/java/org/scribe/builder/api/TwitterApi.java");
      resources.add("src/main/java/org/scribe/builder/api/UbuntuOneApi.java");
      resources.add("src/main/java/org/scribe/builder/api/ViadeoApi.java");
      resources.add("src/main/java/org/scribe/builder/api/VimeoApi.java");
      resources.add("src/main/java/org/scribe/builder/api/VkontakteApi.java");
      resources.add("src/main/java/org/scribe/builder/api/XingApi.java");
      resources.add("src/main/java/org/scribe/builder/api/YahooApi.java");
      resources.add("src/main/java/org/scribe/builder/api/YammerApi.java");
      resources.add("src/main/java/org/scribe/exceptions");
      resources
            .add("src/main/java/org/scribe/exceptions/OAuthConnectionException.java");
      resources.add("src/main/java/org/scribe/exceptions/OAuthException.java");
      resources
            .add("src/main/java/org/scribe/exceptions/OAuthParametersMissingException.java");
      resources
            .add("src/main/java/org/scribe/exceptions/OAuthSignatureException.java");
      resources.add("src/main/java/org/scribe/extractors");
      resources
            .add("src/main/java/org/scribe/extractors/AccessTokenExtractor.java");
      resources
            .add("src/main/java/org/scribe/extractors/BaseStringExtractor.java");
      resources
            .add("src/main/java/org/scribe/extractors/BaseStringExtractorImpl.java");
      resources.add("src/main/java/org/scribe/extractors/HeaderExtractor.java");
      resources
            .add("src/main/java/org/scribe/extractors/HeaderExtractorImpl.java");
      resources
            .add("src/main/java/org/scribe/extractors/JsonTokenExtractor.java");
      resources
            .add("src/main/java/org/scribe/extractors/RequestTokenExtractor.java");
      resources
            .add("src/main/java/org/scribe/extractors/TokenExtractor20Impl.java");
      resources
            .add("src/main/java/org/scribe/extractors/TokenExtractorImpl.java");
      resources.add("src/main/java/org/scribe/model");
      resources.add("src/main/java/org/scribe/model/OAuthConfig.java");
      resources.add("src/main/java/org/scribe/model/OAuthConstants.java");
      resources.add("src/main/java/org/scribe/model/OAuthRequest.java");
      resources.add("src/main/java/org/scribe/model/Parameter.java");
      resources.add("src/main/java/org/scribe/model/ParameterList.java");
      resources.add("src/main/java/org/scribe/model/Request.java");
      resources.add("src/main/java/org/scribe/model/RequestTuner.java");
      resources.add("src/main/java/org/scribe/model/Response.java");
      resources.add("src/main/java/org/scribe/model/SignatureType.java");
      resources.add("src/main/java/org/scribe/model/Token.java");
      resources.add("src/main/java/org/scribe/model/Verb.java");
      resources.add("src/main/java/org/scribe/model/Verifier.java");
      resources.add("src/main/java/org/scribe/oauth");
      resources.add("src/main/java/org/scribe/oauth/OAuth10aServiceImpl.java");
      resources.add("src/main/java/org/scribe/oauth/OAuth20ServiceImpl.java");
      resources.add("src/main/java/org/scribe/oauth/OAuthService.java");
      resources.add("src/main/java/org/scribe/services");
      resources.add("src/main/java/org/scribe/services/Base64Encoder.java");
      resources.add("src/main/java/org/scribe/services/CommonsEncoder.java");
      resources
            .add("src/main/java/org/scribe/services/DatatypeConverterEncoder.java");
      resources
            .add("src/main/java/org/scribe/services/HMACSha1SignatureService.java");
      resources
            .add("src/main/java/org/scribe/services/PlaintextSignatureService.java");
      resources
            .add("src/main/java/org/scribe/services/RSASha1SignatureService.java");
      resources.add("src/main/java/org/scribe/services/SignatureService.java");
      resources.add("src/main/java/org/scribe/services/TimestampService.java");
      resources
            .add("src/main/java/org/scribe/services/TimestampServiceImpl.java");
      resources.add("src/main/java/org/scribe/utils");
      resources.add("src/main/java/org/scribe/utils/MapUtils.java");
      resources.add("src/main/java/org/scribe/utils/OAuthEncoder.java");
      resources.add("src/main/java/org/scribe/utils/Preconditions.java");
      resources.add("src/main/java/org/scribe/utils/StreamUtils.java");
   }
}
