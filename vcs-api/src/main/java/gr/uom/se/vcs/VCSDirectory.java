/**
 * 
 */
package gr.uom.se.vcs;

import gr.uom.se.vcs.exceptions.VCSRepositoryException;
import gr.uom.se.vcs.walker.ResourceVisitor;

import java.util.Set;


/**
 * Represents a directory of {@link VCSRepository}.
 * <p>
 * 
 * This is a specific case of {@link VCSResource}, which allows one to get the
 * files of a directory.
 * 
 * @author Elvis Ligu
 * @since 0.0.1
 * @version 0.0.1
 * @see {@link VCSResource}
 */
public interface VCSDirectory extends VCSResource {

   /**
    * Get the files contained in this directory.
    * <p>
    * 
    * @return a set of files contained in this directory
    * @throws VCSRepositoryException
    *            if there is a problem with the repository
    */
   Set<VCSFile> getFiles() throws VCSRepositoryException;

   /**
    * Get the directories contained in this directory.
    * <p>
    * 
    * @return a set of directories contained in this directory
    * @throws VCSRepositoryException
    *            if there is a problem with the repository
    */
   Set<VCSDirectory> getDirectories() throws VCSRepositoryException;

   /**
    * Collect the resources within this directory.
    * <p>
    * If <code>files</code> and <code>dir</code> parameters are both false an
    * exception wil be thrown. You should specify at least one type of resources
    * to collect.
    * 
    * @param files
    *           if true {@link VCSFile} objects will also be returned in results
    * @param dirs
    *           if true {@link VCSDirectory} objects will also be returned
    * @param recursive
    *           if true all the resources under this directory will be returned
    * @return a set of resources contained in this directory (if recursive true
    *         all the resources under this path)
    * @throws VCSRepositoryException
    *            if there is a problem with the repository
    */
   Set<VCSResource> getResources(final boolean files, final boolean dirs,
         final boolean recursive) throws VCSRepositoryException;

   /**
    * Walk the resources under this directory.
    * <p>
    * 
    * This method behavior is similar to
    * {@link #getResources(boolean, boolean, boolean)} with the difference that
    * an instance of {@link ResourceVisitor} will be used to get the parameters,
    * and each resource will be passed to {@link ResourceVisitor#visit(Object)}
    * method.
    * <p>
    * 
    * This method is usually more efficient if you don't want to collect all
    * results in a single place and then to use them.
    * 
    * @param visitor
    * @throws VCSRepositoryException
    */
   void walkResources(final ResourceVisitor<VCSResource> visitor)
         throws VCSRepositoryException;
}
