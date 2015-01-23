/**
 * 
 */
package gr.uom.se.util.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import gr.uom.se.util.config.ConfigConstants;
import gr.uom.se.util.config.ConfigDomain;
import gr.uom.se.util.config.ConfigManager;
import gr.uom.se.util.config.DefaultConfigDomain;
import gr.uom.se.util.config.DefaultConfigManager;
import gr.uom.se.util.module.ModuleLoader;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public class DefaultConfigDomainTest {

   static ModuleLoader moduleLoader = null;
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
