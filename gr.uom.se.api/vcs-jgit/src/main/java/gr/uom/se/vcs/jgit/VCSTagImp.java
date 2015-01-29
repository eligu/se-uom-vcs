/**
 * 
 */
package gr.uom.se.vcs.jgit;

import gr.uom.se.vcs.VCSCommit;
import gr.uom.se.vcs.VCSTag;
import gr.uom.se.vcs.exceptions.VCSRepositoryException;

import java.io.IOException;

import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevWalk;


/**
 * Implementation of {@link VCSTag} based on JGit library.
 * <p>
 * 
 * @author Elvis Ligu
 * @since 0.0.1
 * @version 0.0.1
 * @see VCSTag
 */

public class VCSTagImp extends GitReference implements VCSTag {

   /**
    * Creates a new instance based on the given parameters.
    * <p>
    * 
    * @param ref
    *           must not be null
    * @param repo
    *           must not be null
    */
   public VCSTagImp(final Ref ref, final Repository repo) {
      super(ref, repo);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public VCSCommit getCommit() throws VCSRepositoryException {

      final RevWalk walk = new RevWalk(this.repo);
      ObjectId id = this.ref.getPeeledObjectId();
      if (id == null) {
         id = this.ref.getObjectId();
      }
      try {

         return id != null ? new VCSCommitImp(walk.parseCommit(id), this.repo)
               : null;

      } catch (final MissingObjectException e) {
         throw new VCSRepositoryException(e);
      } catch (final IncorrectObjectTypeException e) {
         throw new VCSRepositoryException(e);
      } catch (final IOException e) {
         throw new VCSRepositoryException(e);
      }
   }
}
