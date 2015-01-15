/**
 * 
 */
package gr.uom.se.util.module;

import junit.framework.TestCase;

import org.junit.Test;

/**
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public class DefaultModuleLoaderTest extends TestCase {

   ModuleLoader moduleLoader = new DefaultModuleLoader(null);
   
   @Test
   public void testLoad1(int i) {
      PersonMock person = moduleLoader.load(PersonMock.class);
      assertNotNull(person);
      assertEquals(person.name, PersonDefaults.PERSON_NAME_MODULE);
      assertEquals(person.age, (int)Integer.valueOf(PersonDefaults.PERSON_AGE_MODULE));
   }
   
   @Test
   public void testLoad2() {
      PersonMock person = moduleLoader.load(PersonMock.class, PersonMock.class);
      assertNotNull(person);
      assertEquals(person.name, PersonDefaults.PERSON_NAME_MODULE);
      assertEquals(person.age, (int)Integer.valueOf(PersonDefaults.PERSON_AGE_MODULE));
   }
}
