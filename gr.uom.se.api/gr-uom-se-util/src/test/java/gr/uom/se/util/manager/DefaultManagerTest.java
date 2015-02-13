package gr.uom.se.util.manager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
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
   static AbstractMainManager mainManager = null;

   @BeforeClass
   public static void initTests() {
      configManager = new DefaultConfigManager();
      moduleManager = new DefaultModuleManager(configManager);
      mainManager = new DefaultMainManager(moduleManager, configManager);

      // Set the default config folder where the config files will be
      // looked for, just to initialize the config manager
      configManager.setProperty(ConfigConstants.DEFAULT_CONFIG_FOLDER_PROPERTY,
            "src/test/resources/config");

      mainManager.startManager(MainManager.class);
   }

   @Test
   public void testManagerRetrievial() {

      ConfigManager cm = mainManager.getManager(ConfigManager.class);
      ModuleManager mm = mainManager.getManager(ModuleManager.class);
      MainManager ma = mainManager.getManager(AbstractMainManager.class);

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

      ModuleLoader loader = mm.getLoader(DBConnectionImp.class);
      assertNotNull(loader);

      DBConnection conn = loader.load(DBConnectionImp.class);
      assertNotNull(conn);

      assertEquals(mainManager, conn.getManager());

      ConnectionConfig cc = conn.getConfig();
      assertNotNull(cc);
      assertEquals(4444, cc.getPort());
      assertEquals("elvis", cc.getUsername());
      assertEquals("123456", cc.getPassword());
      assertEquals("org.example.jdbc.JDriver", cc.getJdbcDriver());
   }

   @Test
   public void testManagerAndBeanWithConfig() {
      // Using configurations we will be able to work only with
      // interfaces avoiding the need for a creating instances
      // by hand. We will also be able to register managers
      // automatically. All the configurations are created
      // on disk by setting up the config files for manager
      // and for module dependencies
      ConfigManager config = mainManager.getManager(ConfigManager.class);
      assertNotNull(config);

      // We should read the db config from here
      config.loadDomain("dbconfig");

      // Now the db manager should be resolved
      DBManager dbManager = mainManager.getManager(DBManager.class);
      assertNotNull(dbManager);

      // The connection should have been loaded automatically
      // from the loader
      DBConnection conn = dbManager.getConnection();
      assertNotNull(conn);

      // Now check the defaults for the connection
      MainManager mainM = conn.getManager();
      assertEquals(mainManager, mainM);

      ConnectionConfig cc = conn.getConfig();
      assertNotNull(cc);
      assertEquals(4444, cc.getPort());
      assertEquals("elvis", cc.getUsername());
      assertEquals("123456", cc.getPassword());
      assertEquals("org.example.jdbc.JDriver", cc.getJdbcDriver());
   }

   @Test
   public void testActivator() {
      ActivatorManager am = mainManager.getManager(DefaultActivatorManager.class);
      assertNotNull(am);

      am.activate(Activator1.class);
      am.activate(Activator2.class);
      am.activate(Activator3.class);
      assertEquals(1, Activator2.count);

      boolean thrown = false;
      try {
         am.activate(Activator4.class);
      } catch (IllegalArgumentException ex) {
         thrown = true;
      }
      assertTrue(thrown);

   }
}
