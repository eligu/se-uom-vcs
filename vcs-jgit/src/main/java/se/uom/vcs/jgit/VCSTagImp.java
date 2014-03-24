/**
 * 
 */
package se.uom.vcs.jgit;

import java.io.IOException;

import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevWalk;

import se.uom.vcs.VCSCommit;
import se.uom.vcs.VCSTag;
import se.uom.vcs.exceptions.VCSRepositoryException;

/**
 * Implementation of {@link VCSTag} based on JGit library.
 * <p>
 * 
 * @author Elvis Ligu
 * @since 0.0.1
 * @version 0.0.1
 * @see VCSTag
 */
public class VCSTagImp implements VCSTag {

	/**
	 * JGit Ref for this tag
	 */
	protected Ref tag;

	/**
	 * The repository this tag comes from
	 */
	protected Repository repo;

	/**
	 * Creates a new instance based on the given parameters.<p>
	 * 
	 * @param ref must not be null
	 * @param repo must not be null
	 */
	public VCSTagImp(final Ref ref, final Repository repo) {
		ArgsCheck.notNull("ref", ref);
		ArgsCheck.notNull("repo", repo);

		try {
			this.tag = repo.getRef(ref.getName());
		} catch (final IOException e) {
			throw new IllegalArgumentException("given reference is not contained in repository");
		}

		this.repo = repo;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getID() {
		return this.tag.getName();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getTagName() {
		return Repository.shortenRefName(this.tag.getName());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public VCSCommit getCommit() throws VCSRepositoryException {

		final RevWalk walk = new RevWalk(this.repo);
		ObjectId id = this.tag.getPeeledObjectId();
		if (id == null) {
			id = this.tag.getObjectId();
		}

		try {

			return id != null ? new VCSCommitImp(walk.parseCommit(id), this.repo) : null;

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
		return this.tag.getName();
	}
}
