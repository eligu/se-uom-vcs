/**
 * 
 */
package gr.uom.se.vcs.analysis.version.provider;

import gr.uom.se.util.validation.ArgsCheck;
import gr.uom.se.vcs.VCSCommit;
import gr.uom.se.vcs.VCSRepository;
import gr.uom.se.vcs.VCSTag;
import gr.uom.se.vcs.exceptions.VCSRepositoryException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * A validator that will check if all version tags conform to reachability.
 * <p>
 * Reachability means that all older versions are reached from a newer version
 * if walking back from newer version the head commit, and finding during the
 * walk all the older. If check ordering is specified it will also check if a
 * current version can resolve the same previous version when walking as that
 * which is the previous in the specified list.
 * 
 * @author Elvis Ligu
 */
public class VersionTagReachabilityValidator extends VersionTagValidator {

   /**
    * Custom comparator to check version strings.
    */
   private final Comparator<VersionString> versionComparator;

   /**
    * If true will also check if all versions are ordered in the same order as
    * they are resolved while walking them.
    */
   private final boolean checkOrdering;

   /**
    * Create a validator that will check if the provided version tags, conform
    * to reachability.
    * <p>
    * Reachability means that all older versions are reached from a newer
    * version if walking back from newer version the head commit, and finding
    * during the walk all the older.
    * 
    * @param resolver
    *           that will convert the version labels from string to version
    *           string
    * @param repo
    *           to resolve tags from labels
    * @param versionComparator
    *           to compare two version strings, if null their default comparing
    *           strategy will be used.
    * @param checkOrdering
    *           if true will also check if the versions are ordered as they are
    *           resolved while walking from newer to older.
    */
   public VersionTagReachabilityValidator(VersionNameResolver resolver,
         VCSRepository repo, Comparator<VersionString> versionComparator,
         boolean checkOrdering) {
      super(resolver, repo);
      this.versionComparator = versionComparator;
      this.checkOrdering = checkOrdering;
   }

   /**
    * Create a validator that will check if the provided version tags, conform
    * to reachability.
    * <p>
    * Reachability means that all older versions are reached from a newer
    * version if walking back from newer version the head commit, and finding
    * during the walk all the older.
    * 
    * @param resolver
    *           that will convert the version labels from string to version
    *           string
    * @param repo
    *           to resolve tags from labels
    * @param checkOrdering
    *           if true will also check if the versions are ordered as they are
    *           resolved while walking from newer to older.
    */
   public VersionTagReachabilityValidator(VersionNameResolver resolver,
         VCSRepository repo, boolean checkOrdering) {
      this(resolver, repo, null, checkOrdering);
   }

   /**
    * Create a validator that will check if the provided version tags, conform
    * to reachability.
    * <p>
    * Reachability means that all older versions are reached from a newer
    * version if walking back from newer version the head commit, and finding
    * during the walk all the older. The new validator will also check for
    * ordering of versions.
    * 
    * @param resolver
    *           that will convert the version labels from string to version
    *           string
    * @param repo
    *           to resolve tags from labels
    * @param versionComparator
    *           to compare two version strings, if null their default comparing
    *           strategy will be used.
    */
   public VersionTagReachabilityValidator(VersionNameResolver resolver,
         VCSRepository repo, Comparator<VersionString> versionComparator) {
      this(resolver, repo, versionComparator, true);
   }

   /**
    * Create a validator that will check if the provided version tags, conform
    * to reachability.
    * <p>
    * Reachability means that all older versions are reached from a newer
    * version if walking back from newer version the head commit, and finding
    * during the walk all the older. The new validator will also check for
    * ordering of versions.
    * 
    * @param resolver
    *           that will convert the version labels from string to version
    *           string
    * @param repo
    *           to resolve tags from labels
    */
   public VersionTagReachabilityValidator(VersionNameResolver resolver,
         VCSRepository repo) {
      this(resolver, repo, null, true);
   }

   /**
    * Create a validator that will check if the provided version tags, conform
    * to reachability.
    * <p>
    * Reachability means that all older versions are reached from a newer
    * version if walking back from newer version the head commit, and finding
    * during the walk all the older. The new validator will also check for
    * ordering of versions.
    * <p>
    * This will use the {@linkplain DefaultVersionNameResolver default} resolver
    * for version names.
    * 
    * @param repo
    *           to resolve tags from labels
    */
   public VersionTagReachabilityValidator(VCSRepository repo) {
      this(null, repo, null, true);
   }

