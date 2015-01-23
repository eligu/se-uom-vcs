/**
 * 
 */
package gr.uom.se.util.module;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import gr.uom.se.util.manager.ConfigManager;
import gr.uom.se.util.manager.DefaultConfigManager;

import org.junit.Before;
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
      config = new DefaultConfigManager();
      parameterProvider = new DefaultParameterProvider(config, null);
      moduleLoader = new DefaultModuleLoader(config, parameterProvider);
      beanInjector = new DefaultPropertyInjector(parameterProvider);
      config.setProperty(ModuleConstants.DEFAULT_MODULE_CONFIG_DOMAIN,
            ModuleConstants.DEFAULT_MODULE_LOADER_PROPERTY, moduleLoader);
   }

   @Before
   public void initBeforeTest() {
      // clear person properties
      setPersonProperties(null, null);
      PersonLoader.counter = 0;
   }

   /**
    * Test the loading using the provider at module annotation.
    */
   @Test
   public void testLoad1() {

      // Check the loading with defaults
      PersonMock1 person = moduleLoader.load(PersonMock1.class);
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
      PersonMock1 person = moduleLoader.load(PersonMock1.class,
            PersonMock1.class);
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

      PersonMock1 person = moduleLoader.load(PersonMock1.class);
      assertNotNull(person);
      assertEquals(expectedName, person.getName());
      assertEquals(expectedAge, person.getAge());
   }

   /**
    * Test the loading using the provider, but create the provider each time.
    * <p>
    */
   @Test
   public void testLoad4() {
      // We should check that a provider instance is always created
      // if it is not stored within config manager
      PersonMock1 person1 = moduleLoader.load(PersonMock1.class,
            PersonLoader.class);
      assertNotNull(person1);
      assertEquals(person1.getName(), PersonDefaults.PERSON_NAME_MODULE);
      assertEquals(person1.getAge(),
            (int) Integer.valueOf(PersonDefaults.PERSON_AGE_MODULE));

      PersonMock2 person2 = moduleLoader.load(PersonMock2.class,
            PersonLoader.class);
      assertNotNull(person2);
      assertEquals(person2.getName(), PersonDefaults.PERSON_NAME_LOADER);
      assertEquals(person2.getAge(),
            (int) Integer.valueOf(PersonDefaults.PERSON_AGE_LOADER));

      // Check that the provider instance is created two times
      assertEquals(2, PersonLoader.counter);
   }

   /**
    * Test the loading using the provider, but store an instance of the provider
    * at a default place that can be retrieved without creation by the loader.
    */
   @Test
   public void testLoad5() {
      // First create a person provider and place it at a default place
      // in config manager
      // Store first at person class default config domain
      PersonLoader personProvider = new PersonLoader();

      // The default module loader will check if there is a provider
      // instance at this place first
      config.setProperty(
            ModuleConstants.getDefaultConfigFor(PersonMock1.class),
            ModuleConstants.getProviderNameFor(PersonMock1.class),
            personProvider);

      // Create a person. The loader will check for the provider at the default
      // person
      // place if it is there
      PersonMock1 person1 = moduleLoader.load(PersonMock1.class);
      assertNotNull(person1);
      assertEquals(person1.getName(), PersonDefaults.PERSON_NAME_MODULE);
      assertEquals(person1.getAge(),
            (int) Integer.valueOf(PersonDefaults.PERSON_AGE_MODULE));

      // Check provider instance number
      // A provider instance will not be created because it
      // is stored at a default place for this class
      // and can be retrieved from there
      assertEquals(1, PersonLoader.counter);

      // Create an other person
      // but this time will use the same provider instance
      person1 = moduleLoader.load(PersonMock1.class);
      assertNotNull(person1);
      assertEquals(person1.getName(), PersonDefaults.PERSON_NAME_MODULE);
      assertEquals(person1.getAge(),
            (int) Integer.valueOf(PersonDefaults.PERSON_AGE_MODULE));

      // Check provider instance number
      // The provider instance will be retreived from the default
      // config for this class
      assertEquals(1, PersonLoader.counter);

      // Create a different person class,
      // this time the provider will be created by the loader because
      // it is not under the default places where the loader can find it
      PersonMock2 person2 = moduleLoader.load(PersonMock2.class,
            PersonLoader.class);
      assertNotNull(person2);
      assertEquals(person2.getName(), PersonDefaults.PERSON_NAME_LOADER);
      assertEquals(person2.getAge(),
            (int) Integer.valueOf(PersonDefaults.PERSON_AGE_LOADER));

      // Check that the provider instance is created two times
      // The provider is not stored at the default place for this
      // class
      assertEquals(2, PersonLoader.counter);

      // Now store the provider at the default place
      // for this class and check that it was not
      // created
      // The default module loader will check if there is a provider
      // instance in default module config
      // only if it is not in the default config of this class
      config.setProperty(ModuleConstants.DEFAULT_MODULE_CONFIG_DOMAIN,
            ModuleConstants.getProviderNameFor(PersonMock2.class),
            personProvider);
      // The provider will not be created here but will be retrieved from the
      // default
      // config domain
      person2 = moduleLoader.load(PersonMock2.class, PersonLoader.class);
      assertNotNull(person2);
      assertEquals(person2.getName(), PersonDefaults.PERSON_NAME_LOADER);
      assertEquals(person2.getAge(),
            (int) Integer.valueOf(PersonDefaults.PERSON_AGE_LOADER));

      // Ensure that provider instance is not created
      // but it is retrieved from default config domain
      assertEquals(2, PersonLoader.counter);
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
   public void testInjector1() throws IllegalAccessException {
      PersonMock1 person = moduleLoader.load(PersonMock1.class);
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
   }

   @Test
   public void testInjector2() {
      PersonMock1 person = moduleLoader.load(PersonMock1.class);
      assertNotNull(person);

      // Check the injection of default values
      beanInjector.injectProperties(person);
      assertEquals(PersonDefaults.PERSON_NAME_INJECTED, person.getName());
      assertEquals((int) Integer.valueOf(PersonDefaults.PERSON_AGE_INJECTED),
            person.getAge());
      assertNotNull(person.getPartner());
      assertNull(person.getAddress());
      // The injected person here should be a person2 instance that is loaded
      // by default constructor (it is not annotated and doesn't declare
      // a provider not or has another provider constructor).
      assertEquals(PersonDefaults.PERSON_NAME_DEFAULT_CONSTRUCTOR, person
            .getPartner().getName());
      assertEquals(
            Integer.parseInt(PersonDefaults.PERSON_AGE_DEFAULT_CONSTRUCTOR),
            person.getPartner().getAge());

      // Check the injection of config values
      PersonMock2 partner = new PersonMock2();
      int expectedAge = 0;
      String expectedName = "expected";
      partner.setAge(expectedAge);
      partner.setName(expectedName);
      // Set the partner to configuration
      config.setProperty(PersonDefaults.PERSON_DOMAIN, "partner", partner);

      // inject again properties to person
      beanInjector.injectProperties(person);
      assertEquals(expectedName, person.getPartner().getName());
      assertEquals(expectedAge, person.getPartner().getAge());
   }

   private void setPersonProperties(String name, Integer age) {
      String property = "name";
      String domain = PersonDefaults.PERSON_DOMAIN;
      
      config.setProperty(domain, property, name);

      property = "age";
      config.setProperty(domain, property, age);
   }
}
