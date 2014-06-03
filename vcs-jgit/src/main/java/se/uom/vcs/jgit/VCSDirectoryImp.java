package se.uom.vcs.jgit;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.AndTreeFilter;
import org.eclipse.jgit.treewalk.filter.PathFilter;

import se.uom.vcs.VCSDirectory;
import se.uom.vcs.VCSFile;
import se.uom.vcs.VCSResource;
import se.uom.vcs.exceptions.VCSRepositoryException;
import se.uom.vcs.exceptions.VCSResourceNotFoundException;
import se.uom.vcs.jgit.utils.RevUtils;
import se.uom.vcs.jgit.utils.TreeUtils;
import se.uom.vcs.jgit.walker.filter.resource.OptimizedResourceFilter;
import se.uom.vcs.jgit.walker.filter.resource.ResourceFilter;
import se.uom.vcs.walker.ResourceVisitor;
import se.uom.vcs.walker.filter.resource.ChildFilter;
import se.uom.vcs.walker.filter.resource.PathPrefixFilter;
import se.uom.vcs.walker.filter.resource.VCSResourceFilter;

/**
 * Implementation of {@link VCSDirectory} based on JGit library.
 * <p>
 * 
 * This is an immutable object and is considered thread safe.
 * 
 * @author Elvis Ligu
 * @since 0.0.1
 * @version 0.0.1
 * @see VCSDirectory
 */
public class VCSDirectoryImp extends VCSResourceImp implements VCSDirectory {

