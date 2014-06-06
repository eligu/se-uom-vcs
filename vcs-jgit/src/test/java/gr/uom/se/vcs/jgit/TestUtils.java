/**
 * 
 */
package gr.uom.se.vcs.jgit;

import gr.uom.se.vcs.VCSBranch;
import gr.uom.se.vcs.VCSChange;
import gr.uom.se.vcs.VCSCommit;
import gr.uom.se.vcs.VCSRepository;
import gr.uom.se.vcs.VCSTag;
import gr.uom.se.vcs.exceptions.VCSRepositoryException;
import gr.uom.se.vcs.jgit.mocks.ChangeMock;
import gr.uom.se.vcs.jgit.mocks.CommitMock;
import gr.uom.se.vcs.jgit.mocks.RepoMock;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;


/**
 * Test utilities to be used from test cases under
 * <code>com.elit.vcs.jgit</code>.
 * <p>
 * 
 * This is a singleton pattern class and contains different utility methods to
 * be used from test cases. Use {@link TestUtils#instance()} to retain the
 * instance of this class.
 * 
 * @author Elvis Ligu
 */
public class TestUtils {

   /**
    * Instance of this class.
    * <p>
    */
   private static TestUtils INSTANCE = null;

   /**
    * The small repo.
    * <p>
    */
   private RepoMock repoSmall = null;

   /**
    * @throws VCSRepositoryException
    * 
    */
   private TestUtils() throws VCSRepositoryException {

      // Create the small repository
      repoSmall = new RepoMock();
   }

   /**
    * @return the instance of this class
    */
   public static TestUtils instance() {
      if (INSTANCE == null) {
         try {
            INSTANCE = new TestUtils();
         } catch (VCSRepositoryException e) {
            throw new IllegalStateException(e);
         }
      }
      return INSTANCE;
   }

   /**
    * @return the small repository
    * @see RepoMock
    */
   public VCSRepository smallRepo() {
      try {
         return repoSmall.repo();
      } catch (VCSRepositoryException e) {
         throw new IllegalStateException(e);
      }
   }

   /**
    * Compare two collections for equality.
    * <p>
    * 
    * This method might reproduce false results if the two collections contains
    * the same number of results, and all elements of c1 are contained in c2.
    * i.e <code>[1 1 2] == [1 2 2] -> true</code>
    * 
    * @param c1
    *           first collection (null not allowed)
    * @param c2
    *           second collection (null not allowed)
    * @param comp
    *           comparator, if null {@link Object#equals(Object)} will be used
    *           to compare objects.
    * @return true if c1 has the same size as c2 and all elements of c1 are
    *         contained in c2
    */
   @SuppressWarnings({ "rawtypes", "unchecked" })
   public static boolean equals(Collection<?> c1, Collection<?> c2,
         Comparator comp) {

      if (c1.size() != c2.size()) {
         return false;
      }

      for (Object o1 : c1) {

         boolean found = false;
         Iterator<?> it = c2.iterator();
         while (it.hasNext() && !found) {
            if (comp != null) {
               if (comp.compare(o1, it.next()) == 0) {
                  found = true;
               }
            } else {
               if (o1.equals(it.next())) {
                  found = true;
               }
            }
         }

         if (!found)
            return false;
      }
      return true;
   }

   /**
    * Compare two tags by ID, name and commit.
    * <p>
    * <p>
    * 
    * @param t1
    *           first tag (not null)
    * @param t2
    *           second tag (not null)
    * @return true if t1 and t2 have the same name, id and commit
    * @throws VCSRepositoryException
    *            thrown by {@link VCSTag#getCommit()}
    */
   public static boolean equals(VCSTag t1, VCSTag t2)
         throws VCSRepositoryException {
      return t1.getID().equals(t2.getID()) && t1.getName().equals(t2.getName())
            && t1.getCommit().getID().equals(t2.getCommit().getID());
   }

   /**
    * References such as tags or branches in Git are like paths. i.e.
    * refs/tags/v1.2 refs/heads/master. This method will keep only the last
    * segment of path.
    * <p>
    * 
    * @param br
    *           git reference
    * @return the last segment of reference
    */
   public static String extractName(String br) {
      String arr[] = br.split("/");
      String name = arr[arr.length - 1];
      return name;
   }

