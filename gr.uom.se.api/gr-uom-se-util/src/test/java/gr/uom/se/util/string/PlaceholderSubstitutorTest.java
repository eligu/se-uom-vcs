package gr.uom.se.util.string;

import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author elvis
 */
public class PlaceholderSubstitutorTest {

   public PlaceholderSubstitutorTest() {
   }

   /**
    * Test of replace method, of class PlaceholderSubstitutor.
    */
   @Test
   public void testReplaceSinglePlaceHolder() {
      String content = "src/${language}/main${manager}${project}";
      String language = "java";
      String manager = "Manager";
      String project = "OfProjects";
      String expected = "src/java/mainManagerOfProjects";
      content = PlaceholderSubstitutor.replace(content, "language", language);
      content = PlaceholderSubstitutor.replace(content, "manager", manager);
      content = PlaceholderSubstitutor.replace(content, "project", project);
      assertEquals(expected, content);
   }

   /**
    * Test of replace method, of class PlaceholderSubstitutor.
    */
   @Test
   public void testReplaceMultiPlaceHolder() {
      String content = "${artifact}-${version}.${suffix}";
      Map<String, String> placeholders = new HashMap<String, String>();
      placeholders.put("version", "4.4.2");
      placeholders.put("artifact", "android");
      placeholders.put("suffix", "apk");
      String expected = "android-4.4.2.apk";
      content = PlaceholderSubstitutor.replace(content, placeholders)
              .toString();
      assertEquals(expected, content);
      
      content = "${artifact-${version}}.${suffix}";
      placeholders.put("artifact-4.4.2", "oldPackage");
      expected = "oldPackage.apk";
      content = PlaceholderSubstitutor.replace(content, placeholders)
              .toString();
      assertEquals(expected, content);
   }
}
