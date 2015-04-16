/**
 * 
 */
package gr.uom.se.util.module;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import gr.uom.se.util.property.DomainPropertyHandler;
import gr.uom.se.util.property.PropertyUtils;

import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Elvis Ligu
 */
public class ModuleContextTest {

   private static ModuleContext context;
   private static DomainPropertyHandler properties;

   @BeforeClass
   public static void init() {
      // Create a property handler
      DomainPropertyHandler handler = PropertyUtils
            .newHandler(new HashMap<String, Map<String, Object>>());
      properties = handler;
      // Create a module context with default locator and no parent
      context = new DefaultModuleContext(handler, null, null);

      // Init the default modules' configuration (loader, parameter provider and
      // injector). Although the procedure of specifying the default is not
      // required because they can resolve the default implementations by itself
      // we are testing the context that is we want to specify these
      ModuleLoader defaultLoader = new DefaultModuleLoader(handler, null);
      ParameterProvider defaultParameterProvider = new DefaultParameterProvider(
            handler, null);
      PropertyInjector defaultInjector = new DefaultPropertyInjector(handler,
            null);
      // Set the default loader to the context
      context.setLoader(defaultLoader, null);
      // Set the default parameter provider
      context.setParameterProvider(defaultParameterProvider, null);
      // Set the default injector
      context.setPropertyInjector(defaultInjector, null);
   }

   @Test
   public void testIModuleDefaults() {
      IModule module = context.load(IModule.class);
      assertNotNull(module);
      assertEquals(22, module.getVar());
   }

   @Test
   public void testIModuleCustom() {
      properties.setProperty("default", "var", 30);
      IModule module = context.load(IModule.class);
      assertNotNull(module);
      assertEquals(30, module.getVar());
      // clear properties
      properties.setProperty("default", "var", null);
   }

   @Test
   public void testPersonDefault() {
      // Test load
      PersonMock1 person = context.load(PersonMock1.class);
      assertNotNull(person);
      String name = PersonDefaults.PERSON_NAME_MODULE;
      int age = Integer.parseInt(PersonDefaults.PERSON_AGE_MODULE);
      assertEquals(name, person.getName());
      assertEquals(age, person.getAge());
      assertNull(person.getAddress());

      // Test inject defaults
      context.inject(person);
      assertEquals(name, person.getName());
      assertEquals(age, person.getAge());
      PersonMock2 partner = person.getPartner();
      assertNotNull(partner);
      name = PersonDefaults.PERSON_NAME_DEFAULT_CONSTRUCTOR;
      age = Integer.parseInt(PersonDefaults.PERSON_AGE_DEFAULT_CONSTRUCTOR);
      assertEquals(name, partner.getName());
      assertEquals(age, partner.getAge());
   }

   @Test
   public void testPersonCustom() {
      String name = "person 1";
      String address = "p1 address";
      int age = 10;
      String domain = PersonDefaults.PERSON_DOMAIN;
      properties.setProperty(domain, "name", name);
      properties.setProperty(domain, "address", address);
      properties.setProperty(domain, "age", age);

      // Test load with custom values and custom loader
      // This will instruct the loader to use the person class itself
      // to load the person because this class provides a provider constructor
      // and a static provider method (this will be used in this case)
      context.setProviderClass(PersonMock1.class, PersonMock1.class);
      PersonMock1 person = context.load(PersonMock1.class);
      assertEquals(name, person.getName());
      assertEquals(age, person.getAge());
      assertEquals(address, person.getAddress());
      // here the partner should be  null because its not injected
      // by the provider
      assertNull(person.getPartner());
      
      // Test the injection of partner
      // First specify the partner provider
      context.setProviderClass(PersonLoader.class, PersonMock2.class);
      // The partner will be injected and other properties to
      context.inject(person);
      assertEquals(name, person.getName());
      assertEquals(age, person.getAge());
      assertEquals(address, person.getAddress());
      // The partner will now have the same values as person
      PersonMock2 partner = person.getPartner();
      assertNotNull(partner);
      assertEquals(person.getName(), partner.getName());
      assertEquals(person.getAge(), partner.getAge());
      
      properties.setProperty(domain, "name", null);
      properties.setProperty(domain, "address", null);
      properties.setProperty(domain, "age", null);
      context.setProviderClass((Class<?>)null,  PersonMock1.class);
      context.setProviderClass((Class<?>)null,  PersonMock2.class);
   }
}
