package gr.uom.se.util.manager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import gr.uom.se.util.config.ConfigManager;
import gr.uom.se.util.config.DefaultConfigManager;
import gr.uom.se.util.module.DefaultModuleManager;
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
}
