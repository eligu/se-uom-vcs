/**
 * 
 */
package se.uom.vcs.walker.filter.commit;

import java.util.Collection;
import java.util.regex.Pattern;

import se.uom.vcs.VCSCommit;

/**
 * Check the message of the current commit if it matches any of the patterns
 * contained in this filter.
 * <p>
 * 
 * This implementation is based on Java patterns so any string passed at the
 * constructor should take care of the special characters used by Java.
 * <p>
 * HINTS: you can sub class this class to construct a filter that parses the
 * message of a commit and finds some known patterns. I.e. a known pattern at
 * commit messages is <code>FIX #123</code> which tells this commit fix the bug
 * with id 123.
 * 
 * @author Elvis Ligu
 * @since 0.0.1
 * @version 0.0.1
 */
public class MessageFilter<T extends VCSCommit> extends PatternsFilter<T>
      implements VCSCommitFilter<T> {

   /**
    * Create a new instance based on given patterns.
    * <p>
    * 
    * @param patterns
    *           that the message of commit will be checked against
    */
   public MessageFilter(Collection<String> patterns) {
      super(patterns);
   }

   /**
    * {@inheritDoc} Returns true if the current entity match any of the message
    * patterns contained in this filter.
    * <p>
    * We do not use cache here, because each commit message is different
    * from the others and that would be very inefficient. 
    */
   @Override
   public boolean include(T entity) {

      String msg = entity.getMessage();
      for (Pattern p : patterns) {
         if (p.matcher(msg).matches()) {
            return true;
         }
      }
      return false;
   }
   
   
}
