package gr.uom.se.vcs.jgit.walker.filter.resource;

import gr.uom.se.vcs.VCSResource;
import gr.uom.se.vcs.jgit.walker.OptimizedFilter;
import gr.uom.se.vcs.walker.filter.resource.VCSResourceFilter;

import org.eclipse.jgit.treewalk.filter.TreeFilter;


/**
 * An optimized version of a {@link VCSResourceFilter}.
 * <p>
 * 
 * This filter will contain a tree filter implementation of JGit library that
 * corresponds, to a filter type of {@link VCSResourceFilter}.
 * <p>
 * For every default resource filter (provided by vcs-api) that is used in a
 * visitor, it will be parsed and converted to a corresponding JGit filter
 * (provided by JGit library or implemented by this implementation). This class
 * represents a parsed and converted default resource filter, ready to be used
 * in a TreeWalk object.(Actually this is a place holder for the filter.
 * see {@link #getCurrent()})
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 * @param <T>
 */
public class OptimizedResourceFilter<T extends VCSResource> extends
      OptimizedFilter<T> implements VCSResourceFilter<T> {

   /**
    * The tree filter.
    * <p>
    */
   private TreeFilter current;

   /**
    * Creates a new instance of filter.
    * <p>
    * 
    * @param filter
    */
   public OptimizedResourceFilter(TreeFilter filter) {
      this.current = filter;
   }

   /**
    * @return the JGit filter
    */
   public TreeFilter getCurrent() {
      return current;
   }

   /**
    * Set the JGit filter.
    * <p>
    * 
    * @param current
    */
   public void setCurrent(TreeFilter current) {
      this.current = current;
   }

   /**
    * Not used method.
    * <p>
    */
   @Override
   public boolean include(T entity) {
      return false;
   }

   /**
    * Not used method.
    * <p>
    */
   @Override
   public boolean enter(T resource) {
      return false;
   }
}
