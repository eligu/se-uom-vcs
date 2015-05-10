/**
 * 
 */
package gr.uom.se.vcs.analysis.version.provider;

import gr.uom.se.util.collection.BiMap;
import gr.uom.se.util.collection.Maps;
import gr.uom.se.util.validation.ArgsCheck;
import gr.uom.se.vcs.VCSCommit;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A base class for all version names providers that maintains a bi directional
 * mapping between version names and version commits.
 * <p>
 * 
 * @author Elvis Ligu
 */
public abstract class AbstractVersionNameProvider implements
      VersionNameProvider {

   /**
    * The mapping of versions to commits and vice versa.
    */
   protected final BiMap<VersionString, VCSCommit> versions;

   /**
    * The mapping of commits to versions, used for quick access, and also as
    * source for commit comparator.
    */
   protected final Map<VCSCommit, VersionString> mappedCommits;

   /**
    * A comparator to compare commits, used by bi map to order the commits.
    */
   private final Comparator<VCSCommit> commitComparator = new Comparator<VCSCommit>() {

      @Override
      public int compare(VCSCommit o1, VCSCommit o2) {
         VersionString v1 = mappedCommits.get(o1);
         VersionString v2 = mappedCommits.get(o2);
         // Use the default comparing mechanism if no
         // version comparator was provided
         if(v1 == v2) {
            return 0;
         } else if(v1 == null) {
            return -1;
         } else if (v2 == null) {
            return 1;
         }
         if (versionComparator == null) {
            return v1.compareTo(v2);
         }
         
         return versionComparator.compare(v1, v2);
      }

   };

   /**
    * Used by bi map to order the version.
    */
   protected final Comparator<? super VersionString> versionComparator;

   /**
    * Create an instance based without initial versions, and a default version
    * comparator.
    * <p>
    */
   protected AbstractVersionNameProvider() {
      this(null);
   }

   /**
    * Create an instance based on the given versions, and comparator.
    * <p>
    * The versions will be ordered, in both directions.
    * 
    * @param versions
    *           the versions to be added as initial versions. It may be null.
    * @param versionComparator
    *           a comparator to order versions. It may be null.
    */
   protected AbstractVersionNameProvider(
         Map<VersionString, VCSCommit> versions,
         Comparator<? super VersionString> versionComparator) {

      if (versionComparator == null) {
         if (versions instanceof SortedMap) {
            SortedMap<VersionString, VCSCommit> vers = (SortedMap<VersionString, VCSCommit>) versions;
            versionComparator = vers.comparator();
         }
      }
      this.versionComparator = versionComparator;
      this.versions = Maps.newOrderedBiMap(versionComparator, commitComparator);
      if (versions != null) {
         for (VersionString vs : versions.keySet()) {
            this.versions.put(vs, versions.get(vs));
         }
      }
      versions = null;
      Map<VCSCommit, VersionString> map = new ConcurrentHashMap<>();
      this.mappedCommits = Maps.getReverted(this.versions, map);
   }

   /**
    * Create an instance based on the given versions, and comparator.
    * <p>
    * The versions will be placed in hash map like and will have no ordering.
    * 
    * @param versions
    *           the versions to be added as initial versions. It may be null.
    */
   protected AbstractVersionNameProvider(Map<VersionString, VCSCommit> versions) {

      this.versions = Maps.newBiMap();
      if (versions != null) {
         for (VersionString vs : versions.keySet()) {
            this.versions.put(vs, versions.get(vs));
         }
      }
      versions = null;
      Map<VCSCommit, VersionString> map = new ConcurrentHashMap<>();
      this.mappedCommits = Maps.getReverted(this.versions, map);
      this.versionComparator = null;
   }

   /**
    * Used by methods of this implementation to resolve a version string, given
    * its name.
    * <p>
    * For each name that is a version, this method will be called to many times,
    * so a good strategy for subclasses would be to keep cached the version
    * strings.
    * 
    * @param label
    * @return
    */
   protected abstract VersionString getVersionString(String label);

   /**
    * {@inheritDoc}
    */
   @Override
   public VCSCommit getCommit(String version) {
      ArgsCheck.notNull("version", version);
      VersionString vs = getVersionString(version);
      ArgsCheck.notNull("version string", vs);
      return versions.get(vs);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public String getName(VCSCommit commit) {
      ArgsCheck.notNull("commit", commit);
      VersionString vs = versions.getKey(commit);
      if (vs == null) {
         return null;
      }
      return vs.getLabel();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Set<String> getNames() {
      Set<VersionString> values = versions.keysCopy();
      Set<String> names = new LinkedHashSet<>();
      for (VersionString vs : values) {
         names.add(vs.getLabel());
      }
      return names;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Set<VCSCommit> getCommits() {
      return versions.valuesCopy();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean isVersion(String ver) {
      VersionString vs = getVersionString(ver);
      if (vs == null) {
         return false;
      }
      return versions.containsKey(vs);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean isVersion(VCSCommit commit) {
      return versions.containsValue(commit);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Map<String, VCSCommit> getVersions() {
      Map<VersionString, VCSCommit> map = versions.keyMappingCopy();
      Map<String, VCSCommit> result = new LinkedHashMap<>();
      for (VersionString vs : map.keySet()) {
         String v = vs.getLabel();
         if (v != null) {
            result.put(v, map.get(vs));
         }
      }
      return result;
   }
}
