/**
 * 
 */
package gr.uom.se.vcs.analysis.version.provider;

import gr.uom.se.util.validation.ArgsCheck;

import java.util.ArrayList;
import java.util.Collection;

/**
 * An implementation of version validator that check if the given versions, are
 * valid if they can be converted to version string instances.
 * <p>
 * 
 * @author Elvis Ligu
 */
public class VersionStringValidator implements VersionValidator {

   /**
    * The version name resolver which will be used to convert a label into a
    * version name, in order to check if the given label is a version.
    */
   private final VersionNameResolver nameResolver;

   /**
    * Create an instance based on the given resolver.
    * <p>
    * 
    * @param the
    *           name resolver to convert a version label to a version string. if
    *           the resolver is null the {@linkplain DefaultVersionNameResolver
    *           default} resolver will be used.
    */
   public VersionStringValidator(VersionNameResolver resolver) {
      if (resolver == null) {
         resolver = new DefaultVersionNameResolver();
      }
      this.nameResolver = resolver;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean areValid(Collection<String> versionLabels) {
      ArgsCheck.notNull("versionLabels", versionLabels);
      Collection<VersionString> versions = new ArrayList<>();
      for (String ver : versionLabels) {
         VersionString vs = nameResolver.resolveVersionString(ver);
         if (vs == null) {
            return false;
         }
         versions.add(vs);
      }
      return areValidStrings(versions);
   }

   /**
    * This method will always return true.
    * <p>
    * Use {@link #areValid(Collection)} in order to specify a list of labels if
    * they are valid versions. This method should be used by subclasses which
    * will get the version string instances by the previous method, so we do not
    * have to create those instances again.
    * 
    * @param versions
    *           to check if they are valid.
    * @return always true
    */
   public boolean areValidStrings(Collection<VersionString> versions) {
      ArgsCheck.notNull("versions", versions);
      return true;
   }
}
