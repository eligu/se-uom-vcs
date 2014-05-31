package se.uom.vcs.jgit.integration;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Test case for testing branch artifacts on a real Git repository.<p>
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public class VCSBranchImpTest extends MainSuite {

   public static final String[] BRANCHES = {"master", "gh-pages", "multipom", "mvn-repo"};
   
   public static final String[][] BRANCH_COMMITS = {
      {"c4dbbcf98992d4f83f51e06ed2b71dc6f7eacb6a"}};
   
   public static final String[][] BRANCH_COMMITS_MSG = {};
   
   
}
