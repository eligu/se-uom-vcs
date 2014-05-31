package se.uom.vcs.jgit;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jgit.lib.ObjectId;
import org.gitective.core.BlobUtils;

import se.uom.vcs.Edit;
import se.uom.vcs.VCSChange;
import se.uom.vcs.VCSFileDiff;

/**
 * Implementation of {@link VCSFileDiff} based on JGit library.
 * <p>
 * 
 * This is an immutable object and is considered thread safe.
 * 
 * @author Elvis Ligu
 * @since 0.0.1
 * @version 0.0.1
 * @see VCSFileDiff
 */
public class VCSFileDiffImp<T extends VCSFileImp> extends VCSChangeImp<T>
      implements VCSFileDiff<T> {

   /**
    * Creates a new instance based on the given arguments.
    * <p>
    * 
    * @param newR
    *           the new resource
    * @param oldR
    *           the old resource
    * @param type
    *           the type of change
    */
   public VCSFileDiffImp(final T newR, final T oldR, final VCSChange.Type type) {
      super(newR, oldR, type);
   }

   /**
    * {@inheritDoc}
    * <p>
    * <b>WARNING: Use this method wisely, because it may cause memory
    * problems.</b> The implementation doesn't make any distinct for the kind of
    * type, so it may produce wrong results (diffing byte files) or may diff a
    * very large byte file which can cause memory problems.
    * <p>
    * Diffing two files is a very expensive process, thus you should cache this
    * result if you intend to use it in the future.
    */
   @Override
   public Set<Edit> getEdits() {

      // If file is deleted or added there is no need to proceed further,
      // just return an empty set
      if (this.type.isDelete() || this.type.isAdd()) {
         return Collections.emptySet();
      }

      // The new file object id
      final ObjectId newF = BlobUtils.getId(this.newResource.commit.repo,
            this.newResource.commit.commit, this.newResource.path);
      // The old file object id
      final ObjectId oldF = BlobUtils.getId(this.oldResource.commit.repo,
            this.oldResource.commit.commit, this.oldResource.path);

      // Produce the diffs
      // WARNING: looking at the implementation of this method we realized
      // that the data are stored in memory
      // for each diff that will be produced. JGit implementation of
      // determining if this file is a byte file
      // or not is a heuristic one, that is it might produce a false positive
      // in whether this resource is a text
      // data or not. On the other hand, if the file is to big it may have
      // memory problems.
      final Collection<org.eclipse.jgit.diff.Edit> edits = BlobUtils.diff(
            this.newResource.commit.repo, oldF, newF);
      final Set<Edit> result = new HashSet<Edit>();

      for (final org.eclipse.jgit.diff.Edit e : edits) {
         result.add(new Edit(e.getBeginA(), e.getEndA(), e.getBeginB(), e
               .getEndB()));
      }

      return result;
   }
}