   /**
    * {@inheritDoc}
    * <p>
    * Will check if a given version tag can reach all its previous version tags,
    * by walking the commits starting from newest to oldest.
    */
   @Override
   public boolean areValidTags(Map<VersionString, VCSTag> versionTags) {
      ArgsCheck.notNull("versionTags", versionTags);
      // Ensure the provided map is a tree map,
      // if not create it
      if (!(versionTags instanceof TreeMap)) {
         TreeMap<VersionString, VCSTag> temp = new TreeMap<>(versionComparator);
         temp.putAll(versionTags);
         versionTags = temp;
      }
      final Map<VCSCommit, VCSTag> vcommits;
      try {
         // Map commits to tags
         vcommits = mapTags(versionTags.values());
         // Validate the versions
         return areValidTags(versionTags, vcommits);
      } catch (VCSRepositoryException e) {
         return false;
      }
   }

   /**
    * Will check if a given version tag can reach all its previous version tags,
    * by walking the commits starting from newest to oldest.
    * <p>
    * 
    * @param versionTags
    *           the tags that are supposed to be versions.
    * @param vcommits
    *           the mapped commits of the tags
    * @return true if each current version can reach all its oldest versions
    *         while walking back from its head commit.
    */
   @SuppressWarnings("unchecked")
   public boolean areValidTags(Map<VersionString, VCSTag> versionTags,
         final Map<VCSCommit, VCSTag> vcommits) {
      ArgsCheck.notNull("versionTags", versionTags);
      ArgsCheck.notNull("vcommits", vcommits);
      // Ensure the provided map is a tree map,
      // if not create it
      if (!(versionTags instanceof TreeMap)) {
         TreeMap<VersionString, VCSTag> temp = new TreeMap<>(versionComparator);
         temp.putAll(versionTags);
         versionTags = temp;
      }

      try {

         TreeMap<VersionString, VCSTag> versions = (TreeMap<VersionString, VCSTag>) versionTags;

         // For each version tag, starting from newest to oldest
         // check if all the previous tags are reachable by the current
         for (VersionString vs : versions.descendingKeySet()) {
            // Get the map with the previous versions
            final SortedMap<VersionString, VCSTag> previous = versions
                  .headMap(vs);
            // If empty that means there are no previous
            // versions
            if (previous.isEmpty()) {
               return true;
            }
            // Get the current head tag
            final VCSTag headTag = versions.get(vs);
            // Resolve the commit of the head
            VCSCommit cc = headTag.getCommit();
            final List<VCSTag> tags = new ArrayList<>();

            // Walk all commits back and collect all tags
            // that we find
            cc.walkCommits(new AbstractCommitVisitor() {

               @Override
               public boolean visit(VCSCommit entity) {
                  VCSTag tag = vcommits.get(entity);
                  if (tag != null && !tag.equals(headTag)) {
                     tags.add(tag);
                  }
                  return true;
               }

            }, true);
            // If we did not collect a tag that is not
            // in the previous map, that means one of the
            // previous tags is not reachable by the current tag
            Collection<VCSTag> values = previous.values();
            if (!tags.containsAll(values)) {
               return false;
            }

            // If check ordering is true
            // we should check if all versions
            // are order as they are walked.
            if (checkOrdering) {
               VCSTag before = tags.get(0);
               VersionString previousString = previous.lastKey();
               VCSTag previousTag = previous.get(previousString);
               if (!before.equals(previousTag)) {
                  return false;
               }
            }
         }
         // Return true if all tags are checked
         return true;
      } catch (VCSRepositoryException e) {
         // If an exception occurs we return a false,
         // generally telling to client that we can not validate the
         // tags
         return false;
      }
   }

   /**
    * Map the given tags to their respective commits.
    * <p>
    * 
    * @param tags
    *           to be mapped
    * @return a mapping of commits to tags
    * @throws VCSRepositoryException
    *            if something goes wrong while resolving the commit of a tag.
    */
   private static Map<VCSCommit, VCSTag> mapTags(final Collection<VCSTag> tags)
         throws VCSRepositoryException {
      Map<VCSCommit, VCSTag> map = new HashMap<>();
      for (VCSTag tag : tags) {
         map.put(tag.getCommit(), tag);
      }
      return map;
   }
}
