/**
 * 
 */
package gr.uom.se.vcs;

import java.io.IOException;
import java.io.InputStream;
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
    *            if contents can not be read
    */
   byte[] getContents() throws IOException;

   /**
    * Write the contents of the file to the specified output stream.
    * <p>
    * 
    * <b>WARNING:</b> the caller is responsible for closing the output stream,
    * after finishing the writing of contents.
    * 
    * @param target
    *           the stream to write the contents
    * @throws IOException
    *            if contents can not be read
    */
   void getContents(OutputStream target) throws IOException;

   /**
    * Get the input stream of this file to read the contents.
    * <p>
    * 
    * <b>WARNING:</b> after reading this file contents the stream must be closed
    * otherwise problem may occur in the future.
    * 
    * @return the input stream of this file
    * @throws IOException
    *            if contents can not be read
    */
   InputStream getContentStream() throws IOException;
}
