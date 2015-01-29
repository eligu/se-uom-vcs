/**
 * 
 */
package gr.uom.se.vcs.walker.filter.resource;

import static org.junit.Assert.assertTrue;
import gr.uom.se.vcs.VCSResource;
import gr.uom.se.vcs.mocks.VCSResourceMock;
import gr.uom.se.vcs.walker.filter.resource.AbstractPathFilter;
import gr.uom.se.vcs.walker.filter.resource.AbstractResourceFilter;
import gr.uom.se.vcs.walker.filter.resource.PathPrefixFilter;
import gr.uom.se.vcs.walker.filter.resource.ResourceFilterUtility;

import org.junit.Test;


/**
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public class ChildFilterTest {

   /**
    * Test method for {@link PathPrefixFilter#include(VCSResource)}.
    */
   @Test
   public void testInclude() {
      VCSResourceMock rmock = new VCSResourceMock("java/path/test.java", null,
            null);
      rmock.setParent(new VCSResourceMock("java/path", null, null));
      AbstractResourceFilter<VCSResource> filter = (AbstractResourceFilter<VCSResource>) ResourceFilterUtility
            .child("java/path");
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
      VCSResourceMock rmock = new VCSResourceMock("java/path/test.java", null,
            null);
      rmock.setParent(new VCSResourceMock("java/path", null, null));
      AbstractResourceFilter<VCSResource> filter = (AbstractResourceFilter<VCSResource>) ResourceFilterUtility
            .child("java/path");
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
