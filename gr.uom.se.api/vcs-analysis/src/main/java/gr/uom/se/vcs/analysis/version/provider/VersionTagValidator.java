/**
 * 
 */
package gr.uom.se.vcs.analysis.version.provider;

import gr.uom.se.util.validation.ArgsCheck;
import gr.uom.se.vcs.VCSRepository;
import gr.uom.se.vcs.VCSTag;
import gr.uom.se.vcs.exceptions.VCSRepositoryException;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Validator to check if the given version strings can be resolved to a vcs tag.
 * 
 * @author Elvis Ligu
 */
public class VersionTagValidator extends VersionStringValidator {

   /**
    * To resolve the tags from.
    */
   private final VCSRepository repo;

   /**
    * Create a validator to check if a label can be converted to a version
    * string, and if this version string is a vcs tag.
    * 
    * @param resolver
    *           to convert a label to a version string.
    * @param repo
    *           to resolve a tag by its label.
    */
   public VersionTagValidator(VersionNameResolver resolver, VCSRepository repo) {
      super(resolver);
      ArgsCheck.notNull("repo", repo);
      this.repo = repo;
   }

   /**
    * {@inheritDoc}
    * <p>
    * This will return false if any of the specified version strings is not a
    * valid vcs tag. Valid, means that by getting its label we can not resolve a
    * tag.
    */
   @Override
   public boolean areValidStrings(Collection<VersionString> versions) {
      ArgsCheck.notNull("versions", versions);
      Map<VersionString, VCSTag> tags = new HashMap<>();
      for (VersionString vs : versions) {
         String label = null;
         if (vs == null || (label = vs.getLabel()) == null) {
            return false;
         }
         try {
            VCSTag tag = repo.resolveTag(label);
            tags.put(vs, tag);
         } catch (VCSRepositoryException re) {
            return false;
         }
      }
      return areValidTags(tags);
   }

   /**
    * Always return true.
    * <p>
    * This method will always return true, and should be used only by its
    * subclasses in order to specify a strategy. The reason why this method has
    * a body and is not abstract is because we want this validator to work as is
    * with other methods but all other subclasses not to recreate version
    * strings and tags.
    * 
    * @param versionTags
    *           the tags to validate
    * @return always true.
    */
   public boolean areValidTags(Map<VersionString, VCSTag> versionTags) {
      ArgsCheck.notNull("versionTags", versionTags);
      return true;
   }
}
