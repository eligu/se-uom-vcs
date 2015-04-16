/**
 * 
 */
package gr.uom.se.util.context;

import gr.uom.se.util.config.ConfigDomain;
import gr.uom.se.util.config.ConfigManager;
import gr.uom.se.util.manager.MainManager;
import gr.uom.se.util.manager.ManagerConstants;
import gr.uom.se.util.manager.annotations.Init;
import gr.uom.se.util.module.ModuleManager;
import gr.uom.se.util.module.annotations.ProvideModule;
import gr.uom.se.util.validation.ArgsCheck;

import java.util.Map;
import java.util.logging.Logger;

/**
 * The default context manager implementation based on abstract context manager
 * implementation.
 * <p>
 * This context manager is designed to work with managers API and modules API.
 * This can be set up to be loaded by managers API by specifying 'manager.'
 * property into managers config. When this manager is initialized it will try
 * to load all context providers that are registered in managers domain config.
 * A context provider should be registered in manager's configuration domain by
 * specifying the provider {@linkplain ContextConstants#PROVIDER_PREFIX prefix}
 * to a context provider class name. Note that each implementation of context
 * provider will be loaded using modules API so any implementation that doesn't
 * adhere to modules API rules will cause this manager to throw an exception
 * during initialization.
 * 
 * @author Elvis Ligu
 */
public class DefaultContextManager extends AbstractContextManager {

   private static final Logger logger = Logger
         .getLogger(DefaultContextManager.class.getName());

   /**
    * The main manager, which will be injected at the constructor by the module
    * loader.
    */
   private final MainManager mainManager;

   /**
    * Create a default context manager by passing a main manager.
    * <p>
    * Generally speaking this manager should be created by modules API and will
    * be automatically loaded by configuration files, when it is requested from
    * main manager. Therefore the main manager instance here should be injected
    * by the parameter provider.
    * 
    * @param manager
    *           the main manager from where to resolve config manager, modules
    *           manager etc. Must not be null.
    */
   @ProvideModule
   public DefaultContextManager(MainManager manager) {
      ArgsCheck.notNull("manager", manager);
      this.mainManager = manager;
   }

   @Init
   public void init() throws ClassNotFoundException {
      // Initialize all context providers
      ConfigManager config = mainManager.getManager(ConfigManager.class);
      ArgsCheck.notNull("config manager", config);

      // Resolve managers' domain
      String domain = config.getProperty(
            ManagerConstants.DEFAULT_DOMAIN_PROPERTY, String.class);
      if (domain == null) {
         domain = ManagerConstants.DEFAULT_DOMAIN;
      }

      // Get the config domain
      ConfigDomain cfgd = config.getDomain(domain);
      if (cfgd == null) {
         logger.warning("manager's domain could not be found");
         return;
      }

      // Get all context provider properties
      Map<String, Object> props = cfgd
            .getPropertiesWithPrefix(ContextConstants.PROVIDER_PREFIX);
      // Look all properties with prefix 'contextProvider.'
      // for each such property resolve the class name which
      // should be after the prefix and load it
      int prefixLen = ContextConstants.PROVIDER_PREFIX.length();
      ModuleManager modules = mainManager.getManager(ModuleManager.class);
      ArgsCheck.notNull("modules manager", modules);
      for (String key : props.keySet()) {
         String className = key.substring(prefixLen);
         // Load the class with the given name and check if
         // it is a provider class
         Class<?> providerClass = Class.forName(className);
         ArgsCheck.isSubtype("provider class", ContextProvider.class,
               providerClass);
         // Load the provider instance using modules API
         // and register it
         Object instance = modules.getLoader(providerClass).load(providerClass);
         ContextProvider provider = (ContextProvider) instance;
         logger.info("Registering initial provider: " + provider);
         this.registerProvider(provider);
      }
   }
}
