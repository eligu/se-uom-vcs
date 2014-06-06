/**
 * 
 */
package gr.uom.se.vcs.mocks;

import gr.uom.se.vcs.VCSCommit;
import gr.uom.se.vcs.VCSResource;
import gr.uom.se.vcs.exceptions.VCSRepositoryException;
import gr.uom.se.vcs.walker.Visitor;

import java.util.Collection;


/**
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public class VCSResourceMock implements VCSResource {

    protected String path;
    protected VCSResource.Type type;
    protected VCSResource parent;
    /**
     * 
     */
    public VCSResourceMock(String path, VCSResource.Type type, VCSResource parent) {
	this.path = path;
	this.type = type;
	this.parent = parent;
    }

    /**
     *  {@inheritDoc)
     * @see gr.uom.se.vcs.VCSResource#getCommit()
     */
    @Override
    public VCSCommit getCommit() {
	return null;
    }

    /**
     *  {@inheritDoc)
     * @see gr.uom.se.vcs.VCSResource#getPath()
     */
    @Override
    public String getPath() {
	return path;
    }
    
    public void setPath(String path) {
	this.path = path;
    }
    
    public void setType(VCSResource.Type type) {
	this.type = type;
    }
    
    public void setParent(VCSResource parent) {
	this.parent = parent;
    }
    
    /**
     *  {@inheritDoc)
     * @see gr.uom.se.vcs.VCSResource#getType()
     */
    @Override
    public Type getType() {
	return type;
    }

    /**
     *  {@inheritDoc)
     * @see gr.uom.se.vcs.VCSResource#getCreationCommits()
     */
    @Override
    public Collection<VCSCommit> getCreationCommits()
	    throws VCSRepositoryException {
	return null;
    }

    /**
     *  {@inheritDoc)
     * @see gr.uom.se.vcs.VCSResource#getDeletionCommits()
     */
    @Override
    public Collection<VCSCommit> getDeletionCommits()
	    throws VCSRepositoryException {
	return null;
    }

    /**
     *  {@inheritDoc)
     * @see gr.uom.se.vcs.VCSResource#getAllCommits()
     */
    @Override
    public Collection<VCSCommit> getAllCommits() throws VCSRepositoryException {
	return null;
    }

    /**
     *  {@inheritDoc)
     * @see gr.uom.se.vcs.VCSResource#isAdded()
     */
    @Override
    public boolean isAdded() throws VCSRepositoryException {
	return false;
    }

    /**
     *  {@inheritDoc)
     * @see gr.uom.se.vcs.VCSResource#isModified()
     */
    @Override
    public boolean isModified() throws VCSRepositoryException {
	return false;
    }

    /**
     *  {@inheritDoc)
     * @see gr.uom.se.vcs.VCSResource#walkAllCommits(gr.uom.se.vcs.walker.Visitor)
     */
    @Override
    public void walkAllCommits(Visitor<VCSCommit> visitor)
	    throws VCSRepositoryException {
    }

    /**
     *  {@inheritDoc)
     * @see gr.uom.se.vcs.VCSResource#getParent()
     */
    @Override
    public VCSResource getParent() {
	return parent;
    }
    
    
}
