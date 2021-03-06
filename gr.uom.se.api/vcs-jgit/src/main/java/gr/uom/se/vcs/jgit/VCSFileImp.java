package gr.uom.se.vcs.jgit;

import gr.uom.se.vcs.VCSFile;
import gr.uom.se.vcs.VCSFileDiff;
import gr.uom.se.vcs.VCSResource;
import gr.uom.se.vcs.jgit.utils.TreeUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.treewalk.TreeWalk;

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
public class VCSFileImp extends VCSResourceImp implements VCSFile {

   /**
    * Creates a new instance based on the given arguments.
    * <p>
    *
    * @param commit where this file is at
    * @param path the path of this file
    * @param repo from where this path comes from
    */
   public VCSFileImp(final VCSCommitImp commit, final String path) {
      super(commit, path, VCSResource.Type.FILE);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public byte[] getContents() throws IOException {
      final ByteArrayOutputStream stream = new ByteArrayOutputStream();
      this.getContents(stream);
      return stream.toByteArray();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void getContents(final OutputStream target) throws IOException {

      final ObjectLoader loader = getObjectLoader(this);
      // Use the loader to copy the contents to the stream
      loader.copyTo(target);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public InputStream getContentStream() throws IOException {
      // Load the object to read contents
      final ObjectLoader loader = getObjectLoader(this);
      return loader.openStream();
   }

   static ObjectLoader getObjectLoader(VCSFileImp file) throws IOException {
      // create a tree walker and set path filter to this path only
      final TreeWalk treeWalk = TreeUtils.createTreeWalkForPathAndCheckIfExist(
              file.commit.commit, file.commit.repo, true, file.path);
      try {
         // get the first object of this walker within tree
         final ObjectId objectId = treeWalk.getObjectId(0);

         // In case object id is zero id that means, it is missing
         if (objectId.equals(ObjectId.zeroId())) {
            throw new IllegalStateException("problem loading file contents");
         }

         // Open an object loader to read file contents
         final ObjectLoader loader = file.commit.repo.open(objectId);
         return loader;
      } finally {
         treeWalk.release();
      }
   }
}
