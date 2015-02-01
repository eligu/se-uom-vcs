/**
 * 
 */
package gr.uom.se.util.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public class DefaultConfigDomainTest {

   static ConfigManager config = null;

   @BeforeClass
   public static void initTests() {
      config = new DefaultConfigManager();
   }

   @Before
   public void initDefaultDomain() {
      config.setProperty(ConfigConstants.DEFAULT_CONFIG_FOLDER_PROPERTY,
            "src/test/resources/config");
   }

   @Test
   public void testCreationOfDefaultDomain() {
      DefaultConfigDomain domain = config.loadDomain(DefaultConfigDomain.class);
      assertNotNull(domain);
      testDefaultConfigValues(domain);
   }

   @Test
   public void testCreationOfDefaultDomainByName() {
      config.loadDomain(ConfigConstants.DEFAULT_CONFIG_DOMAIN);
      ConfigDomain domain = config
            .getDomain(ConfigConstants.DEFAULT_CONFIG_DOMAIN);
      assertNotNull(domain);
      testDefaultConfigValues(domain);
   }

   @Test
   public void testPropertyListener() {
      config.loadDomain(ConfigConstants.DEFAULT_CONFIG_DOMAIN);
      ConfigDomain domain = config
            .getDomain(ConfigConstants.DEFAULT_CONFIG_DOMAIN);
      assertNotNull(domain);

      // Test global listener
      ConfigDomainListener listener = new ConfigDomainListener();
      DefaultConfigDomain ddomain = (DefaultConfigDomain) domain;
      ddomain.addChangeListener(listener);

      String name = "newProperty";
      String value = "neValue";

      // Test the listener for the change of the property
      domain.setProperty(name, value);
      assertEquals(1, listener.changed.size());
      assertEquals(value, listener.changed.get(name));
      assertEquals(value, domain.getProperty(name));
      // Listner should be notified here
      assertTrue(listener.notified);

      // Test that the listener is removed successfully
      ddomain.removeChangeListener(listener);
      
      String oldName = name;
      String oldValue = value;
      name = "anotherProperty";
      value = "anotherValue";
      
      listener.notified = false;
      ddomain.setProperty(name, value);
      
      // Ensure that no other change was received
      assertEquals(1, listener.changed.size());
      assertEquals(oldValue, listener.changed.get(oldName));
      assertEquals(null, listener.changed.get(name));
      assertEquals(oldValue, domain.getProperty(oldName));
      assertEquals(value, domain.getProperty(name));
      assertFalse(listener.notified);

      // Test listener for specific property
      ddomain.addChangeListener(name, listener);
      // Clear listener
      listener.changed.clear();
      
      value = "a value";
      listener.notified = false;
      domain.setProperty(name, value);

      assertEquals(1, listener.changed.size());
      assertEquals(value, listener.changed.get(name));
      assertTrue(listener.notified);
      
      // Check that listener of a specific property
      // doesn't listen to changes for other properties
      listener.notified = false;
      domain.setProperty("property1", value);
      assertEquals(1, listener.changed.size());
      assertEquals(value, listener.changed.get(name));
      assertEquals(value, domain.getProperty(name));
      assertEquals(value, domain.getProperty("property1"));
      assertEquals(null, listener.changed.get("property1"));
      assertFalse(listener.notified);
      
      // Test that listener doesn't get any new event
      // if the old value of the property is the same
      // as the new value
      domain.setProperty(name, value);
      domain.setProperty(name, value);
      
      assertEquals(1, listener.changed.size());
      assertEquals(value, listener.changed.get(name));
      assertFalse(listener.notified);
      
      // Test for null values which can
      // be notified
      domain.setProperty(name, null);
      
      assertEquals(1, listener.changed.size());
      assertEquals(null, domain.getProperty(name));
      assertEquals(null, listener.changed.get(name));
      assertTrue(listener.notified);
      
      // Test again when sending a null value
      // it will not send an event as the old value is
      // still null
      listener.notified = false;
      domain.setProperty(name, null);
      
      assertEquals(1, listener.changed.size());
      assertEquals(null, domain.getProperty(name));
      assertEquals(null, listener.changed.get(name));
      assertFalse(listener.notified);
   }

   private void testDefaultConfigValues(ConfigDomain domain) {
      assertEquals("This is a string", domain.getProperty("strProp"));
      assertEquals("20", domain.getProperty("intProp"));
      assertEquals("23.02", domain.getProperty("dblProp"));
      assertEquals("30.1", domain.getProperty("fltProp"));
      assertEquals("20", domain.getProperty("srtProp"));
      assertEquals("2333333222", domain.getProperty("lngProp"));
      assertEquals(ConfigConstants.DEFAULT_CONFIG_DOMAIN, domain.getName());
   }
}
