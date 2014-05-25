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
import se.uom.vcs.walker.CommitVisitor;
import se.uom.vcs.walker.filter.commit.VCSCommitFilter;
import se.uom.vcs.walker.filter.resource.VCSResourceFilter;

/**
 * Implementation of {@link VCSBranch} based on JGit library.
 * <p>
 * 
 * @author Elvis Ligu
 * @since 0.0.1
 * @version 0.0.1
 * @see VCSBranch
 */
public class VCSBranchImp implements VCSBranch {

    /**
     * The JGit branch reference this instance is related to.
     * <p>
     */
    protected Ref branch;

    /**
     * The JGit repository reference this branch is related to.
     * <p>
     */
    protected Repository repo;

    /**
     * Creates a new instance based on the given branch reference and
     * repository.
     * <p>
     * 
     * Branch reference and repository must not be null. Also branch reference
     * must exists within repository. {@link Repository#getRef(String)} will be
     * used to check if the given reference exists within repository.
     * 
     * @param branch
     *            the reference this branch is related to
     * @param repository
     *            the repository this branch comes from
     */
    public VCSBranchImp(final Ref branch, final Repository repository) {

	ArgsCheck.notNull("branch", branch);
	ArgsCheck.notNull("repository", repository);

	try {
	    this.branch = repository.getRef(branch.getName());
	} catch (final IOException e) {
	    throw new IllegalArgumentException(
		    "given reference is not contained in repository");
	}

	this.repo = repository;
    }

    /**
     * {@inheritDoc}
     * <p>
     * The ID of a JGit reference is retrieved by calling {@link Ref#getName()}
     */
    @Override
    public String getID() {
	return this.branch.getName();
    }

    /**
     * {@inheritDoc} {@link Repository#shortenRefName(String)} will be called to
     * keep only a human readable name of this branch.
     */
    @Override
    public String getName() {
	return Repository.shortenRefName(this.branch.getName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public VCSCommit getHead() throws VCSRepositoryException {

	RevWalk walk = null;
	try {

	    walk = new RevWalk(this.repo);
	    return new VCSCommitImp(
		    walk.parseCommit(this.branch.getObjectId()), this.repo);

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

	});
	return commits;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void walkCommits(final CommitVisitor<VCSCommit> visitor)
	    throws VCSRepositoryException {
	this.getHead().walkCommitBack(visitor);
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
	    return VCSCommitImp.isAncestor(rCommit, new RevWalk(this.repo)
		    .parseCommit(this.branch.getObjectId()), this.repo);
	} catch (final MissingObjectException e) {
	    throw new VCSRepositoryException(e);
	} catch (final IncorrectObjectTypeException e) {
	    throw new VCSRepositoryException(e);
	} catch (final IOException e) {
	    throw new VCSRepositoryException(e);
	}
    }

    @Override
    public String toString() {
	return this.branch.getName();
    }
}
