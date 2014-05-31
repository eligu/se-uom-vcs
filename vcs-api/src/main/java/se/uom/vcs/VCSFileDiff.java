/**
 * 
 */
package se.uom.vcs;

import java.util.Set;

/**
 * A change type that is produced as a result of a diff between two commits and
 * refers to only files.
 * <p>
 * 
 * A change is linked to a repository file. A change is the result produced by
 * calculating the differences between a base commit (the old one) and a new
 * commit. See {@link VCSCommit#getFileChanges(VCSCommit)}
 * 
 * @author Elvis Ligu
 * @since 0.0.1
 * @version 0.0.1
 * @see VCSChange
 */
public interface VCSFileDiff<T extends VCSFile> extends VCSChange<T> {

   /**
    * Return a set of changes from old file to new one.
    * <p>
    * 
    * Usually the changes are calculated for text files. IF this file is not a
    * text file then there is nonsense to produce a set of Edits.
    * <p>
    * 
    * If this change is {@link VCSChange.Type#DELETED} or
    * {@link VCSChange.Type#ADDED} will be returned an empty set.
    * 
    * @return the set of edits that were performed on this file from old to new
    *         version
    * @see Edit
    */
   Set<Edit> getEdits();
}
