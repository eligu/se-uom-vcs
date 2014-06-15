/**
 * 
 */
package gr.uom.se.vcs.analysis.version;

import gr.uom.se.util.validation.ArgsCheck;
import gr.uom.se.vcs.VCSCommit;
import gr.uom.se.vcs.VCSRepository;
import gr.uom.se.vcs.VCSResource;
import gr.uom.se.vcs.VCSTag;
import gr.uom.se.vcs.exceptions.VCSRepositoryException;
import gr.uom.se.vcs.walker.CommitVisitor;
import gr.uom.se.vcs.walker.filter.commit.VCSCommitFilter;
import gr.uom.se.vcs.walker.filter.resource.VCSResourceFilter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeSet;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.Maps;

/**
 * A tag version provider consider each tag as a version of a project.
 * <p>
 * This is the simplest implementation of a version provider because it find all
 * versions with a little effort. However keep in mind that some projects may
 * have versionProvider for hot fixes or for other works. If this is the case,
 * this class will fail to provide accurate information for versions.
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public class TagVersionProvider implements VersionProvider,
      CommitCheckVersion {

   /**
    * Where all versions will be stored.
    */
   protected final BiMap<VCSCommit, String> versions;

   /**
    * This will be used to quickly find the commit version of a given commit.
    * Commits are sorted based on commit date from older to new one.
    */
   protected final TreeSet<VCSCommit> versionCommits;

   /**
    * This comparator is used to build ordered versions, by their commit time.
    * <p>
    * Note that in some situations two commits that differ from each other may
    * have the same commit time. In this situation this comparator will check if
    * both commits are different but they have the same time it will return -1
    * (the first one is ordered first by default), and will not return 0 in any.
    * circumstances.
    * <p>
    * The decision to return -1 for the first object is made based on the
    * implementation of TreeSet (and TreeMap, a tree set is backed of) of java
    * as when using methods such as tail(key) the first object of the comparator
    * will be the key we provide and not the already stored object, so we can
    * get the commit that is at least the same newer than our commit.
    * Considerations should be taken when using the head(key) method for example
    * because our key will be older than a stored key if they have the same time
    * so it will return the next at least older than our key!!!
    */
   public static final Comparator<VCSCommit> ascendingCommitTime = new Comparator<VCSCommit>() {

      @Override
      public int compare(VCSCommit o1, VCSCommit o2) {
         boolean equals = o1.equals(o2);
         int comp = 0;
         // If two commits are equals return 0,
         // if they have the same time (comp == 0) return -1
         // otherwise return comp.
         // This is crucial as two commits may have same time!!!
         // however they are different so we can not return 0
         if (equals) {
            return 0;
            // They are not equals but have the same time!!!
            // By default the first is before second
         } else if ((comp = o1.getCommitDate().compareTo(o2.getCommitDate())) == 0) {
            return -1;
         }
         return comp;
      }
   };

   /**
    * The lock used for versions info
    */
   final private ReadWriteLock versionsInfoLock = new ReentrantReadWriteLock();
   /**
    * The version commits and the commit that logically belongs to each version.
    * <p>
    * Note this require all commits of the repository to be stored here!!!
    */
   private Map<VCSCommit, Set<VCSCommit>> versionsInfo;

   /**
    * Creates a tag provider for the following repository.
    * <p>
    * The provided {@code versionCheckStrategy} argument will be used to check
    * whether a given commit belongs to a version.
    * 
    * @param repo
    *           the repository from where will load the versions
    * @param versionCheckStrategy
    *           the strategy to use when finding a version for a given commit.
    *           If null {@link WalkingCommitVersionChecker} will be used as the
    *           default strategy.
    * 
    * @throws VCSRepositoryException
    *            if the repository could not be read
    */
   public TagVersionProvider(VCSRepository repo,
         CommitCheckVersion versionCheckStrategy) throws VCSRepositoryException {
      // Repo must not be null
      ArgsCheck.notNull("repo", repo);
      versions = createVersions(repo);
      if (versions == null) {
         throw new IllegalStateException("version mapping is not created");
      }
      versionCommits = new TreeSet<VCSCommit>(ascendingCommitTime);
      versionCommits.addAll(versions.keySet());
      this.initVersionsInfo();
   }

   /**
    * Initialize the versions info
    */
   private void initVersionsInfo() {
      this.versionsInfo = new HashMap<VCSCommit, Set<VCSCommit>>(
            versionCommits.size());
      for (VCSCommit version : versions.keySet()) {
         versionsInfo.put(version, new HashSet<VCSCommit>());
      }
   }

   /**
    * Creates a tag provider for the following repository.
    * <p>
    * The default strategy for deciding weather a commit belongs to a version
    * will be {@link WalkingCommitVersionChecker}.
    * 
    * @param repo
    *           the repository from where will load the versions
    * 
    * @throws VCSRepositoryException
    *            if the repository could not be read
    */
   public TagVersionProvider(VCSRepository repo)
         throws VCSRepositoryException {
      this(repo, null);
   }

   /**
    * Will extract versions from the repository and put them to
    * {@link #versions} map.
    * <p>
    * This is actually the strategy of version finder. Subclasses may override
    * this method if they want to provide a different strategy for finding
    * versions of a repository.
    * 
    * @param repo
    *           from which to extract versions
    * @throws VCSRepositoryException
    *            if we can not read the repository
    */
   protected BiMap<VCSCommit, String> createVersions(VCSRepository repo)
         throws VCSRepositoryException {
      SortedMap<VCSCommit, String> tags = Maps.newTreeMap(ascendingCommitTime);

      for (VCSTag tag : repo.getTags()) {
         tags.put(tag.getCommit(), tag.getName());
      }

      return ImmutableBiMap.copyOf(tags);
   }

   /**
    * {@inheritDoc}
    * 
    * @return the versions as a map between names and commits
    */
   @Override
   public Map<String, VCSCommit> getVersions() {
      return versions.inverse();
   }

   @Override
   public Set<String> getVersionNames() {
      return Collections.unmodifiableSet(this.versions.values());
   }

   @Override
   public Set<VCSCommit> getVersionCommits() {
      return Collections.unmodifiableSet(this.versions.keySet());
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public String findVersion(VCSCommit commit) {

      ArgsCheck.notNull("commit", commit);
      if (this.versions.containsKey(commit)) {
         return this.versions.get(commit);
      }

      versionsInfoLock.readLock().lock();
      try {
         for (VCSCommit version : versionsInfo.keySet()) {
            if (versionsInfo.get(version).contains(commit)) {
               return this.versions.get(version);
            }
         }
      } finally {
         versionsInfoLock.readLock().unlock();
      }
      return null;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public VCSCommit getCommit(String ver) {
      Map<String, VCSCommit> vers = versions.inverse();
      ArgsCheck.containsKey("ver", ver, vers, "versions");
      return vers.get(ver);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean isInVersion(String ver, VCSCommit commit) {

      VCSCommit version = this.getCommit(ver);
      return this.isInVersion(version, commit);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean isInVersion(VCSCommit versionCommit, VCSCommit commit) {
      ArgsCheck.containsKey("versionCommit", versionCommit, versions,
            "versions");
      versionsInfoLock.readLock().lock();
      try {
         return versionsInfo.get(versionCommit).contains(commit);
      } finally {
         versionsInfoLock.readLock().unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public String getName(VCSCommit commit) {
      ArgsCheck.containsKey("commit", commit, versions, "versions");
      return this.versions.get(commit);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public VCSCommit getPrevious(VCSCommit commit) {
      ArgsCheck.containsKey("commit", commit, versions, "versions");
      return versionCommits.lower(commit);
   }

   /**
    * Given a version commit return the next version commit (based in time) or
    * null if there is not a next version.
    * <p>
    * 
    * @param commit
    *           the version commit
    * @return the next version commit or null if there is not a next version
    */
   @Override
   public VCSCommit getNext(VCSCommit commit) {
      ArgsCheck.containsKey("commit", commit, versions, "versions");
      return versionCommits.higher(commit);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean isVersion(VCSCommit commit) {
      ArgsCheck.notNull("commit", commit);
      return this.versions.containsKey(commit);
   }

   @Override
   public boolean isVersion(String ver) {
      ArgsCheck.notNull("ver", ver);
      return this.versions.containsValue(ver);
   }

   /**
    * This method should be called before querying this version provider to find
    * out what version a commit belongs to.
    * <p>
    * However you can use this provider to get the version names and their
    * commits, but you should call this method in order to find for a specific
    * commit that is not a version commit, where he belongs.
    * <p>
    * This method is very expensive an will try to load all commits of a
    * repository by performing multiply walks on commits in order to load all
    * informations required to answer {@link #findVersion(VCSCommit)}.
    * 
    * @throws VCSRepositoryException
    *            if the repository could not be read
    */
   public void collectVersionInfo() throws VCSRepositoryException {

      versionsInfoLock.writeLock().lock();
      try {
         initVersionsInfo();
         CommitCollector collector = new CommitCollector();
         // Collect commits for each version
         for (VCSCommit version : versionCommits) {
            collector.version = version;
            version.walkCommits(collector, true);
         }
      } finally {
         versionsInfoLock.writeLock().unlock();
      }
   }

   /**
    * Given a version will return the number of commits this version contains.
    * <p>
    * 
    * @param version
    *           the commit version
    * @return the number of commits for this version
    */
   public int getSize(VCSCommit version) {
      versionsInfoLock.readLock().lock();
      try {
         ArgsCheck.containsKey("version", version, versionsInfo, "versions");
         return versionsInfo.get(version).size() + 1;
      } finally {
         versionsInfoLock.readLock().unlock();
      }
   }

   /**
    * Given a version name will return the number of commits this version
    * contains.
    * <p>
    * 
    * @param ver
    *           the name of the version
    * @return the number of commits this version contains
    */
   public int getSize(String ver) {
      VCSCommit version = this.getCommit(ver);
      return this.getSize(version);
   }

   /**
    * Given a commit version get all commits of this version.
    * <p>
    * 
    * @param version
    *           commit of the version
    * @return the commits of the given version
    */
   public Set<VCSCommit> getCommits(VCSCommit version) {
      versionsInfoLock.readLock().lock();
      try {
         ArgsCheck.containsKey("version", version, versionsInfo, "versions");
         return Collections.unmodifiableSet(versionsInfo.get(version));
      } finally {
         versionsInfoLock.readLock().unlock();
      }
   }

   /**
    * Given a name version return all commits of this version.
    * <p>
    * 
    * @param ver
    *           name of the version
    * @return the commits of this version
    */
   public Set<VCSCommit> getCommits(String ver) {
      VCSCommit version = this.getCommit(ver);
      return this.getCommits(version);
   }

   /**
    * This visitor will be used to collect the commits for each version.
    * <p>
    * The algorithm used is: using sorted versions (the field versionCommits) it
    * assumes that is going to perform a walk for each version. For each version
    * walking, each commit that is visited will be check if he is in a previous
    * version, if not than it will count this commit as belonging to the current
    * version.
    */
   private class CommitCollector implements CommitVisitor {

      VCSCommit version;

      @Override
      public boolean visit(VCSCommit entity) {
         // This is not a version commit
         if (!versions.containsKey(entity)) {
            // If this is the first version we do not need to check
            // any previous version
            if (versionCommits.first().equals(version)) {
               versionsInfo.get(version).add(entity);
               return true;
            }
            // Check the previous versions
            Iterator<VCSCommit> it = versionCommits.iterator();
            boolean contains = false;
            // Here we are sure that we have at least out first
            // version so we must check all previous versions
            // until to this one if any of them contains this commit
            // If so then this commit is not part of our version
            while (it.hasNext() && !contains) {
               VCSCommit previousVersion = it.next();
               // If we arrived at our version than we need to stop
               // checking previous versions
               if (previousVersion.equals(version)) {
                  break; // we need to break because we have our
                         // version here so
                  // no need to check ourself
               }
               // If previous version contains current commit
               // we need to break
               if (versionsInfo.get(previousVersion).contains(entity)) {
                  contains = true;
               }
            }
            // We have checked previous versions and now
            // we are sure no other version contains this commit
            if (!contains) {
               versionsInfo.get(version).add(entity);
            }
         }
         return true;
      }

      @Override
      public <R extends VCSResource> VCSResourceFilter<R> getResourceFilter() {
         return null;
      }

      @SuppressWarnings("unchecked")
      @Override
      public VCSCommitFilter getFilter() {
         return null;
      }
   }

   @Override
   public Iterator<VCSCommit> iterator() {
      return Collections.unmodifiableSet(versionCommits).iterator();
   }

   @Override
   public Iterator<VCSCommit> reverseIterator() {
      Iterator<VCSCommit> it = versionCommits.descendingIterator();
      ArrayList<VCSCommit> commits = new ArrayList<VCSCommit>(versionCommits.size());
      while(it.hasNext()) {
         commits.add(it.next());
      }
      return commits.iterator();
   }
}
