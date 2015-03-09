/**
 * 
 */
package gr.uom.se.vcs.analysis.version.provider;

import gr.uom.se.util.validation.ArgsCheck;
import gr.uom.se.vcs.VCSCommit;
import gr.uom.se.vcs.VCSRepository;
import gr.uom.se.vcs.VCSResource;
import gr.uom.se.vcs.exceptions.VCSRepositoryException;
import gr.uom.se.vcs.walker.CommitVisitor;
import gr.uom.se.vcs.walker.filter.commit.VCSCommitFilter;
import gr.uom.se.vcs.walker.filter.resource.VCSResourceFilter;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A tag version provider consider each tag as a version of a project.
 * <p>
 * This is the simplest implementation of a version provider because it find all
 * versions with a little effort. However keep in mind that some projects may
 * have version for hot fixes or for other works. If this is the case, this
 * class will fail to provide accurate information for versions.
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public class ConnectedTagVersionProvider implements ConnectedVersionProvider {

   /**
    * The version name provider.
    */
   private final ConnectedVersionNameProvider provider;
   /**
    * The lock used for versions info
    */
   final private ReadWriteLock lock = new ReentrantReadWriteLock();

   /**
    * The version commits and the commit that logically belongs to each version.
    * <p>
    * Note this require all commits of the repository to be stored here!!!
    */
   private Map<VCSCommit, Set<VCSCommit>> versionsInfo;

   /**
    * Size of versions.
    */
   private final int vsize;

   /**
    * The first version.
    */
   private final VCSCommit first;

   /**
    * Creates a tag provider for the following repository.
    * <p>
    * 
    * This will load the versions based on default
    * {@linkplain DefaultVersionNameResolver version name resolver}, on default
    * {@linkplain ConnectedTagVersionNameProvider version name provider} and on
    * default {@linkplain BranchTagProvider tag provider}.
    * 
    * @param repo
    *           the repository from where will load the versions
    * 
    * @throws VCSRepositoryException
    *            if the repository could not be read
    */
   public ConnectedTagVersionProvider(VCSRepository repo)
         throws VCSRepositoryException {
      // Repo must not be null
      ArgsCheck.notNull("repo", repo);
      this.provider = createVersions(repo);
      if (provider == null) {
         throw new IllegalStateException("version mapping is not created");
      }
      Collection<VCSCommit> commits = this.provider.getCommits();
      vsize = commits.size();
      if (vsize == 0) {
         first = commits.iterator().next();
      } else {
         first = null;
      }
      this.initVersionsInfo();
   }

   /**
    * Creates a tag provider for the following version name provider.
    * <p>
    * 
    * @param repo
    *           the repository from where will load the versions
    * @param
    * @throws VCSRepositoryException
    *            if the repository could not be read
    */
   public ConnectedTagVersionProvider(ConnectedVersionNameProvider provider) {
      ArgsCheck.notNull("provider", provider);
      this.provider = provider;

      Collection<VCSCommit> commits = this.provider.getCommits();
      vsize = commits.size();
      if (vsize == 0) {
         first = commits.iterator().next();
      } else {
         first = null;
      }
      this.initVersionsInfo();
   }

   /**
    * Initialize the versions info
    */
   private void initVersionsInfo() {
      this.versionsInfo = new HashMap<VCSCommit, Set<VCSCommit>>(vsize);
      for (VCSCommit version : provider) {
         versionsInfo.put(version, new HashSet<VCSCommit>());
      }
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
   protected ConnectedTagVersionNameProvider createVersions(VCSRepository repo)
         throws VCSRepositoryException {
      TagProvider tagProvider = new BranchTagProvider(repo);
      ConnectedTagVersionNameProvider provider = new ConnectedTagVersionNameProvider(
            tagProvider);
      return provider;
   }

   /**
    * {@inheritDoc}
    * 
    * @return the versions as a map between names and commits
    */
   @Override
   public Map<String, VCSCommit> getVersions() {
      return provider.getVersions();
   }

   @Override
   public Set<String> getNames() {
      return provider.getNames();
   }

   @Override
   public Set<VCSCommit> getCommits() {
      return provider.getCommits();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public String findVersion(VCSCommit commit) {

      ArgsCheck.notNull("commit", commit);
      if (provider.isVersion(commit)) {
         return provider.getName(commit);
      }

      lock.readLock().lock();
      try {
         for (VCSCommit version : versionsInfo.keySet()) {
            if (versionsInfo.get(version).contains(commit)) {
               return provider.getName(version);
            }
         }
      } finally {
         lock.readLock().unlock();
      }
      return null;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public VCSCommit getCommit(String ver) {
      return provider.getCommit(ver);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean isInVersion(String ver, VCSCommit commit) {
      VCSCommit version = provider.getCommit(ver);
      if (version == null) {
         return false;
      }
      lock.readLock().lock();
      try {
         return versionsInfo.get(version).contains(commit);
      } finally {
         lock.readLock().unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean isInVersion(VCSCommit versionCommit, VCSCommit commit) {
      if (!provider.isVersion(versionCommit)) {
         return false;
      }
      lock.readLock().lock();
      try {
         return versionsInfo.get(versionCommit).contains(commit);
      } finally {
         lock.readLock().unlock();
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public String getName(VCSCommit commit) {
      return provider.getName(commit);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public VCSCommit getPrevious(VCSCommit commit) {
      return provider.getPrevious(commit);
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
      return provider.getNext(commit);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean isVersion(VCSCommit commit) {
      return provider.isVersion(commit);
   }

   @Override
   public boolean isVersion(String ver) {
      return provider.isVersion(ver);
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

      lock.writeLock().lock();
      try {
         initVersionsInfo();
         CommitCollector collector = new CommitCollector();
         // Collect commits for each version
         for (VCSCommit version : provider) {
            collector.version = version;
            version.walkCommits(collector, true);
         }
      } finally {
         lock.writeLock().unlock();
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
      if (provider.isVersion(version)) {
         throw new IllegalArgumentException("commit is not a version");
      }
      lock.readLock().lock();
      try {
         return versionsInfo.get(version).size() + 1;
      } finally {
         lock.readLock().unlock();
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
      VCSCommit version = provider.getCommit(ver);
      ArgsCheck.notNull("version", version);
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
      if (provider.isVersion(version)) {
         throw new IllegalArgumentException("commit is not a version");
      }
      lock.readLock().lock();
      try {
         return Collections.unmodifiableSet(versionsInfo.get(version));
      } finally {
         lock.readLock().unlock();
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
      VCSCommit version = provider.getCommit(ver);
      ArgsCheck.notNull("version", version);
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
         if (!provider.isVersion(entity)) {
            // If this is the first version we do not need to check
            // any previous version
            if (first.equals(version)) {
               versionsInfo.get(version).add(entity);
               return true;
            }
            // Check the previous versions
            Iterator<VCSCommit> it = provider.iterator();
            boolean contains = false;
            // Here we are sure that we have at least our first
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
      return provider.iterator();
   }

   @Override
   public Iterator<VCSCommit> descendingIterator() {
      return provider.descendingIterator();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public String getPrevious(String name) {
      return provider.getPrevious(name);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public String getNext(String name) {
      return provider.getNext(name);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Iterator<String> nameIterator() {
      return provider.nameIterator();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Iterator<String> descendingNameIterator() {
      return provider.descendingNameIterator();
   }
}
