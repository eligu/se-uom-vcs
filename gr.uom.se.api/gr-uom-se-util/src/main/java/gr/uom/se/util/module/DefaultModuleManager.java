/**
 * 
 */
package gr.uom.se.util.module;

import gr.uom.se.util.config.AbstractConfigManager;
import gr.uom.se.util.config.ConfigManager;
import gr.uom.se.util.config.DefaultConfigManager;
import gr.uom.se.util.manager.ManagerConstants;
import gr.uom.se.util.manager.annotations.Init;
import gr.uom.se.util.module.annotations.Property;

/**
 * Default implementation of module manager.
 * <p>
 * This module manager implementation is based on {@link AbstractModuleManager}
 * and it only provides the implementation for {@link #getConfig()} method to
 * provide the module manager a config manager implementation. If a config
 * manager is not provided during the construction of this manager then it will
 * use the {@link AbstractConfigManager}, which will be created in memory
 * without a configuration.
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
@Property(domain = ManagerConstants.DEFAULT_DOMAIN, name = ModuleConstants.DEFAULT_MODULE_MANAGER_PROPERTY)
public class DefaultModuleManager extends AbstractModuleManager {

   /**
    * The config manager that should be used by this manager.
    */
   private ConfigManager manager;
   /**
    * A flag to check if the config manager was provided or created by this
    * manager.
    */
   private boolean created = false;
   
   /**
    * Create an instance of this manager without initializing it.
    * <p>
    * Clients must call {@link #init()} before they use this manager. If the
    * config manager provided is not null the client must initialize it first
    * before the initialization of this manager.
    * 
    * @param manager
    *           the config manager, may be null.
    */
   public DefaultModuleManager(ConfigManager manager) {
      if (manager == null) {
         manager = new DefaultConfigManager();
         created = true;
      }
      this.manager = manager;
   }

   /**
    * If the config manager was not provided when this manager was created, but
    * was created by this manager then init it.
    */
   private void initConfig() {
      if (created) {
         DefaultConfigManager manager = (DefaultConfigManager) this.manager;
         manager.init();
      }
   }

   /**
    * Get the default domain of modules.
    * 
    * @return
    */
   private String getDefaultDomain() {
      String domain = manager
            .getProperty(ModuleConstants.DEFAULT_MODULE_CONFIG_DOMAIN_PROPERTY,
                  String.class);
      if (domain == null) {
         domain = ModuleConstants.DEFAULT_MODULE_CONFIG_DOMAIN;
      }
      return domain;
   }

   /**
    * Will try to load the default domain of modules without throwing an
    * exception if it can not be loaded.
    */
   private void initDefaultDomain() {
      String domain = getDefaultDomain();
      try {
         this.manager.loadDomain(domain);
      } catch (Exception e) {
         // TODO this line must be checked for consistency
         // Probably a logger would be a better idea
         System.err.println("Default domain " + domain
               + " could not be loaded. Reason: " + e.getMessage());
      }
   }

   /**
    * This method will register the default loader, parameter provider and
    * injector.
    */
   private void initDefaults() {
      // Register the default loader
      ModuleLoader loader = this.getLoader(ModuleManager.class);
      this.registerDefaultLoader(loader);
      // Register the default parameter provider
      ParameterProvider provider = this
            .getParameterProvider(ModuleManager.class);
      this.registerDefaultParameterProvider(provider);
      // Register the default bean injector
      PropertyInjector injector = this.getPropertyInjector(ModuleManager.class);
      this.registerDefaultPropertyInjector(injector);
   }

   /**
    * Initialize this manager by loading its default domain, if any and
    * registering the default implementations.
    * <p>
    * WARNING: if the config manager was provided during the creation of this
    * manager then the client must ensure that the config manager was
    * initialized before this method is called.
    */
   @Init
   public void init() {
      // Try to init the default config if the config manager
      // was not provided
      initConfig();

      // Try to init the default domain for this manager
      initDefaultDomain();

      // Init defaults
      initDefaults();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected ConfigManager getConfig() {
      return manager;
   }
}
