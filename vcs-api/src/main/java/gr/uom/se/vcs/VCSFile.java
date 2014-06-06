/**
 * 
 */
package gr.uom.se.vcs;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Represents a file of {@link VCSRepository}.
 * <p>
 * 
 * This is a specific case of {@link VCSResource} which allows one to get the
 * contents of the file.
 * 
 * @author Elvis Ligu
 * @since 0.0.1
 * @version 0.0.1
 * @see {@link VCSResource}
 */
public interface VCSFile extends VCSResource {

   /**
    * Get the contents of the file.
    * <p>
    * 
    * @return the contents, never null, when there are no contents an empty byte
    *         array is returned
    * @throws IOException
    */
   byte[] getContents() throws IOException;

   /**
    * Write the contents of the file to the specified output stream.
    * <p>
    * 
    * @param target
    *           the stream to write the contents
    */
   void getContents(OutputStream target) throws IOException;
}
