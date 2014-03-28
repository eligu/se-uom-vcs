/**
 * 
 */
package se.uom.vcs.walker.filter;

import se.uom.vcs.VCSResource;

/**
 * Filter only those resources that are of type {@link VCSResource.Type#DIR}.<p>
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public class DirFilter<T extends VCSResource> implements VCSResourceFilter<T> {

    @Override
    public boolean include(T entity) {
	return entity.getType().equals(VCSResource.Type.DIR);
    }
}