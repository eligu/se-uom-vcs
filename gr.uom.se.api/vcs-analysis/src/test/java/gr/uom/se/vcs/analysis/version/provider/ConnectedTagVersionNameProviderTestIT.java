/**
 *
 */
package gr.uom.se.vcs.analysis.version.provider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import gr.uom.se.vcs.VCSBranch;
import gr.uom.se.vcs.VCSCommit;
import gr.uom.se.vcs.VCSRepository;
import gr.uom.se.vcs.exceptions.VCSRepositoryException;
import gr.uom.se.vcs.jgit.VCSRepositoryImp;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

/**
 * @author Elvis Ligu
 */
public class ConnectedTagVersionNameProviderTestIT {

   private static final Collection<String> URLS = new ArrayList<String>();
   private static final Logger logger = Logger
         .getLogger(ConnectedTagVersionNameProviderTestIT.class.getName());

   static {
      URLS.add("https://github.com/jboss-javassist/javassist.git");
      URLS.add("https://github.com/pubnub/java.git");
      URLS.add("https://github.com/spullara/mustache.java.git");
   }

   @Test
   public void testBranchSelection() throws IOException, VCSRepositoryException {
      String url = "https://github.com/ReactiveX/RxNetty.git";
      logger.info("Available Branches:");
      Path f = Files.createTempDirectory("vcs_repo");
      logger.info("Path: " + f.toAbsolutePath());
      Files.createDirectories(f);
      try (VCSRepository repo = new VCSRepositoryImp(f.toString(), url)) {
         cloneRepo(url, repo);
         Collection<VCSBranch> branches = repo.getBranches();
         VCSBranch maxBranch = null;
         int maxTagsCount = 0;
         for (VCSBranch branch : branches) {
            TagProvider tagProvider = new BranchTagProvider(repo,
                  branch.getName());
            ConnectedTagVersionNameProvider versionProvider = new ConnectedTagVersionNameProvider(
                  tagProvider);
            VersionTagReachabilityValidator validator = new VersionTagReachabilityValidator(
                  repo);
            boolean valid = validator.areValid(versionProvider.getNames());
            assertTrue(valid);
            int currentTagsCount = tagProvider.getTags().size();
            logger.info("Branch : " + branch.getName() + " |Tags| = "
                  + currentTagsCount);
            if (currentTagsCount > maxTagsCount) {
               maxBranch = branch;
               maxTagsCount = currentTagsCount;
            }
         }
         assertNotNull(maxBranch);
         assertEquals("origin/0.x", maxBranch.getName());
      } finally {
         delete(f);
      }
   }

   @Test
   public void test() throws IOException, VCSRepositoryException {
      for (String url : URLS) {
         test(url);
      }
   }

   private void test(String url) throws IOException, VCSRepositoryException {
      Path f = Files.createTempDirectory("vcs_repo");
      logger.info("Path: " + f.toAbsolutePath());
      Files.createDirectories(f);
      try (VCSRepository repo = new VCSRepositoryImp(f.toString(), url)) {
         cloneRepo(url, repo);

         long start = System.currentTimeMillis();

         TagProvider tagProvider = new BranchTagProvider(repo);

         ConnectedTagVersionNameProvider versionProvider = new ConnectedTagVersionNameProvider(
               tagProvider);
         VersionTagReachabilityValidator validator = new VersionTagReachabilityValidator(
               repo);
         logger.info("Resolving versions in "
               + (System.currentTimeMillis() - start) + " ms");

         start = System.currentTimeMillis();
         Iterator<String> names = versionProvider.descendingNameIterator();
         Iterator<VCSCommit> commits = versionProvider.descendingIterator();
         while (names.hasNext()) {
            String name = names.next();
            VCSCommit commit = versionProvider.getCommit(name);
            VCSCommit current = commits.next();
            assertEquals(commit, current);
         }
         logger.info("Iterating versions in "
               + (System.currentTimeMillis() - start) + " ms");

         start = System.currentTimeMillis();
         boolean valid = validator.areValid(versionProvider.getNames());
         logger.info("Validating versions in "
               + (System.currentTimeMillis() - start) + " ms");
         assertTrue("versions are not valid", valid);

         names = versionProvider.descendingNameIterator();
         StringBuilder sb = new StringBuilder("Resolved versions: \n");
         while (names.hasNext()) {
            sb.append(names.next()).append("\n");
         }
         logger.info(sb.toString());
      } finally {
         delete(f);
      }
   }

   private void cloneRepo(String url, final VCSRepository repo)
         throws VCSRepositoryException {
      logger.info("Cloning... " + url);
      long start = System.currentTimeMillis();
      repo.cloneRemote();
      logger.info("done cloning in " + (System.currentTimeMillis() - start)
            + " ms");
   }

   private void delete(Path f) throws IOException {
      FileUtils.deleteDirectory(f.toFile());
   }
}
