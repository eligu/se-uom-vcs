/**
 * 
 */
package gr.uom.se.vcs.analysis.version;

import gr.uom.se.vcs.VCSCommit;

/**
 * This is the strategy interface for checking where a given commit belongs to a
 * given version.
 * <p>
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public interface CommitCheckVersion {

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
}
