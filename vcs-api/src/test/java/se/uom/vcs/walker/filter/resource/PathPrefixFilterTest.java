package se.uom.vcs.walker.filter.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static se.uom.vcs.walker.filter.resource.ResourceFilterUtility.and;
import static se.uom.vcs.walker.filter.resource.ResourceFilterUtility.child;
import static se.uom.vcs.walker.filter.resource.ResourceFilterUtility.or;
import static se.uom.vcs.walker.filter.resource.ResourceFilterUtility.path;
import static se.uom.vcs.walker.filter.resource.ResourceFilterUtility.prefix;
import static se.uom.vcs.walker.filter.resource.ResourceFilterUtility.suffix;

import org.junit.Test;

import se.uom.vcs.VCSResource;
import se.uom.vcs.mocks.VCSResourceMock;

public class PathPrefixFilterTest {

   ResourceFilterUtility filters = new ResourceFilterUtility();

   @Test
   public void testInclude() {

      VCSResourceMock rmock = new VCSResourceMock("src/path/test.java",
            VCSResource.Type.FILE, null);
      VCSResourceFilter<VCSResource> filter = prefix("/src/path");
      assertTrue(filter.include(rmock));

      filter = ResourceFilterUtility.prefix("/src/path");
      rmock.setPath("src/path");
      assertTrue(filter.include(rmock));

      rmock.setPath("src/none/test.java");
      assertTrue(!filter.include(rmock));

      rmock.setPath("sr");
      assertTrue(!filter.include(rmock));

      filter = ResourceFilterUtility.prefix("java/test/path", "java/path",
            "java");
      rmock.setPath("java");
      assertTrue(filter.include(rmock));
   }

   @Test
   public void testCorrectAndCheckPath() {
      String path = "/java/path";
      assertEquals("java/path", PathPrefixFilter.correctAndCheckPath(path));

      path = "java/";
      assertEquals("java", PathPrefixFilter.correctAndCheckPath(path));

      path = "\\java\\path\\";
      assertEquals("java/path", PathPrefixFilter.correctAndCheckPath(path));
   }

   @Test
   public void testIsPrefix() {
      String path = "java/path";
      assertTrue(AbstractPathFilter.isPrefix("java", path));
      assertTrue(!AbstractPathFilter.isPrefix("c", path));
   }

   @Test
   public void testAllow() {
      VCSResourceMock rmock = new VCSResourceMock("src/path/test.java",
            VCSResource.Type.FILE, null);
      VCSResourceFilter<VCSResource> filter = ResourceFilterUtility
            .prefix("/src/path");
      assertTrue(filter.enter(rmock));

      rmock.setPath("path");
      assertTrue(!filter.enter(rmock));

      rmock.setPath("src");
      assertTrue(filter.enter(rmock));
   }

