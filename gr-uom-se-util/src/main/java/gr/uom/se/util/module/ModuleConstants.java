/**
 * 
 */
package gr.uom.se.util.module;

/**
 * A class containing different constant values used by module system.
 * <p>
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public class ModuleConstants {

   /**
    * The property name for the default module loader.
    * <p>
    */
   public static final String DEFAULT_MODULE_LOADER_PROPERTY = "defaultModuleLoader";

   /**
    * The property name for the default module loader's class.
    * <p>
    */
   public static final String DEFAULT_MODULE_LOADER_CLASS_PROPERTY = "ModuleLoader.class";

   /**
    * The property name for the default module config.
    * <p>
    */
   public static final String DEFAULT_MODULE_CONFIG_DOMAIN = "gr.uom.se.util.module";

   /**
    * The property name for the module loader.
    * <p>
    */
   public static final String LOADER_PROPERTY = "moduleLoader";

   /**
    * The property name for the module provider.
    * <p>
    */
   public static final String PROVIDER_PROPERTY = "moduleProvider";

   /**
    * The name of the property that stores the configuration manager to be
    * resolved.
    * <p>
    */
   public static final String CONFIG_MANAGER_PROPERTY = "configManager";

   /**
    * The name of the property where the default parameter provider is stored.
    * <p>
    */
   public static final String DEFAULT_PARAMETER_PROVIDER_PROPERTY = "defaultParameterProvider";

   /**
    * The name of the property where the default property injector will be
    * stored.
    * <p>
    */
   public static final String DEFAUL_PROPERTY_INJECTOR_PROPERTY = "defaultPropertyInjector";

   /**
    * Get the default config domain for the given class.
    * <p>
    * This will return the fully qualified name of the given class.
    * 
    * @param clazz
    * @return
    */
   public static String getDefaultConfigFor(Class<?> clazz) {
      return clazz.getName();
   }

   /**
    * Get the name of the property where the system can find the provider of the
    * given class.
    * <p>
    * By default the provider is stored at a property named same as the fully
    * qualified name of the class it provides appended .moduleProvider.
    * 
    * @param clazz
    * @return
    */
   public static String getProviderNameFor(Class<?> clazz) {
      return clazz.getName() + "." + PROVIDER_PROPERTY;
   }
}
