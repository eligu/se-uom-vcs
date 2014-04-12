/**
 * 
 */
package se.uom.vcs.walker.filter.resource;

import se.uom.vcs.VCSResource;

/**
 * A filter that check the type of a resource.<p>
 * 
 * This filter will include only the resources that has the same type as the
 * given type when this was created.
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public class TypeFilter<T extends VCSResource> extends ExactFilter<T> implements VCSResourceFilter<T> {

    /**
     * The type to check each resource.<p>
     */
    private VCSResource.Type type;
    
    public TypeFilter(VCSResource.Type type) {
	if(type == null) {
	    throw new IllegalArgumentException("type must not be null");
	}
	this.type = type;
    }
    
    /**
     * {@inheritDoc}
     * <p>
     * Returns true only if the given entity has the same 
     * type as this filter type.
     */
    @Override
    public boolean include(T entity) {
	return entity.getType().equals(type);
    }

    @Override
    public VCSResourceFilter<T> not() {
        return new TypeFilter<T>(type) {
            @Override
            public boolean include(T entity) {
                return !super.include(entity);
            }
            @Override
            public VCSResourceFilter<T> not() {
                return new TypeFilter<T>(type);
            }
        };
    }
    
    @Override
    protected boolean excludesAnd(AbstractResourceFilter<T> filter) {
        if(filter instanceof PathFilter) {
            return true;
        }
        if(filter instanceof TypeFilter) {
            return true;
        }
        return false;
    }
}