   /**
    * Creates a new instance with the given arguments.
    * <p>
    * 
    * @param commit
    *           where this directory is alive
    * @param path
    *           of directory
    */
   public VCSDirectoryImp(final VCSCommitImp commit, final String path) {
      super(commit, path, VCSResource.Type.DIR);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Set<VCSFile> getFiles() throws VCSRepositoryException {

      // The resources will be stored here
      final Set<VCSFile> resources = new HashSet<VCSFile>();

      // Create a visitor to walk through this directory and collect the
      // results
      final ResourceVisitor<VCSResource> visitor = new ResourceVisitor<VCSResource>() {

         @Override
         public boolean visit(final VCSResource entity) {
            resources.add((VCSFile) entity);
            return true;
         }

         @Override
         public boolean includeDirs() {
            return false;
         }

         @Override
         public boolean includeFiles() {
            return true;
         }

         @SuppressWarnings("unchecked")
         @Override
         public VCSResourceFilter<VCSResource> getFilter() {
            return new ChildFilter<VCSResource>(Arrays.asList(path));
         }
      };

      // Start walking the results
      this.walkResources(visitor);

      return resources;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Set<VCSDirectory> getDirectories() throws VCSRepositoryException {

      // The resources will be stored here
      final Set<VCSDirectory> resources = new HashSet<VCSDirectory>();

      // Create a visitor to walk through this directory and collect the
      // results
      final ResourceVisitor<VCSResource> visitor = new ResourceVisitor<VCSResource>() {

         @Override
         public boolean visit(final VCSResource entity) {
            resources.add((VCSDirectory) entity);
            return true;
         }

         @Override
         public boolean includeDirs() {
            return true;
         }

         @Override
         public boolean includeFiles() {
            return false;
         }

         @SuppressWarnings("unchecked")
         @Override
         public VCSResourceFilter<VCSResource> getFilter() {
            return new ChildFilter<VCSResource>(Arrays.asList(path));
         }

      };

      // Start walking the results
      this.walkResources(visitor);

      return resources;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Set<VCSResource> getResources(final boolean files,
         final boolean dirs, final boolean recursive)
         throws VCSRepositoryException {

      ArgsCheck.isTrue("files || dirs", files || dirs);

      // The resources will be stored here
      final Set<VCSResource> resources = new HashSet<VCSResource>();

      // Create a visitor to walk through this directory and collect the
      // results
      final ResourceVisitor<VCSResource> visitor = new ResourceVisitor<VCSResource>() {

         @Override
         public boolean visit(final VCSResource entity) {
            resources.add(entity);
            return true;
         }

         @Override
         public boolean includeDirs() {
            return dirs;
         }

         @Override
         public boolean includeFiles() {
            return files;
         }

         @SuppressWarnings("unchecked")
         @Override
         public VCSResourceFilter<VCSResource> getFilter() {
            if (recursive) {
               return new PathPrefixFilter<VCSResource>(Arrays.asList(path));
            }
            return new ChildFilter<VCSResource>(Arrays.asList(path));

         }

      };

      // Start walking the results
      this.walkResources(visitor);

      return resources;
   }

   /**
    * {@inheritDoc}
    */

   @Override
   public void walkResources(final ResourceVisitor<VCSResource> visitor)
         throws VCSRepositoryException {

      ArgsCheck.notNull("visitor", visitor);

      final boolean dirs = visitor.includeDirs();
      final boolean files = visitor.includeFiles();

      ArgsCheck.isTrue("includeDirs || includeFiles", dirs || files);

      // Get the filter from visitor if any
      VCSResourceFilter<VCSResource> filter = visitor.getFilter();
      // Check if the filter allow to enter this resource
      if (filter != null) {
         if (!filter.enter(this)) {
            throw new IllegalArgumentException(
                  "provided filter doesn't allow to enter path " + this.path);
         }
      }

      final TreeWalk walk;

      try {
         // Create the tree walk with recursive false so we can have TREE
         // entries from walk
         walk = TreeUtils.getTreeWalkForPath(this.commit.commit,
               this.commit.repo, this.path);

         if (filter != null) {
            OptimizedResourceFilter<VCSResource> of = ResourceFilter.parse(
                  filter, null);
            if (of != null) {
               walk.setFilter(AndTreeFilter.create(PathFilter.create(path),
                     of.getCurrent()));
               filter = null;
            }
         }

         // The walk should definitely return a subdir because we requested
         // it for this
         // path which is a directory path
         // Check if this path is a subtree (a directory)
         // NOTE: we only allow paths under normal directories so if this is
         // a symlink or a submodule
         // we exclude it
         if (walk.isSubtree() && RevUtils.isDirMode(walk.getFileMode(0))) {

            walk.enterSubtree();

            if (filter == null) {
               this.commit.walkTreeNoFilter(visitor, walk);
            } else {
               this.commit.walkTreeWithFilter(visitor, walk);
            }
            // TODO: must remove old implementation after check for consistency
            /*
             * while (walk.next()) {
             * 
             * // This is probably a directory if (walk.isSubtree()) {
             * 
             * // Check to be sure if this is a directory type // This will
             * ensure that we do not follow symlinks and // other submodules if
             * (VCSCommitImp.isDirMode(walk.getFileMode(0))) {
             * 
             * // if directories will be included then add this // entry
             * VCSResource resource = new VCSDirectoryImp(this.commit,
             * walk.getPathString()); if (dirs) { if (filter != null) { if
             * (filter.include(resource)) { if (!visitor.visit(resource)) {
             * return; } } } else { // Visitor is not willing to continue the //
             * tree // walk if (!visitor.visit(resource)) { return; } } }
             * 
             * // Enter the subtree if all resources under the // given
             * directory are required if (filter != null) { if
             * (filter.enter(resource)) walk.enterSubtree(); } else {
             * walk.enterSubtree(); } } // This is not a subtree so it must be a
             * file // Check to be sure this is a regular file and // if visitor
             * wants files then pass a file to visitor } else if
             * (VCSCommitImp.isFileMode(walk.getFileMode(0)) && files) {
             * 
             * VCSResource resource = new VCSFileImp(this.commit,
             * walk.getPathString()); if (filter != null) { if
             * (filter.include(resource)) { if (!visitor.visit(resource)) {
             * return; } } } else if (!visitor.visit(resource)) { return; } } }
             */
            // the walking ends here so just return
            return;
         }

         // If the returned walk is not a subtree probably there is an
         // illegal state, because this resource
         // is a directory.

      } catch (final IOException e) {
         throw new VCSRepositoryException(e);
      } catch (final VCSResourceNotFoundException e) {
         throw new IllegalStateException(e);
      }

      throw new IllegalStateException(this.path + " is not a directory");
   }
}
