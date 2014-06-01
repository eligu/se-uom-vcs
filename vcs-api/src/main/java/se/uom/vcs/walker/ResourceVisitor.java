/**
 * 
 */
package se.uom.vcs.walker;

import se.uom.vcs.VCSResource;
import se.uom.vcs.walker.filter.resource.VCSResourceFilter;

/**
 * A visitor used when walking resources, that apply resource filters.
 * <p>
 * 
 * This is a marker interface and will limit the range of filter returned to a
 * resource filter.
 * <p>
 * {@link #getFilter()} will return a {@link VCSResourceFilter} that will be
 * used to filter the results.
 * <p>
 * When walking resources keep in mind that the process requires a lot of
 * processing power, and it is highly recommended that you use a resource filter
 * if only certain resources are required. Do not filter the results at
 * {@link Visitor#visit(Object)} level, because it will not allow the
 * implementation to make any optimization, otherwise for certain types of
 * resources that are under some specified paths the whole tree will be parsed.
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public interface ResourceVisitor<T extends VCSResource> extends
      FilterVisitor<T> {

   /**
    * {@inheritDoc}
    * <p>
    * <b>WARNING:</b> Implementations has to ensure that this will return a
    * resource filter.
    * 
    * @see VCSResourceFilter
    */
   @SuppressWarnings("unchecked")
   public VCSResourceFilter<T> getFilter();

   /**
    * Specify if the tree walking should include directory resources to be
    * visited by visitor.
    * <p>
    * 
    * Note that, some VCSs does not track directories, but only files, that
    * means the returned value will have no effect at all. However the user
    * should read the implementation's API to specify if this is true.
    * 
    * @return true if this visitor should visit directories
    */
   public boolean includeDirs();

   /**
    * Specify if the tree walking should include file resources to be visited by
    * this visitor.
    * <p>
    * 
    * A good idea is, this value to be true by default, because in most
    * situations the files are the only entities that provide true information
    * from a VCS.
    * 
    * @return true if this visitor should visit files
    */
   public boolean includeFiles();
}
