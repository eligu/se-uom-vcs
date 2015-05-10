/**
 * 
 */
package gr.uom.se.vcs.analysis.version.provider;

import java.util.Collections;
import java.util.Set;

import gr.uom.se.util.validation.ArgsCheck;
import gr.uom.se.vcs.VCSCommit;
import gr.uom.se.vcs.exceptions.VCSRepositoryException;

/**
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public interface ConnectedVersionProvider extends Iterable<VCSCommit>,
      ConnectedVersionNameProvider {

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
    * commit and it is not in a previous version that means it belongs to this
    * version.
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
    * Find out if the given commit is part of the given version.
    * <p>
    * A commit is part of a version if he has a history of the given version and
    * is not part of another version (probably the previous one). That is,
    * having two or more versions a commit is considered to be part of version 2
    * if walking back from version 2 we reach this commit but we do not reach
    * another version before this reaching the required commit.
    * 
    * @param ver
    *           the version to check if the given commit is part of
    * @param commit
    *           the commit to check if he is part of the given version
    * @return true if the given commit is part of the given version
    */
   public boolean isInVersion(String ver, VCSCommit commit);

   /**
    * Find out if the given commit is part of the given version.
    * <p>
    * A commit is part of a version if he has a history of the given version and
    * is not part of another version (probably the previous one). That is,
    * having two or more versions a commit is considered to be part of version 2
    * if walking back from version 2 we reach this commit but no other previous
    * version reaches this commit.
    * 
    * @param versionCommit
    *           the version to check if the given commit is part of
    * @param commit
    *           the commit to check if he is part of the given version
    * @return true if the given commit is part of the given version
    */
   public boolean isInVersion(VCSCommit versionCommit, VCSCommit commit);

   /**
    * Initialize this versions provider.
    * <p>
    * This method should be executed before using this provider.
    * 
    * @throws VCSRepositoryException
    */
   void init() throws VCSRepositoryException;

   /**
    * Given a name version return all commits of this version.
    * <p>
    * 
    * @param ver
    *           name of the version
    * @return the commits of this version
    */
   Set<VCSCommit> getCommits(String ver);

   /**
    * Given a commit version get all commits of this version.
    * <p>
    * 
    * @param version
    *           commit of the version
    * @return the commits of the given version
    */
   Set<VCSCommit> getCommits(VCSCommit version);
}
