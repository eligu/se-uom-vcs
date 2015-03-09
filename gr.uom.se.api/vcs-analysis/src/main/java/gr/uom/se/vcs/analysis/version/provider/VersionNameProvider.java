/**
 * 
 */
package gr.uom.se.vcs.analysis.version.provider;

import gr.uom.se.vcs.VCSCommit;

import java.util.Map;
import java.util.Set;

/**
 * A version name provider interface.
 * <p>
 * Instances of this type should be able to resolve a version commit by getting
 * providing a version name, and vice versa. Also they should be able to resolve
 * all version names and or version commits, when required.
 * 
 * @author Elvis Ligu
 */
public interface VersionNameProvider {

   /**
    * Given a version name, return the corresponding version commit.
    * <p>
    * 
    * @param version
    *           the name of the version
    * @return a version commit for the given version name, or null if it was not
    *         found.
    */
   VCSCommit getCommit(String version);

   /**
    * Given a version commit, return the corresponding version name.
    * <p>
    * 
    * @param commit
    *           the version commit.
    * @return a version name for the given version commit, or null if it was not
    *         found.
    */
   String getName(VCSCommit commit);

   /**
    * Get the version names.
    * <p>
    * 
    * @return version names.
    */
   Set<String> getNames();

   /**
    * Get the version commits.
    * <p>
    * 
    * @return version commits.
    */
   Set<VCSCommit> getCommits();

   /**
    * Get the versions this provider has discovered from repository.
    * <p>
    * Generally speaking a version is a repository snapshot at a given point in
    * time, which corresponds to a single commit (the tree of a commit). In most
    * cases a version is a tagged commit, however keep in mind that a commit
    * must have one or more versions. That means we are only giving a unique tag
    * per version. Which tag will be that depends on implementations.
    * 
    * @return the versions and their corresponding commits
    */
   public Map<String, VCSCommit> getVersions();

   /**
    * Return true if the given string is a version string.
    * <p>
    * 
    * @param ver
    *           the name of the version to check
    * @return true if the given name is a version
    */
   public boolean isVersion(String ver);

   /**
    * Return true if the given commit is a version commit.
    * <p>
    * 
    * @param commit
    *           to check if it is a version
    * @return true if the given commit is a version
    */
   public boolean isVersion(VCSCommit commit);

}
