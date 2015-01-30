package gr.uom.se.util.manager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import gr.uom.se.util.config.ConfigConstants;
import gr.uom.se.util.config.ConfigManager;
import gr.uom.se.util.config.DefaultConfigManager;
import gr.uom.se.util.module.DefaultModuleManager;
import gr.uom.se.util.module.ModuleLoader;
import gr.uom.se.util.module.ModuleManager;
import gr.uom.se.util.module.PropertyInjector;

import org.junit.BeforeClass;
import org.junit.Test;

public class DefaultManagerTest {

   static ConfigManager configManager = null;
   static ModuleManager moduleManager = null;
   static AbstractManager mainManager = null;

   @BeforeClass
   public static void initTests() {
      configManager = new DefaultConfigManager();
      moduleManager = new DefaultModuleManager(configManager);
      mainManager = new DefaultManager(moduleManager, configManager);
      
      // Set the default config folder where the config files will be
      // looked for
      configManager.setProperty(ConfigConstants.DEFAULT_CONFIG_FOLDER_PROPERTY,
            "src/test/resources/config");
   }

   @Test
   public void testManagerRetrievial() {

      ConfigManager cm = mainManager.getManager(ConfigManager.class);
      ModuleManager mm = mainManager.getManager(ModuleManager.class);
      AbstractManager ma = mainManager.getManager(AbstractManager.class);

      assertNotNull(cm);
      assertNotNull(mm);
      assertNotNull(ma);
      assertEquals(configManager, cm);
      assertEquals(moduleManager, mm);
      assertEquals(mainManager, ma);

      mainManager.registerManager(DefaultDummyManager.class);
      DummyManager dummyManager = mainManager.getManager(DummyManager.class);

      assertNotNull(dummyManager);
      assertNotNull(dummyManager.getManager());
      assertEquals(mainManager, dummyManager.getManager());

      DummyManager dummyManager2 = mainManager.getManager(DummyManager.class);
      assertEquals(dummyManager, dummyManager2);
   }

   @Test
   public void testProviderOfManagers() {
      ModuleManager mm = mainManager.getManager(ModuleManager.class);
      assertNotNull(mm);

      Bean bean = mm.getLoader(Bean.class).load(Bean.class);
      assertNotNull(bean);
      assertEquals(configManager, bean.configManager);
      assertEquals(moduleManager, bean.moduleManager);
      assertEquals(mainManager, bean.mainManager);
   }

   @Test
   public void testInjectionOfManagers() {

      Bean bean = new Bean(null, null, null);
      ModuleManager mm = mainManager.getManager(ModuleManager.class);
      assertNotNull(mm);

      PropertyInjector injector = mm.getPropertyInjector(Bean.class);
      assertNotNull(injector);
      
      injector.injectProperties(bean);

      assertEquals(configManager, bean.configManager);
      assertEquals(moduleManager, bean.moduleManager);
      assertEquals(mainManager, bean.mainManager);
   }
   
   @Test
   public void testBeanDependecies() {
      
      ModuleManager mm = mainManager.getManager(ModuleManager.class);
      assertNotNull(mm);
      
      ConfigManager config = mainManager.getManager(ConfigManager.class);
      assertNotNull(config);
      
      config.loadDomain("dbconfig");
      
      ModuleLoader loader = mm.getLoader(DBConnection.class);
      assertNotNull(loader);
      
      DBConnection conn = loader.load(DBConnection.class);
      assertNotNull(conn);
      
      assertEquals(mainManager, conn.getManager());
      
      ConnectionConfig cc = conn.getConfig();
      assertNotNull(cc);
      assertEquals(4444, cc.getPort());
      assertEquals("elvis", cc.getUsername());
      assertEquals("123456", cc.getPassword());
      assertEquals("org.example.jdbc.JDriver", cc.getJdbcDriver());
   }
}
