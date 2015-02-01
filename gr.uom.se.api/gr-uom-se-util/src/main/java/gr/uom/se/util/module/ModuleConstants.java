/**
 * 
 */
package gr.uom.se.util.module;

/**
 * A class containing different constant values used by module API.
 * <p>
 * 
 * @author Elvis Ligu
 * @version 0.0.1
 * @since 0.0.1
 */
public class ModuleConstants {

   /**
    * The property name for the default module config.
    * <p>
    */
   public static final String DEFAULT_MODULE_CONFIG_DOMAIN = "gr.uom.se.util.module";
   
   /**
    * The name of the property where the module manager should be saved.
    * <p>
    */
   public static final String DEFAULT_MODULE_MANAGER_PROPERTY = "moduleManager";

   /**
    * The property name for the module loader.
    * <p>
    */
   public static final String LOADER_PROPERTY = "moduleLoader";

   /**
    * The property name for the default module loader's class.
    * <p>
    */
   public static final String LOADER_CLASS_PROPERTY = "moduleLoader.class";

   /**
    * The property name for the module provider.
    * <p>
    */
   public static final String PROVIDER_PROPERTY = "moduleProvider";

   /**
    * The property name for the module provider class.
    * <p>
    */
   public static final String PROVIDER_CLASS_PROPERTY = "moduleProvider.class";

   /**
    * The name of the property where a parameter provider for a given module is
    * stored.
    * <p>
    */
   public static final String PARAMETER_PROVIDER_PROPERTY = "parameterProvider";

   /**
    * The name of the property where a parameter provider for a given module is
    * stored.
    * <p>
    */
   public static final String PARAMETER_PROVIDER_CLASS_PROPERTY = "parameterProvider.class";

   /**
    * The name of the property that stores the configuration manager to be
    * resolved.
    * <p>
    */
   public static final String CONFIG_MANAGER_PROPERTY = "configManager";

   /**
    * The name of the property where the default property injector will be
    * stored.
    * <p>
    */
   public static final String PROPERTY_INJECTOR_PROPERTY = "propertyInjector";

   /**
    * The name of the property where a property injector for a given module is
    * stored.
    * <p>
    */
   public static final String PROPERTY_PROVIDER_CLASS_PROPERTY = "propertyInjector.class";

   /**
    * The name of the property where the default mapper factory will be stored.
    * <p>
    */
   public static final String DEFAULT_MAPPER_FACTORY_PROPERTY = "defaultMapperFactory";

   /**
    * A prefix of the mapper property for a given class.
    * <p>
    */
   public static final String MAPPER_PROPERTY = ".mapper.";

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

   public static String getProviderClassNameFor(Class<?> clazz) {
      return clazz.getName() + "." + PROVIDER_CLASS_PROPERTY;
   }

   /**
    * Get the name of the property where system can find a mapper for the
    * specified types.
    * <p>
    * 
    */
   public static String getMapperNameFor(Class<?> from, Class<?> to) {
      return from.getName() + MAPPER_PROPERTY + to.getName();
   }

   /**
    * Given a property name return a new property name appended {@code Class} at
    * the end of the name.
    * <p>
    * 
    * @param name
    *           the property name
    * @return the property name of the class of the given property name
    */
   public static String getPropertyNameForConfigClass(String name) {
      return name + ".class";
   }
}