   @SuppressWarnings("unchecked")
   @Test
   public void testAllInclude() {

      // Construct a filter that filters:
      // 1 - only all under java/test
      // 2 - only files under java/main that are .java
      // 3 - the paths src/java/test.java and src/main/java
      // 4 - only children of java/path that ends with .txt
      // 5 - but not children of java/main

      AbstractResourceFilter<VCSResource> filter = (AbstractResourceFilter<VCSResource>) or(
      // allow all under java/test
            and(prefix("java/test"), filters.type(VCSResource.Type.FILE)),
            // allow files under java/main that are .java
            and(prefix("java/main"), suffix(".java"),
                  filters.type(VCSResource.Type.FILE)),
            // or paths
            path("src/java/test.java", "src/main/java"),
            // or children of java/path that ends with .txt
            and(child("java/path"), suffix(".txt")));

      // and not children of java/main
      filter = (AbstractResourceFilter<VCSResource>) and(filter,
            ((AbstractResourceFilter<VCSResource>) child("java/main")).not());

      VCSResourceMock rmock = new VCSResourceMock("java/test",
            VCSResource.Type.FILE, null);
      rmock.setParent(new VCSResourceMock("java", null, null));
      assertTrue(filter.include(rmock));

      rmock.setPath("java/test/test.txt"); // should be allowed because is a
                                           // child of java/test that ends with
                                           // .txt
      assertTrue(filter.include(rmock));

      rmock.setParent(new VCSResourceMock("java/main", null, null));
      rmock.setPath("java/main/file.java"); // should not be allowed because
                                            // file.java is a child of java/main
      assertTrue(!filter.include(rmock));

      rmock.setParent(new VCSResourceMock("java/main/path", null, null));
      rmock.setPath("java/main/path/file.java"); // should be allowed because is
                                                 // under java/main but not a
                                                 // child of
      assertTrue(filter.include(rmock));

      rmock.setParent(new VCSResourceMock("src/main/java", null, null));
      rmock.setPath("src/main/java"); // should be allowed because is path
                                      // src/main/java
      assertTrue(filter.include(rmock));

      rmock.setParent(new VCSResourceMock("src/java", null, null));
      rmock.setPath("src/java/test.java"); // should be allowed because is path
                                           // src/java/test.java
      assertTrue(filter.include(rmock));

      rmock.setParent(new VCSResourceMock("java/path", null, null));
      rmock.setPath("java/path/test.txt"); // should be allowed because is a
                                           // child of java/path that ends with
                                           // .txt
      assertTrue(filter.include(rmock));

      rmock.setPath("java/path/test.java"); // should not be allowed because is
                                            // a child of java/path that ends
                                            // with .java
      assertTrue(!filter.include(rmock));
   }

   @SuppressWarnings("unchecked")
   @Test
   public void testAllAllow() {

      // Construct a filter that filters:
      // 1 - only all under java/test
      // 2 - only files under java/main that are .java
      // 3 - the paths src/java/test.java and src/main/java
      // 4 - only children of java/path that ends with .txt
      // 5 - but not children of java/main

      AbstractResourceFilter<VCSResource> filter = (AbstractResourceFilter<VCSResource>) or(
      // allow all under java/test
            and(prefix("java/test"), filters.type(VCSResource.Type.FILE)),
            // allow files under java/main that are .java
            and(prefix("java/main"), suffix(".java"),
                  filters.type(VCSResource.Type.FILE)),
            // or paths
            path("src/java/test.java", "src/main/java"),
            // or children of java/path that ends with .txt
            and(child("java/path"), suffix(".txt")));

      // and not children of java/main
      filter = (AbstractResourceFilter<VCSResource>) and(filter,
            ((AbstractResourceFilter<VCSResource>) child("java/main")).not());

      VCSResourceMock rmock = new VCSResourceMock("java/main",
            VCSResource.Type.FILE, null);
      rmock.setParent(new VCSResourceMock("java", null, null));
      assertTrue(filter.enter(rmock));

      rmock.setParent(new VCSResourceMock("java/test", null, null));
      rmock.setPath("java/test/test.txt"); // should be allowed because is a
                                           // child of java/test that ends with
                                           // .txt
      assertTrue(filter.enter(rmock));

      rmock.setParent(new VCSResourceMock("java/main", null, null));
      rmock.setPath("java/main/test.java"); // should be allowed because is
                                            // under the path java/main
      assertTrue(filter.enter(rmock));
      assertTrue(!filter.include(rmock)); // however should not be included
                                          // because is a child of java/main

      rmock.setParent(new VCSResourceMock("java/main/path", null, null));
      rmock.setPath("java/main/path/file.java"); // should be allowed because is
                                                 // under java/main
      assertTrue(filter.enter(rmock));

      rmock.setPath("src/main/java"); // should be allowed because is path
                                      // src/main/java
      assertTrue(!filter.enter(rmock));

      rmock.setPath("src/java"); // should be allowed because is path
                                 // src/java/test.java
      assertTrue(filter.enter(rmock));

      rmock.setParent(new VCSResourceMock("java/main", null, null));
      rmock.setPath("java/main/p"); // should be allowed because is a child of
                                    // java/path that ends with .java
      assertTrue(filter.enter(rmock));
   }
}
