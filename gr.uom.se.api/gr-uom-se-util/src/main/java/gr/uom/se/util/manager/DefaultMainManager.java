/**
 * 
 */
package gr.uom.se.util.manager;

import gr.uom.se.util.config.ConfigManager;
import gr.uom.se.util.config.DefaultConfigManager;
import gr.uom.se.util.module.DefaultModuleManager;
import gr.uom.se.util.module.ModuleManager;
import gr.uom.se.util.module.annotations.Property;

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
 * the manager can be retrieved.
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
      // Register the config first as it will be required in all cases
      // by module manager
      registerLoaded(config);
      // Register the module manager
      // so he can be injected to other modules
      registerLoaded(moduleManager);
      // Register this manager to itself so he can be injected
      // to other modules
      registerLoaded(this);
      // Now create a new parameter provider that can handle
      // the injection of managers to other modules
      ManagerProvider provider = new ManagerProvider(
            moduleManager.getParameterProvider(ManagerProvider.class), this);
      // Set this provider as the default provider
      this.moduleManager.registerDefaultParameterProvider(provider);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected ModuleManager getModuleManager() {
      return moduleManager;
   }
}
