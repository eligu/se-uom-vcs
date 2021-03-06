package gr.uom.se.vcs.jgit;

import gr.uom.se.util.validation.ArgsCheck;
import gr.uom.se.vcs.VCSDirectory;
import gr.uom.se.vcs.VCSFile;
import gr.uom.se.vcs.VCSResource;
import gr.uom.se.vcs.exceptions.VCSRepositoryException;
import gr.uom.se.vcs.exceptions.VCSResourceNotFoundException;
import gr.uom.se.vcs.jgit.utils.RevUtils;
import gr.uom.se.vcs.jgit.utils.TreeUtils;
import gr.uom.se.vcs.jgit.walker.filter.resource.OptimizedResourceFilter;
import gr.uom.se.vcs.jgit.walker.filter.resource.ResourceFilter;
import gr.uom.se.vcs.walker.ResourceVisitor;
import gr.uom.se.vcs.walker.filter.resource.ChildFilter;
import gr.uom.se.vcs.walker.filter.resource.PathPrefixFilter;
import gr.uom.se.vcs.walker.filter.resource.VCSResourceFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.AndTreeFilter;
import org.eclipse.jgit.treewalk.filter.PathFilter;

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

      TreeWalk walk = null;

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
            return;
         }

         // If the returned walk is not a subtree probably there is an
         // illegal state, because this resource
         // is a directory.

      } catch (final IOException e) {
         throw new VCSRepositoryException(e);
      } catch (final VCSResourceNotFoundException e) {
         throw new IllegalStateException(e);
      } finally {
         if (walk != null) {
            walk.release();
         }
      }

      throw new IllegalStateException(this.path + " is not a directory");
   }
}
