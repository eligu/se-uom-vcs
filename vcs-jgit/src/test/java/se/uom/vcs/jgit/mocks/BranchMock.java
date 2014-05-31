package se.uom.vcs.jgit.mocks;

import java.util.ArrayList;
import java.util.List;

import se.uom.vcs.jgit.VCSBranchImp;

/**
 * Test mock for class {@link VCSBranchImp}.
 * <p>
 * 
 * All branches are contained within {@link #BRANCHES} arrays. Each branch mock
 * contains:
 * <ul>
 * <li>id</li>
 * <li>name</li>
 * <li>HEAD (SHA-1)</li>
 * <li>commits</li> - the commits that are accessible from this branch
 * </ul>
 * 
 * {@link #BRANCHES_BAD} array contains branches that are within repository (see
 * {@link RepoMock}) but their commits do not comply with those in repository.
 * <p>
 * 
 * @author Elvis Ligu
 * 
 */
public class BranchMock {

   /**
    * The index of 'master' branch in {@link #BRANCHES} array.
    * <p>
    */
   public static final int INDEX_MASTER = 0;

   /**
    * The index of 'fix' branch in {@link #BRANCHES} array.
    * <p>
    */
   public static final int INDEX_FIX = 1;

   /**
    * Index of 'master' branch HEAD commit in {@link CommitMock#COMMITS} array.
    * <p>
    */
   public static final int INDEX_MASTER_HEAD = 0;

   /**
    * Index of 'fix' branch HEAD commit in {@link CommitMock#COMMITS} array.
    * <p>
    */
   public static final int INDEX_FIX_HEAD = 1;

   /**
    * Is used as a reference to store the length of {@link CommitMock#COMMITS}.
    * <p>
    * All commits in the array are stored from new to old, and if we make other
    * commits in repository, the commit references that each mock have will
    * remain the same, because they are referred to as the distance from the
    * length of the array.
    */
   private static final int len = CommitMock.COMMITS.length;

   /**
    * Some branches to be resolved that are in small repository.
    * <p>
    * <p>
    */
   public static final BranchMock[] BRANCHES = new BranchMock[] {
         new BranchMock("refs/remotes/origin/master", "master",
               CommitMock.COMMITS[INDEX_MASTER_HEAD].id, len - 8, len - 5,
               len - 2, len - 1),
         new BranchMock("refs/remotes/origin/fix", "fix",
               CommitMock.COMMITS[INDEX_FIX_HEAD].id, len - 7, len - 6,
               len - 4, len - 3, len - 2, len - 1) };

   /**
    * Some branches to be resolved that are in small repository.
    * <p>
    * Each commit of these branches (when called {@link #commits()} is not
    * contained in the branch.
    */
   public static final BranchMock[] BRANCHES_BAD = new BranchMock[] {
         new BranchMock("refs/remotes/origin/master", "master",
               CommitMock.COMMITS[INDEX_MASTER_HEAD].id, len - 7, len - 6,
               len - 4, len - 3),
         new BranchMock("refs/remotes/origin/fix", "fix",
               CommitMock.COMMITS[INDEX_FIX_HEAD].id, len - 8, len - 5) };

   /**
    * Branch names that are in the small repository.
    * <p>
    */
   public static final String[] SMALL_REPO_BRANCH_NAMES = new String[] {
         "master", "fix" };

   /**
    * Branch ID.
    * <p>
    */
   public final String id;

   /**
    * Branch name.
    * <p>
    */
   public final String name;

   /**
    * SHA-1 of branch head.
    * <p>
    */
   public final String head;

   /**
    * Indices of commits in {@link CommitMock#COMMITS} that are part of this
    * branch history.
    * <p>
    */
   private final int[] indices;

   public BranchMock(String id, String name, String head, int... indices) {
      this.id = id;
      this.name = name;
      this.head = head;
      this.indices = indices;
   }

   /**
    * Return all the commit mocks this branch contains.
    * <p>
    * 
    * @return
    */
   public List<CommitMock> commits() {
      List<CommitMock> commits = new ArrayList<CommitMock>();
      for (int i : indices) {
         commits.add(CommitMock.COMMITS[i]);
      }
      return commits;
   }

   /**
    * Return all the commit SHA-1 this branch contains.
    * <p>
    * 
    * @return
    */
   public List<String> commitIds() {
      List<String> ids = new ArrayList<String>();
      for (int i : indices) {
         ids.add(CommitMock.COMMITS[i].id);
      }
      return ids;
   }
}