/**
 * 
 */
package gr.uom.se.vcs.jgit.integration;

import gr.uom.se.vcs.VCSRepository;
import gr.uom.se.vcs.exceptions.VCSRepositoryException;
import gr.uom.se.vcs.jgit.TestUtils;
import gr.uom.se.vcs.jgit.VCSRepositoryImp;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;


/**
 * A main test suite for initializing all tests under this package.
 * <p>
 * 
 * All tests within this package are conducted on a real repository and this
 * class should be extended to download repository if it is not where it should
 * be (under test/resources).
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public class MainSuite {

   /**
    * Path to test resources.
    * <p>
    */
   public static final String RESOURCES = "target/test-classes/";

   /**
    * Path to a small local git repository.
    * <p>
    */
   public static final String LOCAL_GIT_PATH = RESOURCES + "integration";

   /**
    * Path to a small 'remote' git repository.
    * <p>
    */
   public static final String REMOTE_GIT_PATH = "https://github.com/fernandezpablo85/scribe-java.git";

   /**
    * The first commit's SHA-1 as defined by GitHub in the specified remote
    * path.
    * <p>
    */
   public static final String FIRST_REV_SHA = "bfbe536886fc3c614b785110f8eba663644862d7";

   /**
    * The repository under test.
    * <p>
    */
   private static VCSRepository repo = null;

   /**
    * Download the remote repository if it is not there or update it.
    * <p>
    * 
    * @throws VCSRepositoryException
    */
   @BeforeClass
   public static void setUp() throws VCSRepositoryException {

      VCSRepository repo = new VCSRepositoryImp(LOCAL_GIT_PATH, REMOTE_GIT_PATH);
      File directory = new File(LOCAL_GIT_PATH);

      // Case there is not a .git directory in the given path
      if (!VCSRepositoryImp.containsGitDir(LOCAL_GIT_PATH)) {

         // Check directory path for access
         if (directory.exists()) {

            // All cases when there is a problem in cloning the new
            // repository, no test will perform if the directory can not
            // be cleaned, so we need to manually clean the directory
            if (!directory.isDirectory()) {

               throw new IllegalStateException("path at: " + LOCAL_GIT_PATH
                     + " is not a directory");

            } else if (directory.listFiles().length > 0) {

               throw new IllegalStateException("can not proceed to "
                     + LOCAL_GIT_PATH + ", directory is not empty");

            } else if (!(directory.canRead() || directory.canWrite())) {

               throw new IllegalStateException(
                     "can not access directory at path: " + LOCAL_GIT_PATH);
            }
         }
         // Try to download the repository
         TestUtils.showMsg(MainSuite.class,("Downloading repository from: " + REMOTE_GIT_PATH
               + " please wait..."));
         long start = System.currentTimeMillis();
         repo.cloneRemote();
         TestUtils.showMsg(MainSuite.class, ("Repository downloaded at "
               + (int) (((System.currentTimeMillis() - start) / 1000) + 0.5) + " sec(s)"));

         // A .git directory is present, however we should update the repository
      } else {

         // Try to update the repository
         TestUtils.showMsg(MainSuite.class, ("Updating repository from: " + REMOTE_GIT_PATH
               + " please wait..."));
         long start = System.currentTimeMillis();
         repo.update();
         TestUtils.showMsg(MainSuite.class, ("Repository updated at "
               + (int) (((System.currentTimeMillis() - start) / 1000) + 0.5) + " sec(s)"));
      }

      MainSuite.repo = repo;
   }

   /**
    * @return repository to where the tests should be performed.
    */
   static VCSRepository repo() {
      return repo;
   }

   protected VCSRepository thisRepo;

   @Before
   public void init() {
      thisRepo = repo();
      if (thisRepo == null) {
         throw new IllegalStateException("repository is null");
      }
   }

   @AfterClass
   public static void cleanUp() throws IOException {

      // Check first if repository was not downloaded/updated
      if (!VCSRepositoryImp.containsGitDir(LOCAL_GIT_PATH)) {

         // We will try to clean all this directory in case there is a problem
         // and there is not any .git dir but other junk files
         File directory = new File(LOCAL_GIT_PATH);
         if (directory.isDirectory() && directory.listFiles().length > 0) {
            FileUtils.deleteDirectory(directory);
         }
      }
   }
}
