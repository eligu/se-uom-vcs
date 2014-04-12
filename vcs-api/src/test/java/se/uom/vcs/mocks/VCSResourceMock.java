/**
 * 
 */
package se.uom.vcs.mocks;

import java.util.Collection;

import se.uom.vcs.VCSCommit;
import se.uom.vcs.VCSResource;
import se.uom.vcs.exceptions.VCSRepositoryException;
import se.uom.vcs.walker.Visitor;

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
     * @see se.uom.vcs.VCSResource#getCommit()
     */
    @Override
    public VCSCommit getCommit() {
	return null;
    }

    /**
     *  {@inheritDoc)
     * @see se.uom.vcs.VCSResource#getPath()
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
     * @see se.uom.vcs.VCSResource#getType()
     */
    @Override
    public Type getType() {
	return type;
    }

    /**
     *  {@inheritDoc)
     * @see se.uom.vcs.VCSResource#getCreationCommits()
     */
    @Override
    public Collection<VCSCommit> getCreationCommits()
	    throws VCSRepositoryException {
	return null;
    }

    /**
     *  {@inheritDoc)
     * @see se.uom.vcs.VCSResource#getDeletionCommits()
     */
    @Override
    public Collection<VCSCommit> getDeletionCommits()
	    throws VCSRepositoryException {
	return null;
    }

    /**
     *  {@inheritDoc)
     * @see se.uom.vcs.VCSResource#getAllCommits()
     */
    @Override
    public Collection<VCSCommit> getAllCommits() throws VCSRepositoryException {
	return null;
    }

    /**
     *  {@inheritDoc)
     * @see se.uom.vcs.VCSResource#isAdded()
     */
    @Override
    public boolean isAdded() throws VCSRepositoryException {
	return false;
    }

    /**
     *  {@inheritDoc)
     * @see se.uom.vcs.VCSResource#isModified()
     */
    @Override
    public boolean isModified() throws VCSRepositoryException {
	return false;
    }

    /**
     *  {@inheritDoc)
     * @see se.uom.vcs.VCSResource#walkAllCommits(se.uom.vcs.walker.Visitor)
     */
    @Override
    public void walkAllCommits(Visitor<VCSCommit> visitor)
	    throws VCSRepositoryException {
    }

    /**
     *  {@inheritDoc)
     * @see se.uom.vcs.VCSResource#getParent()
     */
    @Override
    public VCSResource getParent() {
	return parent;
    }
    
    
}
