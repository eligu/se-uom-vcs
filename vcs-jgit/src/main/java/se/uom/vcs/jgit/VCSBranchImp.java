/**
 * 
 */
package se.uom.vcs.jgit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

import se.uom.vcs.VCSBranch;
import se.uom.vcs.VCSCommit;
import se.uom.vcs.VCSResource;
import se.uom.vcs.exceptions.VCSRepositoryException;
import se.uom.vcs.jgit.utils.RevUtils;
import se.uom.vcs.walker.CommitVisitor;
import se.uom.vcs.walker.filter.commit.VCSCommitFilter;
import se.uom.vcs.walker.filter.resource.VCSResourceFilter;

/**
 * Implementation of {@link VCSBranch} based on JGit library.
 * <p>
 * 
 * This is an immutable object and is considered thread safe.
 * 
 * @author Elvis Ligu
 * @since 0.0.1
 * @version 0.0.1
 * @see VCSBranch
 */
public class VCSBranchImp extends GitReference implements VCSBranch {

   

   /**
    * Creates a new instance based on the given branch reference and repository.
    * <p>
    * 
    * Branch reference and repository must not be null. Also branch reference
    * must exists within repository. {@link Repository#getRef(String)} will be
    * used to check if the given reference exists within repository.
    * 
    * @param branch
    *           the reference this branch is related to
    * @param repository
    *           the repository this branch comes from
    */
   public VCSBranchImp(final Ref branch, final Repository repository) {
      super(branch, repository);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public VCSCommit getHead() throws VCSRepositoryException {

      RevWalk walk = null;
      try {

         walk = new RevWalk(this.repo);
         return new VCSCommitImp(walk.parseCommit(this.ref.getObjectId()),
               this.repo);

      } catch (final MissingObjectException e) {
         throw new VCSRepositoryException(e);
      } catch (final IncorrectObjectTypeException e) {
         throw new VCSRepositoryException(e);
      } catch (final IOException e) {
         throw new VCSRepositoryException(e);
      } finally {
         if (walk != null) {
            walk.release();
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Collection<VCSCommit> getAllCommits() throws VCSRepositoryException {

      final List<VCSCommit> commits = new ArrayList<VCSCommit>();
      this.walkCommits(new CommitVisitor<VCSCommit>() {

         @Override
         public boolean visit(final VCSCommit entity) {
            commits.add(entity);
            return true;
         }

         @Override
         public <R extends VCSResource> VCSResourceFilter<R> getResourceFilter() {
            return null;
         }

         @SuppressWarnings("unchecked")
         @Override
         public VCSCommitFilter<VCSCommit> getFilter() {
            return null;
         }

      }, true);
      return commits;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void walkCommits(final CommitVisitor<VCSCommit> visitor,
         boolean descending) throws VCSRepositoryException {
      this.getHead().walkCommits(visitor, descending);
   }

   /**
    * {@inheritDoc}
    * <p>
    * Given commit must be of type {@link VCSCommitImp}
    */
   @Override
   public boolean isContained(final VCSCommit commit)
         throws VCSRepositoryException {

      // check first to see if the given commit is of type VCSCommitImp
      // because we need access to JGit RevCommit
      ArgsCheck.isSubtype("commit", VCSCommitImp.class, commit);

      final RevCommit rCommit = ((VCSCommitImp) commit).commit;

      // We can not check if the commit is null so return false
      ArgsCheck.notNull("'commit' property of commit argument", rCommit);

      try {
         return RevUtils.isAncestor(rCommit,
               new RevWalk(this.repo).parseCommit(this.ref.getObjectId()),
               this.repo);
      } catch (final MissingObjectException e) {
         throw new VCSRepositoryException(e);
      } catch (final IncorrectObjectTypeException e) {
         throw new VCSRepositoryException(e);
      } catch (final IOException e) {
         throw new VCSRepositoryException(e);
      }
   }
}
