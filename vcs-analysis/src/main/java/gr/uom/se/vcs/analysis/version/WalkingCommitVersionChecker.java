/**
 * 
 */
package gr.uom.se.vcs.analysis.version;

import gr.uom.se.util.validation.ArgsCheck;
import gr.uom.se.vcs.VCSCommit;
import gr.uom.se.vcs.VCSResource;
import gr.uom.se.vcs.exceptions.VCSRepositoryException;
import gr.uom.se.vcs.walker.CommitVisitor;
import gr.uom.se.vcs.walker.filter.commit.VCSCommitFilter;
import gr.uom.se.vcs.walker.filter.resource.VCSResourceFilter;

import java.util.Map;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;

/**
 * An implementation of commit check version.
 * <p>
 * This class will check for a given commit if the version provided is stored
 * within versions it contains and the commit has a part of the history of a
 * version commit.
 * <p>
 * The strategy implemented is to walk back a given version while we find the
 * commit but prior to find any other version. While walking a version if
 * another stored version is found and the commit is not yet found this will
 * return false, as it is means the commit may be part of a previous version or
 * a next version.
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public class WalkingCommitVersionChecker implements CommitCheckVersion {

   /**
    * Where all versions will be stored.
    */
   protected BiMap<VCSCommit, String> versions;

   /**
    * Create a new instance based on the given versions.
    * <p>
    * 
    * @param versions
    *           the commit versions and their respective names
    */
   public WalkingCommitVersionChecker(Map<VCSCommit, String> versions) {
      ArgsCheck.notNull("versions", versions);
      ArgsCheck.containsNoNull("versions", versions.values());

      this.versions = ImmutableBiMap.copyOf(versions);
   }

   public synchronized void setVersions(Map<VCSCommit, String> versions) {
      ArgsCheck.notNull("versions", versions);
      ArgsCheck.containsNoNull("versions", versions.values());
      this.versions = ImmutableBiMap.copyOf(versions);
   }

   /**
    * Check if the given commit is reachable by the version commit without
    * reaching another version first.
    * <p>
    * This method will return true if the given commit is the same as the given
    * version commit.
    * 
    * @param version
    *           the commit of a version
    * @param commit
    *           the commit to check
    * @return true if the commit is in version
    * @throws VCSRepositoryException
    *            while walking repository a problem occurs
    */
   protected boolean checkForVersion(VCSCommit version, VCSCommit commit)
         throws VCSRepositoryException {
      if (version.equals(commit)) {
         return true;
      }
      Visitor v = new Visitor(commit, version);
      version.walkCommits(v, true);
      return v.inVersion;
   }

   /**
    * A commit C is considered to be in version V if walking the version commit
    * VC will find C before finding any other version commit.
    * <p>
    * Warning: this class should be used in case the version commit is not equal
    * to the commit itself otherwise
    * 
    * @author Elvis Ligu
    * @version 0.0.1
    * @since 0.0.1
    */
   private class Visitor implements CommitVisitor {

      boolean inVersion = false;
      VCSCommit commit;
      VCSCommit vc;

      public Visitor(VCSCommit c, VCSCommit vc) {
         commit = c;
         this.vc = vc;
      }

      @Override
      public boolean visit(VCSCommit entity) {

         /*
          * just for precautions in case a walker do not stop if we return false
          */
         if (inVersion) {
            throw new IllegalStateException(
                  "walker is not stopping, however we returned false!!!");
         }
         // If the current commit is not the same as the version commit
         // but is contained in the set of version commits than it means
         // we have found another version that before finding our commit
         // so the commit is not in required version vc.
         if (!entity.equals(vc) && versions.containsKey(entity)) {
            inVersion = false;
            // to stop the walking
            return false;
         } else if (entity.equals(commit)) {
            inVersion = true;
            // to stop walking
            return false;
         }
         // Keep walking
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

   /**
    * {@inheritDoc}
    * <p>
    * This will return an exception if the version is not found in versions this
    * object contains. An exception will be thrown to if the walking of a commit
    * throws an exception.
    * <p>
    */
   @Override
   public boolean isInVersion(String ver, VCSCommit commit) {
      ArgsCheck.notNull("versionCommit", ver);
      VCSCommit vc = this.versions.inverse().get(ver);
      if( vc == null ) {
         throw new IllegalArgumentException(
               "the provided ver has not a commit version");
      }
      return this.isInVersion(vc, commit);
   }

   @Override
   public boolean isInVersion(VCSCommit versionCommit, VCSCommit commit) {
      ArgsCheck.notNull("versionCommit", versionCommit);
      ArgsCheck.notNull("commit", commit);
      
      if(!versions.containsKey(versionCommit)) {
         throw new IllegalArgumentException(
               "the provided ver has not a commit version");
      }
      
      try {
         return checkForVersion(versionCommit, commit);
      } catch (VCSRepositoryException e) {
         throw new IllegalStateException(e);
      }
   }
}
