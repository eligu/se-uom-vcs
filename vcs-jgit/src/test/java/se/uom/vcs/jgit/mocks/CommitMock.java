/**
 * 
 */
package se.uom.vcs.jgit.mocks;

import java.util.ArrayList;
import java.util.List;

import se.uom.vcs.VCSCommit;
import se.uom.vcs.VCSRepository;
import se.uom.vcs.jgit.VCSCommitImp;

/**
 * Test mock for class {@link VCSCommitImp}.
 * <p>
 * 
 * This class contains all commit SHA-1 in {@link #COMMITS} array. For each
 * commit mock is stored its SHA-1 (as {@link #id}) an array of parent commit
 * mocks and children. Each time we want to resolve a {@link VCSCommit} from the
 * repository we can call {@link VCSRepository#resolveCommit(String)}, where the
 * string argument will be its SHA-1.
 * 
 * @author Elvis Ligu
 */
public class CommitMock {

   /**
    * Is used as a reference to store the length of {@link #COMMITS}.
    * <p>
    * All commits in the array are stored from new to old, and if we make other
    * commits in repository, the commit references that each mock have will
    * remain the same, because they are referred to as the distance from the
    * length of the array.
    */
   private static final int len = 8;

   /**
    * Some commit SHA-1 to be resolved, that are in small repository.
    * <p>
    */
   public static final CommitMock[] COMMITS = {
         new CommitMock("006af5054d07875212c029bd6757db95da5aa8e7",
               new int[] { len - 5 }, new int[] {}), 				// 0 = len - 8
         new CommitMock("65129c040a725e2dd1244021ae5884d10595dc99",
               new int[] { len - 6 }, new int[] {}),				// 1 = len - 7
         new CommitMock("2ae91a56a9e866bdaa0c0e5587e0ab4314d506ea",
               new int[] { len - 4 }, new int[] { len - 7 }),			// 2 = len - 6
         new CommitMock("bfb050192bd2c7bd415574a64f39eeb1cad235a8",
               new int[] { len - 2 }, new int[] { len - 8 }), 		// 3 = len - 5
         new CommitMock("171e9a3e2ba519fca83a4f2cf412373b097869f8",
               new int[] { len - 3 }, new int[] { len - 6 }),			// 4 = len - 4
         new CommitMock("84c438f93b5ccea16ea5c27e53950c14e2e6cabf",
               new int[] { len - 2 }, new int[] { len - 4 }),			// 5 = len - 3
         new CommitMock("48912e4b240946c277e1ef46dd4b398cf5ec2dd8",
               new int[] { len - 1 }, new int[] { len - 3, len - 5 }),	// 6 =
                                                                       // len -
                                                                       // 2
         new CommitMock("1265d30232fac179d8a6aa38cbe4330df452218c",
               new int[] {}, new int[] { len - 2 })					// 7 = len - 1
   };

   /**
    * Index of oldest commit in {@link #COMMITS} array.
    * <p>
    */
   public static final int INDEX_FIRST_COMMIT = COMMITS.length - 1;

   /**
    * Commit id.
    * <p>
    */
   public final String id;

   /**
    * Indices of parents.
    * <p>
    */
   private final int[] parents;

   /**
    * Indices of children (next).
    * <p>
    */
   private final int[] children;

   public CommitMock(final String id, final int[] parents, final int[] children) {
      this.id = id;
      this.parents = parents;
      this.children = children;
   }

   /**
    * @return a list of children commit mocks
    */
   public List<CommitMock> children() {
      final List<CommitMock> child = new ArrayList<CommitMock>();
      for (final int i : this.children) {
         child.add(COMMITS[i]);
      }
      return child;
   }

   /**
    * 
    * @return a list of parent commit mocks
    */
   public List<CommitMock> parents() {
      final List<CommitMock> parents = new ArrayList<CommitMock>();
      for (final int i : this.parents) {
         parents.add(COMMITS[i]);
      }
      return parents;
   }

   public boolean isParent(final String cid) {

      for (final int i : this.parents) {
         if (COMMITS[i].id.equals(cid)) {
            return true;
         }
      }
      return false;
   }

   @Override
   public String toString() {
      return this.id;
   }

   @Override
   public boolean equals(final Object o) {
      final CommitMock m = (CommitMock) o;
      return this.id.equals(m.id);
   }
}
