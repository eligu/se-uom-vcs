/**
 * 
 */
package gr.uom.se.vcs.analysis.version.provider;

/**
 * A version name resolver that will resolve a version only if it is a known
 * version scheme label.
 * 
 * @author Elvis Ligu
 */
public interface VersionNameResolver {

   /**
    * Given a version label it will return a version string if this label
    * conforms to a known versioning scheme.
    * <p>
    * 
    * @param label
    *           the version label
    * @return a version string if this label is a version label, or null
    */
   VersionString resolveVersionString(String label);
}
