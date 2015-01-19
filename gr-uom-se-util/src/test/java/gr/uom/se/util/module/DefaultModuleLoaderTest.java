/**
 * 
 */
package gr.uom.se.util.module;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import gr.uom.se.util.manager.ConfigManager;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public class DefaultModuleLoaderTest {

   static ModuleLoader moduleLoader = null;
   static ConfigManager config = null;
   static ParameterProvider parameterProvider = null;
   static DefaultPropertyInjector beanInjector = null;

   @BeforeClass
   public static void prepareTests() {
      config = new ConfigManagerMock();
      parameterProvider = new DefaultParameterProvider(config, null);
      moduleLoader = new DefaultModuleLoader(config, parameterProvider);
      beanInjector = new DefaultPropertyInjector(parameterProvider);
      config.setProperty(ModuleConstants.DEFAULT_MODULE_CONFIG_DOMAIN,
            ModuleConstants.DEFAULT_MODULE_LOADER_PROPERTY, moduleLoader);
   }

   /**
    * Test the loading using the provider at module annotation.
    */
   @Test
   public void testLoad1() {

      // Check the loading with defaults
      PersonMock person = moduleLoader.load(PersonMock.class);
      assertNotNull(person);
      assertEquals(person.getName(), PersonDefaults.PERSON_NAME_MODULE);
      assertEquals(person.getAge(),
            (int) Integer.valueOf(PersonDefaults.PERSON_AGE_MODULE));
   }

   /**
    * Test the loading using the static method of person as a provider.
    */
   @Test
   public void testLoad2() {
      PersonMock person = moduleLoader.load(PersonMock.class, PersonMock.class);
      assertNotNull(person);
      assertEquals(person.getName(), PersonDefaults.PERSON_NAME_MODULE);
      assertEquals(person.getAge(),
            (int) Integer.valueOf(PersonDefaults.PERSON_AGE_MODULE));
   }

   /**
    * Test the loading using specified properties from config manager.
    */
   @Test
   public void testLoad3() {
      // Check the loading with values from config manager
      String expectedName = "a name";
      int expectedAge = 20;
      setPersonProperties(expectedName, expectedAge);

      PersonMock person = moduleLoader.load(PersonMock.class);
      assertNotNull(person);
      assertEquals(expectedName, person.getName());
      assertEquals(expectedAge, person.getAge());

      // clear person properties
      setPersonProperties(null, null);
   }

   /**
    * Test the loading of an interface type.
    */
   @Test
   public void testProvider() {
      IModule module = moduleLoader.load(IModule.class);
      assertNotNull(module);
      assertEquals(22, module.getVar());
   }

   /**
    * Test the injection of properties.
    * 
    * @throws IllegalAccessException
    */
   @Test
   public void testInjector() throws IllegalAccessException {
      PersonMock person = moduleLoader.load(PersonMock.class);
      assertNotNull(person);
      
      // Check the injection of default values
      beanInjector.injectProperties(person);
      assertEquals(PersonDefaults.PERSON_NAME_INJECTED, person.getName());
      assertEquals((int) Integer.valueOf(PersonDefaults.PERSON_AGE_INJECTED),
            person.getAge());

      // Check the injection of config values
      String expectedName = "a name";
      int expectedAge = 20;
      setPersonProperties(expectedName, expectedAge);

      beanInjector.injectProperties(person);
      assertEquals(expectedName, person.getName());
      assertEquals(expectedAge, person.getAge());
      
      // Clear injected properties
      setPersonProperties(null, null);
   }

   private void setPersonProperties(String name, Integer age) {
      String property = "name";
      String domain = PersonDefaults.PERSON_DOMAIN;
      config.setProperty(domain, property, name);

      property = "age";
      config.setProperty(domain, property, age);
   }
}
