/**
 * 
 */
package gr.uom.se.vcs.walker.filter.resource;

import gr.uom.se.vcs.VCSResource;

import java.util.Collection;


/**
 * A base class for all path filters that require an exact path.
 * <p>
 * 
 * A path is considered exact only if a class needs exactly the specified paths,
 * and their immediate sub paths (a.k.a. children). This is very important when
 * {@link VCSResourceFilter#enter(VCSResource)} is called because any path under
 * the exact path should not be entered, that means we should stop the tree
 * walking at the exact path and collect the results if any. This allows us to
 * make any necessary optimizations. Sub classes of this class when combined
 * (AND operator) with another filter that doesn't deal with paths, such as
 * {@link SuffixFilter} should stop the entering of any resource at the exact
 * paths.
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public abstract class ExactAbstractPathFilter<T extends VCSResource> extends
      AbstractPathFilter<T> implements VCSResourceFilter<T> {

   public ExactAbstractPathFilter(Collection<String> paths) {
      super(paths);
   }

   /**
    * {@inheritDoc}
    * <p>
    * Enter only if the parent of this resource is a prefix of one of the given
    * paths. This will ensure that only the parent resource will need to enter.
    * So children will be excluded from entering. If the parent is null (usually
    * when this resource is a top level resource) then it will return true only
    * if the path of this resource is a prefix (but not equal to one of the
    * specified paths).
    */
   @Override
   public boolean enter(T resource) {

      String path = resource.getPath();
      if (paths.contains(path)) {
         return false;
      }
      if (path != null) {
         for (String prefix : paths) {
            if (isPrefix(path, prefix)) {
               return true;
            }
         }
      }
      return false;
   }
}
