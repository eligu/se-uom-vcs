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
public class PathFilterTest {

    ResourceFilterFactory filters = new ResourceFilterFactory();
    
    /**
     * Test method for {@link PathFilter#include(VCSResource)}.
     */
    @Test
    public void testInclude() {
	VCSResourceMock rmock = new VCSResourceMock("java/path", null, null);
	AbstractResourceFilter<VCSResource> filter = (AbstractResourceFilter<VCSResource>) filters.path("java/path", "java/none/path");
	assertTrue(filter.include(rmock));
	assertTrue(!filter.not().include(rmock));
	
	rmock.setPath("java/none/path");
	assertTrue(filter.include(rmock));
	assertTrue(!filter.not().include(rmock));
	assertTrue(((AbstractResourceFilter<VCSResource>) filter.not()).not().include(rmock));
	
	
	rmock.setPath("java");
	assertTrue(!filter.include(rmock));
	assertTrue(filter.not().include(rmock));
	assertTrue(!((AbstractResourceFilter<VCSResource>) filter.not()).not().include(rmock));
	
	rmock.setPath("java/path/none");
	assertTrue(!filter.include(rmock));
	assertTrue(filter.not().include(rmock));
	assertTrue(!((AbstractResourceFilter<VCSResource>) filter.not()).not().include(rmock));
    }

    /**
     * Test method for {@link AbstractPathFilter#enter(VCSResource)}.
     */
    @Test
    public void testAllow() {
	VCSResourceMock rmock = new VCSResourceMock("java/path", null, null);
	AbstractResourceFilter<VCSResource> filter = (AbstractResourceFilter<VCSResource>) filters.path("java/path", "java/none/path");
	// This should not be entered because we need to stop entering path
	assertTrue(!filter.enter(rmock));
	
	// This should enter because we can not get at java/path
	rmock.setPath("java");
	assertTrue(filter.enter(rmock));
	assertTrue(filter.not().enter(rmock));
	
	// This should not enter because it is a path under java/path
	rmock.setPath("java/path/none");
	assertTrue(!filter.enter(rmock));
	assertTrue(filter.not().enter(rmock));
	
	
	// This should enter because we can not get at java/none/path
	rmock.setPath("java/none");
	assertTrue(filter.enter(rmock));
	assertTrue(filter.not().enter(rmock));
	
	// This should not enter because we don't want to enter the java/none/path
	rmock.setPath("java/none/path");
	assertTrue(!filter.enter(rmock));
	assertTrue(filter.not().enter(rmock));

	
	// This should not be entered because we need to check only the required paths
	rmock.setPath("java/mypath/path");
	assertTrue(!filter.enter(rmock));
	assertTrue(filter.not().enter(rmock));
	
	
    }

}
