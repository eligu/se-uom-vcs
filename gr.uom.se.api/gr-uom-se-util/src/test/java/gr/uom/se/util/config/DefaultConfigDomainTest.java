/**
 * 
 */
package gr.uom.se.util.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
      
      domain.setProperty(name, value);
      assertEquals(1, listener.changed.size());
      
      assertEquals(value, listener.changed.get(name));
      
      ddomain.removeChangeListener(listener);
      name = "anotherProperty";
      value = "anotherValue";
      ddomain.setProperty(name, value);
      // Ensure that no other change was received
      assertEquals(1, listener.changed.size());
      
      // Test listener for specific property
      ddomain.addChangeListener(name, listener);
      // Clear listener
      listener.changed.clear();
      
      value = "a value";
      domain.setProperty(name, value);
      
      assertEquals(1, listener.changed.size());
      assertEquals(value, listener.changed.get(name));
      
      domain.setProperty("property1", value);
      assertEquals(1, listener.changed.size());
      assertEquals(value, listener.changed.get(name));
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
