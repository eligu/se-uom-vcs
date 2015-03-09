/**
 * 
 */
package gr.uom.se.vcs.analysis.version.provider;

import gr.uom.se.vcs.VCSTag;
import gr.uom.se.vcs.exceptions.VCSRepositoryException;

import java.util.Collection;

/**
 * A tag provider interface that is used to resolve tags by tag version
 * providers.
 * <p>
 * 
 * @author Elvis Ligu
 */
public interface TagProvider {

   /**
    * Get all available tags.
    * <p>
    * 
    * @return the available tags
    * @throws VCSRepositoryException
    *            if tags can not be resolved by repository
    */
   Collection<VCSTag> getTags() throws VCSRepositoryException;
}
