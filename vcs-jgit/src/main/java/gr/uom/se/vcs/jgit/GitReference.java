/**
 * 
 */
package gr.uom.se.vcs.jgit;

import java.io.IOException;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;

/**
 * This is a base class for all Git references (branches/tags).
 * <p>
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public abstract class GitReference {

   /**
    * The JGit reference this instance is related to.
    * <p>
    */
   protected Ref ref;

   /**
    * The JGit repository reference this is related to.
    * <p>
    */
   protected Repository repo;
   
   /**
    * Creates a new instance based on the given reference and repository.
    * <p>
    * 
    * Ref and repository must not be null. Also ref
    * must exists within repository. {@link Repository#getRef(String)} will be
    * used to check if the given reference exists within repository.
    * 
    * @param ref
    *           the reference this is related to
    * @param repository
    *           the repository this reference comes from
    */
   public GitReference(final Ref ref, final Repository repository) {

      ArgsCheck.notNull("branch", ref);
      ArgsCheck.notNull("repository", repository);

      try {
         this.ref = repository.getRef(ref.getName());
      } catch (final IOException e) {
         throw new IllegalArgumentException(
               "given reference is not contained in repository");
      }

      this.repo = repository;
   }
   
   /**
    * The ID of a JGit reference is retrieved by calling {@link Ref#getName()}
    */
   public String getID() {
      return this.ref.getName();
   }

   /**
    * {@link Repository#shortenRefName(String)} will be called to
    * keep only a human readable name of this branch.
    */
   public String getName() {
      return Repository.shortenRefName(this.ref.getName());
   }
   
   @Override
   public String toString() {
      return this.ref.getName();
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((ref == null) ? 0 : ref.hashCode());
      result = prime * result + ((repo == null) ? 0 : repo.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      GitReference other = (GitReference) obj;
      if (ref == null) {
         if (other.ref != null)
            return false;
      } else if (!ObjectId.equals(other.ref.getObjectId(), ref.getObjectId()))
         return false;
      if (repo == null) {
         if (other.repo != null)
            return false;
      } else if (!repo.getDirectory().equals(other.repo.getDirectory()))
         return false;
      return true;
   }
}
