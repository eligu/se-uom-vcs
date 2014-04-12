/**
 * 
 */
package se.uom.vcs.walker.filter.resource;

import se.uom.vcs.VCSResource;
import se.uom.vcs.exceptions.VCSRepositoryException;

/**
 * Filter only the resources that are modified at a given commit.<p>
 * 
 * A modified resource should return true when {@link VCSResource#isAdded()} ||
 * {@link VCSResource#isModified()}.
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public class ModifiedFilter<T extends VCSResource> extends ExactFilter<T> implements VCSResourceFilter<T> {

    @Override
    public boolean include(T entity) {
	try {
	    return entity.isAdded() || entity.isModified();
	} catch (VCSRepositoryException e) {
	   throw new IllegalArgumentException(e);
	}
    }

    @Override
    public VCSResourceFilter<T> not() {
	return new ModifiedFilter<T>() {
	    @Override
	    public boolean include(T entity) {
		return !super.include(entity);
	    }
	    @Override
	    public VCSResourceFilter<T> not() {
	        return new ModifiedFilter<T>();
	    }
	};
    }
    
    @Override
    protected boolean excludesAnd(AbstractResourceFilter<T> filter) {
        if(filter instanceof ModifiedFilter) {
            return true;
        }
        return false;
    }
}
