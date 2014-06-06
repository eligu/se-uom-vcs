/**
 * 
 */
package gr.uom.se.vcs;

import gr.uom.se.vcs.exceptions.VCSRepositoryException;

/**
 * Represents a tag of a {@link VCSRepository}.
 * <p>
 * 
 * A tag is a special label attached to a commit in order to get quick reference
 * to e given commit. Usually a version release of a software is the source code
 * at a given commit tagged with a label (i.e. v1.2).
 * <p>
 * <b>NOTE:</b> not all VCSs support tags natively, that means the applications
 * should not rely in implementations of this interface in order to guarante
 * that they will work with every VCS. Read implementation documents in order to
 * check if tagging is supported.
 * 
 * @author Elvis Ligu
 * @since 0.0.1
 * @version 0.0.1
 */
public interface VCSTag {

   /**
    * Return the ID of this tag (a.k.a. tag name).
    * <p>
    * The ID of this tag is used for internal purposes and usually it is
    * expected to work with {@link VCSRepository#resolveTag(String)}. It is
    * guaranteed that calling the above method with the value this method
    * returns will return this tag.
    * 
    * @return the ID of this tag
    */
   String getID();

   /**
    * Get the commit this tag is attached to.
    * <p>
    * Each tag object is connected to a commit and is usually a marker for a
    * commit.
    * 
    * @return the commit this tag is marks
    * @throws VCSRepositoryException
    */
   VCSCommit getCommit() throws VCSRepositoryException;

   /**
    * Get tag name.
    * <p>
    * Generally speaking the tag name would be the same as its ID, however
    * implementations may return different tag name that their ID.
    * 
    * @return the name of this tag
    */
   public abstract String getName();
}
