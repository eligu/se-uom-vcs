/**
 * 
 */
package gr.uom.se.vcs.analysis.version;

import gr.uom.se.vcs.VCSCommit;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public interface VersionProvider extends Iterable<VCSCommit> {

   /**
    * Get the versions this provider has discovered from repository.
    * <p>
    * Generally speaking a version is a repository snapshot at a given point in
    * time, which corresponds to a single commit (the tree of a commit). In most
    * cases a version is a tagged commit, however keep in mind that a commit
    * must have one or more versionProvider. That means we are only giving a
    * unique tag per version. Which tag will be that depends on implementations.
    * 
    * @return the versions and their corresponding commits
    */
   public Map<String, VCSCommit> getVersions();

   /**
    * Find the version where the specified commit belongs to.
    * <p>
    * Finding the version where a commit belongs to is in some cases very
    * resource angry. The reason for that is that in some repositories a commit
    * may have a older time than the previous version. For example in Git
    * repositories if we have commit c1 from a repository which was cloned from
    * the central repository before version v0 happened, and this commit was
    * merged into repository after the version v0 but before v1 than this commit
    * is part of v1 as it was not merged into v0, however its commit time is
    * older than v0.
    * <p>
    * For finding the version for a commit, probably the best strategy is to
    * find the most close version in time to this commit, and check if the
    * commit is reachable by this version, but not reachable by any other
    * previous version. Keep in mind that this will require to keep in memory
    * all commits that belong to previous versions, so if we find the required
    * commit and it is not in a previous version that means it belongs to this version.
    * <p>
    * Note that if the given commit is equal to a commit of a version then it
    * will return that version, in this case we consider that the version commit
    * is it self part of the version.
    * 
    * @param commit
    *           to find the version for
    * @return the version this commit belongs to
    */
   public String findVersion(VCSCommit commit);

   /**
    * <b>WARNING:</b> an exception will be thrown if the specified version is
    * not a known version. Use {@link #isVersion(String)} before using this.
    * 
    * @param ver
    *           the version name to find the commit for.
    * @return return the commit associated with the given version. There is only
    *         one commit associated with a version. Will return null if no
    *         commit is associated with this version.
    */
   public VCSCommit getCommit(String ver);

   /**
    * Given a commit of a version return its name or null if the given commit is
    * not a version commit.
    * <p>
    * <b>WARNING:</b> an exception will be thrown if this commit is not a known
    * version. Use {@link #isVersion(VCSCommit)} before using this.
    * 
    * @param commit
    *           the version commit
    * @return the version name
    * @throws IllegalArgumentException
    *            if the given commit is not a version
    */
   public String getName(VCSCommit commit);

   /**
    * Given a version commit, return the previous version commit (based on
    * commit time).
    * <p>
    * <b>WARNING:</b> an exception will be thrown if this commit is not a known
    * version. Use {@link #isVersion(VCSCommit)} before using this.
    * 
    * @param commit
    *           the version commit
    * @return the previous version commit
    * @throws IllegalArgumentException
    *            if the given commit is not a version
    */
   public VCSCommit getPrevious(VCSCommit commit);

   /**
    * Given a version commit return the next version commit (based in time) or
    * null if there is not a next version.
    * <p>
    * <b>WARNING:</b> an exception will be thrown if this commit is not a known
    * version. Use {@link #isVersion(VCSCommit)} before using this.
    * 
    * @param commit
    *           the version commit
    * @return the next version commit or null if there is not a next version
    * @throws IllegalArgumentException
    *            if the given commit is not a version
    */
   public VCSCommit getNext(VCSCommit commit);

   /**
    * @param commit
    *           to check if it is a version
    * @return true if the given commit is a version
    */
   public boolean isVersion(VCSCommit commit);

   /**
    * @return all available version commits
    */
   public Set<VCSCommit> getVersionCommits();

   /**
    * @return the names of the available versions
    */
   public Set<String> getVersionNames();

   /**
    * Return an iterator for all version commits this provider has discovered.
    * <p>
    * The version commits provided by this iterator will be in ascending order,
    * from older to newer version. Do not count on the date of version commits
    * as it relies on implementation to decide which version is older than the
    * other.
    * <p>
    * You do not have to check each returned version commit if it is a version
    * because this iterator guarantees that all returned commits are versions.
    * <p>
    * If you need a descending iterator from newer to older version then use
    * {@link #reverseIterator()} to obtain one. Keep in mind that using the
    * iterator you may have an unmodifiable copy of the versions and you may
    * probable be not able to modify the versions. However this relies on
    * implementation.
    * <p>
    * If you need to work with version names than you can use
    * {@link #getName(VCSCommit)} to get the name of each returned version by
    * this iterator.
    * 
    * @see #reverseIterator()
    */
   @Override
   public Iterator<VCSCommit> iterator();

   /**
    * Return a descending iterator of version commits from newer to older.
    * <p>
    * The use of this iterator and the invariants are described in
    * {@link #iterator()}.
    * 
    * @return an ascending iterator
    */
   public Iterator<VCSCommit> reverseIterator();

   /**
    * @param ver the name of the version to check
    * @return true if the given name is a version
    */
   public boolean isVersion(String ver);
}
