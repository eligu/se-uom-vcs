/**
 * 
 */
package gr.uom.se.vcs.analysis.version.provider;

import java.util.Iterator;

import gr.uom.se.vcs.VCSCommit;

/**
 * A connected version name provider which ensures that versions should be
 * ordered, and connected.
 * <p>
 * Having ordered versions, connected version means that a walking a version
 * commit back (from newest to oldest) it will reach all other previous
 * versions, also the first version to reach will be its previous.
 * 
 * @author Elvis Ligu
 */
public interface ConnectedVersionNameProvider extends VersionNameProvider,
      Iterable<VCSCommit> {

   /**
    * Given a version commit, return the previous version commit.
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
    * Given a version name, return the previous version name.
    * <p>
    * <b>WARNING:</b> an exception will be thrown if this name is not a known
    * version. Use {@link #isVersion(String)} before using this.
    * 
    * @param name
    *           the version name
    * @return the previous version name
    * @throws IllegalArgumentException
    *            if the given name is not a version
    */
   public String getPrevious(String name);

   /**
    * Given a version commit return the next version commit or null if there is
    * not a next version.
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
    * Given a version name, return the next version name.
    * <p>
    * <b>WARNING:</b> an exception will be thrown if this name is not a known
    * version. Use {@link #isVersion(String)} before using this.
    * 
    * @param name
    *           the version name
    * @return the next version name
    * @throws IllegalArgumentException
    *            if the given name is not a version
    */
   public String getNext(String name);

   /**
    * Get a commit iterator in ascending order.
    * <p>
    * Ascending means that it will return each version commit from older to
    * newer. However commit dates aren't guaranteed to be in order.
    * 
    * @return a version commit iterator from newest to oldest.
    */
   @Override
   public Iterator<VCSCommit> iterator();

   /**
    * Get a commit iterator in descending order.
    * <p>
    * Descending means that it will return each version commit from newer to
    * older. However commit dates aren't guaranteed to be in order.
    * 
    * @return a version commit iterator from newest to oldest.
    */
   public Iterator<VCSCommit> descendingIterator();

   /**
    * Get a name iterator in ascending order.
    * <p>
    * Ascending means that it will return each version name from older to newer.
    * 
    * @return a version name iterator from newest to oldest.
    */
   public Iterator<String> nameIterator();

   /**
    * Get a name iterator in descending order.
    * <p>
    * Descending means that it will return each version name from newer to
    * older.
    * 
    * @return a version name iterator from newest to oldest.
    */
   public Iterator<String> descendingNameIterator();
}
