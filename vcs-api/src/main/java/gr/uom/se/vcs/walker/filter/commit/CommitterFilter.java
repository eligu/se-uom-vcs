/**
 * 
 */
package gr.uom.se.vcs.walker.filter.commit;

import gr.uom.se.vcs.VCSCommit;

import java.util.Collection;
import java.util.regex.Pattern;


/**
 * Test the current commit if it is from a specified list of committers.
 * <p>
 * 
 * This implementation is based on Java patterns so any string passed at the
 * constructor should take care of the special characters used by Java.
 * <p>
 * Some distributed VCSs make distinct between an author and a committer. An
 * author is the one who makes the changes at his own local copy of repository,
 * and the committer is the person who commits these changes to the remote
 * repository (usually is the central repository). In such cases the author of a
 * commit is not the same as the committer. However, in cases we have no such
 * distinct (centralized VCS, such as SVN) the author is the same as the
 * committer.
 * 
 * @author Elvis Ligu
 * @since 0.0.1
 * @version 0.0.1
 */
public class CommitterFilter extends PatternsFilter<VCSCommit>
      implements VCSCommitFilter {

   /**
    * Creates a new instance based on the given committer patterns.
    * <p>
    * 
    * @param committersPatterns
    *           Java patterns that will be checked against the committer of a
    *           given commit.
    */
   public CommitterFilter(Collection<String> committersPatterns) {
      super(committersPatterns);
   }

   /**
    * {@inheritDoc} Returns true if the current entity match any of the
    * committer patterns contained in this filter.
    */
   @Override
   public boolean include(VCSCommit entity) {

      String committer = entity.getCommiter();
      if(super.checkCache(committer)) {
         return true;
      }
      
      super.addInCache(committer, false);

      for (Pattern p : patterns) {
         if (p.matcher(committer).matches()) {
            super.cache.put(committer, true);
            return true;
         }
      }
      return false;
   }
}
