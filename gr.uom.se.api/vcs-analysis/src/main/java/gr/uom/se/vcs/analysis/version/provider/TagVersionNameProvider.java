/**
 * 
 */
package gr.uom.se.vcs.analysis.version.provider;

import gr.uom.se.util.validation.ArgsCheck;
import gr.uom.se.vcs.VCSCommit;
import gr.uom.se.vcs.VCSRepository;
import gr.uom.se.vcs.VCSTag;
import gr.uom.se.vcs.exceptions.VCSRepositoryException;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * A version name provider based on repository tags.
 * <p>
 * This implementation may use all the repository tags, or they can be specified
 * at the creation time.
 * 
 * @author Elvis Ligu
 */
public class TagVersionNameProvider extends AbstractVersionNameProvider {

   /**
    * Cached mapped names to version strings. This is used by its super class to
    * resolve a version string when a name is required.
    */
   private final Map<String, VersionString> versionNames;

   /**
    * Create an instance based on a repository, a name resolver and a
    * comparator.
    * <p>
    * Using this constructor it will collect all tags of the repository, and
    * will allow only those that the version name resolver allows. The versions
    * will be ordered based on the given comparator or in their natural order if
    * the comparator is null.
    * 
    * @param repo
    *           a repository from where to resolve versions.
    * @param resolver
    *           to create a version string giving a tag name. If null a
    *           {@linkplain DefaultVersionNameResolver default} implementation
    *           will be used.
    * @param versionComparator
    *           used to order the versions.
    * @throws VCSRepositoryException
    *            if the repository could not be read.
    * 
    */
   public TagVersionNameProvider(VCSRepository repo,
         VersionNameResolver resolver,
         Comparator<VersionString> versionComparator)
         throws VCSRepositoryException {

      super(null, versionComparator);

      ArgsCheck.notNull("repo", repo);
      if (resolver == null) {
         resolver = new DefaultVersionNameResolver();
      }
      Map<VersionString, VCSCommit> versions = getMappedVersions(getVersionTags(
            repo, resolver));

      this.versionNames = getMappedNames(versions.keySet());
      this.initVersions(versions);
   }

   /**
    * Create an instance based on a repository, a name resolver and a
    * comparator.
    * <p>
    * Using this constructor it will collect all tags of the repository, and
    * will allow only those that the version name resolver allows. The versions
    * will not be ordered.
    * 
    * @param repo
    *           a repository from where to resolve versions.
    * @param resolver
    *           to create a version string giving a tag name. If null a
    *           {@linkplain DefaultVersionNameResolver default} implementation
    *           will be used.
    * @param versionComparator
    *           used to order the versions.
    * @throws VCSRepositoryException
    *            if the repository could not be read.
    * 
    */
   public TagVersionNameProvider(VCSRepository repo,
         VersionNameResolver resolver) throws VCSRepositoryException {

      super(null);

      ArgsCheck.notNull("repo", repo);
      if (resolver == null) {
         resolver = new DefaultVersionNameResolver();
      }
      Map<VersionString, VCSCommit> versions = getMappedVersions(getVersionTags(
            repo, resolver));

      this.versionNames = getMappedNames(versions.keySet());
      this.initVersions(versions);
   }

   /**
    * Create an instance based on a repository, a name resolver and a
    * comparator.
    * <p>
    * Using this constructor it will filter the tags and will allow only those
    * that the version name resolver allows. The versions will be ordered based
    * on the given comparator or in their natural order if the comparator is
    * null.
    * 
    * @param tags
    *           the initial tags to be used as versions.
    * @param resolver
    *           to create a version string giving a tag name. If null a
    *           {@linkplain DefaultVersionNameResolver default} implementation
    *           will be used.
    * @param versionComparator
    *           used to order the versions.
    * @throws VCSRepositoryException
    *            if the repository could not be read.
    * 
    */
   public TagVersionNameProvider(Collection<VCSTag> tags,
         VersionNameResolver resolver,
         Comparator<VersionString> versionComparator)
         throws VCSRepositoryException {

      super(null, versionComparator);

      if (resolver == null) {
         resolver = new DefaultVersionNameResolver();
      }
      Map<VersionString, VCSCommit> versions = getMappedVersions(getVersionTags(
            tags, resolver));
      this.versionNames = getMappedNames(versions.keySet());
      this.initVersions(versions);
   }

   /**
    * Create an instance based on a repository, a name resolver and a
    * comparator.
    * <p>
    * Using this constructor it will collect all tags of the repository, and
    * will allow only those that the version name resolver allows. The versions
    * will not be ordered.
    * 
    * @param tags
    *           the initial tags to be used as versions.
    * @param resolver
    *           to create a version string giving a tag name. If null a
    *           {@linkplain DefaultVersionNameResolver default} implementation
    *           will be used.
    * @throws VCSRepositoryException
    *            if the repository could not be read.
    * 
    */
   public TagVersionNameProvider(Collection<VCSTag> tags,
         VersionNameResolver resolver) throws VCSRepositoryException {

      super(null);

      if (resolver == null) {
         resolver = new DefaultVersionNameResolver();
      }
      Map<VersionString, VCSCommit> versions = getMappedVersions(getVersionTags(
            tags, resolver));
      this.versionNames = getMappedNames(versions.keySet());
      this.initVersions(versions);
   }

   /**
    * Put the given version to the maps.
    * <p>
    * 
    * @param versions
    *           to set up this provider
    */
   private void initVersions(Map<VersionString, VCSCommit> versions) {
      for (VersionString vs : versions.keySet()) {
         VCSCommit commit = versions.get(vs);
         this.mappedCommits.put(commit, vs);
         this.versions.put(vs, commit);
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected final VersionString getVersionString(String label) {
      return versionNames.get(label);
   }

   /**
    * Resolve version tags from a repo, and a name resolver.
    * 
    * @param repo
    * @param nameResolver
    * @return
    * @throws VCSRepositoryException
    */
   private Map<VersionString, VCSTag> getVersionTags(VCSRepository repo,
         VersionNameResolver nameResolver) throws VCSRepositoryException {
      return getVersionTags(repo.getTags(), nameResolver);
   }

   /**
    * Resolve version tags from the given versions, and the resolver.
    * 
    * @param versionTags
    * @param nameResolver
    * @return
    * @throws VCSRepositoryException
    */
   protected Map<VersionString, VCSTag> getVersionTags(
         Collection<VCSTag> versionTags, VersionNameResolver nameResolver)
         throws VCSRepositoryException {
      Map<VersionString, VCSTag> tags = new HashMap<>();
      for (VCSTag tag : versionTags) {
         VersionString vs = nameResolver.resolveVersionString(tag.getName());
         if (vs != null) {
            tags.put(vs, tag);
         }
      }
      return tags;
   }

   /**
    * Substitute tags with commits, from the given map.
    * 
    * @param tags
    * @return
    * @throws VCSRepositoryException
    */
   private Map<VersionString, VCSCommit> getMappedVersions(
         Map<VersionString, VCSTag> tags) throws VCSRepositoryException {
      Map<VersionString, VCSCommit> versions = new HashMap<>();
      for (VersionString vs : tags.keySet()) {
         versions.put(vs, tags.get(vs).getCommit());
      }
      return versions;
   }

   /**
    * Map the names of the versions to their respective version strings.
    * 
    * @param versions
    * @return
    */
   private Map<String, VersionString> getMappedNames(
         Collection<VersionString> versions) {
      Map<String, VersionString> map = new HashMap<>();
      for (VersionString vs : versions) {
         map.put(vs.getLabel(), vs);
      }
      return map;
   }
}