   /**
    * Compare two branches for equality.
    * <p>
    * 
    * Compare by id and head commit. NOTE: branch ID will be altered to keep
    * only the last segment of reference, using {@link #extractName(String)}.
    * 
    * @param b1
    *           first branch (not null)
    * @param b2
    *           second branch (not null)
    * @return true if branches are equals
    * @throws VCSRepositoryException
    *            thrown by {@link VCSBranch#getHead()}
    */
   public static boolean equals(VCSBranch b1, VCSBranch b2)
         throws VCSRepositoryException {
      String id1 = extractName(b1.getID());
      String id2 = extractName(b2.getID());
      String hid1 = b1.getHead().getID();
      String hid2 = b2.getHead().getID();

      return (id1.endsWith(id2)) && (hid1.equals(hid2));
   }

   /**
    * Compare a {@link ChangeMock} with a {@link VCSChange} for equality.
    * <p>
    * 
    * @param mock
    *           change mock
    * @param change
    *           the change
    * @return if mock is equal to change
    */
   @SuppressWarnings("rawtypes")
   public static boolean equals(ChangeMock mock, VCSChange change) {

      String oldPath = (change.getOldResource() != null ? change
            .getOldResource().getPath() : null);
      String newPath = (change.getNewResource() != null ? change
            .getNewResource().getPath() : null);
      String oldCommit = (change.getOldCommit() != null ? change.getOldCommit()
            .getID() : null);
      String newCommit = (change.getNewCommit() != null ? change.getNewCommit()
            .getID() : null);

      return equals(mock.newPath, newPath) && equals(mock.oldPath, oldPath)
            && equals(mock.newCommit, newCommit)
            && equals(mock.oldCommit, oldCommit)
            && mock.type.equals(change.getType());
   }

   /**
    * Compare two strings for equality.
    * <p>
    * 
    * This method will return true if both strings are <code>null</code>.
    * 
    * @param s1
    *           first string (null allowed)
    * @param s2
    *           second string (null allowed)
    * @return true if s1 equals to s2 or if they are both null
    */
   public static boolean equals(String s1, String s2) {
      if (s1 != null)
         return s1.equals(s2);
      if (s2 != null)
         return s2.equals(s1);
      return true;
   }

   /**
    * Compare a mock with a commit.
    * <p>
    * 
    * They are true only if they have the same SHA-1 and same parents and same
    * children.
    * 
    * @param mock
    * @param commit
    * @return
    * @throws VCSRepositoryException
    */
   public static boolean equals(CommitMock mock, VCSCommit commit) {

      // If they have different id return false
      if (!mock.id.equals(commit.getID())) {
         return false;
      }

      // If they have different children return false
      boolean child;
      try {
         child = equals(mock.children(), commit.getNext(), new CommitComp());
         if (!child) {
            return false;
         }

         // Check parents
         boolean parent = equals(mock.parents(), commit.getPrevious(),
               new CommitComp());

         // They must have same id, same children and same parents
         return parent;

      } catch (VCSRepositoryException e) {
         throw new IllegalStateException(e);
      }
   }

   /**
    * Used as a comparator for
    * {@link TestUtils#equals(Collection, Collection, Comparator)} to compare a
    * {@link CommitMock} with a {@link VCSCommit} only by their id (SHA-1).
    * 
    * @author Elvis Ligu
    * 
    */
   @SuppressWarnings("rawtypes")
   private static class CommitComp implements Comparator {
      public int compare(Object o1, Object o2) {

         CommitMock cm = (o1 instanceof CommitMock ? (CommitMock) o1
               : (CommitMock) o2);
         VCSCommit c = (o1 instanceof VCSCommit ? (VCSCommit) o1
               : (VCSCommit) o2);
         if (cm.id.equals(c.getID())) {
            return 0;
         }
         return -1;
      }
   }

   /**
    * Print a formatted string to stdout.
    * <p>
    * 
    * @param clazz
    * @param msg
    */
   public static void showMsg(Class<?> clazz, String msg) {
      System.out.println("[INFO - " + clazz.getCanonicalName() + "]" + " "
            + msg);
   }
}
