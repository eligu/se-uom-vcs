package gr.uom.se.util.manager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import gr.uom.se.util.config.ConfigConstants;
import gr.uom.se.util.config.ConfigManager;
import gr.uom.se.util.config.DefaultConfigManager;
import gr.uom.se.util.context.Context;
import gr.uom.se.util.context.ContextManager;
import gr.uom.se.util.context.ContextProvider;
import gr.uom.se.util.module.DefaultModuleManager;
import gr.uom.se.util.module.FastDynamicModulePropertyLocator;
import gr.uom.se.util.module.ModuleContext;
import gr.uom.se.util.module.ModuleLoader;
import gr.uom.se.util.module.ModuleManager;
import gr.uom.se.util.module.PropertyInjector;
import gr.uom.se.util.property.DomainPropertyHandler;
import gr.uom.se.util.property.PropertyUtils;

import java.util.HashMap;
import java.util.Map;

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
      checkDefault(dbManager);
   }

   private void checkDefault(DBManager db) {
      // The connection should have been loaded automatically
      // from the loader
      DBConnection conn = db.getConnection();
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
      assertEquals(conn.getClass(), DBConnectionImp.class);
   }

   @Test
   public void testManagerAndBeanWithProperties() {
      // Will inject these properties during the loading process to
      // override the config properties
      Map<String, Map<String, Object>> properties = new HashMap<>();
      String domain = "dbconfig";

      // Using configurations we will be able to work only with
      // interfaces avoiding the need for a creating instances
      // by hand. We will also be able to register managers
      // automatically. All the configurations are created
      // on disk by setting up the config files for manager
      // and for module dependencies
      ConfigManager config = mainManager.getManager(ConfigManager.class);
      assertNotNull(config);

      // We should read the db config from here
      // However the properties will not be the same as those from db config
      // we just load this to ensure that it will not be read by parameter
      // provider
      config.loadDomain(domain);

      // Set up the properties to be injected
      Map<String, Object> dbconfig = new HashMap<>();
      String port = "1045";
      String username = "default";
      String password = "userpass";
      String driver = "com.example.jdbc.Driver";
      dbconfig.put("port", port);
      dbconfig.put("username", username);
      dbconfig.put("password", password);
      dbconfig.put("jdbcDriver", driver);
      properties.put(domain, dbconfig);

      ModuleManager modules = mainManager.getManager(ModuleManager.class);
      assertNotNull(modules);
      ModuleLoader loader = modules.getLoader(DBManager.class);
      assertNotNull(loader);
      // Here we should expect that db manager will load with properties
      // that is the loaded dbconfig from file will be ignored
      DBManager db = loader.load(DBManager.class, properties);
      assertNotNull(db);

      ConnectionConfig cconfig = db.getConnection().getConfig();
      assertNotNull(cconfig);
      assertEquals(Integer.parseInt(port), cconfig.getPort());
      assertEquals(username, cconfig.getUsername());
      assertEquals(password, cconfig.getPassword());
      assertEquals(driver, cconfig.getJdbcDriver());

      // Second test with dynamic property locator
      // This should look first into the properties map in order
      // to retrieve properties
      FastDynamicModulePropertyLocator locator = new FastDynamicModulePropertyLocator(
            properties);

      db = loader.load(DBManager.class, locator);
      assertNotNull(db);

      cconfig = db.getConnection().getConfig();
      assertNotNull(cconfig);
      assertEquals(Integer.parseInt(port), cconfig.getPort());
      assertEquals(username, cconfig.getUsername());
      assertEquals(password, cconfig.getPassword());
      assertEquals(driver, cconfig.getJdbcDriver());

      // Test with dynamic context
      DomainPropertyHandler handler = PropertyUtils.newHandler(properties);
      // Specify the new properties
      port = "1099";
      username = "db_user";
      password = "db_pass";
      driver = "gr.uom.jdbc.Driver";
      handler.setProperty(domain, "port", port);
      handler.setProperty(domain, "username", username);
      handler.setProperty(domain, "password", password);
      handler.setProperty(domain, "jdbcDriver", driver);

      // Specify the new provider for db connection
      ModuleContext context = modules.newDynamicContext(null, handler);
      context.setProviderClass(DBConnectionDynamic.class, DBConnection.class);
      // Load db manager
      db = context.load(DBManager.class);
      assertNotNull(db);

      // Check that the properties are injected successfully
      DBConnection connection = db.getConnection();
      assertNotNull(connection);
      cconfig = connection.getConfig();
      assertNotNull(cconfig);
      assertEquals(Integer.parseInt(port), cconfig.getPort());
      assertEquals(username, cconfig.getUsername());
      assertEquals(password, cconfig.getPassword());
      assertEquals(driver, cconfig.getJdbcDriver());

      // Check the type of connection it should be dynamic
      assertEquals(DBConnectionDynamic.class, connection.getClass());

      // Assert that managers are inserted
      assertEquals(connection.getManager(), mainManager);

      // Now check that the previous properties are not
      // affected by dynamic operations
      db = loader.load(DBManager.class);
      checkDefault(db);
   }

   
   @Test
   public void testActivator() {
      ActivatorManager am = mainManager.getManager(ModuleManager.class)
            .getLoader(ActivatorManager.class).load(ActivatorManager.class);
      mainManager.registerLoaded(am);
      mainManager.startManager(ActivatorManager.class);
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

   @Test
   public void testContext() {
      // check the defaults for context manager
      assertTrue(mainManager.isRegistered(ContextManager.class));
      assertFalse(mainManager.isLoaded(ContextManager.class));
      assertFalse(mainManager.isStarted(ContextManager.class));
      // Get the context manager
      ContextManager contextManager = mainManager.getManager(ContextManager.class);
      // Look up the context provider for the Bean class
      ContextProvider provider = contextManager.getProvider(Bean.class);
      assertNotNull(provider);
      
      // Check that provider is of the desired type
      assertEquals(TestContextProvider.class, provider.getClass());
      
      // Check that the returned context is the desired type
      Context context = provider.getContext((Class<?>)null, null);
      assertNotNull(context);
      assertEquals(Bean.BeanContext.class, context.getClass());
      
      // Check that the manager will return the same provider
      assertEquals(provider, contextManager.getProvider(Bean.class));
      
      // Check the resolving of contexts from manager
      assertEquals(context, contextManager.lookupContext(Bean.class, null));
   }
}
