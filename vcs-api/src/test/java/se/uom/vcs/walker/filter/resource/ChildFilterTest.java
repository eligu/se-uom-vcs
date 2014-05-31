/**
 * 
 */
package se.uom.vcs.walker.filter.resource;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import se.uom.vcs.VCSResource;
import se.uom.vcs.mocks.VCSResourceMock;

/**
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public class ChildFilterTest {

    ResourceFilterUtility filters = new ResourceFilterUtility();
    
    /**
     * Test method for {@link PathPrefixFilter#include(VCSResource)}.
     */
    @Test
    public void testInclude() {
	VCSResourceMock rmock = new VCSResourceMock("java/path/test.java", null, null);
	rmock.setParent(new VCSResourceMock("java/path", null, null));
	AbstractResourceFilter<VCSResource> filter = (AbstractResourceFilter<VCSResource>) filters.child("java/path");
	assertTrue(filter.include(rmock));
	assertTrue(!filter.not().include(rmock));
	
	rmock.setPath("java");
	rmock.setParent(null);
	assertTrue(!filter.include(rmock));
	assertTrue(filter.not().include(rmock));
	
	rmock.setPath("java/pat/test");
	rmock.setParent(new VCSResourceMock("java/pat", null, null));
	assertTrue(!filter.include(rmock));
	assertTrue(filter.not().include(rmock));
	
	rmock.setPath("java/path/test/path");
	rmock.setParent(new VCSResourceMock("java/path/test", null, null));
	assertTrue(!filter.include(rmock));
	assertTrue(filter.not().include(rmock));
    }

    /**
     * Test method for {@link AbstractPathFilter#enter(VCSResource)}.
     */
    @Test
    public void testAllow() {
	VCSResourceMock rmock = new VCSResourceMock("java/path/test.java", null, null);
	rmock.setParent(new VCSResourceMock("java/path", null, null));
	AbstractResourceFilter<VCSResource> filter = (AbstractResourceFilter<VCSResource>) filters.child("java/path");
	assertTrue(!filter.enter(rmock));
	
	rmock.setPath("java/pat/test");
	rmock.setParent(new VCSResourceMock("java/pat", null, null));
	assertTrue(!filter.enter(rmock));
	
	rmock.setPath("java/path/test/path");
	rmock.setParent(new VCSResourceMock("java/path/test", null, null));
	assertTrue(!filter.enter(rmock));
	
	rmock.setPath("java");
	rmock.setParent(null);
	assertTrue(filter.not().enter(rmock));
    }

}
