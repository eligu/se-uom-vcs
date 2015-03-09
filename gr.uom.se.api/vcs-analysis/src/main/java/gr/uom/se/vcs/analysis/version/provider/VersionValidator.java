/**
 * 
 */
package gr.uom.se.vcs.analysis.version.provider;

import java.util.Collection;

/**
 * A version validator that will check if a collection of version labels are
 * valid versions.
 * <p>
 * This is important in cases a client needs to resolve versions based on a
 * specific strategy but the user input is wrong.
 * 
 * @author Elvis Ligu
 */
public interface VersionValidator {

   /**
    * Return true if the given collection of the versions are valid.
    * <p>
    * 
    * @param versionLabels
    *           the labels of the versions to validate
    * @return true if the version labels are valid.
    */
   boolean areValid(Collection<String> versionLabels);
}
