/**
 * 
 */
package gr.uom.se.util.manager;

import gr.uom.se.util.config.ConfigDomain;
import gr.uom.se.util.config.ConfigManager;
import gr.uom.se.util.config.DefaultConfigManager;
import gr.uom.se.util.manager.annotations.Init;
import gr.uom.se.util.module.DefaultModuleManager;
import gr.uom.se.util.module.ModuleManager;
import gr.uom.se.util.module.annotations.Property;
import gr.uom.se.util.reflect.ReflectionUtils;

import java.util.Map;

/**
 * The default manager implementation usually used in a non container
 * environment.
 * <p>
 * This manager will wait for a {@link ModuleManager} implementation and for a
 * {@link ConfigManager} implementation. If one of them is not provided it will
 * create the corresponding default implementation. The two provided managers
 * will be registered as managers within this manager, also this manager would
 * register itself as a manager. It will wrap the default parameter provider
 * returned by {@link ModuleManager} into an instance of {@link ManagerProvider}
 * which will provide to different callers the managers when they are required.
 * Note that when a value for parameter type is requested the provider will
 * check if that type is a manager and will try to look the manager at this
 * instance, that means different property annotations of the parameter will be
 * avoided. However when the given parameter type is not a registered manager it
 * will not try to resolve this as a manager but will call the default parameter
 * provider. Note that, changing the default parameter provider may cause the
 * injection of managers into modules to have problems. Though, it can work in
 * case the given parameter has a property annotation that describe where the
 * parameter can be stored. In order to ensure that a change of the default
 * parameter provider would not cause injection of manager problems, try to
 * annotate all the manager parameters by indicating the possible place where
 * the manager can be retrieved, and build the different modules with injected
 * annotated parameters.
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
@Property(domain = ManagerConstants.DEFAULT_DOMAIN, name = ManagerConstants.DEFAULT_MANAGER_PROPERTY)
public class DefaultMainManager extends AbstractMainManager {

   /**
    * The module manager that is required by this manager to load other
    * managers.
    * <p>
    */
   private ModuleManager moduleManager;

   /**
    * Used as a cache when initializing this manager.
    */
   private ConfigManager config;

   /**
    * Create an instance given the two managers it requires.
    * <p>
    * If one of the managers is not provided it will be created as a default
    * implementation and will be registered within this manager.
    * 
    * @param moduleManager
    *           used to load the other managers
    * @param config
    *           to look up properties when working with module loader
    */
   public DefaultMainManager(ModuleManager moduleManager, ConfigManager config) {
      if (config == null) {
         config = new DefaultConfigManager();
      }
      if (moduleManager == null) {
         moduleManager = new DefaultModuleManager(config);
      }
      // Set first the module manager
      // because it will be used when registering
      // the two other managers
      this.moduleManager = moduleManager;
      // Set the config just to cache it when initializing this
      // manager
      this.config = config;

      // Register first the default managers
      registerDefaultManagers();
   }

   /**
    * Register the config manager, the module manager and this manager to itself
    * in order for them to be looked up by a parameter provider when they are
    * required. Also they should be looked up by calling this manager get()
    * method.
    */
   private void registerDefaultManagers() {
      // Register the config first as it will be required in all cases
      // by module manager
      registerLoaded(config);
      // Register the module manager
      // so he can be injected to other modules
      registerLoaded(moduleManager);
      // Register this manager to itself so he can be injected
      // to other modules
      registerLoaded(this);
   }

   /**
    * Load the default domain of managers.
    */
   private void loadDefaultDomain() {
      String domain = getDefaultDomain();
      try {
         config.loadDomain(domain);
      } catch (Exception e) {
         // TODO this line must be checked for consistency
         // Probably a logger would be a better idea
         System.err.println("Default domain " + domain
               + " could not be loaded. Reason: " + e.getMessage());
      }
   }

   /**
    * Return the default domain of managers by looking it at the config manager.
    */
   private String getDefaultDomain() {
      String domain = this.config.getProperty(
            ManagerConstants.DEFAULT_DOMAIN_PROPERTY, String.class);
      if (domain == null) {
         domain = ManagerConstants.DEFAULT_DOMAIN;
      }
      return domain;
   }

   /**
    * Will register the defined managers at managers configuration domain.
    * <p>
    */
   private void registerDomainManagers() {
      // Get the default managers domain
      String domainName = getDefaultDomain();
      ConfigDomain domain = config.getDomain(domainName);

      // Extract all properties with the prefix "manager."
      Map<String, Object> managers = domain
            .getPropertiesWithPrefix(ManagerConstants.MANAGER_PREFIX);

      int prefixLen = ManagerConstants.MANAGER_PREFIX.length();

      // Do for each manager declaration in config
      for (String name : managers.keySet()) {

         // Get the class name of the manager
         // When a property within managers domain has a prefix
         // with "manager." the following part should be the
         // fully qualified name of manager class or interface
         // we are declaring
         String className = name.substring(prefixLen);

         // Tell module manager to register the defaults
         // (provider, parameter provider, loader, property injector)
         // however do not override any of the default if they
         // are already specified. I.e if there are defined defaults
         // in module's domain then it takes precedence
         moduleManager.registerDefaultsForModule(className,
               domain.getProperties());

         try {
            // Try to look up the class
            Class<?> managerClass = Class.forName(className);

            // The defined manager should be concrete otherwise we
            // can not resolve manager dependencies if we load it using its
            // interface and then register it, because all other managers are
            // not started or are not registered at all
            if (!ReflectionUtils.isConcrete(managerClass)) {
               throw new RuntimeException(
                     "The defined managers should be concrete classes in order to register them");

            }
            registerManager(managerClass);

         } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
         }
      }
   }

   @Init
   public void init() {

      // Start the config manager to load the default config if any
      startManager(ConfigManager.class);
      // Start the module manager to load manager defaults
      startManager(ModuleManager.class);

      // Now we are safe to create a default parameter provider
      // as this will allow new modules to be injected the managers
      // Create a new parameter provider that can handle
      // the injection of managers to other modules
      ManagerProvider provider = new ManagerProvider(
            moduleManager.getParameterProvider(MainManager.class), this);
      // Set this provider as the default provider
      this.moduleManager.registerDefaultParameterProvider(provider);

      // Load the default domain of managers
      loadDefaultDomain();

      // Register the managers that are described at default domain
      registerDomainManagers();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected ModuleManager getModuleManager() {
      return moduleManager;
   }
}
