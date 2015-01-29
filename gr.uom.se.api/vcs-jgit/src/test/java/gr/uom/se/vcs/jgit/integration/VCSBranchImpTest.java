package gr.uom.se.vcs.jgit.integration;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import gr.uom.se.vcs.VCSBranch;
import gr.uom.se.vcs.VCSCommit;
import gr.uom.se.vcs.exceptions.VCSRepositoryException;
import gr.uom.se.vcs.jgit.TestUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;


/**
 * Test case for testing branch artifacts on a real Git repository.
 * <p>
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public class VCSBranchImpTest extends MainSuite {

   public static final String[] BRANCHES = { "master", "gh-pages", "multipom",
         "mvn-repo" };

   public static final String[][] BRANCH_COMMITS = {
         // master commits
         { "c4dbbcf98992d4f83f51e06ed2b71dc6f7eacb6a",
               "26792c063045e9557e79c9e84c3709f5ada22395",
               "7afdb1f8a0636e6c6db9d7f6c6ee44728417908d",
               "94add5a8dc5e14e06f3f10108a3e33f7825d56e6" },
         // gh-pages commits
         { "78f57fb2203b9547034cde6f7d0bd5bd67a19d5c",
               "c1b3561c5d4421ff7dfc2a9df2bfb15262b66369",
               "7b4fe73ba2b958627d838704ee6a706abd266301",
               "417d2e782f079459f1486ef23cac433e6ff4c8c3" },
         // multipom commits
         { "b06efa5058fef019b493e8125dd015fe2e1e6ec5",
               "65ae79d2702ccb192161db8fc6d1edaa5df07be8",
               "f000558883403da839df73b33796825a3c626c48",
               "11dc6b1f541633a6432a41d258357f7fa483778c" },
         // mvn-repo commits
         { "cf6f10e3dfa9a7aedd82c5d8bb1862f498dc9717",
               "ad55e225a668facb367956b47d7230fd4d253413",
               "b85ca334ccda499ca7f7f02f0555c8f4a107151d",
               "b1878c44375761e77ca3720f35480f8188dc988d" } };

   public static final Map<String, String[]> branches_to_commits = new HashMap<String, String[]>();
   static {
      for (int i = 0; i < BRANCHES.length; i++) {
         branches_to_commits.put(BRANCHES[i], BRANCH_COMMITS[i]);
      }
   }

   /**
    * Will check each returned branch if they contain some predefined commits.
    * <p>
    * 
    * @throws VCSRepositoryException
    */
   @Test
   public void testBranchCommits() throws VCSRepositoryException {

      Collection<VCSBranch> branches = thisRepo.getBranches();
      assertTrue(!branches.isEmpty());

      for (VCSBranch branch : branches) {

         String[] sname = branch.getName().split("/");
         String name = sname[sname.length - 1];

         String[] commits = branches_to_commits.get(name);

         if (commits != null) {
            TestUtils.showMsg(this.getClass(), "Checking branch " + branch);
            for (String id : commits) {

               VCSCommit commit = thisRepo.resolveCommit(id);
               assertNotNull(commit);
               assertTrue(branch.isContained(commit));
            }
         }
      }
   }
}
