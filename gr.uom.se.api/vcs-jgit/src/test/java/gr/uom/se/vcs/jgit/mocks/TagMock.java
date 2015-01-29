package gr.uom.se.vcs.jgit.mocks;

import gr.uom.se.vcs.VCSTag;
import gr.uom.se.vcs.jgit.VCSTagImp;

/**
 * Test mock for class {@link VCSTagImp}.
 * <p>
 * 
 * Each tag mock has a one to one relation with {@link VCSTag} that is the
 * fields are the same. {@link #TAGS} array contains the tags in repository (see
 * {@link RepoMock} and {@link #SMALL_REPO_TAG_NAMES} contains only the names of
 * each tag.
 * 
 * @author Elvis Ligu
 * 
 */
public class TagMock {

   /**
    * Tag names that are in the small repository.
    * <p>
    */
   public static final String[] SMALL_REPO_TAG_NAMES = new String[] {
         "first_commit", "fix_first_commit" };

   /**
    * Is used as a reference to store the length of {@link #COMMITS}.
    * <p>
    * All commits in the array are stored from new to old, and if we make other
    * commits in repository, the commit references that each mock have will
    * remain the same, because they are referred to as the distance from the
    * length of the array.
    */
   private static final int len = CommitMock.COMMITS.length;

   /**
    * Some tags to be resolved that are in small repository.
    * <p>
    * <p>
    */
   public static final TagMock[] TAGS = new TagMock[] {
         new TagMock("refs/tags/first_commit", "first_commit",
               CommitMock.COMMITS[CommitMock.INDEX_FIRST_COMMIT].id),
         new TagMock("refs/tags/fix_first_commit", "fix_first_commit",
               CommitMock.COMMITS[len - 3].id) };

   /**
    * Tag id.
    * <p>
    */
   public final String id;

   /**
    * Tag name.
    * <p>
    */
   public final String name;

   /**
    * Tag commit SHA-1.
    * <p>
    */
   public final String commit;

   public TagMock(String id, String name, String commit) {
      this.id = id;
      this.name = name;
      this.commit = commit;
   }
}