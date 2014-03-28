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
public class FileNameFilter<T extends VCSResource> extends NameFilter<T>
	implements VCSResourceFilter<T> {

    public FileNameFilter(Collection<String> patterns) {
	super(patterns);
    }

    private FileFilter<VCSResource> fileFilter = new FileFilter<VCSResource>();

    @Override
    public boolean include(T entity) {

	return fileFilter.include(entity) && super.include(entity);
    }
}
