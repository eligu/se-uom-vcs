/**
 * 
 */
package se.uom.vcs.walker.filter;

import java.util.Collection;

import se.uom.vcs.VCSResource;

/**
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public class DirNameFilter<T extends VCSResource> extends NameFilter<T>
	implements VCSResourceFilter<T> {

    public DirNameFilter(Collection<String> patterns) {
	super(patterns);
    }

    private DirFilter<VCSResource> dirFilter = new DirFilter<VCSResource>();

    @Override
    public boolean include(T entity) {

	return dirFilter.include(entity) && super.include(entity);
    }
}