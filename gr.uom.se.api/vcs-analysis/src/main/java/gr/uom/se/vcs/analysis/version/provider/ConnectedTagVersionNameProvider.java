/**
 * 
 */
package gr.uom.se.vcs.analysis.version.provider;

import gr.uom.se.util.collection.Maps;
import gr.uom.se.util.collection.OrderedBiMap;
import gr.uom.se.util.validation.ArgsCheck;
import gr.uom.se.vcs.VCSCommit;
import gr.uom.se.vcs.VCSTag;
import gr.uom.se.vcs.exceptions.VCSRepositoryException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * A version name provider based on tags, that are connected.
 * <p>
 * By connected tags we mean that starting from a commit of a tag we can resolve
 * each previous tag walking it backward. Also the order of the resolved tags
 * should be the same as the order of the versions stored within this provider.
 * That is each version should resolve first its previous, and then the previous
 * of its previous and so on.
 * 
 * @author Elvis Ligu
 */
public class ConnectedTagVersionNameProvider extends TagVersionNameProvider
      implements ConnectedVersionNameProvider {

   /**
    * The mapping of versions to commits and vice versa. This is used just as a
    * casted filed from the base class.
    */
   private final OrderedBiMap<VersionString, VCSCommit> myVersions;

   /**
    * Create a version name provider based on a tag provider, a name resolver,
    * and a version string comparator.
    * <p>
    * The comparator is very important because it can affect the function of
    * this provider. If a null is provided then the default comparing mechanism
    * of version string implementation is used.
    * 
    * @param tagProvider
    *           a provider of tags that will be used to resolve the tags. Must
    *           not be null.
    * @param resolver
    *           to be used to resolve a version string by a tag name. A version
    *           tag is considered a tag whose name can be converted to a version
    *           string. If null a {@linkplain DefaultVersionNameResolver
    *           default} implementation will be used.
    * @param versionComparator
    *           a comparator, to be used to order versions. If null the default
    *           version string comparing mechanism will be used.
    * @throws VCSRepositoryException
    */
   public ConnectedTagVersionNameProvider(TagProvider tagProvider,
         VersionNameResolver resolver,
         Comparator<VersionString> versionComparator)
         throws VCSRepositoryException {

      super(tagProvider.getTags(), resolver, versionComparator);
      myVersions = (OrderedBiMap<VersionString, VCSCommit>) super.versions;
   }

   /**
    * Create a version name provider based on a tag provider.
    * <p>
    * This will order the versions based an the default comparing mechanism of
    * version strings implementation.
    * 
    * @param tagProvider
    *           a provider of tags that will be used to resolve the tags. Must
    *           not be null.
    * @throws VCSRepositoryException
    */
   public ConnectedTagVersionNameProvider(TagProvider tagProvider)
         throws VCSRepositoryException {

      this(tagProvider, null, null);
   }

   /**
    * {@inheritDoc}
    * <p>
    * By implementing this method, we instruct the super class to collect only
    * the tags that are connected between them.
    */
   @Override
   protected final Map<VersionString, VCSTag> getVersionTags(
         Collection<VCSTag> versionTags, VersionNameResolver nameResolver)
         throws VCSRepositoryException {

      Map<VCSTag, VersionString> versions = getVersions(versionTags,
            nameResolver);
      versions = getConnectedVersions(versions);
      return Maps.getReverted(versions, new TreeMap<VersionString, VCSTag>(
            versionComparator));
   }

   /**
    * Collect only those version tags that can be converted to version strings.
    * 
    * @param versionTags
    *           the tags to filter
    * @param nameResolver
    *           the name resolver to convert tag names to version strings.
    * @return a mapping of tags to their respective version strings.
    */
   private Map<VCSTag, VersionString> getVersions(
         Collection<VCSTag> versionTags, VersionNameResolver nameResolver) {

      Map<VCSTag, VersionString> versions = new HashMap<>();
      for (VCSTag tag : versionTags) {
         VersionString vs = nameResolver.resolveVersionString(tag.getName());
         if (vs != null) {
            versions.put(tag, vs);
         }
      }
      return versions;
   }

   /**
    * Retain only the connected versions in the given map.
    * <p>
    * 
    * Connected means, that for each current version all the previous versions
    * are resolved if walking back from this version until start. Also the
    * immediate previous version of the current version should be the first to
    * resolve when walking back the current version.
    * 
    * @param versionsMap
    *           source of versions.
    * @return the same map
    * @throws VCSRepositoryException
    */
   @SuppressWarnings("unchecked")
   private Map<VCSTag, VersionString> getConnectedVersions(
         final Map<VCSTag, VersionString> versionsMap)
         throws VCSRepositoryException {

      // Return if map is empty
      if (versionsMap.isEmpty()) {
         return versionsMap;
      }
      // Revert the versions so we can start by getting version strings
      // from newest to old.
      TreeMap<VersionString, VCSTag> versions = Maps.getReverted(versionsMap,
            new TreeMap<VersionString, VCSTag>(versionComparator));

      // Create a map between commits and their tags so we
      // can resolve a tag given a commit during walk.
      final Map<VCSCommit, VCSTag> vcommits = mapTags(versions.values());

      // Where resolved tags should be stored
      List<VCSTag> list = new ArrayList<>();
      while (!versions.isEmpty()) {

         // Get the heads
         final VersionString headString = versions.lastKey();
         final VCSTag head = versions.get(headString);
         list.add(head);
         // No need to walk any commit since there no
         // version left to check for connection
         if (versions.size() == 1) {
            break;
         }
         // The commit to walk back
         final VCSCommit cc = head.getCommit();
         // The resolved tags will be placed here
         final Set<VersionString> tags = new HashSet<>();

         cc.walkCommits(new AbstractCommitVisitor() {

            @Override
            public boolean visit(VCSCommit commit) {
               // Get the corresponding tag
               VCSTag tag = vcommits.get(commit);
               // This is a version commit
               if (tag != null) {
                  VersionString vs = versionsMap.get(tag);
                  tags.add(vs);
               }
               return true;
            }
         }, true);

         // Remove the head as it is not needed any more
         // By removing the head we ensure that the previous tag of this
         // head will be the previous version of current version, because
         // if all versions in this map are previous of the current
         // and previous of the previous version then the previous version
         // is the first version that is encountered when walking the current
         versions.remove(headString);
         // Retain only those tags that are collected from
         // current head, all others are not resolved from this head (no
         // connection) so we do not want to walk those tags
         versions.keySet().retainAll(tags);
      }
      // Retain only the heads that we walked
      // remove all the others
      versionsMap.keySet().retainAll(list);
      return versionsMap;
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

   /**
    * {@inheritDoc}
    */
   @Override
   public VCSCommit getPrevious(VCSCommit commit) {
      ArgsCheck.notNull("commit", commit);
      return myVersions.getPreviousValue(commit);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public String getPrevious(String name) {
      ArgsCheck.notNull("name", name);
      VersionString vs = getVersionString(name);
      if (vs == null) {
         return null;
      }
      vs = myVersions.getPrevious(vs);
      if (vs == null) {
         return null;
      }
      return vs.getLabel();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public VCSCommit getNext(VCSCommit commit) {
      ArgsCheck.notNull("commit", commit);
      return myVersions.getNextValue(commit);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public String getNext(String name) {
      ArgsCheck.notNull("name", name);
      VersionString vs = getVersionString(name);
      if (vs == null) {
         return null;
      }
      vs = myVersions.getNext(vs);
      if (vs == null) {
         return null;
      }
      return vs.getLabel();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Iterator<VCSCommit> iterator() {
      return myVersions.valuesIterator();
   }

   /**
    * {@inheritDoc}
    */
   public Iterator<VCSCommit> descendingIterator() {
      return myVersions.reverseValuesIterator();
   }

   /**
    * {@inheritDoc}
    */
   public Iterator<String> nameIterator() {
      return new NameIterator(myVersions.iterator());
   }
   
   /**
    * {@inheritDoc}
    */
   public Iterator<String> descendingNameIterator() {
      return new NameIterator(myVersions.reverseIterator());
   }

   /**
    * An iterator to iterate over a version string iterator which return the
    * label of each version string.
    * 
    * @author Elvis Ligu
    */
   private static class NameIterator implements Iterator<String> {

      final Iterator<VersionString> versions;

      /**
       * 
       */
      public NameIterator(Iterator<VersionString> versions) {
         this.versions = versions;
      }

      /**
       * {@inheritDoc}
       */
      @Override
      public boolean hasNext() {
         return versions.hasNext();
      }

      /**
       * {@inheritDoc}
       */
      @Override
      public String next() {
         return versions.next().getLabel();
      }

      /**
       * {@inheritDoc}
       */
      @Override
      public void remove() {
         versions.remove();
      }
   }
}
